export declare function hashPassword(password: string): Promise<string>;
export declare function comparePassword(plainText: string, hashedPassword: string): Promise<boolean>;
