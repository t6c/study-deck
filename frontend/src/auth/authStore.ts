import type { AuthUser } from '../api/types';

const TOKEN_KEY = 'studyDeck.accessToken';
const USER_KEY = 'studyDeck.user';

export type AuthSnapshot = {
  accessToken: string | null;
  user: AuthUser | null;
};

type StoredAuthSnapshot = {
  accessToken: string;
  user: AuthUser;
};

export function getAccessToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function readAuthSnapshot(): AuthSnapshot {
  const accessToken = getAccessToken();
  const rawUser = localStorage.getItem(USER_KEY);

  if (!accessToken || !rawUser) {
    return { accessToken: null, user: null };
  }

  try {
    return { accessToken, user: JSON.parse(rawUser) as AuthUser };
  } catch {
    clearAuthSnapshot();
    return { accessToken: null, user: null };
  }
}

export function writeAuthSnapshot(snapshot: StoredAuthSnapshot) {
  localStorage.setItem(TOKEN_KEY, snapshot.accessToken);
  localStorage.setItem(USER_KEY, JSON.stringify(snapshot.user));
}

export function clearAuthSnapshot() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}
