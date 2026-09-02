import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Budget } from './entities/budget.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Budget])],
})
export class BudgetsModule {}
