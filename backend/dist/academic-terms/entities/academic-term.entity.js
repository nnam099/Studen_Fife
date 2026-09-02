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
exports.AcademicTerm = void 0;
const typeorm_1 = require("typeorm");
const alert_entity_1 = require("../../alerts/entities/alert.entity");
const budget_entity_1 = require("../../budgets/entities/budget.entity");
const milestone_entity_1 = require("../../milestones/entities/milestone.entity");
const transaction_entity_1 = require("../../transactions/entities/transaction.entity");
const user_entity_1 = require("../../users/entities/user.entity");
let AcademicTerm = class AcademicTerm {
};
exports.AcademicTerm = AcademicTerm;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)('uuid'),
    __metadata("design:type", String)
], AcademicTerm.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 255 }),
    __metadata("design:type", String)
], AcademicTerm.prototype, "name", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'date', name: 'start_date' }),
    __metadata("design:type", Date)
], AcademicTerm.prototype, "startDate", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'date', name: 'end_date' }),
    __metadata("design:type", Date)
], AcademicTerm.prototype, "endDate", void 0);
__decorate([
    (0, typeorm_1.Column)({ default: 'active' }),
    __metadata("design:type", String)
], AcademicTerm.prototype, "status", void 0);
__decorate([
    (0, typeorm_1.CreateDateColumn)({ name: 'created_at' }),
    __metadata("design:type", Date)
], AcademicTerm.prototype, "createdAt", void 0);
__decorate([
    (0, typeorm_1.UpdateDateColumn)({ name: 'updated_at' }),
    __metadata("design:type", Date)
], AcademicTerm.prototype, "updatedAt", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => user_entity_1.User, (user) => user.academicTerms, { onDelete: 'CASCADE' }),
    __metadata("design:type", user_entity_1.User)
], AcademicTerm.prototype, "user", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => budget_entity_1.Budget, (budget) => budget.academicTerm),
    __metadata("design:type", Array)
], AcademicTerm.prototype, "budgets", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => transaction_entity_1.Transaction, (transaction) => transaction.academicTerm),
    __metadata("design:type", Array)
], AcademicTerm.prototype, "transactions", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => milestone_entity_1.Milestone, (milestone) => milestone.academicTerm),
    __metadata("design:type", Array)
], AcademicTerm.prototype, "milestones", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => alert_entity_1.Alert, (alert) => alert.academicTerm),
    __metadata("design:type", Array)
], AcademicTerm.prototype, "alerts", void 0);
exports.AcademicTerm = AcademicTerm = __decorate([
    (0, typeorm_1.Entity)('academic_terms')
], AcademicTerm);
//# sourceMappingURL=academic-term.entity.js.map