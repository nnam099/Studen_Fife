"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.User = void 0;
const typeorm_1 = require("typeorm");
const academic_term_entity_1 = require("../../academic-terms/entities/academic-term.entity");
const alert_entity_1 = require("../../alerts/entities/alert.entity");
const budget_entity_1 = require("../../budgets/entities/budget.entity");
const category_entity_1 = require("../../categories/entities/category.entity");
const milestone_entity_1 = require("../../milestones/entities/milestone.entity");
const subscription_entity_1 = require("../../subscriptions/entities/subscription.entity");
const transaction_entity_1 = require("../../transactions/entities/transaction.entity");
let User = class User {
};
exports.User = User;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)('uuid'),
    __metadata("design:type", String)
], User.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)({ unique: true, length: 255 }),
    __metadata("design:type", String)
], User.prototype, "email", void 0);
__decorate([
    (0, typeorm_1.Column)({ name: 'full_name', length: 255 }),
    __metadata("design:type", String)
], User.prototype, "fullName", void 0);
__decorate([
    (0, typeorm_1.Column)({ name: 'password_hash', type: 'text' }),
    __metadata("design:type", String)
], User.prototype, "passwordHash", void 0);
__decorate([
    (0, typeorm_1.Column)({ name: 'is_active', default: true }),
    __metadata("design:type", Boolean)
], User.prototype, "isActive", void 0);
__decorate([
    (0, typeorm_1.CreateDateColumn)({ name: 'created_at' }),
    __metadata("design:type", Date)
], User.prototype, "createdAt", void 0);
__decorate([
    (0, typeorm_1.UpdateDateColumn)({ name: 'updated_at' }),
    __metadata("design:type", Date)
], User.prototype, "updatedAt", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => academic_term_entity_1.AcademicTerm, (academicTerm) => academicTerm.user),
    __metadata("design:type", Array)
], User.prototype, "academicTerms", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => category_entity_1.Category, (category) => category.user),
    __metadata("design:type", Array)
], User.prototype, "categories", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => budget_entity_1.Budget, (budget) => budget.user),
    __metadata("design:type", Array)
], User.prototype, "budgets", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => transaction_entity_1.Transaction, (transaction) => transaction.user),
    __metadata("design:type", Array)
], User.prototype, "transactions", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => milestone_entity_1.Milestone, (milestone) => milestone.user),
    __metadata("design:type", Array)
], User.prototype, "milestones", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => alert_entity_1.Alert, (alert) => alert.user),
    __metadata("design:type", Array)
], User.prototype, "alerts", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => subscription_entity_1.Subscription, (subscription) => subscription.user),
    __metadata("design:type", Array)
], User.prototype, "subscriptions", void 0);
exports.User = User = __decorate([
    (0, typeorm_1.Entity)('users')
], User);
//# sourceMappingURL=user.entity.js.map