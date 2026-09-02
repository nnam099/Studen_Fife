import {
  Column,
  CreateDateColumn,
  Entity,
  JoinColumn,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { Alert } from '../../alerts/entities/alert.entity';
import { Budget } from '../../budgets/entities/budget.entity';
import { Milestone } from '../../milestones/entities/milestone.entity';
import { Transaction } from '../../transactions/entities/transaction.entity';
import { User } from '../../users/entities/user.entity';

@Entity('academic_terms')
export class AcademicTerm {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ length: 255 })
  name: string;

  @Column({ type: 'date', name: 'start_date' })
  startDate: Date;

  @Column({ type: 'date', name: 'end_date' })
  endDate: Date;

  @Column({ default: 'active' })
  status: 'active' | 'completed';

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @ManyToOne(() => User, (user) => user.academicTerms, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @OneToMany(() => Budget, (budget) => budget.academicTerm)
  budgets: Budget[];

  @OneToMany(() => Transaction, (transaction) => transaction.academicTerm)
  transactions: Transaction[];

  @OneToMany(() => Milestone, (milestone) => milestone.academicTerm)
  milestones: Milestone[];

  @OneToMany(() => Alert, (alert) => alert.academicTerm)
  alerts: Alert[];
}
