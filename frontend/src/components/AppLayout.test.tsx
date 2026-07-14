import { screen } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { AppLayout } from './AppLayout';
import { writeAuthSnapshot } from '../auth/authStore';
import { renderWithProviders } from '../test/render';

describe('AppLayout', () => {
  it('renders the expanded primary navigation', () => {
    writeAuthSnapshot({
      accessToken: 'abc123',
      user: { id: 1, email: 'test@example.com', displayName: 'Test User' },
    });

    renderWithProviders(
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<div>Home content</div>} />
        </Route>
      </Routes>,
    );

    expect(screen.getByRole('heading', { name: 'Study Deck' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Home' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Library' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Decks' })).toHaveAttribute('href', '/decks');
    expect(screen.getByRole('link', { name: 'Practice Tests' })).toHaveAttribute('href', '/practice-tests');
    expect(screen.getByRole('button', { name: 'Logout' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Collapse sidebar' })).toBeInTheDocument();
  });
});
