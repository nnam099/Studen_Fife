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
exports.Budget = void 0;
const typeorm_1 = require("typeorm");
const academic_term_entity_1 = require("../../academic-terms/entities/academic-term.entity");
const alert_entity_1 = require("../../alerts/entities/alert.entity");
const category_entity_1 = require("../../categories/entities/category.entity");
const user_entity_1 = require("../../users/entities/user.entity");
let Budget = class Budget {
};
exports.Budget = Budget;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)('uuid'),
    __metadata("design:type", String)
], Budget.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'decimal', precision: 12, scale: 2, default: 0 }),
    __metadata("design:type", Number)
], Budget.prototype, "amount", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'varchar', default: 'weekly' }),
    __metadata("design:type", String)
], Budget.prototype, "periodType", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'date', name: 'start_date' }),
    __metadata("design:type", Date)
], Budget.prototype, "startDate", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'date', name: 'end_date' }),
    __metadata("design:type", Date)
], Budget.prototype, "endDate", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'varchar', default: 'VND' }),
    __metadata("design:type", String)
], Budget.prototype, "currency", void 0);
__decorate([
    (0, typeorm_1.CreateDateColumn)({ name: 'created_at' }),
    __metadata("design:type", Date)
], Budget.prototype, "createdAt", void 0);
__decorate([
    (0, typeorm_1.UpdateDateColumn)({ name: 'updated_at' }),
    __metadata("design:type", Date)
], Budget.prototype, "updatedAt", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => user_entity_1.User, (user) => user.budgets, { onDelete: 'CASCADE' }),
    __metadata("design:type", user_entity_1.User)
], Budget.prototype, "user", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => academic_term_entity_1.AcademicTerm, (academicTerm) => academicTerm.budgets, {
        onDelete: 'CASCADE',
    }),
    __metadata("design:type", academic_term_entity_1.AcademicTerm)
], Budget.prototype, "academicTerm", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => category_entity_1.Category, (category) => category.budgets, {
        onDelete: 'CASCADE',
    }),
    __metadata("design:type", category_entity_1.Category)
], Budget.prototype, "category", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => alert_entity_1.Alert, (alert) => alert.budget),
    __metadata("design:type", Array)
], Budget.prototype, "alerts", void 0);
exports.Budget = Budget = __decorate([
    (0, typeorm_1.Entity)('budgets')
], Budget);
//# sourceMappingURL=budget.entity.js.map