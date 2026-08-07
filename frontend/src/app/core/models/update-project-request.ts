export interface UpdateProjectRequest {
  name: string;
  description: string;
  startDate: string | null;
  endDate: string | null;
  status: string;
}