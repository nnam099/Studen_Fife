import { Type } from 'class-transformer';
import { IsDateString, IsIn, IsNumber, IsPositive, IsString, IsUUID, MaxLength } from 'class-validator';

export class CreateBudgetDto {
  @Type(() => Number)
  @IsNumber({ maxDecimalPlaces: 2 })
  @IsPositive()
  amount: number;

  @IsIn(['weekly', 'monthly', 'academic_term'])
  periodType: 'weekly' | 'monthly' | 'academic_term';

  @IsDateString()
  startDate: string;

  @IsDateString()
  endDate: string;

  @IsString()
  @MaxLength(10)
  currency = 'VND';

  @IsUUID()
  academicTermId: string;

  @IsUUID()
  categoryId: string;
}
