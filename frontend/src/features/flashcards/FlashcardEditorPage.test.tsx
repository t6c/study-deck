import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { FlashcardEditorPage } from './FlashcardEditorPage';

function renderPage() {
  return renderWithProviders(
    <Routes>
      <Route path="/decks/:deckId/flashcards/edit" element={<FlashcardEditorPage />} />
    </Routes>,
    { initialEntries: ['/decks/2/flashcards/edit'] },
  );
}

describe('FlashcardEditorPage', () => {
  it('creates and edits flashcards', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];

    server.use(
      http.get('/api/v1/decks/2', () =>
        HttpResponse.json({ id: 2, folderId: null, title: 'English Vocabulary', description: null, visibility: 'PRIVATE', createdAt: '', updatedAt: '' }),
      ),
      http.get('/api/v1/decks/2/flashcards', () =>
        HttpResponse.json([
          {
            id: 7,
            deckId: 2,
            term: 'abate',
            definition: 'lessen',
            termImageUrl: 'https://example.com/term.png',
            definitionImageUrl: 'https://example.com/definition.png',
            starred: false,
            position: 0,
            createdAt: '',
            updatedAt: '',
          },
        ]),
      ),
      http.post('/api/v1/decks/2/flashcards', async ({ request }) => {
        calls.push(`create ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 8,
          deckId: 2,
          term: 'brisk',
          definition: 'quick',
          termImageUrl: null,
          definitionImageUrl: null,
          starred: false,
          position: 1,
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.patch('/api/v1/flashcards/7', async ({ request }) => {
        calls.push(`edit ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 7,
          deckId: 2,
          term: 'abate',
          definition: 'become less intense',
          termImageUrl: 'https://example.com/term.png',
          definitionImageUrl: 'https://example.com/definition.png',
          starred: false,
          position: 0,
          createdAt: '',
          updatedAt: '',
        });
      }),
    );

    renderPage();

    expect(await screen.findByText('abate')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'New card' }));
    await screen.findByRole('heading', { name: 'Create card' });
    await user.type(await screen.findByPlaceholderText('abate'), 'brisk');
    await user.type(screen.getByPlaceholderText('become less intense'), 'quick');
    await user.click(screen.getByRole('button', { name: 'Create card' }));

    const card = screen.getByText('abate').closest('.mantine-Card-root') as HTMLElement;
    await user.click(within(card).getByRole('button', { name: 'Edit card' }));
    await user.clear(screen.getByPlaceholderText('become less intense'));
    await user.type(screen.getByPlaceholderText('become less intense'), 'become less intense');
    expect(screen.getByRole('textbox', { name: 'Term image URL' })).toHaveValue('https://example.com/term.png');
    expect(screen.getByRole('textbox', { name: 'Definition image URL' })).toHaveValue('https://example.com/definition.png');
    await user.click(screen.getByRole('button', { name: 'Save card' }));

    await waitFor(() =>
      expect(calls).toEqual([
        'create {"term":"brisk","definition":"quick","termImageUrl":null,"definitionImageUrl":null}',
        'edit {"term":"abate","definition":"become less intense","termImageUrl":"https://example.com/term.png","definitionImageUrl":"https://example.com/definition.png"}',
      ]),
    );
  }, 10000);

  it('stars and deletes flashcards', async () => {
    const user = userEvent.setup();
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const calls: string[] = [];

    server.use(
      http.get('/api/v1/decks/2', () =>
        HttpResponse.json({ id: 2, folderId: null, title: 'English Vocabulary', description: null, visibility: 'PRIVATE', createdAt: '', updatedAt: '' }),
      ),
      http.get('/api/v1/decks/2/flashcards', () =>
        HttpResponse.json([
          {
            id: 7,
            deckId: 2,
            term: 'abate',
            definition: 'lessen',
            termImageUrl: 'https://example.com/term.png',
            definitionImageUrl: 'https://example.com/definition.png',
            starred: false,
            position: 0,
            createdAt: '',
            updatedAt: '',
          },
        ]),
      ),
      http.patch('/api/v1/flashcards/7/star', async ({ request }) => {
        calls.push(`star ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({
          id: 7,
          deckId: 2,
          term: 'abate',
          definition: 'lessen',
          termImageUrl: 'https://example.com/term.png',
          definitionImageUrl: 'https://example.com/definition.png',
          starred: true,
          position: 0,
          createdAt: '',
          updatedAt: '',
        });
      }),
      http.delete('/api/v1/flashcards/7', () => {
        calls.push('delete 7');
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderPage();

    const card = (await screen.findByText('abate')).closest('.mantine-Card-root') as HTMLElement;
    await user.click(within(card).getByRole('button', { name: 'Star card' }));
    await user.click(within(card).getByRole('button', { name: 'Delete card' }));

    await waitFor(() => expect(calls).toEqual(['star {"starred":true}', 'delete 7']));
    expect(confirm).toHaveBeenCalledWith('Delete flashcard "abate"?');
    confirm.mockRestore();
  });

  it('disables card actions while their mutations are pending', async () => {
    const user = userEvent.setup();
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(true);
    let resolveStar: () => void = () => {};
    let resolveDelete: () => void = () => {};
    const starPending = new Promise<void>((resolve) => {
      resolveStar = resolve;
    });
    const deletePending = new Promise<void>((resolve) => {
      resolveDelete = resolve;
    });

    server.use(
      http.get('/api/v1/decks/2', () =>
        HttpResponse.json({ id: 2, folderId: null, title: 'English Vocabulary', description: null, visibility: 'PRIVATE', createdAt: '', updatedAt: '' }),
      ),
      http.get('/api/v1/decks/2/flashcards', () =>
        HttpResponse.json([
          {
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
          },
        ]),
      ),
      http.patch('/api/v1/flashcards/7/star', async () => {
        await starPending;
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
      http.delete('/api/v1/flashcards/7', async () => {
        await deletePending;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderPage();

    const card = (await screen.findByText('abate')).closest('.mantine-Card-root') as HTMLElement;

    await user.click(within(card).getByRole('button', { name: 'Star card' }));
    expect(within(card).getByRole('button', { name: 'Star card' })).toBeDisabled();
    resolveStar();
    await waitFor(() => expect(within(card).getByRole('button', { name: 'Star card' })).not.toBeDisabled());

    await user.click(within(card).getByRole('button', { name: 'Delete card' }));
    expect(within(card).getByRole('button', { name: 'Delete card' })).toBeDisabled();
    resolveDelete();

    await waitFor(() => expect(confirm).toHaveBeenCalledWith('Delete flashcard "abate"?'));
    confirm.mockRestore();
  });
});
