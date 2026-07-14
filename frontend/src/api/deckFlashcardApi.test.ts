import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../test/server';
import {
  addDeckToFolder,
  createDeck,
  createFolder,
  deleteDeck,
  deleteFolder,
  getDeck,
  getDeckSummary,
  getFolder,
  listDecks,
  listFolders,
  listViewerCards,
  removeDeckFromFolder,
  updateDeck,
  updateFolder,
} from './deckApi';
import {
  createFlashcard,
  deleteFlashcard,
  listFlashcards,
  setFlashcardStarred,
  updateFlashcard,
} from './flashcardApi';
import { answerSortingItem, createSortingSession } from './sortingApi';

describe('deck and flashcard API modules', () => {
  it('calls folder and deck endpoints under /api/v1', async () => {
    const calls: string[] = [];

    server.use(
      http.get('/api/v1/folders', () => {
        calls.push('GET /folders');
        return HttpResponse.json([]);
      }),
      http.post('/api/v1/folders', async ({ request }) => {
        calls.push(`POST /folders ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ id: 1, name: 'English', description: null, position: 0, createdAt: '', updatedAt: '' });
      }),
      http.get('/api/v1/folders/1', () => {
        calls.push('GET /folders/1');
        return HttpResponse.json({ id: 1, name: 'English', description: null, position: 0, createdAt: '', updatedAt: '' });
      }),
      http.patch('/api/v1/folders/1', async ({ request }) => {
        calls.push(`PATCH /folders/1 ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ id: 1, name: 'Languages', description: 'Updated', position: 0, createdAt: '', updatedAt: '' });
      }),
      http.delete('/api/v1/folders/1', () => {
        calls.push('DELETE /folders/1');
        return new HttpResponse(null, { status: 204 });
      }),
      http.get('/api/v1/decks', () => {
        calls.push('GET /decks');
        return HttpResponse.json([]);
      }),
      http.post('/api/v1/decks', async ({ request }) => {
        calls.push(`POST /decks ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 2,
          folderId: 1,
          title: 'Vocabulary',
          description: null,
          visibility: 'PRIVATE',
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.get('/api/v1/decks/2', () => {
        calls.push('GET /decks/2');
        return HttpResponse.json({
          id: 2,
          folderId: 1,
          title: 'Vocabulary',
          description: null,
          visibility: 'PRIVATE',
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.patch('/api/v1/decks/2', async ({ request }) => {
        calls.push(`PATCH /decks/2 ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 2,
          folderId: 1,
          title: 'Advanced Vocabulary',
          description: 'Updated',
          visibility: 'PRIVATE',
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.delete('/api/v1/decks/2', () => {
        calls.push('DELETE /decks/2');
        return new HttpResponse(null, { status: 204 });
      }),
      http.post('/api/v1/folders/1/decks/2', () => {
        calls.push('POST /folders/1/decks/2');
        return HttpResponse.json({
          id: 2,
          folderId: 1,
          title: 'Vocabulary',
          description: null,
          visibility: 'PRIVATE',
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.delete('/api/v1/folders/1/decks/2', () => {
        calls.push('DELETE /folders/1/decks/2');
        return HttpResponse.json({
          id: 2,
          folderId: null,
          title: 'Vocabulary',
          description: null,
          visibility: 'PRIVATE',
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.get('/api/v1/decks/2/summary', () => {
        calls.push('GET /decks/2/summary');
        return HttpResponse.json({
          deckId: 2,
          totalCards: 0,
          starredCards: 0,
          dueSrsCards: 0,
          newCards: 0,
          learningCards: 0,
          reviewCards: 0,
          availableModes: [],
        });
      }),
      http.get('/api/v1/decks/2/viewer-cards', () => {
        calls.push('GET /decks/2/viewer-cards');
        return HttpResponse.json([]);
      }),
    );

    await listFolders();
    await createFolder({ name: 'English', description: null });
    await getFolder(1);
    await updateFolder(1, { name: 'Languages', description: 'Updated' });
    await deleteFolder(1);
    await listDecks();
    await createDeck({ folderId: 1, title: 'Vocabulary', description: null });
    await getDeck(2);
    await updateDeck(2, { title: 'Advanced Vocabulary', description: 'Updated' });
    await deleteDeck(2);
    await addDeckToFolder(1, 2);
    await removeDeckFromFolder(1, 2);
    await getDeckSummary(2);
    await listViewerCards(2);

    expect(calls).toEqual([
      'GET /folders',
      'POST /folders {"name":"English","description":null}',
      'GET /folders/1',
      'PATCH /folders/1 {"name":"Languages","description":"Updated"}',
      'DELETE /folders/1',
      'GET /decks',
      'POST /decks {"folderId":1,"title":"Vocabulary","description":null}',
      'GET /decks/2',
      'PATCH /decks/2 {"title":"Advanced Vocabulary","description":"Updated"}',
      'DELETE /decks/2',
      'POST /folders/1/decks/2',
      'DELETE /folders/1/decks/2',
      'GET /decks/2/summary',
      'GET /decks/2/viewer-cards',
    ]);
  });

  it('calls flashcard endpoints under /api/v1', async () => {
    const calls: string[] = [];

    server.use(
      http.get('/api/v1/decks/2/flashcards', () => {
        calls.push('GET /decks/2/flashcards');
        return HttpResponse.json([]);
      }),
      http.post('/api/v1/decks/2/flashcards', async ({ request }) => {
        calls.push(`POST /decks/2/flashcards ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 7,
          deckId: 2,
          term: 'abate',
          definition: 'lessen',
          termImageUrl: null,
          definitionImageUrl: null,
          starred: false,
          position: 0,
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.patch('/api/v1/flashcards/7/star', async ({ request }) => {
        calls.push(`PATCH /flashcards/7/star ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 7,
          deckId: 2,
          term: 'abate',
          definition: 'lessen',
          termImageUrl: null,
          definitionImageUrl: null,
          starred: true,
          position: 0,
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.patch('/api/v1/flashcards/7', async ({ request }) => {
        calls.push(`PATCH /flashcards/7 ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 7,
          deckId: 2,
          term: 'abate',
          definition: 'become less intense',
          termImageUrl: null,
          definitionImageUrl: null,
          starred: true,
          position: 0,
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.delete('/api/v1/flashcards/7', () => {
        calls.push('DELETE /flashcards/7');
        return new HttpResponse(null, { status: 204 });
      }),
    );

    await listFlashcards(2);
    await createFlashcard(2, { term: 'abate', definition: 'lessen', termImageUrl: null, definitionImageUrl: null });
    await setFlashcardStarred(7, true);
    await updateFlashcard(7, {
      term: 'abate',
      definition: 'become less intense',
      termImageUrl: null,
      definitionImageUrl: null,
    });
    await deleteFlashcard(7);

    expect(calls).toEqual([
      'GET /decks/2/flashcards',
      'POST /decks/2/flashcards {"term":"abate","definition":"lessen","termImageUrl":null,"definitionImageUrl":null}',
      'PATCH /flashcards/7/star {"starred":true}',
      'PATCH /flashcards/7 {"term":"abate","definition":"become less intense","termImageUrl":null,"definitionImageUrl":null}',
      'DELETE /flashcards/7',
    ]);
  });

  it('calls sorting session endpoints under /api/v1', async () => {
    const calls: string[] = [];

    server.use(
      http.post('/api/v1/decks/2/sorting-sessions', async ({ request }) => {
        calls.push(`POST /decks/2/sorting-sessions ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 11,
          status: 'ACTIVE',
          knownCount: 0,
          doNotKnowCount: 0,
          items: [],
        });
      }),
      http.post('/api/v1/sorting-sessions/11/answers', async ({ request }) => {
        calls.push(`POST /sorting-sessions/11/answers ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 11,
          status: 'ACTIVE',
          knownCount: 1,
          doNotKnowCount: 0,
          items: [],
        });
      }),
    );

    await createSortingSession(2, { starredOnly: true, shuffle: false });
    await answerSortingItem(11, { itemId: 99, answer: 'KNOW' });

    expect(calls).toEqual([
      'POST /decks/2/sorting-sessions {"starredOnly":true,"shuffle":false}',
      'POST /sorting-sessions/11/answers {"itemId":99,"answer":"KNOW"}',
    ]);
  });
});
