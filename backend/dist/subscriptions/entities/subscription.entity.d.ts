import { User } from '../../users/entities/user.entity';
export declare class Subscription {
    id: string;
    planCode: 'free' | 'term' | 'annual';
    status: 'active' | 'expired' | 'canceled';
    startedAt?: Date;
    endedAt?: Date;
    createdAt: Date;
    updatedAt: Date;
    user: User;
}
