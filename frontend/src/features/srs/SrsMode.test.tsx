import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { SrsDashboardPage } from './SrsDashboardPage';
import { SrsIntroPage } from './SrsIntroPage';
import { SrsReviewPage } from './SrsReviewPage';

function renderSrs(initialEntry = '/decks/2/srs/intro') {
  return renderWithProviders(
    <Routes>
      <Route path="/decks/:deckId/srs/intro" element={<SrsIntroPage />} />
      <Route path="/decks/:deckId/srs" element={<SrsDashboardPage />} />
      <Route path="/decks/:deckId/srs/review" element={<SrsReviewPage />} />
    </Routes>,
    { initialEntries: [initialEntry] },
  );
}

describe('spaced repetition mode', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('acknowledges intro, shows dashboard stats, and reviews a due card', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];
    vi.spyOn(Date, 'now').mockReturnValue(1000);

    server.use(
      http.get('/api/v1/decks/2/srs/stats', () => {
        calls.push('stats');
        return HttpResponse.json({ newCards: 3, learningCards: 2, reviewCards: 1, dueCards: 1 });
      }),
      http.get('/api/v1/decks/2/srs/due', () => {
        calls.push('due');
        return HttpResponse.json([{ flashcardId: 7, term: 'abate', definition: 'lessen', dueAt: '2026-07-14T00:00:00Z', state: 'NEW' }]);
      }),
      http.post('/api/v1/flashcards/7/srs/reviews', async ({ request }) => {
        calls.push(`review ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ flashcardId: 7, rating: 'GOOD', state: 'REVIEW', dueAt: '2026-07-15T00:00:00Z', reps: 1, lapses: 0 });
      }),
    );

    renderSrs();

    await user.click(screen.getByRole('button', { name: 'Continue to SRS' }));
    expect(localStorage.getItem('studyDeck.srsIntroAcknowledged.2')).toBe('true');
    expect(await screen.findByText('Due now')).toBeInTheDocument();
    expect(screen.getAllByText('1').length).toBeGreaterThan(0);

    await user.click(screen.getByRole('link', { name: 'Review due cards' }));
    expect(await screen.findByText('abate')).toBeInTheDocument();
    vi.mocked(Date.now).mockReturnValue(2400);
    await user.click(screen.getByRole('button', { name: 'Show answer' }));
    expect(screen.getByText('lessen')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Good' }));

    expect(await screen.findByText('Review complete')).toBeInTheDocument();
    await waitFor(() => expect(calls).toEqual(['stats', 'due', 'review {"rating":"GOOD","durationMs":1400}']));
  });

  it('shows an empty due-card state', async () => {
    server.use(
      http.get('/api/v1/decks/2/srs/due', () => HttpResponse.json([])),
    );

    renderSrs('/decks/2/srs/review');

    expect(await screen.findByText('No cards due')).toBeInTheDocument();
  });

  it('shows an error alert when due cards fail to load', async () => {
    server.use(
      http.get('/api/v1/decks/2/srs/due', () =>
        HttpResponse.json({ message: 'SRS service unavailable' }, { status: 500 }),
      ),
    );

    renderSrs('/decks/2/srs/review');

    expect(await screen.findByText('Could not load due cards')).toBeInTheDocument();
    expect(screen.getByText('Refresh the page or try again later.')).toBeInTheDocument();
  });
});
