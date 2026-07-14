import { Alert, Button, Card, Group, Loader, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { IconAlertCircle, IconCards, IconFolder, IconPlus, IconStar } from '@tabler/icons-react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { listDecks, listFolders } from '../../api/deckApi';
import { EmptyState } from '../../components/EmptyState';
import { PageHeader } from '../../components/PageHeader';
import { StatCard } from '../../components/StatCard';

export function HomePage() {
  const folders = useQuery({ queryKey: ['folders'], queryFn: listFolders });
  const decks = useQuery({ queryKey: ['decks'], queryFn: listDecks });
  const isLoading = folders.isLoading || decks.isLoading;
  const isError = folders.isError || decks.isError;
  const recentDecks = [...(decks.data ?? [])]
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 4);

  return (
    <Stack gap="xl">
      <PageHeader
        title="Home"
        description="Pick up where you left off."
        actions={
          <Button component={Link} to="/library" leftSection={<IconPlus size={16} />}>
            Create
          </Button>
        }
      />

      {isError ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load dashboard">
          Check that the backend is running, then try again.
        </Alert>
      ) : null}

      <SimpleGrid cols={{ base: 1, sm: 3 }}>
        <StatCard
          label="Decks"
          value={isLoading ? <Loader aria-label="Loading dashboard" size="sm" /> : (decks.data?.length ?? 0)}
          icon={<IconCards size={20} />}
        />
        <StatCard
          label="Folders"
          value={isLoading ? <Loader aria-label="Loading dashboard" size="sm" /> : (folders.data?.length ?? 0)}
          icon={<IconFolder size={20} />}
        />
        <StatCard label="Starred" value="Open deck" icon={<IconStar size={20} />} />
      </SimpleGrid>

      <Group gap="sm">
        <Button component={Link} to="/library" variant="light">
          Browse library
        </Button>
        <Button component={Link} to="/srs" variant="subtle">
          Spaced repetition
        </Button>
      </Group>

      <Stack gap="md">
        <Title order={2}>Recent decks</Title>
        {isLoading ? <Loader aria-label="Loading recent decks" /> : null}
        {!isLoading && recentDecks.length === 0 ? (
          <EmptyState
            title="No decks yet"
            description="Create your first deck from the library."
            action={
              <Button component={Link} to="/library">
                Open library
              </Button>
            }
          />
        ) : null}
        <SimpleGrid cols={{ base: 1, sm: 2, lg: 4 }}>
          {recentDecks.map((deck) => (
            <Card key={deck.id} withBorder radius="sm">
              <Stack gap="sm">
                <Text fw={700}>{deck.title}</Text>
                <Text c="dimmed" size="sm" lineClamp={2}>
                  {deck.description ?? 'No description'}
                </Text>
                <Button component={Link} to={`/decks/${deck.id}`} variant="light">
                  Open
                </Button>
              </Stack>
            </Card>
          ))}
        </SimpleGrid>
      </Stack>
    </Stack>
  );
}
