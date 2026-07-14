import { fireEvent, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { LibraryPage } from './LibraryPage';

describe('LibraryPage', () => {
  beforeEach(() => {
    document.body.removeAttribute('data-scroll-locked');
    document.querySelectorAll('[data-mantine-shared-portal-node]').forEach((element) => element.remove());
  });

  function clickComboboxOption(label: string) {
    const option = screen
      .getAllByText(label)
      .map((element) => element.closest('[data-combobox-option]'))
      .filter((element): element is HTMLElement => Boolean(element))
      .at(-1);

    if (!option) {
      throw new Error(`Could not find combobox option: ${label}`);
    }

    fireEvent.click(option);
  }

  const folders = [
    { id: 1, name: 'English', description: 'Vocabulary decks', position: 0, createdAt: '', updatedAt: '' },
    { id: 2, name: 'Science', description: 'Lab notes', position: 1, createdAt: '', updatedAt: '' },
  ];
  const decks = [
    {
      id: 2,
      folderId: 1,
      title: 'English Vocabulary',
      description: 'Intermediate words',
      visibility: 'PRIVATE',
      createdAt: '',
      updatedAt: '',
    },
    {
      id: 3,
      folderId: 2,
      title: 'Biology',
      description: 'Cell terms',
      visibility: 'PRIVATE',
      createdAt: '',
      updatedAt: '',
    },
  ];

  it('renders folders and decks from the backend', async () => {
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([folders[0]])),
      http.get('/api/v1/decks', () => HttpResponse.json([decks[0]])),
    );

    renderWithProviders(<LibraryPage />);

    expect(await screen.findAllByText('English')).toHaveLength(2);
    expect(await screen.findByText('English Vocabulary')).toBeInTheDocument();
    expect(screen.getByText('Vocabulary decks')).toBeInTheDocument();
  });

  it('filters folders and decks by search text and folder', async () => {
    const user = userEvent.setup();
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json(folders)),
      http.get('/api/v1/decks', () => HttpResponse.json(decks)),
    );

    renderWithProviders(<LibraryPage />);

    expect(await screen.findByText('English Vocabulary')).toBeInTheDocument();
    expect(screen.getByText('Biology')).toBeInTheDocument();

    await user.type(screen.getByLabelText('Search'), 'bio');

    expect(screen.queryByText('English Vocabulary')).not.toBeInTheDocument();
    expect(screen.getByText('Biology')).toBeInTheDocument();

    await user.clear(screen.getByLabelText('Search'));
    await user.click(screen.getByRole('combobox', { name: 'Folder' }));
    await waitFor(() => expect(screen.getAllByText('Science').length).toBeGreaterThan(1));
    clickComboboxOption('Science');

    expect(screen.queryByText('English Vocabulary')).not.toBeInTheDocument();
    expect(screen.getByText('Biology')).toBeInTheDocument();
  });

  it('opens and submits the create folder modal', async () => {
    const user = userEvent.setup();
    const calls: unknown[] = [];
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([])),
      http.get('/api/v1/decks', () => HttpResponse.json([])),
      http.post('/api/v1/folders', async ({ request }) => {
        calls.push(await request.json());
        return HttpResponse.json({ id: 4, name: 'History', description: 'Dates', position: 0, createdAt: '', updatedAt: '' });
      }),
    );

    renderWithProviders(<LibraryPage />);

    await user.click(screen.getByRole('button', { name: 'New folder' }));
    await screen.findByRole('heading', { name: 'Create folder' });

    await user.type(screen.getByPlaceholderText('English'), 'History');
    await user.type(screen.getByPlaceholderText('Vocabulary decks'), 'Dates');
    await user.click(screen.getByRole('button', { name: 'Create folder' }));

    await waitFor(() => expect(calls).toEqual([{ name: 'History', description: 'Dates' }]));
  });

  it('clears create folder validation errors when reopened', async () => {
    const user = userEvent.setup();
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([])),
      http.get('/api/v1/decks', () => HttpResponse.json([])),
    );

    renderWithProviders(<LibraryPage />);

    await user.click(screen.getByRole('button', { name: 'New folder' }));
    await screen.findByRole('heading', { name: 'Create folder' });
    await user.click(screen.getByRole('button', { name: 'Create folder' }));

    expect(await screen.findByText('Folder name is required')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    await user.click(screen.getByRole('button', { name: 'New folder' }));
    await screen.findByRole('heading', { name: 'Create folder' });

    expect(screen.queryByText('Folder name is required')).not.toBeInTheDocument();
  });

  it('opens and submits the create deck modal', async () => {
    const user = userEvent.setup();
    const calls: unknown[] = [];
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([folders[0]])),
      http.get('/api/v1/decks', () => HttpResponse.json([decks[0]])),
      http.post('/api/v1/decks', async ({ request }) => {
        calls.push(await request.json());
        return HttpResponse.json({ ...decks[0], id: 8, title: 'Grammar' });
      }),
    );

    renderWithProviders(<LibraryPage />);

    await user.click(screen.getByRole('button', { name: 'New deck' }));
    await screen.findByRole('heading', { name: 'Create deck' });

    await user.click(screen.getAllByRole('combobox', { name: 'Folder' }).at(-1)!);
    await waitFor(() => expect(screen.getAllByText('English').length).toBeGreaterThan(1));
    clickComboboxOption('English');
    await user.type(screen.getByPlaceholderText('English Vocabulary'), 'Grammar');
    await user.type(screen.getByPlaceholderText('Intermediate words'), 'Rules');
    await user.click(screen.getByRole('button', { name: 'Create deck' }));

    await waitFor(() => expect(calls).toEqual([{ folderId: 1, title: 'Grammar', description: 'Rules' }]));
  });

  it('clears create deck validation errors when reopened', async () => {
    const user = userEvent.setup();
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([folders[0]])),
      http.get('/api/v1/decks', () => HttpResponse.json([])),
    );

    renderWithProviders(<LibraryPage />);

    await user.click(screen.getByRole('button', { name: 'New deck' }));
    await screen.findByRole('heading', { name: 'Create deck' });
    await user.click(screen.getByRole('button', { name: 'Create deck' }));

    expect(await screen.findByText('Deck title is required')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    await user.click(screen.getByRole('button', { name: 'New deck' }));
    await screen.findByRole('heading', { name: 'Create deck' });

    expect(screen.queryByText('Deck title is required')).not.toBeInTheDocument();
  });

  it('edits items through the actions menu', async () => {
    const user = userEvent.setup();
    const calls: string[] = [];
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([folders[0]])),
      http.get('/api/v1/decks', () => HttpResponse.json([decks[0]])),
      http.patch('/api/v1/folders/1', async ({ request }) => {
        calls.push(`PATCH folder ${JSON.stringify(await request.json())}`);
        return HttpResponse.json({ ...folders[0], name: 'Languages' });
      }),
    );

    renderWithProviders(<LibraryPage />);

    await screen.findByText('English Vocabulary');
    const folderCard = screen.getByText('Vocabulary decks').closest('.mantine-Card-root') as HTMLElement;
    await user.click(within(folderCard).getByRole('button', { name: 'Open actions menu' }));
    await user.click(await screen.findByRole('menuitem', { name: 'Edit' }));
    await screen.findByRole('heading', { name: 'Edit folder' });

    await user.clear(screen.getByPlaceholderText('English'));
    await user.type(screen.getByPlaceholderText('English'), 'Languages');
    await user.click(screen.getByRole('button', { name: 'Save folder' }));

    await waitFor(() => expect(calls).toEqual(['PATCH folder {"name":"Languages","description":"Vocabulary decks"}']));
  });

  it('deletes items through the actions menu', async () => {
    const user = userEvent.setup();
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const calls: string[] = [];
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([folders[0]])),
      http.get('/api/v1/decks', () => HttpResponse.json([decks[0]])),
      http.delete('/api/v1/decks/2', () => {
        calls.push('DELETE deck 2');
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderWithProviders(<LibraryPage />);

    await screen.findByText('English Vocabulary');
    const deckCard = screen.getByText('Intermediate words').closest('.mantine-Card-root') as HTMLElement;
    await user.click(within(deckCard).getByRole('button', { name: 'Open actions menu' }));
    await user.click(await screen.findByRole('menuitem', { name: 'Delete' }));

    await waitFor(() => expect(calls).toEqual(['DELETE deck 2']));
    expect(confirm).toHaveBeenCalledWith('Delete deck "English Vocabulary"?');
    confirm.mockRestore();
  });

  it('shows loading, error, and empty states', async () => {
    server.use(
      http.get('/api/v1/folders', () => new Promise(() => undefined)),
      http.get('/api/v1/decks', () => new Promise(() => undefined)),
    );

    const { unmount } = renderWithProviders(<LibraryPage />);
    expect(screen.getByLabelText('Loading library')).toBeInTheDocument();
    unmount();

    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([], { status: 500 })),
      http.get('/api/v1/decks', () => HttpResponse.json([], { status: 500 })),
    );

    const errorView = renderWithProviders(<LibraryPage />);
    expect(await screen.findByText('Could not load library')).toBeInTheDocument();
    errorView.unmount();

    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([])),
      http.get('/api/v1/decks', () => HttpResponse.json([])),
    );

    renderWithProviders(<LibraryPage />);
    expect(await screen.findByText('No library items found')).toBeInTheDocument();
  });
});
