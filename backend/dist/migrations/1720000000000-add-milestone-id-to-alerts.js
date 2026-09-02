"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AddMilestoneIdToAlerts1720000000000 = void 0;
class AddMilestoneIdToAlerts1720000000000 {
    constructor() {
        this.name = 'AddMilestoneIdToAlerts1720000000000';
    }
    async up(queryRunner) {
        await queryRunner.query(`
      ALTER TABLE alerts
      ADD COLUMN milestone_id UUID;
    `);
        await queryRunner.query(`
      ALTER TABLE alerts
      ADD CONSTRAINT fk_alerts_milestone
      FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE SET NULL;
    `);
        await queryRunner.query(`
      CREATE INDEX idx_alerts_milestone_id ON alerts(milestone_id);
    `);
    }
    async down(queryRunner) {
        await queryRunner.query(`DROP INDEX IF EXISTS idx_alerts_milestone_id;`);
        await queryRunner.query(`
      ALTER TABLE alerts
      DROP CONSTRAINT IF EXISTS fk_alerts_milestone;
    `);
        await queryRunner.query(`
      ALTER TABLE alerts
      DROP COLUMN IF EXISTS milestone_id;
    `);
    }
}
exports.AddMilestoneIdToAlerts1720000000000 = AddMilestoneIdToAlerts1720000000000;
//# sourceMappingURL=1720000000000-add-milestone-id-to-alerts.js.map