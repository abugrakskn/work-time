export interface CreateTaskRequest {
  title: string;
  description: string;
  dueDate: string | null;
  estimatedDurationMinutes: number | null;
  priority: string | null;
  projectId: number;
  assignedUserId: number | null;
}