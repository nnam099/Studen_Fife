import { Alert } from '../../alerts/entities/alert.entity';
import { Budget } from '../../budgets/entities/budget.entity';
import { Milestone } from '../../milestones/entities/milestone.entity';
import { Transaction } from '../../transactions/entities/transaction.entity';
import { User } from '../../users/entities/user.entity';
export declare class AcademicTerm {
    id: string;
    name: string;
    startDate: Date;
    endDate: Date;
    status: 'active' | 'completed';
    createdAt: Date;
    updatedAt: Date;
    user: User;
    budgets: Budget[];
    transactions: Transaction[];
    milestones: Milestone[];
    alerts: Alert[];
}
