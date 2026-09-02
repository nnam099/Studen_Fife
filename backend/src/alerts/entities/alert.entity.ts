import {
  Column,
  CreateDateColumn,
  Entity,
  ManyToOne,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { AcademicTerm } from '../../academic-terms/entities/academic-term.entity';
import { Budget } from '../../budgets/entities/budget.entity';
import { Milestone } from '../../milestones/entities/milestone.entity';
import { User } from '../../users/entities/user.entity';

@Entity('alerts')
export class Alert {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ type: 'varchar' })
  type: 'budget_warning' | 'deadline_soon' | 'milestone_risk';

  @Column({ length: 255 })
  title: string;

  @Column({ type: 'text' })
  message: string;

  @Column({ type: 'varchar', default: 'warning' })
  severity: 'info' | 'warning' | 'critical';

  @Column({ type: 'timestamp', name: 'triggered_at', default: () => 'CURRENT_TIMESTAMP' })
  triggeredAt: Date;

  @Column({ default: 'new' })
  status: 'new' | 'read' | 'dismissed';

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @ManyToOne(() => User, (user) => user.alerts, { onDelete: 'CASCADE' })
  user: User;

  @ManyToOne(() => AcademicTerm, (academicTerm) => academicTerm.alerts, {
    onDelete: 'CASCADE',
  })
  academicTerm: AcademicTerm;

  @ManyToOne(() => Budget, (budget) => budget.alerts, {
    nullable: true,
    onDelete: 'SET NULL',
  })
  budget: Budget | null;

  @ManyToOne(() => Milestone, (milestone) => milestone.alerts, {
    nullable: true,
    onDelete: 'SET NULL',
  })
  milestone: Milestone | null;
}
