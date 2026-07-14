import { describe, expect, it } from 'vitest';
import { normalizeApiError } from './errors';
import type { ApiError } from './errors';

describe('normalizeApiError', () => {
  it('returns an already-normalized API error unchanged', () => {
    const apiError: ApiError = {
      status: 400,
      message: 'Email is already registered',
      path: '/api/v1/auth/register',
      errors: [{ field: 'email', message: 'Email is already registered' }],
    };

    expect(normalizeApiError(apiError)).toBe(apiError);
  });
});
