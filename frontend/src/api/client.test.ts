import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { apiClient } from './client';
import { writeAuthSnapshot } from '../auth/authStore';
import { server } from '../test/server';

describe('apiClient', () => {
  it('adds the JWT authorization header when a token is stored', async () => {
    writeAuthSnapshot({
      accessToken: 'abc123',
      user: { id: 1, email: 'test@example.com', displayName: 'Test User' },
    });

    server.use(
      http.get('/api/v1/protected', ({ request }) =>
        HttpResponse.json({ authorization: request.headers.get('authorization') }),
      ),
    );

    const response = await apiClient.get<{ authorization: string }>('/protected');

    expect(response.data.authorization).toBe('Bearer abc123');
  });
});
