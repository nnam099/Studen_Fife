import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Category } from './entities/category.entity';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';

@Injectable()
export class CategoriesService {
  constructor(@InjectRepository(Category) private readonly repository: Repository<Category>) {}

  findAll(userId: string) {
    return this.repository.find({ where: { user: { id: userId } }, order: { name: 'ASC' } });
  }

  async findOne(userId: string, id: string) {
    const category = await this.repository.findOne({ where: { id, user: { id: userId } } });
    if (!category) throw new NotFoundException('Category not found');
    return category;
  }

  create(userId: string, dto: CreateCategoryDto) {
    return this.repository.save(this.repository.create({
      name: dto.name,
      type: dto.type || 'expense',
      color: dto.color,
      user: { id: userId } as any,
    }));
  }

  async update(userId: string, id: string, dto: UpdateCategoryDto) {
    const category = await this.findOne(userId, id);
    Object.assign(category, dto);
    return this.repository.save(category);
  }

  async remove(userId: string, id: string) {
    const category = await this.findOne(userId, id);
    await this.repository.remove(category);
    return { deleted: true, id };
  }
}
