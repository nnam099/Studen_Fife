import {
  Column,
  CreateDateColumn,
  Entity,
  ManyToOne,
  PrimaryGeneratedColumn,
} from 'typeorm';
import { AcademicTerm } from '../../academic-terms/entities/academic-term.entity';
import { Category } from '../../categories/entities/category.entity';
import { Milestone } from '../../milestones/entities/milestone.entity';
import { User } from '../../users/entities/user.entity';

@Entity('transactions')
export class Transaction {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ type: 'decimal', precision: 12, scale: 2 })
  amount: number;

  @Column({ type: 'varchar', default: 'expense' })
  type: 'income' | 'expense';

  @Column({ length: 255 })
  description: string;

  @Column({ type: 'date', name: 'occurred_at' })
  occurredAt: Date;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @ManyToOne(() => User, (user) => user.transactions, { onDelete: 'CASCADE' })
  user: User;

  @ManyToOne(() => Category, (category) => category.transactions, {
    onDelete: 'CASCADE',
  })
  category: Category;

  @ManyToOne(() => AcademicTerm, (academicTerm) => academicTerm.transactions, {
    onDelete: 'CASCADE',
  })
  academicTerm: AcademicTerm;

  @ManyToOne(() => Milestone, (milestone) => milestone.transactions, {
    onDelete: 'SET NULL',
    nullable: true,
  })
  milestone: Milestone | null;
}
