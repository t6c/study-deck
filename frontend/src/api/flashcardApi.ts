import { apiClient } from './client';
import type {
  CreateFlashcardRequest,
  FlashcardResponse,
  StarFlashcardRequest,
  UpdateFlashcardRequest,
} from './types';

export async function listFlashcards(deckId: number) {
  const response = await apiClient.get<FlashcardResponse[]>(`/decks/${deckId}/flashcards`);
  return response.data;
}

export async function createFlashcard(deckId: number, request: CreateFlashcardRequest) {
  const response = await apiClient.post<FlashcardResponse>(`/decks/${deckId}/flashcards`, request);
  return response.data;
}

export async function updateFlashcard(flashcardId: number, request: UpdateFlashcardRequest) {
  const response = await apiClient.patch<FlashcardResponse>(`/flashcards/${flashcardId}`, request);
  return response.data;
}

export async function deleteFlashcard(flashcardId: number) {
  await apiClient.delete(`/flashcards/${flashcardId}`);
}

export async function starFlashcard(flashcardId: number, request: StarFlashcardRequest) {
  const response = await apiClient.patch<FlashcardResponse>(`/flashcards/${flashcardId}/star`, request);
  return response.data;
}

export async function setFlashcardStarred(flashcardId: number, starred: boolean) {
  return starFlashcard(flashcardId, { starred });
}
