import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { MatchingOptionsPage } from './MatchingOptionsPage';
import { MatchingSessionPage } from './MatchingSessionPage';

function renderMatching(initialEntry = '/decks/2/matching/options') {
  return renderWithProviders(
    <Routes>
      <Route path="/decks/:deckId/matching/options" element={<MatchingOptionsPage />} />
      <Route path="/matching-sessions/:sessionId" element={<MatchingSessionPage />} />
    </Routes>,
    { initialEntries: [initialEntry] },
  );
}

describe('matching mode', () => {
  it('starts a matching session, matches a pair, and completes', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];
    const activeSession = {
      id: 51,
      status: 'ACTIVE',
      cardCount: 1,
      matchedCount: 0,
      durationMs: 0,
      items: [{ id: 61, flashcardId: 7, term: 'abate', definition: 'lessen', matched: false }],
    };

    server.use(
      http.post('/api/v1/decks/2/matching-sessions', async ({ request }) => {
        calls.push(`create ${JSON.stringify(await request.json())}`);
        return HttpResponse.json(activeSession);
      }),
      http.get('/api/v1/matching-sessions/51', () => HttpResponse.json(activeSession)),
      http.post('/api/v1/matching-sessions/51/matches', async ({ request }) => {
        calls.push(`match ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          ...activeSession,
          matchedCount: 1,
          items: [{ ...activeSession.items[0], matched: true }],
        });
      }),
      http.post('/api/v1/matching-sessions/51/complete', () => {
        calls.push('complete');
        return HttpResponse.json({
          ...activeSession,
          status: 'COMPLETED',
          matchedCount: 1,
          durationMs: 1200,
          items: [{ ...activeSession.items[0], matched: true }],
        });
      }),
    );

    renderMatching();

    await user.clear(screen.getByLabelText('Card count'));
    await user.type(screen.getByLabelText('Card count'), '1');
    await user.click(screen.getByRole('button', { name: 'Start matching' }));

    expect(await screen.findByText('abate')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Term: abate' }));
    await user.click(screen.getByRole('button', { name: 'Definition: lessen' }));
    await user.click(await screen.findByRole('button', { name: 'Complete session' }));

    expect(await screen.findByText('Matching complete')).toBeInTheDocument();

    await waitFor(() =>
      expect(calls).toEqual([
        'create {"cardCount":1,"starredOnly":false}',
        'match {"itemId":61}',
        'complete',
      ]),
    );
  });

  it('shows a mismatch without posting an answer', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];

    server.use(
      http.get('/api/v1/matching-sessions/51', () =>
        HttpResponse.json({
          id: 51,
          status: 'ACTIVE',
          cardCount: 2,
          matchedCount: 0,
          durationMs: 0,
          items: [
            { id: 61, flashcardId: 7, term: 'abate', definition: 'lessen', matched: false },
            { id: 62, flashcardId: 8, term: 'brisk', definition: 'quick', matched: false },
          ],
        }),
      ),
      http.post('/api/v1/matching-sessions/51/matches', () => {
        calls.push('unexpected match');
        return HttpResponse.json({});
      }),
    );

    renderMatching('/matching-sessions/51');

    expect(await screen.findByText('abate')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Term: abate' }));
    await user.click(screen.getByRole('button', { name: 'Definition: quick' }));

    expect(await screen.findByText('Not a match')).toBeInTheDocument();
    expect(calls).toEqual([]);
  });

  it('marks the selected matching tile for assistive tech', async () => {
    const user = userEvent.setup();

    server.use(
      http.get('/api/v1/matching-sessions/51', () =>
        HttpResponse.json({
          id: 51,
          status: 'ACTIVE',
          cardCount: 1,
          matchedCount: 0,
          durationMs: 0,
          items: [{ id: 61, flashcardId: 7, term: 'abate', definition: 'lessen', matched: false }],
        }),
      ),
    );

    renderMatching('/matching-sessions/51');

    const term = await screen.findByRole('button', { name: 'Term: abate' });
    expect(term).toHaveAttribute('aria-pressed', 'false');

    await user.click(term);

    expect(term).toHaveAttribute('aria-pressed', 'true');
  });

  it('shows an error alert when starting a matching session fails', async () => {
    const user = userEvent.setup();

    server.use(
      http.post('/api/v1/decks/2/matching-sessions', () =>
        HttpResponse.json({ message: 'Could not create matching session' }, { status: 500 }),
      ),
    );

    renderMatching();

    await user.click(screen.getByRole('button', { name: 'Start matching' }));

    expect(await screen.findByText('Could not start matching')).toBeInTheDocument();
    expect(screen.getByText('Check that the deck has cards, then try again.')).toBeInTheDocument();
  });
});
