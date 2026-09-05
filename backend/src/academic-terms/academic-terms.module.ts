import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AcademicTerm } from './entities/academic-term.entity';
import { AuthModule } from '../auth/auth.module';
import { AcademicTermsController } from './academic-terms.controller';
import { AcademicTermsService } from './academic-terms.service';

@Module({
  imports: [TypeOrmModule.forFeature([AcademicTerm]), AuthModule],
  controllers: [AcademicTermsController],
  providers: [AcademicTermsService],
  exports: [AcademicTermsService],
})
export class AcademicTermsModule {}
