import { IsDateString, IsIn, IsNotEmpty, IsOptional, IsString, MaxLength } from 'class-validator';

export class UpdateAcademicTermDto {
	@IsOptional()
	@IsString()
	@IsNotEmpty()
	@MaxLength(255)
	name?: string;

	@IsOptional()
	@IsDateString()
	startDate?: string;

	@IsOptional()
	@IsDateString()
	endDate?: string;

	@IsOptional()
	@IsIn(['active', 'completed'])
	status?: 'active' | 'completed';
}
