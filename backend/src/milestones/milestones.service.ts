import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { AcademicTerm } from '../academic-terms/entities/academic-term.entity';
import { CreateMilestoneDto } from './dto/create-milestone.dto';
import { UpdateMilestoneDto } from './dto/update-milestone.dto';
import { Milestone } from './entities/milestone.entity';

@Injectable()
export class MilestonesService {
  constructor(
    @InjectRepository(Milestone) private readonly repository: Repository<Milestone>,
    @InjectRepository(AcademicTerm) private readonly terms: Repository<AcademicTerm>,
  ) {}

  findAll(userId: string) {
    return this.repository.find({ where: { user: { id: userId } }, relations: ['academicTerm'], order: { dueDate: 'ASC' } });
  }

  async findOne(userId: string, id: string) {
    const milestone = await this.repository.findOne({ where: { id, user: { id: userId } }, relations: ['academicTerm'] });
    if (!milestone) throw new NotFoundException('Milestone not found');
    return milestone;
  }

  async create(userId: string, dto: CreateMilestoneDto) {
    const term = await this.terms.findOne({ where: { id: dto.academicTermId, user: { id: userId } } });
    if (!term) throw new NotFoundException('Academic term not found');
    return this.repository.save(this.repository.create({
      title: dto.title,
      description: dto.description,
      dueDate: new Date(dto.dueDate),
      type: dto.type,
      isCompleted: dto.isCompleted ?? false,
      priority: dto.priority ?? 1,
      user: { id: userId } as any,
      academicTerm: term,
    }));
  }

  async update(userId: string, id: string, dto: UpdateMilestoneDto) {
    const milestone = await this.findOne(userId, id);
    if (dto.academicTermId) {
      const term = await this.terms.findOne({ where: { id: dto.academicTermId, user: { id: userId } } });
      if (!term) throw new NotFoundException('Academic term not found');
      milestone.academicTerm = term;
    }
    Object.assign(milestone, { ...dto, dueDate: dto.dueDate ? new Date(dto.dueDate) : milestone.dueDate });
    delete (milestone as any).academicTermId;
    return this.repository.save(milestone);
  }

  async remove(userId: string, id: string) {
    const milestone = await this.findOne(userId, id);
    await this.repository.remove(milestone);
    return { deleted: true, id };
  }
}
