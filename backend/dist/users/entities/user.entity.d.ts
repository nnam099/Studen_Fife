import { AcademicTerm } from '../../academic-terms/entities/academic-term.entity';
import { Alert } from '../../alerts/entities/alert.entity';
import { Budget } from '../../budgets/entities/budget.entity';
import { Category } from '../../categories/entities/category.entity';
import { Milestone } from '../../milestones/entities/milestone.entity';
import { Subscription } from '../../subscriptions/entities/subscription.entity';
import { Transaction } from '../../transactions/entities/transaction.entity';
export declare class User {
    id: string;
    email: string;
    fullName: string;
    passwordHash: string;
    isActive: boolean;
    createdAt: Date;
    updatedAt: Date;
    academicTerms: AcademicTerm[];
    categories: Category[];
    budgets: Budget[];
    transactions: Transaction[];
    milestones: Milestone[];
    alerts: Alert[];
    subscriptions: Subscription[];
}
