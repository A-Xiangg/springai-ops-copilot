import { http } from './http';

export interface DashboardStats {
  knowledgeBaseCount: number;
  sessionCount: number;
  recentMessages: Array<{
    id: string;
    sessionId: string;
    title: string;
    preview: string;
    time: string;
  }>;
}

export async function getDashboardStats(): Promise<DashboardStats> {
  return http.get<DashboardStats>('/dashboard/summary').then((response) => response.data);
}
