import axios from 'axios';

export interface ChatRequest {
  message: string;
  sessionId?: string;
}

export interface ChatResponse {
  sessionId: string;
  answer: string;
  createdAt: string;
}

export function sendChatMessage(payload: ChatRequest) {
  return axios.post<ChatResponse>('/api/chat', payload).then((response) => response.data);
}
