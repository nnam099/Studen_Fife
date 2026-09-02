import { MigrationInterface, QueryRunner } from 'typeorm';

export class AddMilestoneIdToAlerts1720000000000 implements MigrationInterface {
  name = 'AddMilestoneIdToAlerts1720000000000';

  public async up(queryRunner: QueryRunner): Promise<void> {
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

  public async down(queryRunner: QueryRunner): Promise<void> {
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
