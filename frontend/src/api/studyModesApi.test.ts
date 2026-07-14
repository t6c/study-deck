import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../test/server';
import {
  answerLearnSessionItem,
  completeLearnSession,
  createLearnSession,
  getLearnSession,
} from './learnApi';
import {
  answerPracticeQuestion,
  createPracticeTest,
  getPracticeTest,
  submitPracticeTest,
} from './practiceApi';
import {
  completeMatchingSession,
  createMatchingSession,
  getMatchingSession,
  matchMatchingItem,
} from './matchingApi';
import { getSrsCardState, getSrsDueCards, getSrsStats, reviewFlashcard } from './srsApi';

describe('study mode API modules', () => {
  it('calls learn session endpoints under /api/v1', async () => {
    const calls: string[] = [];

    server.use(
      http.post('/api/v1/decks/2/learn-sessions', async ({ request }) => {
        calls.push(`POST /decks/2/learn-sessions ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ id: 11, status: 'ACTIVE', totalItems: 0, correctCount: 0, wrongCount: 0, items: [] });
      }),
      http.get('/api/v1/learn-sessions/11', () => {
        calls.push('GET /learn-sessions/11');
        return HttpResponse.json({ id: 11, status: 'ACTIVE', totalItems: 0, correctCount: 0, wrongCount: 0, items: [] });
      }),
      http.post('/api/v1/learn-sessions/11/answers', async ({ request }) => {
        calls.push(`POST /learn-sessions/11/answers ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ id: 11, status: 'ACTIVE', totalItems: 1, correctCount: 1, wrongCount: 0, items: [] });
      }),
      http.post('/api/v1/learn-sessions/11/complete', () => {
        calls.push('POST /learn-sessions/11/complete');
        return HttpResponse.json({ id: 11, status: 'COMPLETED', totalItems: 1, correctCount: 1, wrongCount: 0, items: [] });
      }),
    );

    await createLearnSession(2, {
      lengthOfRounds: 10,
      flashcards: true,
      multipleChoice: true,
      written: true,
      trueFalse: false,
      starredOnly: true,
      shuffleTerms: false,
    });
    await getLearnSession(11);
    await answerLearnSessionItem(11, { itemId: 21, answer: 'lessen' });
    await completeLearnSession(11);

    expect(calls).toEqual([
      'POST /decks/2/learn-sessions {"lengthOfRounds":10,"flashcards":true,"multipleChoice":true,"written":true,"trueFalse":false,"starredOnly":true,"shuffleTerms":false}',
      'GET /learn-sessions/11',
      'POST /learn-sessions/11/answers {"itemId":21,"answer":"lessen"}',
      'POST /learn-sessions/11/complete',
    ]);
  });

  it('calls practice test endpoints under /api/v1', async () => {
    const calls: string[] = [];

    server.use(
      http.post('/api/v1/decks/2/practice-tests', async ({ request }) => {
        calls.push(`POST /decks/2/practice-tests ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ id: 31, status: 'ACTIVE', questionCount: 1, answeredCount: 0, scorePercent: 0, questions: [] });
      }),
      http.get('/api/v1/practice-tests/31', () => {
        calls.push('GET /practice-tests/31');
        return HttpResponse.json({ id: 31, status: 'ACTIVE', questionCount: 1, answeredCount: 0, scorePercent: 0, questions: [] });
      }),
      http.post('/api/v1/practice-tests/31/answers', async ({ request }) => {
        calls.push(`POST /practice-tests/31/answers ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ id: 31, status: 'ACTIVE', questionCount: 1, answeredCount: 1, scorePercent: 0, questions: [] });
      }),
      http.post('/api/v1/practice-tests/31/submit', () => {
        calls.push('POST /practice-tests/31/submit');
        return HttpResponse.json({ id: 31, status: 'SUBMITTED', questionCount: 1, answeredCount: 1, scorePercent: 100, questions: [] });
      }),
    );

    await createPracticeTest(2, {
      questionCount: 5,
      multipleChoice: true,
      written: true,
      trueFalse: false,
      starredOnly: false,
      answerWithTerm: true,
      answerWithDefinition: false,
    });
    await getPracticeTest(31);
    await answerPracticeQuestion(31, { questionId: 41, answer: 'abate' });
    await submitPracticeTest(31);

    expect(calls).toEqual([
      'POST /decks/2/practice-tests {"questionCount":5,"multipleChoice":true,"written":true,"trueFalse":false,"starredOnly":false,"answerWithTerm":true,"answerWithDefinition":false}',
      'GET /practice-tests/31',
      'POST /practice-tests/31/answers {"questionId":41,"answer":"abate"}',
      'POST /practice-tests/31/submit',
    ]);
  });

  it('calls matching session endpoints under /api/v1', async () => {
    const calls: string[] = [];

    server.use(
      http.post('/api/v1/decks/2/matching-sessions', async ({ request }) => {
        calls.push(`POST /decks/2/matching-sessions ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ id: 51, status: 'ACTIVE', cardCount: 1, matchedCount: 0, durationMs: 0, items: [] });
      }),
      http.get('/api/v1/matching-sessions/51', () => {
        calls.push('GET /matching-sessions/51');
        return HttpResponse.json({ id: 51, status: 'ACTIVE', cardCount: 1, matchedCount: 0, durationMs: 0, items: [] });
      }),
      http.post('/api/v1/matching-sessions/51/matches', async ({ request }) => {
        calls.push(`POST /matching-sessions/51/matches ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ id: 51, status: 'ACTIVE', cardCount: 1, matchedCount: 1, durationMs: 1000, items: [] });
      }),
      http.post('/api/v1/matching-sessions/51/complete', () => {
        calls.push('POST /matching-sessions/51/complete');
        return HttpResponse.json({ id: 51, status: 'COMPLETED', cardCount: 1, matchedCount: 1, durationMs: 1000, items: [] });
      }),
    );

    await createMatchingSession(2, { cardCount: 6, starredOnly: true });
    await getMatchingSession(51);
    await matchMatchingItem(51, { itemId: 61 });
    await completeMatchingSession(51);

    expect(calls).toEqual([
      'POST /decks/2/matching-sessions {"cardCount":6,"starredOnly":true}',
      'GET /matching-sessions/51',
      'POST /matching-sessions/51/matches {"itemId":61}',
      'POST /matching-sessions/51/complete',
    ]);
  });

  it('calls SRS endpoints under /api/v1', async () => {
    const calls: string[] = [];

    server.use(
      http.get('/api/v1/decks/2/srs/due', () => {
        calls.push('GET /decks/2/srs/due');
        return HttpResponse.json([]);
      }),
      http.post('/api/v1/flashcards/7/srs/reviews', async ({ request }) => {
        calls.push(`POST /flashcards/7/srs/reviews ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ flashcardId: 7, rating: 'GOOD', state: 'REVIEW', dueAt: '', reps: 1, lapses: 0 });
      }),
      http.get('/api/v1/flashcards/7/srs-state', () => {
        calls.push('GET /flashcards/7/srs-state');
        return HttpResponse.json({ flashcardId: 7, state: 'REVIEW', dueAt: '', stability: 1, difficulty: 5, reps: 1, lapses: 0 });
      }),
      http.get('/api/v1/decks/2/srs/stats', () => {
        calls.push('GET /decks/2/srs/stats');
        return HttpResponse.json({ newCards: 1, learningCards: 2, reviewCards: 3, dueCards: 4 });
      }),
    );

    await getSrsDueCards(2);
    await reviewFlashcard(7, { rating: 'GOOD', durationMs: 2500 });
    await getSrsCardState(7);
    await getSrsStats(2);

    expect(calls).toEqual([
      'GET /decks/2/srs/due',
      'POST /flashcards/7/srs/reviews {"rating":"GOOD","durationMs":2500}',
      'GET /flashcards/7/srs-state',
      'GET /decks/2/srs/stats',
    ]);
  });
});
