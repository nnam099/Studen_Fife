import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { AcademicTermsModule } from './academic-terms/academic-terms.module';
import { BudgetsModule } from './budgets/budgets.module';
import { TransactionsModule } from './transactions/transactions.module';
import { CategoriesModule } from './categories/categories.module';
import { MilestonesModule } from './milestones/milestones.module';
import { AlertsModule } from './alerts/alerts.module';
import { SubscriptionsModule } from './subscriptions/subscriptions.module';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { User } from './users/entities/user.entity';
import { AcademicTerm } from './academic-terms/entities/academic-term.entity';
import { Category } from './categories/entities/category.entity';
import { Budget } from './budgets/entities/budget.entity';
import { Transaction } from './transactions/entities/transaction.entity';
import { Milestone } from './milestones/entities/milestone.entity';
import { Alert } from './alerts/entities/alert.entity';
import { Subscription } from './subscriptions/entities/subscription.entity';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true, envFilePath: ['.env', '.env.local'] }),
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        type: 'postgres',
        host: configService.get<string>('DB_HOST', 'localhost'),
        port: configService.get<number>('DB_PORT', 5432),
        username: configService.get<string>('DB_USERNAME', 'postgres'),
        password: configService.get<string>('DB_PASSWORD', 'postgres'),
        database: configService.get<string>('DB_NAME', 'student_finance'),
        entities: [
          User,
          AcademicTerm,
          Category,
          Budget,
          Transaction,
          Milestone,
          Alert,
          Subscription,
        ],
        synchronize: false,
        migrations: ['dist/migrations/*.js'],
        logging: configService.get<string>('NODE_ENV') !== 'production',
      }),
    }),
    AuthModule,
    UsersModule,
    AcademicTermsModule,
    BudgetsModule,
    TransactionsModule,
    CategoriesModule,
    MilestonesModule,
    AlertsModule,
    SubscriptionsModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
