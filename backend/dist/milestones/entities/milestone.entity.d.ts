import { AcademicTerm } from '../../academic-terms/entities/academic-term.entity';
import { Alert } from '../../alerts/entities/alert.entity';
import { Transaction } from '../../transactions/entities/transaction.entity';
import { User } from '../../users/entities/user.entity';
export declare class Milestone {
    id: string;
    title: string;
    description?: string;
    dueDate: Date;
    type: 'exam' | 'assignment' | 'fee' | 'personal_goal';
    isCompleted: boolean;
    priority: number;
    createdAt: Date;
    updatedAt: Date;
    user: User;
    academicTerm: AcademicTerm;
    transactions: Transaction[];
    alerts: Alert[];
}
