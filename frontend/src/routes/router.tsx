import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from '../auth/ProtectedRoute';
import { AppLayout } from '../components/AppLayout';
import { DeckDetailPage } from '../features/decks/DeckDetailPage';
import { HomePage } from '../features/dashboard/HomePage';
import { FlashcardEditorPage } from '../features/flashcards/FlashcardEditorPage';
import { FlashcardViewerPage } from '../features/flashcards/FlashcardViewerPage';
import { LearnOptionsPage } from '../features/learn/LearnOptionsPage';
import { LearnSessionPage } from '../features/learn/LearnSessionPage';
import { LibraryPage } from '../features/library/LibraryPage';
import { LoginPage } from '../features/auth/LoginPage';
import { MatchingOptionsPage } from '../features/matching/MatchingOptionsPage';
import { MatchingSessionPage } from '../features/matching/MatchingSessionPage';
import { PracticeOptionsPage } from '../features/practice/PracticeOptionsPage';
import { PracticeSessionPage } from '../features/practice/PracticeSessionPage';
import { PracticeTestsPage } from '../features/practice/PracticeTestsPage';
import { RegisterPage } from '../features/auth/RegisterPage';
import { CardSortingPanel } from '../features/sorting/CardSortingPanel';
import { SrsDashboardPage } from '../features/srs/SrsDashboardPage';
import { SrsIntroPage } from '../features/srs/SrsIntroPage';
import { SrsReviewPage } from '../features/srs/SrsReviewPage';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<HomePage />} />
          <Route path="library" element={<LibraryPage />} />
          <Route path="folders" element={<LibraryPage />} />
          <Route path="decks" element={<LibraryPage />} />
          <Route path="starred" element={<LibraryPage />} />
          <Route path="srs" element={<LibraryPage />} />
          <Route path="profile" element={<LibraryPage />} />
          <Route path="decks/:deckId" element={<DeckDetailPage />} />
          <Route path="decks/:deckId/flashcards/edit" element={<FlashcardEditorPage />} />
          <Route path="decks/:deckId/flashcards/viewer" element={<FlashcardViewerPage />} />
          <Route path="decks/:deckId/sorting" element={<CardSortingPanel />} />
          <Route path="decks/:deckId/learn/options" element={<LearnOptionsPage />} />
          <Route path="learn-sessions/:sessionId" element={<LearnSessionPage />} />
          <Route path="decks/:deckId/practice/options" element={<PracticeOptionsPage />} />
          <Route path="practice-tests" element={<PracticeTestsPage />} />
          <Route path="practice-tests/:practiceTestId" element={<PracticeSessionPage />} />
          <Route path="decks/:deckId/matching/options" element={<MatchingOptionsPage />} />
          <Route path="matching-sessions/:sessionId" element={<MatchingSessionPage />} />
          <Route path="decks/:deckId/srs" element={<SrsDashboardPage />} />
          <Route path="decks/:deckId/srs/intro" element={<SrsIntroPage />} />
          <Route path="decks/:deckId/srs/review" element={<SrsReviewPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
