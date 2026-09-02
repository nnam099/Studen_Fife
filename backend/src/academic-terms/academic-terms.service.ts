import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { AcademicTerm } from './entities/academic-term.entity';
import { CreateAcademicTermDto } from './dto/create-academic-term.dto';
import { UpdateAcademicTermDto } from './dto/update-academic-term.dto';

@Injectable()
export class AcademicTermsService {
  constructor(@InjectRepository(AcademicTerm) private readonly repository: Repository<AcademicTerm>) {}

  findAll(userId: string) {
    return this.repository.find({ where: { user: { id: userId } }, order: { startDate: 'DESC' } });
  }

  async findOne(userId: string, id: string) {
    const term = await this.repository.findOne({ where: { id, user: { id: userId } } });
    if (!term) throw new NotFoundException('Academic term not found');
    return term;
  }

  async create(userId: string, dto: CreateAcademicTermDto) {
    if (new Date(dto.endDate) < new Date(dto.startDate)) {
      throw new BadRequestException('endDate must be on or after startDate');
    }
    const term = this.repository.create({
      name: dto.name,
      startDate: new Date(dto.startDate),
      endDate: new Date(dto.endDate),
      status: dto.status || 'active',
      user: { id: userId } as any,
    });
    return this.repository.save(term);
  }

  async update(userId: string, id: string, dto: UpdateAcademicTermDto) {
    const term = await this.findOne(userId, id);
    const start = dto.startDate ? new Date(dto.startDate) : term.startDate;
    const end = dto.endDate ? new Date(dto.endDate) : term.endDate;
    if (end < start) throw new BadRequestException('endDate must be on or after startDate');
    Object.assign(term, {
      ...dto,
      startDate: dto.startDate ? start : term.startDate,
      endDate: dto.endDate ? end : term.endDate,
    });
    return this.repository.save(term);
  }

  async remove(userId: string, id: string) {
    const term = await this.findOne(userId, id);
    await this.repository.remove(term);
    return { deleted: true, id };
  }
}
