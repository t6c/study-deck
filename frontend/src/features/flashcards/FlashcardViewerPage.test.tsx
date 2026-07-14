import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { FlashcardViewerPage } from './FlashcardViewerPage';

function renderPage() {
  return renderWithProviders(
    <Routes>
      <Route path="/decks/:deckId/flashcards/viewer" element={<FlashcardViewerPage />} />
    </Routes>,
    { initialEntries: ['/decks/2/flashcards/viewer'] },
  );
}

describe('FlashcardViewerPage', () => {
  it('navigates, flips, stars, shuffles, and links to sorting', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];
    server.use(
      http.get('/api/v1/decks/2/viewer-cards', ({ request }) => {
        calls.push(new URL(request.url).search);
        return HttpResponse.json([
          { id: 7, term: 'abate', definition: 'lessen', termImageUrl: null, definitionImageUrl: null, starred: false, position: 0 },
          { id: 8, term: 'brisk', definition: 'quick', termImageUrl: null, definitionImageUrl: null, starred: true, position: 1 },
        ]);
      }),
      http.patch('/api/v1/flashcards/7/star', async ({ request }) => {
        calls.push(`star ${JSON.stringify(await request.json())}`);
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
    );

    renderPage();

    expect(await screen.findByText('abate')).toBeInTheDocument();
    expect(screen.getByText('Card 1 of 2')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Reveal answer' }));
    expect(screen.getByText('lessen')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Next card' }));
    expect(screen.getByText('brisk')).toBeInTheDocument();
    expect(screen.getByText('Card 2 of 2')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Previous card' }));
    await user.click(screen.getByRole('button', { name: 'Star card' }));
    await user.click(screen.getByRole('button', { name: 'Shuffle cards' }));

    await waitFor(() => expect(calls).toContain('star {"starred":true}'));
    expect(screen.getByRole('link', { name: 'Card sorting' })).toHaveAttribute('href', '/decks/2/sorting');
  });

  it('clamps the current card when refreshed cards shrink', async () => {
    const user = userEvent.setup();
    let viewerRequestCount = 0;

    server.use(
      http.get('/api/v1/decks/2/viewer-cards', () => {
        viewerRequestCount += 1;

        if (viewerRequestCount === 1) {
          return HttpResponse.json([
            { id: 7, term: 'abate', definition: 'lessen', termImageUrl: null, definitionImageUrl: null, starred: false, position: 0 },
            { id: 8, term: 'brisk', definition: 'quick', termImageUrl: null, definitionImageUrl: null, starred: false, position: 1 },
          ]);
        }

        return HttpResponse.json([
          { id: 7, term: 'abate', definition: 'lessen', termImageUrl: null, definitionImageUrl: null, starred: false, position: 0 },
        ]);
      }),
      http.patch('/api/v1/flashcards/8/star', () =>
        HttpResponse.json({
          id: 8,
          deckId: 2,
          term: 'brisk',
          definition: 'quick',
          termImageUrl: null,
          definitionImageUrl: null,
          starred: true,
          position: 1,
          createdAt: '',
          updatedAt: '',
        }),
      ),
    );

    renderPage();

    expect(await screen.findByText('abate')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Next card' }));
    expect(screen.getByText('brisk')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Star card' }));

    expect(await screen.findByText('Card 1 of 1')).toBeInTheDocument();
    expect(screen.getByText('abate')).toBeInTheDocument();
  });
});
