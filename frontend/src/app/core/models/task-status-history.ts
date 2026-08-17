import { TaskStatus } from './task-status';

export interface TaskStatusHistory {
  id: number;
  previousStatus: TaskStatus;
  newStatus: TaskStatus;
  changedByUserId: number;
  changedByUserName: string;
  changedAt: string;
}