"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AppModule = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const typeorm_1 = require("@nestjs/typeorm");
const auth_module_1 = require("./auth/auth.module");
const users_module_1 = require("./users/users.module");
const academic_terms_module_1 = require("./academic-terms/academic-terms.module");
const budgets_module_1 = require("./budgets/budgets.module");
const transactions_module_1 = require("./transactions/transactions.module");
const categories_module_1 = require("./categories/categories.module");
const milestones_module_1 = require("./milestones/milestones.module");
const alerts_module_1 = require("./alerts/alerts.module");
const subscriptions_module_1 = require("./subscriptions/subscriptions.module");
const app_controller_1 = require("./app.controller");
const app_service_1 = require("./app.service");
const user_entity_1 = require("./users/entities/user.entity");
const academic_term_entity_1 = require("./academic-terms/entities/academic-term.entity");
const category_entity_1 = require("./categories/entities/category.entity");
const budget_entity_1 = require("./budgets/entities/budget.entity");
const transaction_entity_1 = require("./transactions/entities/transaction.entity");
const milestone_entity_1 = require("./milestones/entities/milestone.entity");
const alert_entity_1 = require("./alerts/entities/alert.entity");
const subscription_entity_1 = require("./subscriptions/entities/subscription.entity");
let AppModule = class AppModule {
};
exports.AppModule = AppModule;
exports.AppModule = AppModule = __decorate([
    (0, common_1.Module)({
        imports: [
            config_1.ConfigModule.forRoot({ isGlobal: true, envFilePath: ['.env', '.env.local'] }),
            typeorm_1.TypeOrmModule.forRoot({
                type: 'postgres',
                host: process.env.DB_HOST || 'localhost',
                port: Number(process.env.DB_PORT || 5432),
                username: process.env.DB_USERNAME || 'postgres',
                password: process.env.DB_PASSWORD || 'postgres',
                database: process.env.DB_NAME || 'student_finance',
                entities: [
                    user_entity_1.User,
                    academic_term_entity_1.AcademicTerm,
                    category_entity_1.Category,
                    budget_entity_1.Budget,
                    transaction_entity_1.Transaction,
                    milestone_entity_1.Milestone,
                    alert_entity_1.Alert,
                    subscription_entity_1.Subscription,
                ],
                synchronize: false,
                migrations: ['dist/migrations/*.js'],
                logging: process.env.NODE_ENV !== 'production',
            }),
            auth_module_1.AuthModule,
            users_module_1.UsersModule,
            academic_terms_module_1.AcademicTermsModule,
            budgets_module_1.BudgetsModule,
            transactions_module_1.TransactionsModule,
            categories_module_1.CategoriesModule,
            milestones_module_1.MilestonesModule,
            alerts_module_1.AlertsModule,
            subscriptions_module_1.SubscriptionsModule,
        ],
        controllers: [app_controller_1.AppController],
        providers: [app_service_1.AppService],
    })
], AppModule);
//# sourceMappingURL=app.module.js.map