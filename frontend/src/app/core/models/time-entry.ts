export interface TimeEntry {
  id: number;
  userId: number;
  userName: string;
  taskId: number;
  taskTitle: string;
  startTime: string;
  endTime: string | null;
  durationMinutes: number | null;
  description: string | null;
}