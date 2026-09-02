import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AcademicTerm } from './entities/academic-term.entity';

@Module({
  imports: [TypeOrmModule.forFeature([AcademicTerm])],
})
export class AcademicTermsModule {}
