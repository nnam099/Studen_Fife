import { Type } from 'class-transformer';
import { IsDateString, IsIn, IsNumber, IsNotEmpty, IsOptional, IsPositive, IsString, IsUUID, MaxLength } from 'class-validator';

export class CreateTransactionDto {
  @Type(() => Number)
  @IsNumber({ maxDecimalPlaces: 2 })
  @IsPositive()
  amount: number;

  @IsIn(['income', 'expense'])
  type: 'income' | 'expense';

  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  description: string;

  @IsDateString()
  occurredAt: string;

  @IsUUID()
  categoryId: string;

  @IsUUID()
  academicTermId: string;

  @IsOptional()
  @IsUUID()
  milestoneId?: string;
}
