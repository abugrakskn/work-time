export type UserRole = 'ADMIN' | 'EMPLOYEE';

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  active: boolean;
}