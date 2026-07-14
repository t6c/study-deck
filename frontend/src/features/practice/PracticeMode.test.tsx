import { fireEvent, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { PracticeOptionsPage } from './PracticeOptionsPage';
import { PracticeSessionPage } from './PracticeSessionPage';

function renderPractice(initialEntry = '/decks/2/practice/options') {
  return renderWithProviders(
    <Routes>
      <Route path="/decks/:deckId/practice/options" element={<PracticeOptionsPage />} />
      <Route path="/practice-tests/:practiceTestId" element={<PracticeSessionPage />} />
    </Routes>,
    { initialEntries: [initialEntry] },
  );
}

describe('practice test mode', () => {
  it('ignores duplicate save clicks while an answer request is in flight', async () => {
    const user = userEvent.setup();
    let releaseAnswer!: () => void;
    const answerBlocked = new Promise<void>((resolve) => {
      releaseAnswer = resolve;
    });
    const calls: string[] = [];

    const activeTest = {
      id: 31,
      status: 'ACTIVE',
      questionCount: 2,
      answeredCount: 0,
      scorePercent: 0,
      questions: [
        {
          id: 41,
          flashcardId: 7,
          questionType: 'WRITTEN',
          promptSide: 'DEFINITION',
          prompt: 'lessen',
          submittedAnswer: null,
          correct: null,
        },
        {
          id: 42,
          flashcardId: 8,
          questionType: 'WRITTEN',
          promptSide: 'TERM',
          prompt: 'abate',
          submittedAnswer: null,
          correct: null,
        },
      ],
    };

    server.use(
      http.get('/api/v1/practice-tests/31', () => HttpResponse.json(activeTest)),
      http.post('/api/v1/practice-tests/31/answers', async ({ request }) => {
        calls.push(`answer ${JSON.stringify(await request.json())}`);
        await answerBlocked;
        return HttpResponse.json({
          ...activeTest,
          answeredCount: 1,
          questions: [{ ...activeTest.questions[0], submittedAnswer: 'abate' }, activeTest.questions[1]],
        });
      }),
    );

    renderPractice('/practice-tests/31');

    expect(await screen.findByText('lessen')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Answer for question 1'), 'abate');

    const saveButton = screen.getByRole('button', { name: 'Save answer' });
    fireEvent.click(saveButton);
    fireEvent.click(saveButton);

    await waitFor(() => expect(calls).toHaveLength(1));
    await waitFor(() => expect(saveButton).toBeDisabled());
    expect(screen.getByRole('button', { name: 'Submit test' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '2' })).toBeDisabled();

    releaseAnswer();

    expect(await screen.findByText('abate')).toBeInTheDocument();
    await waitFor(() => expect(calls).toEqual(['answer {"questionId":41,"answer":"abate"}']));
  });

  it('starts a practice test, answers a question, and submits results', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];

    const activeTest = {
      id: 31,
      status: 'ACTIVE',
      questionCount: 1,
      answeredCount: 0,
      scorePercent: 0,
      questions: [
        {
          id: 41,
          flashcardId: 7,
          questionType: 'WRITTEN',
          promptSide: 'DEFINITION',
          prompt: 'lessen',
          submittedAnswer: null,
          correct: null,
        },
      ],
    };

    server.use(
      http.post('/api/v1/decks/2/practice-tests', async ({ request }) => {
        calls.push(`create ${JSON.stringify(await request.json())}`);
        return HttpResponse.json(activeTest);
      }),
      http.get('/api/v1/practice-tests/31', () => HttpResponse.json(activeTest)),
      http.post('/api/v1/practice-tests/31/answers', async ({ request }) => {
        calls.push(`answer ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          ...activeTest,
          answeredCount: 1,
          questions: [{ ...activeTest.questions[0], submittedAnswer: 'abate' }],
        });
      }),
      http.post('/api/v1/practice-tests/31/submit', () => {
        calls.push('submit');
        return HttpResponse.json({
          ...activeTest,
          status: 'SUBMITTED',
          answeredCount: 1,
          scorePercent: 100,
          questions: [{ ...activeTest.questions[0], submittedAnswer: 'abate', correct: true }],
        });
      }),
    );

    renderPractice();

    await user.clear(screen.getByLabelText('Question count'));
    await user.type(screen.getByLabelText('Question count'), '1');
    await user.click(screen.getByRole('button', { name: 'Start test' }));

    expect(await screen.findByText('lessen')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Answer for question 1'), 'abate');
    await user.click(screen.getByRole('button', { name: 'Save answer' }));
    await user.click(await screen.findByRole('button', { name: 'Submit test' }));

    expect(await screen.findByText('Score: 100%')).toBeInTheDocument();
    expect(screen.getByText('Correct')).toBeInTheDocument();

    await waitFor(() =>
      expect(calls).toEqual([
        'create {"questionCount":1,"multipleChoice":true,"written":true,"trueFalse":false,"starredOnly":false,"answerWithTerm":true,"answerWithDefinition":true}',
        'answer {"questionId":41,"answer":"abate"}',
        'submit',
      ]),
    );
  });

  it('saves the current typed answer before direct submit', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];

    const activeTest = {
      id: 31,
      status: 'ACTIVE',
      questionCount: 1,
      answeredCount: 0,
      scorePercent: 0,
      questions: [
        {
          id: 41,
          flashcardId: 7,
          questionType: 'WRITTEN',
          promptSide: 'DEFINITION',
          prompt: 'lessen',
          submittedAnswer: null,
          correct: null,
        },
      ],
    };

    server.use(
      http.get('/api/v1/practice-tests/31', () => HttpResponse.json(activeTest)),
      http.post('/api/v1/practice-tests/31/answers', async ({ request }) => {
        calls.push(`answer ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          ...activeTest,
          answeredCount: 1,
          questions: [{ ...activeTest.questions[0], submittedAnswer: 'abate' }],
        });
      }),
      http.post('/api/v1/practice-tests/31/submit', () => {
        calls.push('submit');
        return HttpResponse.json({
          ...activeTest,
          status: 'SUBMITTED',
          answeredCount: 1,
          scorePercent: 100,
          questions: [{ ...activeTest.questions[0], submittedAnswer: 'abate', correct: true }],
        });
      }),
    );

    renderPractice('/practice-tests/31');

    expect(await screen.findByText('lessen')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Answer for question 1'), 'abate');
    await user.click(screen.getByRole('button', { name: 'Submit test' }));

    expect(await screen.findByText('Score: 100%')).toBeInTheDocument();

    await waitFor(() => expect(calls).toEqual(['answer {"questionId":41,"answer":"abate"}', 'submit']));
  });

  it('shows an empty practice test state', async () => {
    server.use(
      http.get('/api/v1/practice-tests/404', () =>
        HttpResponse.json({ id: 404, status: 'ACTIVE', questionCount: 0, answeredCount: 0, scorePercent: 0, questions: [] }),
      ),
    );

    renderPractice('/practice-tests/404');

    expect(await screen.findByText('No practice questions')).toBeInTheDocument();
  });

  it('shows an error alert when starting a practice test fails', async () => {
    const user = userEvent.setup();

    server.use(
      http.post('/api/v1/decks/2/practice-tests', () =>
        HttpResponse.json({ message: 'Could not create practice test' }, { status: 500 }),
      ),
    );

    renderPractice();

    await user.click(screen.getByRole('button', { name: 'Start test' }));

    expect(await screen.findByText('Could not start test')).toBeInTheDocument();
    expect(screen.getByText('Check that the deck has cards, then try again.')).toBeInTheDocument();
  });
});
