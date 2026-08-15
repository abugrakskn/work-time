export interface CreateManualTimeEntryRequest {
  taskId: number;
  startTime: string;
  endTime: string;
  description: string;
}