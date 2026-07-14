import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { CardSortingPanel } from './CardSortingPanel';

function renderPage() {
  return renderWithProviders(
    <Routes>
      <Route path="/decks/:deckId/sorting" element={<CardSortingPanel />} />
    </Routes>,
    { initialEntries: ['/decks/2/sorting'] },
  );
}

describe('CardSortingPanel', () => {
  it('creates a sorting session and submits know/do not know answers', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];
    server.use(
      http.post('/api/v1/decks/2/sorting-sessions', async ({ request }) => {
        calls.push(`create ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 11,
          status: 'ACTIVE',
          knownCount: 0,
          doNotKnowCount: 0,
          items: [
            {
              id: 21,
              flashcardId: 7,
              term: 'abate',
              definition: 'lessen',
              termImageUrl: null,
              definitionImageUrl: null,
              starred: false,
              position: 0,
              answer: null,
            },
            {
              id: 22,
              flashcardId: 8,
              term: 'brisk',
              definition: 'quick',
              termImageUrl: null,
              definitionImageUrl: null,
              starred: true,
              position: 1,
              answer: null,
            },
          ],
        });
      }),
      http.post('/api/v1/sorting-sessions/11/answers', async ({ request }) => {
        calls.push(`answer ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 11,
          status: 'ACTIVE',
          knownCount: 1,
          doNotKnowCount: 0,
          items: [],
        });
      }),
    );

    renderPage();

    expect(await screen.findByText('abate')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Know' }));
    expect(await screen.findByText('brisk')).toBeInTheDocument();
    expect(screen.getByText('Known: 1')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: "Don't know" }));

    await waitFor(() =>
      expect(calls).toEqual([
        'create {"starredOnly":false,"shuffle":false}',
        'answer {"itemId":21,"answer":"KNOW"}',
        'answer {"itemId":22,"answer":"DO_NOT_KNOW"}',
      ]),
    );
    expect(screen.getByText('Sorting complete')).toBeInTheDocument();
    expect(screen.getByText("Don't know: 1")).toBeInTheDocument();
  });

  it('locks restart, options, and answers while an answer is pending', async () => {
    const user = userEvent.setup();
    let resolveAnswer: () => void = () => {};
    const answerStarted = new Promise<void>((resolve) => {
      resolveAnswer = resolve;
    });

    server.use(
      http.post('/api/v1/decks/2/sorting-sessions', () =>
        HttpResponse.json({
          id: 11,
          status: 'ACTIVE',
          knownCount: 0,
          doNotKnowCount: 0,
          items: [
            {
              id: 21,
              flashcardId: 7,
              term: 'abate',
              definition: 'lessen',
              termImageUrl: null,
              definitionImageUrl: null,
              starred: false,
              position: 0,
              answer: null,
            },
          ],
        }),
      ),
      http.post('/api/v1/sorting-sessions/11/answers', async () => {
        await answerStarted;
        return HttpResponse.json({
          id: 11,
          status: 'COMPLETED',
          knownCount: 1,
          doNotKnowCount: 0,
          items: [],
        });
      }),
    );

    renderPage();

    expect(await screen.findByText('abate')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Know' }));

    expect(screen.getByRole('button', { name: 'Restart' })).toBeDisabled();
    expect(screen.getByRole('checkbox', { name: 'Starred only' })).toBeDisabled();
    expect(screen.getByRole('checkbox', { name: 'Shuffle' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Know' })).toBeDisabled();
    expect(screen.getByRole('button', { name: "Don't know" })).toBeDisabled();

    resolveAnswer();
    expect(await screen.findByText('Sorting complete')).toBeInTheDocument();
  });
});
