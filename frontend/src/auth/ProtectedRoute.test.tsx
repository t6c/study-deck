import { screen } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { ProtectedRoute } from './ProtectedRoute';
import { renderWithProviders } from '../test/render';

describe('ProtectedRoute', () => {
  it('redirects anonymous users to login', () => {
    renderWithProviders(
      <Routes>
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<div>Private page</div>} />
        </Route>
        <Route path="/login" element={<div>Sign in screen</div>} />
      </Routes>,
    );

    expect(screen.getByText('Sign in screen')).toBeInTheDocument();
    expect(screen.queryByText('Private page')).not.toBeInTheDocument();
  });
});
