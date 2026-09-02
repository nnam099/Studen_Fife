import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Milestone } from './entities/milestone.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Milestone])],
})
export class MilestonesModule {}
