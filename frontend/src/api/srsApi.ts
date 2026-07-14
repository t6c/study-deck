import { apiClient } from './client';
import type {
  SrsCardStateResponse,
  SrsDueCardResponse,
  SrsReviewRequest,
  SrsReviewResponse,
  SrsStatsResponse,
} from './types';

export async function getSrsDueCards(deckId: number) {
  const response = await apiClient.get<SrsDueCardResponse[]>(`/decks/${deckId}/srs/due`);
  return response.data;
}

export async function getSrsStats(deckId: number) {
  const response = await apiClient.get<SrsStatsResponse>(`/decks/${deckId}/srs/stats`);
  return response.data;
}

export async function reviewFlashcard(flashcardId: number, request: SrsReviewRequest) {
  const response = await apiClient.post<SrsReviewResponse>(`/flashcards/${flashcardId}/srs/reviews`, request);
  return response.data;
}

export async function getSrsCardState(flashcardId: number) {
  const response = await apiClient.get<SrsCardStateResponse>(`/flashcards/${flashcardId}/srs-state`);
  return response.data;
}
