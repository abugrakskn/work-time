export interface UpdateTaskRequest {
  title: string;
  description: string;
  dueDate: string | null;
  estimatedDurationMinutes: number | null;
  priority: string | null;
  status: string;
  projectId: number;
  assignedUserId: number | null;
}