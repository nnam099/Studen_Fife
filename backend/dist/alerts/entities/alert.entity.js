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
exports.Alert = void 0;
const typeorm_1 = require("typeorm");
const academic_term_entity_1 = require("../../academic-terms/entities/academic-term.entity");
const budget_entity_1 = require("../../budgets/entities/budget.entity");
const milestone_entity_1 = require("../../milestones/entities/milestone.entity");
const user_entity_1 = require("../../users/entities/user.entity");
let Alert = class Alert {
};
exports.Alert = Alert;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)('uuid'),
    __metadata("design:type", String)
], Alert.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'varchar' }),
    __metadata("design:type", String)
], Alert.prototype, "type", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 255 }),
    __metadata("design:type", String)
], Alert.prototype, "title", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'text' }),
    __metadata("design:type", String)
], Alert.prototype, "message", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'varchar', default: 'warning' }),
    __metadata("design:type", String)
], Alert.prototype, "severity", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'timestamp', name: 'triggered_at', default: () => 'CURRENT_TIMESTAMP' }),
    __metadata("design:type", Date)
], Alert.prototype, "triggeredAt", void 0);
__decorate([
    (0, typeorm_1.Column)({ default: 'new' }),
    __metadata("design:type", String)
], Alert.prototype, "status", void 0);
__decorate([
    (0, typeorm_1.CreateDateColumn)({ name: 'created_at' }),
    __metadata("design:type", Date)
], Alert.prototype, "createdAt", void 0);
__decorate([
    (0, typeorm_1.UpdateDateColumn)({ name: 'updated_at' }),
    __metadata("design:type", Date)
], Alert.prototype, "updatedAt", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => user_entity_1.User, (user) => user.alerts, { onDelete: 'CASCADE' }),
    __metadata("design:type", user_entity_1.User)
], Alert.prototype, "user", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => academic_term_entity_1.AcademicTerm, (academicTerm) => academicTerm.alerts, {
        onDelete: 'CASCADE',
    }),
    __metadata("design:type", academic_term_entity_1.AcademicTerm)
], Alert.prototype, "academicTerm", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => budget_entity_1.Budget, (budget) => budget.alerts, {
        nullable: true,
        onDelete: 'SET NULL',
    }),
    __metadata("design:type", Object)
], Alert.prototype, "budget", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => milestone_entity_1.Milestone, (milestone) => milestone.alerts, {
        nullable: true,
        onDelete: 'SET NULL',
    }),
    __metadata("design:type", Object)
], Alert.prototype, "milestone", void 0);
exports.Alert = Alert = __decorate([
    (0, typeorm_1.Entity)('alerts')
], Alert);
//# sourceMappingURL=alert.entity.js.map