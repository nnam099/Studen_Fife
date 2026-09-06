import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { EntityManager, In, Repository } from 'typeorm';
import { Budget } from '../budgets/entities/budget.entity';
import { Milestone } from '../milestones/entities/milestone.entity';
import { Transaction } from '../transactions/entities/transaction.entity';
import { Alert } from './entities/alert.entity';

@Injectable()
export class AlertsService {
  constructor(
    @InjectRepository(Alert) private readonly alertRepo: Repository<Alert>,
    @InjectRepository(Budget) private readonly budgetRepo: Repository<Budget>,
    @InjectRepository(Transaction) private readonly txRepo: Repository<Transaction>,
    @InjectRepository(Milestone) private readonly milestoneRepo: Repository<Milestone>,
  ) {}

  async findAll(userId: string, status?: string) {
    if (status && !['unread', 'new', 'read', 'dismissed'].includes(status)) {
      throw new BadRequestException('Invalid alert status');
    }
    const whereClause: any = { user: { id: userId } };
    if (status) {
      if (status === 'unread') {
        whereClause.status = In(['unread', 'new']);
      } else {
        whereClause.status = status;
      }
    }
    return this.alertRepo.find({
      where: whereClause,
      relations: ['academicTerm', 'budget', 'budget.category', 'milestone'],
      order: { triggeredAt: 'DESC' },
    });
  }

  async markAsRead(userId: string, id: string) {
    const alert = await this.alertRepo.findOne({
      where: { id, user: { id: userId } },
      relations: ['academicTerm', 'budget', 'milestone'],
    });
    if (!alert) throw new NotFoundException('Alert not found');
    if (alert.status !== 'dismissed') alert.status = 'read';
    return this.alertRepo.save(alert);
  }

  async markAsDismissed(userId: string, id: string) {
    const alert = await this.alertRepo.findOne({
      where: { id, user: { id: userId } },
      relations: ['academicTerm', 'budget', 'milestone'],
    });
    if (!alert) throw new NotFoundException('Alert not found');
    alert.status = 'dismissed';
    return this.alertRepo.save(alert);
  }

  // Budgets have no status column: active means today's date is inside the
  // inclusive budget window and the owning academic term is active.
  async checkBudgetAlerts(userId: string, manager: EntityManager) {
    const today = new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit',
    }).format(new Date());
    const budgets = await manager.getRepository(Budget).createQueryBuilder('budget')
      .innerJoinAndSelect('budget.academicTerm', 'term')
      .innerJoinAndSelect('budget.category', 'category')
      .where('budget.user_id = :userId', { userId })
      .andWhere('term.status = :status', { status: 'active' })
      .andWhere(':today BETWEEN budget.start_date AND budget.end_date', { today })
      .orderBy('budget.id', 'ASC')
      .getMany();
    const alerts = manager.getRepository(Alert);
    for (const budget of budgets) {
      const result = await manager.getRepository(Transaction).createQueryBuilder('tx')
        .select('COALESCE(SUM(tx.amount), 0)', 'total')
        .where('tx.user_id = :userId', { userId })
        .andWhere('tx.academic_term_id = :termId', { termId: budget.academicTerm.id })
        .andWhere('tx.category_id = :categoryId', { categoryId: budget.category.id })
        .andWhere('tx.type = :type', { type: 'expense' })
        .andWhere('tx.occurred_at BETWEEN :start AND :end', { start: budget.startDate, end: budget.endDate })
        .getRawOne();
      // Stored monetary values have two decimal places; compare integer cents.
      const spent = Math.round(Number(result.total) * 100);
      const limit = Math.round(Number(budget.amount) * 100);
      let alert = await alerts.findOne({
        where: { user: { id: userId }, budget: { id: budget.id }, type: 'budget_warning', status: In(['unread', 'new']) },
      });
      if (spent <= limit) {
        // Corrected/moved/deleted expenses must not leave a stale unread warning.
        if (alert) { alert.status = 'dismissed'; await alerts.save(alert); }
        continue;
      }
      const milestone = await manager.getRepository(Milestone).createQueryBuilder('m')
        .where('m.user_id = :userId', { userId })
        .andWhere('m.academic_term_id = :termId', { termId: budget.academicTerm.id })
        .andWhere('m.is_completed = false')
        .andWhere("m.due_date BETWEEN CAST(:today AS date) AND CAST(:today AS date) + 7", { today })
        .orderBy('m.due_date', 'ASC').addOrderBy('m.id', 'ASC').getOne();
      const excess = (spent - limit) / 100;
      const percent = limit > 0 ? Math.round((spent - limit) / limit * 10000) / 100 : null;
      const period = { weekly: 'tuần', monthly: 'tháng', academic_term: 'học kỳ' }[budget.periodType];
      if (!alert) alert = alerts.create({ user: { id: userId }, budget, academicTerm: budget.academicTerm });
      Object.assign(alert, {
        type: 'budget_warning', status: 'unread', milestone,
        title: `Cảnh báo vượt ngân sách ${period}`,
        message: `Ngân sách ${period} (budget_id: ${budget.id}, period_type: ${budget.periodType}) danh mục "${budget.category.name}" đã chi ${(spent / 100).toLocaleString('vi-VN')} / ${(limit / 100).toLocaleString('vi-VN')} ${budget.currency}, vượt ${excess.toLocaleString('vi-VN')} ${budget.currency}${percent === null ? '' : ` (${percent}%)`}.`,
        severity: spent >= limit * 1.2 ? 'critical' : 'warning',
        triggeredAt: new Date(),
      });
      await alerts.save(alert);
    }
  }
}
