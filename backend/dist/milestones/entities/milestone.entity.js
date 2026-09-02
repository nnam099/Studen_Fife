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
exports.Milestone = void 0;
const typeorm_1 = require("typeorm");
const academic_term_entity_1 = require("../../academic-terms/entities/academic-term.entity");
const alert_entity_1 = require("../../alerts/entities/alert.entity");
const transaction_entity_1 = require("../../transactions/entities/transaction.entity");
const user_entity_1 = require("../../users/entities/user.entity");
let Milestone = class Milestone {
};
exports.Milestone = Milestone;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)('uuid'),
    __metadata("design:type", String)
], Milestone.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 255 }),
    __metadata("design:type", String)
], Milestone.prototype, "title", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'text', nullable: true }),
    __metadata("design:type", String)
], Milestone.prototype, "description", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'date', name: 'due_date' }),
    __metadata("design:type", Date)
], Milestone.prototype, "dueDate", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'varchar', default: 'exam' }),
    __metadata("design:type", String)
], Milestone.prototype, "type", void 0);
__decorate([
    (0, typeorm_1.Column)({ default: false }),
    __metadata("design:type", Boolean)
], Milestone.prototype, "isCompleted", void 0);
__decorate([
    (0, typeorm_1.Column)({ default: 1 }),
    __metadata("design:type", Number)
], Milestone.prototype, "priority", void 0);
__decorate([
    (0, typeorm_1.CreateDateColumn)({ name: 'created_at' }),
    __metadata("design:type", Date)
], Milestone.prototype, "createdAt", void 0);
__decorate([
    (0, typeorm_1.UpdateDateColumn)({ name: 'updated_at' }),
    __metadata("design:type", Date)
], Milestone.prototype, "updatedAt", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => user_entity_1.User, (user) => user.milestones, { onDelete: 'CASCADE' }),
    __metadata("design:type", user_entity_1.User)
], Milestone.prototype, "user", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => academic_term_entity_1.AcademicTerm, (academicTerm) => academicTerm.milestones, {
        onDelete: 'CASCADE',
    }),
    __metadata("design:type", academic_term_entity_1.AcademicTerm)
], Milestone.prototype, "academicTerm", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => transaction_entity_1.Transaction, (transaction) => transaction.milestone),
    __metadata("design:type", Array)
], Milestone.prototype, "transactions", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => alert_entity_1.Alert, (alert) => alert.milestone),
    __metadata("design:type", Array)
], Milestone.prototype, "alerts", void 0);
exports.Milestone = Milestone = __decorate([
    (0, typeorm_1.Entity)('milestones')
], Milestone);
//# sourceMappingURL=milestone.entity.js.map