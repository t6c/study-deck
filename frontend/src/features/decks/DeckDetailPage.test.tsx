import { screen, within } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { DeckDetailPage } from './DeckDetailPage';

function renderPage() {
  return renderWithProviders(
    <Routes>
      <Route path="/decks/:deckId" element={<DeckDetailPage />} />
    </Routes>,
    { initialEntries: ['/decks/2'] },
  );
}

describe('DeckDetailPage', () => {
  it('renders deck data, stats, flashcard count, and mode links', async () => {
    server.use(
      http.get('/api/v1/decks/2', () =>
        HttpResponse.json({
          id: 2,
          folderId: 1,
          title: 'English Vocabulary',
          description: 'Intermediate words',
          visibility: 'PRIVATE',
          createdAt: '',
          updatedAt: '',
        }),
      ),
      http.get('/api/v1/decks/2/summary', () =>
        HttpResponse.json({
          deckId: 2,
          totalCards: 3,
          starredCards: 1,
          dueSrsCards: 2,
          newCards: 1,
          learningCards: 1,
          reviewCards: 1,
          availableModes: ['LEARN', 'FLASHCARDS'],
        }),
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
            starred: true,
            position: 0,
            createdAt: '',
            updatedAt: '',
          },
          {
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
          },
        ]),
      ),
    );

    renderPage();

    expect(await screen.findByRole('heading', { name: 'English Vocabulary' })).toBeInTheDocument();
    expect(screen.getByText('Intermediate words')).toBeInTheDocument();
    expect(screen.getByText('3 total cards')).toBeInTheDocument();
    expect(screen.getByText('2 loaded cards')).toBeInTheDocument();
    expect(screen.getByText('1 starred')).toBeInTheDocument();
    expect(screen.getByText('2 due for SRS')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Back to library' })).toHaveAttribute('href', '/library');
    expect(screen.getByRole('link', { name: 'Edit cards' })).toHaveAttribute('href', '/decks/2/flashcards/edit');

    const modes = screen.getByLabelText('Study modes');
    expect(within(modes).getByRole('link', { name: /Learn/ })).toHaveAttribute('href', '/decks/2/learn/options');
    expect(within(modes).getByRole('link', { name: /Practice Test/ })).toHaveAttribute('href', '/decks/2/practice/options');
    expect(within(modes).getByRole('link', { name: /Match/ })).toHaveAttribute('href', '/decks/2/matching/options');
    expect(within(modes).getByRole('link', { name: /Flashcards/ })).toHaveAttribute('href', '/decks/2/flashcards/viewer');
    expect(within(modes).getByRole('link', { name: /Card Sorting/ })).toHaveAttribute('href', '/decks/2/sorting');
    expect(within(modes).getByRole('link', { name: /Spaced Repetition/ })).toHaveAttribute('href', '/decks/2/srs');
  });
});
