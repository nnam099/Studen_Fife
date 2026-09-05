import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, LessThanOrEqual, MoreThanOrEqual, Repository } from 'typeorm';
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
    alert.status = 'read';
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

  async checkBudgetAlerts(userId: string, academicTermId: string, categoryId: string, transactionDate: Date) {
    // 1. Find all active budgets for user and category matching date range
    const budgets = await this.budgetRepo.find({
      where: {
        user: { id: userId },
        category: { id: categoryId },
        startDate: LessThanOrEqual(transactionDate),
        endDate: MoreThanOrEqual(transactionDate),
      },
      relations: ['category', 'academicTerm'],
    });

    const generatedAlerts: Alert[] = [];

    for (const budget of budgets) {
      // 2. Sum expenses in budget timeframe
      const qb = this.txRepo.createQueryBuilder('tx')
        .where('tx.user_id = :userId', { userId })
        .andWhere('tx.category_id = :categoryId', { categoryId })
        .andWhere('tx.type = :type', { type: 'expense' })
        .andWhere('tx.occurred_at >= :startDate', { startDate: budget.startDate })
        .andWhere('tx.occurred_at <= :endDate', { endDate: budget.endDate });

      const res = await qb.select('SUM(tx.amount)', 'total').getRawOne();
      const totalExpenses = Number(res?.total) || 0;
      const budgetAmount = Number(budget.amount) || 0;

      // 3. If totalExpenses > budgetAmount, create or update Alert
      if (budgetAmount > 0 && totalExpenses > budgetAmount) {
        const exceededAmount = totalExpenses - budgetAmount;
        const exceededPercent = Math.round((totalExpenses / budgetAmount) * 100);
        const severity: 'warning' | 'critical' = totalExpenses >= budgetAmount * 1.2 ? 'critical' : 'warning';

        // 4. Find nearest milestone within 7 days in the same academic term
        const txTime = new Date(transactionDate).getTime();
        const sevenDaysMs = 7 * 24 * 60 * 60 * 1000;
        const milestones = await this.milestoneRepo.find({
          where: {
            user: { id: userId },
            academicTerm: { id: budget.academicTerm?.id || academicTermId },
          },
          order: { dueDate: 'ASC' },
        });

        const nearestMilestone = milestones.find((m) => {
          const dueTime = new Date(m.dueDate).getTime();
          return dueTime >= txTime - (24 * 60 * 60 * 1000) && dueTime <= txTime + sevenDaysMs;
        }) || null;

        const periodLabel = budget.periodType === 'weekly' ? 'tuần' : budget.periodType === 'monthly' ? 'tháng' : 'học kỳ';

        // Check if an unread alert for this budget already exists to update
        let alert = await this.alertRepo.findOne({
          where: {
            user: { id: userId },
            budget: { id: budget.id },
            status: In(['unread', 'new']),
          },
        });

        if (!alert) {
          alert = this.alertRepo.create({
            user: { id: userId } as any,
            academicTerm: { id: budget.academicTerm?.id || academicTermId } as any,
            budget,
            milestone: nearestMilestone,
            type: 'budget_warning',
            title: `Cảnh báo vượt ngân sách ${periodLabel}`,
            message: `Ngân sách ${periodLabel} (ID: ${budget.id}) danh mục "${budget.category.name}" đã chi ${totalExpenses.toLocaleString('vi-VN')} / ${budgetAmount.toLocaleString('vi-VN')} VNĐ, vượt ${exceededAmount.toLocaleString('vi-VN')} VNĐ (${exceededPercent}%).`,
            severity,
            status: 'unread',
            triggeredAt: new Date(),
          });
        } else {
          alert.message = `Ngân sách ${periodLabel} (ID: ${budget.id}) danh mục "${budget.category.name}" đã chi ${totalExpenses.toLocaleString('vi-VN')} / ${budgetAmount.toLocaleString('vi-VN')} VNĐ, vượt ${exceededAmount.toLocaleString('vi-VN')} VNĐ (${exceededPercent}%).`;
          alert.severity = severity;
          alert.triggeredAt = new Date();
          if (nearestMilestone) alert.milestone = nearestMilestone;
        }

        const savedAlert = await this.alertRepo.save(alert);
        generatedAlerts.push(savedAlert);
      }
    }

    return generatedAlerts;
  }
}
