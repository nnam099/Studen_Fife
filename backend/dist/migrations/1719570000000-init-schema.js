"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.InitSchema1719570000000 = void 0;
class InitSchema1719570000000 {
    constructor() {
        this.name = 'InitSchema1719570000000';
    }
    async up(queryRunner) {
        await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS users (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        email VARCHAR(255) NOT NULL UNIQUE,
        full_name VARCHAR(255) NOT NULL,
        password_hash TEXT NOT NULL,
        is_active BOOLEAN NOT NULL DEFAULT true,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS academic_terms (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL,
        name VARCHAR(255) NOT NULL,
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        status VARCHAR(16) NOT NULL DEFAULT 'active',
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_academic_terms_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
      );
    `);
        await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS categories (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL,
        name VARCHAR(255) NOT NULL,
        type VARCHAR(16) NOT NULL DEFAULT 'expense',
        color VARCHAR(16),
        is_default BOOLEAN NOT NULL DEFAULT false,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
      );
    `);
        await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS budgets (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL,
        academic_term_id UUID NOT NULL,
        category_id UUID NOT NULL,
        amount DECIMAL(12,2) NOT NULL DEFAULT 0,
        period_type VARCHAR(20) NOT NULL DEFAULT 'weekly',
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        currency VARCHAR(10) NOT NULL DEFAULT 'VND',
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT fk_budgets_term FOREIGN KEY (academic_term_id) REFERENCES academic_terms(id) ON DELETE CASCADE,
        CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
      );
    `);
        await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS milestones (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL,
        academic_term_id UUID NOT NULL,
        title VARCHAR(255) NOT NULL,
        description TEXT,
        due_date DATE NOT NULL,
        type VARCHAR(20) NOT NULL DEFAULT 'exam',
        is_completed BOOLEAN NOT NULL DEFAULT false,
        priority INT NOT NULL DEFAULT 1,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_milestones_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT fk_milestones_term FOREIGN KEY (academic_term_id) REFERENCES academic_terms(id) ON DELETE CASCADE
      );
    `);
        await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS transactions (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL,
        category_id UUID NOT NULL,
        academic_term_id UUID NOT NULL,
        milestone_id UUID,
        type VARCHAR(16) NOT NULL DEFAULT 'expense',
        amount DECIMAL(12,2) NOT NULL,
        description VARCHAR(255) NOT NULL,
        occurred_at DATE NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
        CONSTRAINT fk_transactions_term FOREIGN KEY (academic_term_id) REFERENCES academic_terms(id) ON DELETE CASCADE,
        CONSTRAINT fk_transactions_milestone FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE SET NULL
      );
    `);
        await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS alerts (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL,
        academic_term_id UUID NOT NULL,
        budget_id UUID,
        type VARCHAR(32) NOT NULL,
        title VARCHAR(255) NOT NULL,
        message TEXT NOT NULL,
        severity VARCHAR(16) NOT NULL DEFAULT 'warning',
        triggered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        status VARCHAR(16) NOT NULL DEFAULT 'new',
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_alerts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT fk_alerts_term FOREIGN KEY (academic_term_id) REFERENCES academic_terms(id) ON DELETE CASCADE,
        CONSTRAINT fk_alerts_budget FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE SET NULL
      );
    `);
        await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS subscriptions (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID NOT NULL,
        plan_code VARCHAR(16) NOT NULL DEFAULT 'free',
        status VARCHAR(16) NOT NULL DEFAULT 'active',
        started_at TIMESTAMP,
        ended_at TIMESTAMP,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
      );
    `);
        await queryRunner.query(`
      CREATE INDEX IF NOT EXISTS idx_academic_terms_user_id ON academic_terms(user_id);
      CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories(user_id);
      CREATE INDEX IF NOT EXISTS idx_budgets_user_term ON budgets(user_id, academic_term_id);
      CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(user_id, occurred_at);
      CREATE INDEX IF NOT EXISTS idx_transactions_term ON transactions(academic_term_id);
      CREATE INDEX IF NOT EXISTS idx_milestones_user_due ON milestones(user_id, due_date);
      CREATE INDEX IF NOT EXISTS idx_alerts_user ON alerts(user_id, status, triggered_at);
    `);
    }
    async down(queryRunner) {
        await queryRunner.query(`DROP TABLE IF EXISTS subscriptions;`);
        await queryRunner.query(`DROP TABLE IF EXISTS alerts;`);
        await queryRunner.query(`DROP TABLE IF EXISTS transactions;`);
        await queryRunner.query(`DROP TABLE IF EXISTS milestones;`);
        await queryRunner.query(`DROP TABLE IF EXISTS budgets;`);
        await queryRunner.query(`DROP TABLE IF EXISTS categories;`);
        await queryRunner.query(`DROP TABLE IF EXISTS academic_terms;`);
        await queryRunner.query(`DROP TABLE IF EXISTS users;`);
    }
}
exports.InitSchema1719570000000 = InitSchema1719570000000;
//# sourceMappingURL=1719570000000-init-schema.js.map