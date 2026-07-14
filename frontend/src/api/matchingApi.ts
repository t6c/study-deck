import { apiClient } from './client';
import type { CreateMatchingSessionRequest, MatchingAnswerRequest, MatchingSessionResponse } from './types';

export async function createMatchingSession(deckId: number, request: CreateMatchingSessionRequest) {
  const response = await apiClient.post<MatchingSessionResponse>(`/decks/${deckId}/matching-sessions`, request);
  return response.data;
}

export async function getMatchingSession(sessionId: number) {
  const response = await apiClient.get<MatchingSessionResponse>(`/matching-sessions/${sessionId}`);
  return response.data;
}

export async function matchMatchingItem(sessionId: number, request: MatchingAnswerRequest) {
  const response = await apiClient.post<MatchingSessionResponse>(`/matching-sessions/${sessionId}/matches`, request);
  return response.data;
}

export async function completeMatchingSession(sessionId: number) {
  const response = await apiClient.post<MatchingSessionResponse>(`/matching-sessions/${sessionId}/complete`);
  return response.data;
}
