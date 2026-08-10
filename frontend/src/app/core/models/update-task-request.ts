import { TaskStatus } from './task-status';

export interface UpdateTaskRequest {
  title: string;
  description: string;
  dueDate: string | null;
  estimatedDurationMinutes: number | null;
  priority: string | null;
  status: TaskStatus;
  projectId: number;
  assignedUserId: number | null;
}