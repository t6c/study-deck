import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { LearnOptionsPage } from './LearnOptionsPage';
import { LearnSessionPage } from './LearnSessionPage';

function renderLearn(initialEntry = '/decks/2/learn/options') {
  return renderWithProviders(
    <Routes>
      <Route path="/decks/:deckId/learn/options" element={<LearnOptionsPage />} />
      <Route path="/learn-sessions/:sessionId" element={<LearnSessionPage />} />
    </Routes>,
    { initialEntries: [initialEntry] },
  );
}

describe('learn mode', () => {
  it('starts a learn session, answers an item, and completes the session', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];

    server.use(
      http.post('/api/v1/decks/2/learn-sessions', async ({ request }) => {
        calls.push(`create ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 11,
          status: 'ACTIVE',
          totalItems: 1,
          correctCount: 0,
          wrongCount: 0,
          items: [
            {
              id: 21,
              flashcardId: 7,
              questionType: 'WRITTEN',
              promptSide: 'TERM',
              prompt: 'abate',
              answer: 'lessen',
              attempts: 0,
            },
          ],
        });
      }),
      http.get('/api/v1/learn-sessions/11', () =>
        HttpResponse.json({
          id: 11,
          status: 'ACTIVE',
          totalItems: 1,
          correctCount: 0,
          wrongCount: 0,
          items: [
            {
              id: 21,
              flashcardId: 7,
              questionType: 'WRITTEN',
              promptSide: 'TERM',
              prompt: 'abate',
              answer: 'lessen',
              attempts: 0,
            },
          ],
        }),
      ),
      http.post('/api/v1/learn-sessions/11/answers', async ({ request }) => {
        calls.push(`answer ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 11,
          status: 'ACTIVE',
          totalItems: 1,
          correctCount: 1,
          wrongCount: 0,
          items: [],
        });
      }),
      http.post('/api/v1/learn-sessions/11/complete', () => {
        calls.push('complete');
        return HttpResponse.json({
          id: 11,
          status: 'COMPLETED',
          totalItems: 1,
          correctCount: 1,
          wrongCount: 0,
          items: [],
        });
      }),
    );

    renderLearn();

    await user.click(screen.getByRole('checkbox', { name: 'Starred only' }));
    await user.clear(screen.getByLabelText('Round length'));
    await user.type(screen.getByLabelText('Round length'), '1');
    await user.click(screen.getByRole('button', { name: 'Start learning' }));

    expect(await screen.findByText('abate')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Your answer'), 'lessen');
    await user.click(screen.getByRole('button', { name: 'Check answer' }));

    expect(await screen.findByText('Correct')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Finish session' }));
    expect(await screen.findByText('Learn complete')).toBeInTheDocument();

    await waitFor(() =>
      expect(calls).toEqual([
        'create {"lengthOfRounds":1,"flashcards":true,"multipleChoice":true,"written":true,"trueFalse":false,"starredOnly":true,"shuffleTerms":true}',
        'answer {"itemId":21,"answer":"lessen"}',
        'complete',
      ]),
    );
  });

  it('shows an empty learn session state', async () => {
    server.use(
      http.get('/api/v1/learn-sessions/404', () =>
        HttpResponse.json({ id: 404, status: 'ACTIVE', totalItems: 0, correctCount: 0, wrongCount: 0, items: [] }),
      ),
    );

    renderLearn('/learn-sessions/404');

    expect(await screen.findByText('No learn items')).toBeInTheDocument();
  });

  it('shows an error alert when starting a learn session fails', async () => {
    const user = userEvent.setup();

    server.use(
      http.post('/api/v1/decks/2/learn-sessions', () =>
        HttpResponse.json({ message: 'Could not create learn session' }, { status: 500 }),
      ),
    );

    renderLearn();

    await user.click(screen.getByRole('button', { name: 'Start learning' }));

    expect(await screen.findByText('Could not start learning')).toBeInTheDocument();
    expect(screen.getByText('Check that the deck has cards, then try again.')).toBeInTheDocument();
  });
});
