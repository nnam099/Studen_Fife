import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Budget } from './entities/budget.entity';
import { AuthModule } from '../auth/auth.module';
import { AcademicTerm } from '../academic-terms/entities/academic-term.entity';
import { Category } from '../categories/entities/category.entity';
import { Transaction } from '../transactions/entities/transaction.entity';
import { BudgetsController } from './budgets.controller';
import { BudgetsService } from './budgets.service';

@Module({
  imports: [TypeOrmModule.forFeature([Budget, AcademicTerm, Category, Transaction]), AuthModule],
  controllers: [BudgetsController],
  providers: [BudgetsService],
})
export class BudgetsModule {}

