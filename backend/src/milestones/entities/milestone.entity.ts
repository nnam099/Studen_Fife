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
import { AcademicTerm } from '../../academic-terms/entities/academic-term.entity';
import { Alert } from '../../alerts/entities/alert.entity';
import { Transaction } from '../../transactions/entities/transaction.entity';
import { User } from '../../users/entities/user.entity';

@Entity('milestones')
export class Milestone {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ length: 255 })
  title: string;

  @Column({ type: 'text', nullable: true })
  description?: string;

  @Column({ type: 'date', name: 'due_date' })
  dueDate: Date;

  @Column({ type: 'varchar', default: 'exam' })
  type: 'exam' | 'assignment' | 'fee' | 'personal_goal';

  @Column({ name: 'is_completed', default: false })
  isCompleted: boolean;

  @Column({ default: 1 })
  priority: number;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @ManyToOne(() => User, (user) => user.milestones, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => AcademicTerm, (academicTerm) => academicTerm.milestones, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'academic_term_id' })
  academicTerm: AcademicTerm;

  @OneToMany(() => Transaction, (transaction) => transaction.milestone)
  transactions: Transaction[];

  @OneToMany(() => Alert, (alert) => alert.milestone)
  alerts: Alert[];
}
