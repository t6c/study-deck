import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/api/v1/auth/login', () =>
    HttpResponse.json({
      accessToken: 'test-token',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: { id: 1, email: 'test@example.com', displayName: 'Test User' },
    }),
  ),
  http.post('/api/v1/auth/register', () =>
    HttpResponse.json({
      accessToken: 'new-token',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: { id: 2, email: 'new@example.com', displayName: 'New User' },
    }),
  ),
  http.get('/api/v1/folders', () => HttpResponse.json([])),
  http.get('/api/v1/decks', () => HttpResponse.json([])),
  http.get('/api/v1/decks/:deckId', ({ params }) =>
    HttpResponse.json({
      id: Number(params.deckId),
      folderId: null,
      title: 'Sample Deck',
      description: null,
      visibility: 'PRIVATE',
      createdAt: '',
      updatedAt: '',
    }),
  ),
  http.get('/api/v1/decks/:deckId/summary', ({ params }) =>
    HttpResponse.json({
      deckId: Number(params.deckId),
      totalCards: 0,
      starredCards: 0,
      dueSrsCards: 0,
      newCards: 0,
      learningCards: 0,
      reviewCards: 0,
      availableModes: [],
    }),
  ),
  http.get('/api/v1/decks/:deckId/flashcards', () => HttpResponse.json([])),
  http.get('/api/v1/decks/:deckId/viewer-cards', () => HttpResponse.json([])),
  http.post('/api/v1/decks/:deckId/flashcards', async ({ params, request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return HttpResponse.json({
      id: 1,
      deckId: Number(params.deckId),
      ...body,
      starred: false,
      position: 0,
      createdAt: '',
      updatedAt: '',
    });
  }),
  http.patch('/api/v1/flashcards/:flashcardId', async ({ params, request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return HttpResponse.json({
      id: Number(params.flashcardId),
      deckId: 1,
      ...body,
      starred: false,
      position: 0,
      createdAt: '',
      updatedAt: '',
    });
  }),
  http.patch('/api/v1/flashcards/:flashcardId/star', async ({ params, request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return HttpResponse.json({
      id: Number(params.flashcardId),
      deckId: 1,
      term: 'Sample',
      definition: 'Sample definition',
      termImageUrl: null,
      definitionImageUrl: null,
      ...body,
      position: 0,
      createdAt: '',
      updatedAt: '',
    });
  }),
  http.delete('/api/v1/flashcards/:flashcardId', () => new HttpResponse(null, { status: 204 })),
  http.post('/api/v1/decks/:deckId/sorting-sessions', () =>
    HttpResponse.json({
      id: 1,
      status: 'ACTIVE',
      knownCount: 0,
      doNotKnowCount: 0,
      items: [],
    }),
  ),
  http.post('/api/v1/sorting-sessions/:sessionId/answers', ({ params }) =>
    HttpResponse.json({
      id: Number(params.sessionId),
      status: 'ACTIVE',
      knownCount: 0,
      doNotKnowCount: 0,
      items: [],
    }),
  ),
  http.post('/api/v1/decks/:deckId/learn-sessions', () =>
    HttpResponse.json({
      id: 1,
      status: 'ACTIVE',
      totalItems: 0,
      correctCount: 0,
      wrongCount: 0,
      items: [],
    }),
  ),
  http.get('/api/v1/learn-sessions/:sessionId', ({ params }) =>
    HttpResponse.json({
      id: Number(params.sessionId),
      status: 'ACTIVE',
      totalItems: 0,
      correctCount: 0,
      wrongCount: 0,
      items: [],
    }),
  ),
  http.post('/api/v1/learn-sessions/:sessionId/answers', ({ params }) =>
    HttpResponse.json({
      id: Number(params.sessionId),
      status: 'ACTIVE',
      totalItems: 0,
      correctCount: 0,
      wrongCount: 0,
      items: [],
    }),
  ),
  http.post('/api/v1/learn-sessions/:sessionId/complete', ({ params }) =>
    HttpResponse.json({
      id: Number(params.sessionId),
      status: 'COMPLETED',
      totalItems: 0,
      correctCount: 0,
      wrongCount: 0,
      items: [],
    }),
  ),
  http.post('/api/v1/decks/:deckId/practice-tests', () =>
    HttpResponse.json({
      id: 1,
      status: 'ACTIVE',
      questionCount: 0,
      answeredCount: 0,
      scorePercent: 0,
      questions: [],
    }),
  ),
  http.get('/api/v1/practice-tests/:practiceTestId', ({ params }) =>
    HttpResponse.json({
      id: Number(params.practiceTestId),
      status: 'ACTIVE',
      questionCount: 0,
      answeredCount: 0,
      scorePercent: 0,
      questions: [],
    }),
  ),
  http.post('/api/v1/practice-tests/:practiceTestId/answers', ({ params }) =>
    HttpResponse.json({
      id: Number(params.practiceTestId),
      status: 'ACTIVE',
      questionCount: 0,
      answeredCount: 0,
      scorePercent: 0,
      questions: [],
    }),
  ),
  http.post('/api/v1/practice-tests/:practiceTestId/submit', ({ params }) =>
    HttpResponse.json({
      id: Number(params.practiceTestId),
      status: 'SUBMITTED',
      questionCount: 0,
      answeredCount: 0,
      scorePercent: 0,
      questions: [],
    }),
  ),
  http.post('/api/v1/decks/:deckId/matching-sessions', () =>
    HttpResponse.json({
      id: 1,
      status: 'ACTIVE',
      cardCount: 0,
      matchedCount: 0,
      durationMs: 0,
      items: [],
    }),
  ),
  http.get('/api/v1/matching-sessions/:sessionId', ({ params }) =>
    HttpResponse.json({
      id: Number(params.sessionId),
      status: 'ACTIVE',
      cardCount: 0,
      matchedCount: 0,
      durationMs: 0,
      items: [],
    }),
  ),
  http.post('/api/v1/matching-sessions/:sessionId/matches', ({ params }) =>
    HttpResponse.json({
      id: Number(params.sessionId),
      status: 'ACTIVE',
      cardCount: 0,
      matchedCount: 0,
      durationMs: 0,
      items: [],
    }),
  ),
  http.post('/api/v1/matching-sessions/:sessionId/complete', ({ params }) =>
    HttpResponse.json({
      id: Number(params.sessionId),
      status: 'COMPLETED',
      cardCount: 0,
      matchedCount: 0,
      durationMs: 0,
      items: [],
    }),
  ),
  http.get('/api/v1/decks/:deckId/srs/due', () => HttpResponse.json([])),
  http.post('/api/v1/flashcards/:flashcardId/srs/reviews', async ({ params, request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return HttpResponse.json({
      flashcardId: Number(params.flashcardId),
      rating: body.rating,
      state: 'REVIEW',
      dueAt: '',
      reps: 1,
      lapses: 0,
    });
  }),
  http.get('/api/v1/flashcards/:flashcardId/srs-state', ({ params }) =>
    HttpResponse.json({
      flashcardId: Number(params.flashcardId),
      state: 'NEW',
      dueAt: '',
      stability: 0,
      difficulty: 0,
      reps: 0,
      lapses: 0,
    }),
  ),
  http.get('/api/v1/decks/:deckId/srs/stats', () =>
    HttpResponse.json({
      newCards: 0,
      learningCards: 0,
      reviewCards: 0,
      dueCards: 0,
    }),
  ),
];
