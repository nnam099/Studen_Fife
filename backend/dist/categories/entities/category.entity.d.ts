import { Budget } from '../../budgets/entities/budget.entity';
import { Transaction } from '../../transactions/entities/transaction.entity';
import { User } from '../../users/entities/user.entity';
export declare class Category {
    id: string;
    name: string;
    type: 'income' | 'expense';
    color?: string;
    isDefault: boolean;
    createdAt: Date;
    updatedAt: Date;
    user: User;
    budgets: Budget[];
    transactions: Transaction[];
}
