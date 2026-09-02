import { AcademicTerm } from '../../academic-terms/entities/academic-term.entity';
import { Alert } from '../../alerts/entities/alert.entity';
import { Category } from '../../categories/entities/category.entity';
import { User } from '../../users/entities/user.entity';
export declare class Budget {
    id: string;
    amount: number;
    periodType: 'weekly' | 'monthly' | 'academic_term';
    startDate: Date;
    endDate: Date;
    currency: string;
    createdAt: Date;
    updatedAt: Date;
    user: User;
    academicTerm: AcademicTerm;
    category: Category;
    alerts: Alert[];
}
