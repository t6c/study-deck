import { apiClient } from './client';
import type { CreateLearnSessionRequest, LearnAnswerRequest, LearnSessionResponse } from './types';

export async function createLearnSession(deckId: number, request: CreateLearnSessionRequest) {
  const response = await apiClient.post<LearnSessionResponse>(`/decks/${deckId}/learn-sessions`, request);
  return response.data;
}

export async function getLearnSession(sessionId: number) {
  const response = await apiClient.get<LearnSessionResponse>(`/learn-sessions/${sessionId}`);
  return response.data;
}

export async function answerLearnSessionItem(sessionId: number, request: LearnAnswerRequest) {
  const response = await apiClient.post<LearnSessionResponse>(`/learn-sessions/${sessionId}/answers`, request);
  return response.data;
}

export async function completeLearnSession(sessionId: number) {
  const response = await apiClient.post<LearnSessionResponse>(`/learn-sessions/${sessionId}/complete`);
  return response.data;
}
