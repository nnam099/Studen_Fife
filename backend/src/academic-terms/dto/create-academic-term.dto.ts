import { IsDateString, IsIn, IsNotEmpty, IsOptional, IsString, MaxLength } from 'class-validator';

export class CreateAcademicTermDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  name: string;

  @IsDateString()
  startDate: string;

  @IsDateString()
  endDate: string;

  @IsOptional()
  @IsIn(['active', 'completed'])
  status?: 'active' | 'completed';
}
