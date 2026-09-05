import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AcademicTerm } from '../academic-terms/entities/academic-term.entity';
import { AuthModule } from '../auth/auth.module';
import { Budget } from '../budgets/entities/budget.entity';
import { Milestone } from '../milestones/entities/milestone.entity';
import { Transaction } from '../transactions/entities/transaction.entity';
import { AlertsController } from './alerts.controller';
import { AlertsService } from './alerts.service';
import { Alert } from './entities/alert.entity';

@Module({
  imports: [
    TypeOrmModule.forFeature([Alert, Budget, Transaction, Milestone, AcademicTerm]),
    AuthModule,
  ],
  controllers: [AlertsController],
  providers: [AlertsService],
  exports: [AlertsService],
})
export class AlertsModule {}
