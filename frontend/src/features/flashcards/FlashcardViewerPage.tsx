import { Alert, Badge, Button, Card, Group, Image, Loader, Stack, Text, Title } from '@mantine/core';
import { IconAlertCircle, IconArrowLeft, IconArrowRight, IconArrowsShuffle, IconRotateClockwise, IconSortAscending, IconStar, IconStarFilled } from '@tabler/icons-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { listViewerCards } from '../../api/deckApi';
import { setFlashcardStarred } from '../../api/flashcardApi';
import { EmptyState } from '../../components/EmptyState';
import { PageHeader } from '../../components/PageHeader';

function shuffledIndexes(length: number) {
  return Array.from({ length }, (_, index) => index).sort(() => Math.random() - 0.5);
}

export function FlashcardViewerPage() {
  const { deckId } = useParams();
  const queryClient = useQueryClient();
  const parsedDeckId = Number(deckId);
  const enabled = Number.isFinite(parsedDeckId);
  const [index, setIndex] = useState(0);
  const [revealed, setRevealed] = useState(false);
  const [order, setOrder] = useState<number[]>([]);
  const cards = useQuery({ queryKey: ['viewer-cards', parsedDeckId], queryFn: () => listViewerCards(parsedDeckId), enabled });

  const orderedCards = useMemo(() => {
    const data = cards.data ?? [];
    if (order.length !== data.length) {
      return data;
    }

    return order.map((cardIndex) => data[cardIndex]).filter(Boolean);
  }, [cards.data, order]);
  const safeIndex = orderedCards.length > 0 ? Math.min(index, orderedCards.length - 1) : 0;
  const currentCard = orderedCards[safeIndex];
  const starMutation = useMutation({
    mutationFn: ({ flashcardId, starred }: { flashcardId: number; starred: boolean }) => setFlashcardStarred(flashcardId, starred),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['viewer-cards', parsedDeckId] });
      queryClient.invalidateQueries({ queryKey: ['flashcards', parsedDeckId] });
      queryClient.invalidateQueries({ queryKey: ['deck-summary', parsedDeckId] });
    },
  });

  function move(nextIndex: number) {
    setIndex(nextIndex);
    setRevealed(false);
  }

  useEffect(() => {
    setIndex((currentIndex) => {
      if (orderedCards.length === 0) {
        return 0;
      }

      return Math.min(currentIndex, orderedCards.length - 1);
    });
  }, [orderedCards.length]);

  return (
    <Stack gap="xl">
      <Button component={Link} to={`/decks/${parsedDeckId}`} variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to deck
      </Button>

      <PageHeader
        title="Flashcards"
        description="Review this deck one card at a time."
        actions={
          <>
            <Button
              variant="light"
              leftSection={<IconArrowsShuffle size={16} />}
              onClick={() => {
                setOrder(shuffledIndexes(cards.data?.length ?? 0));
                move(0);
              }}
            >
              Shuffle cards
            </Button>
            <Button component={Link} to={`/decks/${parsedDeckId}/sorting`} leftSection={<IconSortAscending size={16} />}>
              Card sorting
            </Button>
          </>
        }
      />

      {cards.isError || !enabled ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load viewer cards">
          Check that the deck exists, then try again.
        </Alert>
      ) : null}

      {cards.isLoading ? <Loader aria-label="Loading viewer cards" /> : null}

      {!cards.isLoading && orderedCards.length === 0 ? (
        <EmptyState title="No cards to review" description="Add cards before opening the flashcard viewer." />
      ) : null}

      {currentCard ? (
        <Stack gap="md" align="stretch">
          <Group justify="space-between">
            <Badge variant="light">Card {safeIndex + 1} of {orderedCards.length}</Badge>
            <Button
              aria-label={currentCard.starred ? 'Unstar card' : 'Star card'}
              variant="subtle"
              leftSection={currentCard.starred ? <IconStarFilled size={16} /> : <IconStar size={16} />}
              onClick={() => starMutation.mutate({ flashcardId: currentCard.id, starred: !currentCard.starred })}
              disabled={starMutation.isPending}
            >
              {currentCard.starred ? 'Starred' : 'Star'}
            </Button>
          </Group>

          <Card withBorder radius="sm" p="xl" mih={280}>
            <Stack gap="lg" align="center" justify="center" mih={220}>
              <Text size="sm" c="dimmed">
                {revealed ? 'Definition' : 'Term'}
              </Text>
              <Title order={2} ta="center">
                {revealed ? currentCard.definition : currentCard.term}
              </Title>
              {(revealed ? currentCard.definitionImageUrl : currentCard.termImageUrl) ? (
                <Image
                  src={revealed ? currentCard.definitionImageUrl : currentCard.termImageUrl}
                  alt={revealed ? 'Definition visual' : 'Term visual'}
                  maw={360}
                  radius="sm"
                />
              ) : null}
              <Button leftSection={<IconRotateClockwise size={16} />} onClick={() => setRevealed((value) => !value)}>
                {revealed ? 'Show term' : 'Reveal answer'}
              </Button>
            </Stack>
          </Card>

          <Group justify="space-between">
            <Button
              aria-label="Previous card"
              variant="light"
              leftSection={<IconArrowLeft size={16} />}
              disabled={safeIndex === 0}
              onClick={() => move(Math.max(0, safeIndex - 1))}
            >
              Previous
            </Button>
            <Button
              aria-label="Next card"
              rightSection={<IconArrowRight size={16} />}
              disabled={safeIndex >= orderedCards.length - 1}
              onClick={() => move(Math.min(orderedCards.length - 1, safeIndex + 1))}
            >
              Next
            </Button>
          </Group>
        </Stack>
      ) : null}
    </Stack>
  );
}
