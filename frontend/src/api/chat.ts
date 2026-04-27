import { http } from './http';

export interface ChatRequest {
  message: string;
  sessionId?: string;
}

export interface ChatResponse {
  sessionId: string;
  answer: string;
  createdAt: string;
}

export interface ChatSessionSummary {
  sessionId: string;
  title: string;
  lastMessagePreview: string;
  messageCount: number;
  updatedAt: string;
}

export interface ChatSessionDetail {
  sessionId: string;
  title: string;
  updatedAt: string;
  messages: Array<{
    id: string;
    role: 'user' | 'assistant';
    content: string;
    modelName: string | null;
    createdAt: string;
  }>;
}

export interface ChatMessageSearchResult {
  sessionId: string;
  sessionTitle: string;
  messageId: string;
  role: 'user' | 'assistant';
  snippet: string;
  createdAt: string;
}

export function sendChatMessage(payload: ChatRequest) {
  return http.post<ChatResponse>('/chat', payload).then((response) => response.data);
}

export function getChatSessions() {
  return http.get<ChatSessionSummary[]>('/chat/sessions').then((response) => response.data);
}

export function getChatSessionMessages(sessionId: string) {
  return http.get<ChatSessionDetail>(`/chat/sessions/${sessionId}/messages`).then((response) => response.data);
}

export function searchChatMessages(keyword: string) {
  return http
    .get<ChatMessageSearchResult[]>('/chat/messages/search', { params: { keyword } })
    .then((response) => response.data);
}
