import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Transaction } from './entities/transaction.entity';
import { AuthModule } from '../auth/auth.module';
import { AcademicTerm } from '../academic-terms/entities/academic-term.entity';
import { Category } from '../categories/entities/category.entity';
import { Milestone } from '../milestones/entities/milestone.entity';
import { TransactionsController } from './transactions.controller';
import { TransactionsService } from './transactions.service';

@Module({
  imports: [TypeOrmModule.forFeature([Transaction, AcademicTerm, Category, Milestone]), AuthModule],
  controllers: [TransactionsController],
  providers: [TransactionsService],
})
export class TransactionsModule {}
