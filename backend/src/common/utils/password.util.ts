import * as bcrypt from 'bcrypt';

export async function hashPassword(password: string): Promise<string> {
  const salt = await bcrypt.genSalt(10);
  return bcrypt.hash(password, salt);
}

export async function comparePassword(
  plainText: string,
  hashedPassword: string,
): Promise<boolean> {
  return bcrypt.compare(plainText, hashedPassword);
}
