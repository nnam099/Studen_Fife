import { Type } from 'class-transformer';
import { IsBoolean, IsDateString, IsIn, IsInt, IsNotEmpty, IsOptional, IsString, IsUUID, Max, MaxLength, Min } from 'class-validator';

export class UpdateMilestoneDto {
  @IsOptional()
  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  title?: string;

  @IsOptional()
  @IsString()
  description?: string;

  @IsOptional()
  @IsDateString()
  dueDate?: string;

  @IsOptional()
  @IsIn(['exam', 'assignment', 'fee', 'personal_goal'])
  type?: 'exam' | 'assignment' | 'fee' | 'personal_goal';

  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  isCompleted?: boolean;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(5)
  priority?: number;

  @IsOptional()
  @IsUUID()
  academicTermId?: string;
}
