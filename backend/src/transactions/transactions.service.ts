import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { AcademicTerm } from '../academic-terms/entities/academic-term.entity';
import { Category } from '../categories/entities/category.entity';
import { Milestone } from '../milestones/entities/milestone.entity';
import { CreateTransactionDto } from './dto/create-transaction.dto';
import { UpdateTransactionDto } from './dto/update-transaction.dto';
import { Transaction } from './entities/transaction.entity';

import { AlertsService } from '../alerts/alerts.service';

@Injectable()
export class TransactionsService {
  constructor(
    @InjectRepository(Transaction) private readonly repository: Repository<Transaction>,
    @InjectRepository(AcademicTerm) private readonly terms: Repository<AcademicTerm>,
    @InjectRepository(Category) private readonly categories: Repository<Category>,
    @InjectRepository(Milestone) private readonly milestones: Repository<Milestone>,
    private readonly alertsService: AlertsService,
  ) {}

  findAll(userId: string) {
    return this.repository.find({ where: { user: { id: userId } }, relations: ['category', 'academicTerm', 'milestone'], order: { occurredAt: 'DESC' } });
  }

  async findOne(userId: string, id: string) {
    const transaction = await this.repository.findOne({ where: { id, user: { id: userId } }, relations: ['category', 'academicTerm', 'milestone'] });
    if (!transaction) throw new NotFoundException('Transaction not found');
    return transaction;
  }

  async create(userId: string, dto: CreateTransactionDto) {
    const [term, category, milestone] = await Promise.all([
      this.terms.findOne({ where: { id: dto.academicTermId, user: { id: userId } } }),
      this.categories.findOne({ where: { id: dto.categoryId, user: { id: userId } } }),
      dto.milestoneId ? this.milestones.findOne({ where: { id: dto.milestoneId, user: { id: userId } } }) : Promise.resolve(null),
    ]);
    if (!term) throw new NotFoundException('Academic term not found');
    if (!category) throw new NotFoundException('Category not found');
    if (dto.milestoneId && !milestone) throw new NotFoundException('Milestone not found');
    const savedTx = await this.repository.save(this.repository.create({
      amount: dto.amount,
      type: dto.type,
      description: dto.description,
      occurredAt: new Date(dto.occurredAt),
      user: { id: userId } as any,
      academicTerm: term,
      category,
      milestone,
    }));

    if (savedTx.type === 'expense') {
      await this.alertsService.checkBudgetAlerts(userId, term.id, category.id, savedTx.occurredAt);
    }
    return savedTx;
  }

  async update(userId: string, id: string, dto: UpdateTransactionDto) {
    const transaction = await this.findOne(userId, id);
    if (dto.academicTermId) {
      const term = await this.terms.findOne({ where: { id: dto.academicTermId, user: { id: userId } } });
      if (!term) throw new NotFoundException('Academic term not found');
      transaction.academicTerm = term;
    }
    if (dto.categoryId) {
      const category = await this.categories.findOne({ where: { id: dto.categoryId, user: { id: userId } } });
      if (!category) throw new NotFoundException('Category not found');
      transaction.category = category;
    }
    if (dto.milestoneId) {
      const milestone = await this.milestones.findOne({ where: { id: dto.milestoneId, user: { id: userId } } });
      if (!milestone) throw new NotFoundException('Milestone not found');
      transaction.milestone = milestone;
    }
    Object.assign(transaction, {
      ...dto,
      occurredAt: dto.occurredAt ? new Date(dto.occurredAt) : transaction.occurredAt,
    });
    delete (transaction as any).categoryId;
    delete (transaction as any).academicTermId;
    delete (transaction as any).milestoneId;
    const updatedTx = await this.repository.save(transaction);

    if (updatedTx.type === 'expense' && updatedTx.academicTerm && updatedTx.category) {
      await this.alertsService.checkBudgetAlerts(userId, updatedTx.academicTerm.id, updatedTx.category.id, updatedTx.occurredAt);
    }
    return updatedTx;
  }

  async remove(userId: string, id: string) {
    const transaction = await this.findOne(userId, id);
    await this.repository.remove(transaction);
    return { deleted: true, id };
  }

  async getReport(userId: string) {
    const transactions = await this.findAll(userId);
    const now = new Date();
    const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

    let totalExpenses = 0;
    let weeklyExpenses = 0;
    const categoryTotals: Record<string, { categoryId: string; categoryName: string; totalAmount: number }> = {};

    for (const tx of transactions) {
      const amount = Number(tx.amount) || 0;
      if (tx.type === 'expense') {
        totalExpenses += amount;
        const occurred = new Date(tx.occurredAt);
        if (occurred >= sevenDaysAgo && occurred <= now) {
          weeklyExpenses += amount;
        }

        const catId = tx.category?.id || 'uncategorized';
        const catName = tx.category?.name || 'Khác';
        if (!categoryTotals[catId]) {
          categoryTotals[catId] = { categoryId: catId, categoryName: catName, totalAmount: 0 };
        }
        categoryTotals[catId].totalAmount += amount;
      }
    }

    const topCategories = Object.values(categoryTotals)
      .map((c) => ({
        ...c,
        percentage: totalExpenses > 0 ? Math.round((c.totalAmount / totalExpenses) * 100) : 0,
      }))
      .sort((a, b) => b.totalAmount - a.totalAmount);

    return {
      totalExpenses,
      weeklyExpenses,
      transactionCount: transactions.length,
      topCategories,
    };
  }
}

