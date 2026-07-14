import { apiClient } from './client';
import type { CreatePracticeTestRequest, PracticeAnswerRequest, PracticeTestResponse } from './types';

export async function createPracticeTest(deckId: number, request: CreatePracticeTestRequest) {
  const response = await apiClient.post<PracticeTestResponse>(`/decks/${deckId}/practice-tests`, request);
  return response.data;
}

export async function getPracticeTest(practiceTestId: number) {
  const response = await apiClient.get<PracticeTestResponse>(`/practice-tests/${practiceTestId}`);
  return response.data;
}

export async function answerPracticeQuestion(practiceTestId: number, request: PracticeAnswerRequest) {
  const response = await apiClient.post<PracticeTestResponse>(`/practice-tests/${practiceTestId}/answers`, request);
  return response.data;
}

export async function submitPracticeTest(practiceTestId: number) {
  const response = await apiClient.post<PracticeTestResponse>(`/practice-tests/${practiceTestId}/submit`);
  return response.data;
}
