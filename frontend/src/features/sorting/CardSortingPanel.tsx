import { Alert, Badge, Button, Card, Checkbox, Group, Image, Loader, Stack, Text, Title } from '@mantine/core';
import { IconAlertCircle, IconArrowLeft, IconCheck, IconRefresh, IconX } from '@tabler/icons-react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useMemo, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { answerSortingItem, createSortingSession } from '../../api/sortingApi';
import type { SortingAnswer, SortingSessionResponse } from '../../api/types';
import { EmptyState } from '../../components/EmptyState';
import { PageHeader } from '../../components/PageHeader';

export function CardSortingPanel() {
  const { deckId } = useParams();
  const parsedDeckId = Number(deckId);
  const enabled = Number.isFinite(parsedDeckId);
  const [starredOnly, setStarredOnly] = useState(false);
  const [shuffle, setShuffle] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [knownCount, setKnownCount] = useState(0);
  const [doNotKnowCount, setDoNotKnowCount] = useState(0);
  const session = useQuery({
    queryKey: ['sorting-session', parsedDeckId, starredOnly, shuffle],
    queryFn: () => createSortingSession(parsedDeckId, { starredOnly, shuffle }),
    enabled,
  });
  const items = session.data?.items ?? [];
  const currentItem = items[currentIndex];
  const completed = items.length > 0 && currentIndex >= items.length;
  const activeItemRef = useRef<{ sessionId: number | null; itemId: number | null }>({ sessionId: null, itemId: null });
  activeItemRef.current = { sessionId: session.data?.id ?? null, itemId: currentItem?.id ?? null };
  const answerMutation = useMutation({
    mutationFn: ({ sessionId, itemId, answer }: { sessionId: number; itemId: number; answer: SortingAnswer }) =>
      answerSortingItem(sessionId, { itemId, answer }),
    onSuccess: (_response, variables) => {
      const activeItem = activeItemRef.current;

      if (variables.sessionId !== activeItem.sessionId || variables.itemId !== activeItem.itemId) {
        return;
      }

      if (variables.answer === 'KNOW') {
        setKnownCount((value) => value + 1);
      } else {
        setDoNotKnowCount((value) => value + 1);
      }
      setCurrentIndex((value) => value + 1);
    },
  });

  const counts = useMemo(() => {
    const response = session.data as SortingSessionResponse | undefined;
    return {
      known: knownCount || response?.knownCount || 0,
      doNotKnow: doNotKnowCount || response?.doNotKnowCount || 0,
    };
  }, [doNotKnowCount, knownCount, session.data]);

  function resetLocalCounts() {
    setCurrentIndex(0);
    setKnownCount(0);
    setDoNotKnowCount(0);
  }

  function submitAnswer(answer: SortingAnswer) {
    if (answerMutation.isPending || !session.data || !currentItem) {
      return;
    }

    answerMutation.mutate({ sessionId: session.data.id, itemId: currentItem.id, answer });
  }

  return (
    <Stack gap="xl">
      <Button component={Link} to={`/decks/${parsedDeckId}`} variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to deck
      </Button>

      <PageHeader
        title="Card sorting"
        description="Mark each card by whether you know it."
        actions={
          <Button
            variant="light"
            leftSection={<IconRefresh size={16} />}
            disabled={answerMutation.isPending}
            onClick={() => {
              resetLocalCounts();
              session.refetch();
            }}
          >
            Restart
          </Button>
        }
      />

      <Group gap="md">
        <Checkbox
          label="Starred only"
          checked={starredOnly}
          disabled={answerMutation.isPending}
          onChange={(event) => {
            setStarredOnly(event.currentTarget.checked);
            resetLocalCounts();
          }}
        />
        <Checkbox
          label="Shuffle"
          checked={shuffle}
          disabled={answerMutation.isPending}
          onChange={(event) => {
            setShuffle(event.currentTarget.checked);
            resetLocalCounts();
          }}
        />
      </Group>

      {session.isError || !enabled ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not start sorting">
          Check that the deck has cards, then try again.
        </Alert>
      ) : null}

      {session.isLoading ? <Loader aria-label="Loading sorting session" /> : null}

      <Group gap="sm">
        <Badge variant="light">Known: {counts.known}</Badge>
        <Badge variant="light">Don't know: {counts.doNotKnow}</Badge>
        <Badge variant="light">
          {Math.min(currentIndex + (currentItem ? 1 : 0), items.length)} / {items.length}
        </Badge>
      </Group>

      {!session.isLoading && items.length === 0 ? (
        <EmptyState title="No cards to sort" description="Add cards or turn off starred-only filtering." />
      ) : null}

      {completed ? (
        <EmptyState
          title="Sorting complete"
          description={`Known: ${counts.known}. Don't know: ${counts.doNotKnow}.`}
          action={
            <Button
              disabled={answerMutation.isPending}
              onClick={() => {
                resetLocalCounts();
                session.refetch();
              }}
            >
              Sort again
            </Button>
          }
        />
      ) : null}

      {currentItem ? (
        <Card withBorder radius="sm" p="xl">
          <Stack gap="lg">
            <Group justify="space-between">
              <Badge variant="light">Card {currentIndex + 1} of {items.length}</Badge>
              {currentItem.starred ? <Badge variant="light">Starred</Badge> : null}
            </Group>
            <Stack gap="xs">
              <Text size="sm" c="dimmed">
                Term
              </Text>
              <Title order={2}>{currentItem.term}</Title>
              {currentItem.termImageUrl ? <Image src={currentItem.termImageUrl} alt="Term visual" maw={320} radius="sm" /> : null}
            </Stack>
            <Stack gap="xs">
              <Text size="sm" c="dimmed">
                Definition
              </Text>
              <Text size="lg">{currentItem.definition}</Text>
              {currentItem.definitionImageUrl ? (
                <Image src={currentItem.definitionImageUrl} alt="Definition visual" maw={320} radius="sm" />
              ) : null}
            </Stack>
            <Group grow>
              <Button
                variant="light"
                color="red"
                leftSection={<IconX size={16} />}
                loading={answerMutation.isPending}
                disabled={answerMutation.isPending}
                onClick={() => submitAnswer('DO_NOT_KNOW')}
              >
                Don't know
              </Button>
              <Button leftSection={<IconCheck size={16} />} loading={answerMutation.isPending} disabled={answerMutation.isPending} onClick={() => submitAnswer('KNOW')}>
                Know
              </Button>
            </Group>
          </Stack>
        </Card>
      ) : null}
    </Stack>
  );
}
