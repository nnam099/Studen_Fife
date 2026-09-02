import { AcademicTerm } from '../../academic-terms/entities/academic-term.entity';
import { Category } from '../../categories/entities/category.entity';
import { Milestone } from '../../milestones/entities/milestone.entity';
import { User } from '../../users/entities/user.entity';
export declare class Transaction {
    id: string;
    amount: number;
    type: 'income' | 'expense';
    description: string;
    occurredAt: Date;
    createdAt: Date;
    user: User;
    category: Category;
    academicTerm: AcademicTerm;
    milestone: Milestone | null;
}
