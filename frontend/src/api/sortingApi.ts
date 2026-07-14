import { apiClient } from './client';
import type { AnswerSortingItemRequest, CreateSortingSessionRequest, SortingSessionResponse } from './types';

export async function createSortingSession(deckId: number, request: CreateSortingSessionRequest) {
  const response = await apiClient.post<SortingSessionResponse>(`/decks/${deckId}/sorting-sessions`, request);
  return response.data;
}

export async function answerSortingItem(sessionId: number, request: AnswerSortingItemRequest) {
  const response = await apiClient.post<SortingSessionResponse>(`/sorting-sessions/${sessionId}/answers`, request);
  return response.data;
}
