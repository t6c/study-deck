import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { HomePage } from './HomePage';

describe('HomePage', () => {
  it('renders dashboard counts and recent decks', async () => {
    server.use(
      http.get('/api/v1/folders', () =>
        HttpResponse.json([{ id: 1, name: 'English', description: null, position: 0, createdAt: '', updatedAt: '' }]),
      ),
      http.get('/api/v1/decks', () =>
        HttpResponse.json([
          {
            id: 2,
            folderId: 1,
            title: 'English Vocabulary',
            description: 'Intermediate words',
            visibility: 'PRIVATE',
            createdAt: '2026-07-01T00:00:00Z',
            updatedAt: '2026-07-14T00:00:00Z',
          },
        ]),
      ),
    );

    renderWithProviders(<HomePage />);

    expect(await screen.findByText('English Vocabulary')).toBeInTheDocument();
    expect(screen.getByText('Decks')).toBeInTheDocument();
    expect(screen.getByText('Folders')).toBeInTheDocument();
  });

  it('shows loading while dashboard data is pending', () => {
    server.use(
      http.get('/api/v1/folders', () => new Promise(() => undefined)),
      http.get('/api/v1/decks', () => new Promise(() => undefined)),
    );

    renderWithProviders(<HomePage />);

    expect(screen.getAllByLabelText('Loading dashboard').length).toBeGreaterThan(0);
    expect(screen.getByLabelText('Loading recent decks')).toBeInTheDocument();
  });

  it('shows an error state when dashboard data fails', async () => {
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([], { status: 500 })),
      http.get('/api/v1/decks', () => HttpResponse.json([], { status: 500 })),
    );

    renderWithProviders(<HomePage />);

    expect(await screen.findByText('Could not load dashboard')).toBeInTheDocument();
  });

  it('shows an empty state when there are no recent decks', async () => {
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([])),
      http.get('/api/v1/decks', () => HttpResponse.json([])),
    );

    renderWithProviders(<HomePage />);

    expect(await screen.findByText('No decks yet')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Open library' })).toHaveAttribute('href', '/library');
  });
});
