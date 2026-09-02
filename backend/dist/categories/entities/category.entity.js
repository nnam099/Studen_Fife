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
exports.Category = void 0;
const typeorm_1 = require("typeorm");
const budget_entity_1 = require("../../budgets/entities/budget.entity");
const transaction_entity_1 = require("../../transactions/entities/transaction.entity");
const user_entity_1 = require("../../users/entities/user.entity");
let Category = class Category {
};
exports.Category = Category;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)('uuid'),
    __metadata("design:type", String)
], Category.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 255 }),
    __metadata("design:type", String)
], Category.prototype, "name", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'varchar', default: 'expense' }),
    __metadata("design:type", String)
], Category.prototype, "type", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 16, nullable: true }),
    __metadata("design:type", String)
], Category.prototype, "color", void 0);
__decorate([
    (0, typeorm_1.Column)({ name: 'is_default', default: false }),
    __metadata("design:type", Boolean)
], Category.prototype, "isDefault", void 0);
__decorate([
    (0, typeorm_1.CreateDateColumn)({ name: 'created_at' }),
    __metadata("design:type", Date)
], Category.prototype, "createdAt", void 0);
__decorate([
    (0, typeorm_1.UpdateDateColumn)({ name: 'updated_at' }),
    __metadata("design:type", Date)
], Category.prototype, "updatedAt", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => user_entity_1.User, (user) => user.categories, { onDelete: 'CASCADE' }),
    __metadata("design:type", user_entity_1.User)
], Category.prototype, "user", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => budget_entity_1.Budget, (budget) => budget.category),
    __metadata("design:type", Array)
], Category.prototype, "budgets", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => transaction_entity_1.Transaction, (transaction) => transaction.category),
    __metadata("design:type", Array)
], Category.prototype, "transactions", void 0);
exports.Category = Category = __decorate([
    (0, typeorm_1.Entity)('categories')
], Category);
//# sourceMappingURL=category.entity.js.map