import { AcademicTerm } from '../../academic-terms/entities/academic-term.entity';
import { Budget } from '../../budgets/entities/budget.entity';
import { Milestone } from '../../milestones/entities/milestone.entity';
import { User } from '../../users/entities/user.entity';
export declare class Alert {
    id: string;
    type: 'budget_warning' | 'deadline_soon' | 'milestone_risk';
    title: string;
    message: string;
    severity: 'info' | 'warning' | 'critical';
    triggeredAt: Date;
    status: 'new' | 'read' | 'dismissed';
    createdAt: Date;
    updatedAt: Date;
    user: User;
    academicTerm: AcademicTerm;
    budget: Budget | null;
    milestone: Milestone | null;
}
