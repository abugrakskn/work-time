export interface Task {
  id: number;
  title: string;
  description: string;
  dueDate: string;
  estimatedDurationMinutes: number;
  priority: string;
  status: string;
  projectId: number;
  projectName: string;
  assignedUserId: number | null;
  assignedUserName: string | null;
}