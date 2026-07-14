import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes, useLocation } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { renderWithProviders } from '../../test/render';
import { LoginPage } from './LoginPage';

function LocationProbe() {
  const location = useLocation();
  return <div>{`${location.pathname}${location.search}${location.hash}`}</div>;
}

describe('LoginPage', () => {
  it('redirects to the full protected location after login', async () => {
    const user = userEvent.setup();

    renderWithProviders(
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/library" element={<LocationProbe />} />
      </Routes>,
      {
        initialEntries: [
          {
            pathname: '/login',
            state: {
              from: {
                pathname: '/library',
                search: '?tab=recent',
                hash: '#deck-1',
              },
            },
          } as unknown as string,
        ],
      },
    );

    await user.type(screen.getByLabelText('Email'), 'test@example.com');
    await user.type(screen.getByLabelText('Password'), 'password123');
    await user.click(screen.getByRole('button', { name: 'Sign in' }));

    await waitFor(() => {
      expect(screen.getByText('/library?tab=recent#deck-1')).toBeInTheDocument();
    });
  });
});
