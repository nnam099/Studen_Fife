import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { AcademicTerm } from '../academic-terms/entities/academic-term.entity';
import { Category } from '../categories/entities/category.entity';
import { Transaction } from '../transactions/entities/transaction.entity';
import { CreateBudgetDto } from './dto/create-budget.dto';
import { UpdateBudgetDto } from './dto/update-budget.dto';
import { Budget } from './entities/budget.entity';

@Injectable()
export class BudgetsService {
  constructor(
    @InjectRepository(Budget) private readonly repository: Repository<Budget>,
    @InjectRepository(AcademicTerm) private readonly terms: Repository<AcademicTerm>,
    @InjectRepository(Category) private readonly categories: Repository<Category>,
    @InjectRepository(Transaction) private readonly transactions: Repository<Transaction>,
  ) {}

  findAll(userId: string) {
    return this.repository.find({ where: { user: { id: userId } }, relations: ['academicTerm', 'category'], order: { startDate: 'DESC' } });
  }

  async findOne(userId: string, id: string) {
    const budget = await this.repository.findOne({ where: { id, user: { id: userId } }, relations: ['academicTerm', 'category'] });
    if (!budget) throw new NotFoundException('Budget not found');
    return budget;
  }

  async create(userId: string, dto: CreateBudgetDto) {
    this.validateDates(dto.startDate, dto.endDate);
    const [academicTerm, category] = await Promise.all([
      this.terms.findOne({ where: { id: dto.academicTermId, user: { id: userId } } }),
      this.categories.findOne({ where: { id: dto.categoryId, user: { id: userId } } }),
    ]);
    if (!academicTerm) throw new NotFoundException('Academic term not found');
    if (!category) throw new NotFoundException('Category not found');
    return this.repository.save(this.repository.create({
      amount: dto.amount,
      periodType: dto.periodType,
      startDate: new Date(dto.startDate),
      endDate: new Date(dto.endDate),
      currency: dto.currency,
      user: { id: userId } as any,
      academicTerm,
      category,
    }));
  }

  async update(userId: string, id: string, dto: UpdateBudgetDto) {
    const budget = await this.findOne(userId, id);
    const start = new Date(dto.startDate || budget.startDate);
    const end = new Date(dto.endDate || budget.endDate);
    this.validateDates(start, end);
    if (dto.academicTermId) {
      const term = await this.terms.findOne({ where: { id: dto.academicTermId, user: { id: userId } } });
      if (!term) throw new NotFoundException('Academic term not found');
      budget.academicTerm = term;
    }
    if (dto.categoryId) {
      const category = await this.categories.findOne({ where: { id: dto.categoryId, user: { id: userId } } });
      if (!category) throw new NotFoundException('Category not found');
      budget.category = category;
    }
    if (dto.amount !== undefined) budget.amount = dto.amount;
    if (dto.periodType !== undefined) budget.periodType = dto.periodType;
    if (dto.currency !== undefined) budget.currency = dto.currency;
    budget.startDate = start;
    budget.endDate = end;
    return this.repository.save(budget);
  }

  async remove(userId: string, id: string) {
    const budget = await this.findOne(userId, id);
    if (budget.category && budget.academicTerm) {
      const count = await this.transactions.count({
        where: {
          user: { id: userId },
          category: { id: budget.category.id },
          academicTerm: { id: budget.academicTerm.id },
        },
      });
      if (count > 0) {
        throw new BadRequestException('Không thể xóa ngân sách đang có giao dịch phát sinh. Vui lòng chuyển hoặc xóa các giao dịch trước.');
      }
    }
    await this.repository.remove(budget);
    return { deleted: true, id };
  }

  private validateDates(startDate: string | Date, endDate: string | Date) {
    if (new Date(endDate) < new Date(startDate)) throw new BadRequestException('endDate must be on or after startDate');
  }
}

