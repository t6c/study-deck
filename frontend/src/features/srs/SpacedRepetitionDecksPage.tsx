import { Alert, Button, Card, Group, Loader, SimpleGrid, Stack, Text } from '@mantine/core';
import { IconAlertCircle, IconRepeat } from '@tabler/icons-react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { listDecks } from '../../api/deckApi';
import { EmptyState } from '../../components/EmptyState';
import { PageHeader } from '../../components/PageHeader';

export function SpacedRepetitionDecksPage() {
  const decks = useQuery({ queryKey: ['decks'], queryFn: listDecks });

  return (
    <Stack gap="xl">
      <PageHeader title="Spaced Repetition" description="Choose a deck to review cards due on the SRS schedule." />

      {decks.isError ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load decks">
          Check that the backend is running, then try again.
        </Alert>
      ) : null}

      {decks.isLoading ? <Loader aria-label="Loading spaced repetition decks" /> : null}

      {!decks.isLoading && decks.data?.length === 0 ? (
        <EmptyState title="No decks yet" description="Create a deck before starting spaced repetition." />
      ) : null}

      {decks.data && decks.data.length > 0 ? (
        <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }}>
          {decks.data.map((deck) => (
            <Card key={deck.id} withBorder radius="sm">
              <Stack gap="sm">
                <Group gap="sm">
                  <IconRepeat size={20} />
                  <Text fw={700}>{deck.title}</Text>
                </Group>
                {deck.description ? (
                  <Text c="dimmed" size="sm" lineClamp={2}>
                    {deck.description}
                  </Text>
                ) : null}
                <Button component={Link} to={`/decks/${deck.id}/srs`} variant="light">
                  Open SRS
                </Button>
              </Stack>
            </Card>
          ))}
        </SimpleGrid>
      ) : null}
    </Stack>
  );
}
