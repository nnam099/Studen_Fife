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
exports.Transaction = void 0;
const typeorm_1 = require("typeorm");
const academic_term_entity_1 = require("../../academic-terms/entities/academic-term.entity");
const category_entity_1 = require("../../categories/entities/category.entity");
const milestone_entity_1 = require("../../milestones/entities/milestone.entity");
const user_entity_1 = require("../../users/entities/user.entity");
let Transaction = class Transaction {
};
exports.Transaction = Transaction;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)('uuid'),
    __metadata("design:type", String)
], Transaction.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'decimal', precision: 12, scale: 2 }),
    __metadata("design:type", Number)
], Transaction.prototype, "amount", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'varchar', default: 'expense' }),
    __metadata("design:type", String)
], Transaction.prototype, "type", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 255 }),
    __metadata("design:type", String)
], Transaction.prototype, "description", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'date', name: 'occurred_at' }),
    __metadata("design:type", Date)
], Transaction.prototype, "occurredAt", void 0);
__decorate([
    (0, typeorm_1.CreateDateColumn)({ name: 'created_at' }),
    __metadata("design:type", Date)
], Transaction.prototype, "createdAt", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => user_entity_1.User, (user) => user.transactions, { onDelete: 'CASCADE' }),
    __metadata("design:type", user_entity_1.User)
], Transaction.prototype, "user", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => category_entity_1.Category, (category) => category.transactions, {
        onDelete: 'CASCADE',
    }),
    __metadata("design:type", category_entity_1.Category)
], Transaction.prototype, "category", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => academic_term_entity_1.AcademicTerm, (academicTerm) => academicTerm.transactions, {
        onDelete: 'CASCADE',
    }),
    __metadata("design:type", academic_term_entity_1.AcademicTerm)
], Transaction.prototype, "academicTerm", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => milestone_entity_1.Milestone, (milestone) => milestone.transactions, {
        onDelete: 'SET NULL',
        nullable: true,
    }),
    __metadata("design:type", Object)
], Transaction.prototype, "milestone", void 0);
exports.Transaction = Transaction = __decorate([
    (0, typeorm_1.Entity)('transactions')
], Transaction);
//# sourceMappingURL=transaction.entity.js.map