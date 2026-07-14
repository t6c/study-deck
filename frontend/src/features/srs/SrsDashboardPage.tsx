import { Alert, Button, Card, Group, Loader, SimpleGrid, Stack, Text } from '@mantine/core';
import { IconAlertCircle, IconArrowLeft, IconBook, IconClock, IconRepeat, IconSparkles } from '@tabler/icons-react';
import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { getSrsStats } from '../../api/srsApi';
import { ModeShell } from '../../components/ModeShell';
import { StatCard } from '../../components/StatCard';
import { getSrsIntroStorageKey } from './SrsIntroPage';

export function SrsDashboardPage() {
  const { deckId } = useParams();
  const parsedDeckId = Number(deckId);
  const enabled = Number.isFinite(parsedDeckId);
  const acknowledged = enabled ? localStorage.getItem(getSrsIntroStorageKey(parsedDeckId)) === 'true' : false;
  const stats = useQuery({
    queryKey: ['srs-stats', parsedDeckId],
    queryFn: () => getSrsStats(parsedDeckId),
    enabled,
  });

  return (
    <ModeShell title="SRS dashboard" description="Track the deck's spaced repetition queue.">
      <Button component={Link} to={`/decks/${parsedDeckId}`} variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to deck
      </Button>

      {!acknowledged && enabled ? (
        <Card withBorder radius="sm" p="md">
          <Group justify="space-between">
            <Text>Review the SRS basics before your first session.</Text>
            <Button component={Link} to={`/decks/${parsedDeckId}/srs/intro`} variant="light">
              Open intro
            </Button>
          </Group>
        </Card>
      ) : null}

      {stats.isError || !enabled ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load SRS stats">
          Refresh the page or try again later.
        </Alert>
      ) : null}

      <SimpleGrid cols={{ base: 1, sm: 2, lg: 4 }}>
        <StatCard label="Due now" value={stats.isLoading ? <Loader size="sm" aria-label="Loading SRS stats" /> : (stats.data?.dueCards ?? 0)} icon={<IconClock size={20} />} />
        <StatCard label="New cards" value={stats.isLoading ? <Loader size="sm" aria-label="Loading SRS stats" /> : (stats.data?.newCards ?? 0)} icon={<IconSparkles size={20} />} />
        <StatCard label="Learning" value={stats.isLoading ? <Loader size="sm" aria-label="Loading SRS stats" /> : (stats.data?.learningCards ?? 0)} icon={<IconBook size={20} />} />
        <StatCard label="Review" value={stats.isLoading ? <Loader size="sm" aria-label="Loading SRS stats" /> : (stats.data?.reviewCards ?? 0)} icon={<IconRepeat size={20} />} />
      </SimpleGrid>

      <Group>
        <Button component={Link} to={`/decks/${parsedDeckId}/srs/review`} disabled={(stats.data?.dueCards ?? 0) === 0}>
          Review due cards
        </Button>
      </Group>
    </ModeShell>
  );
}
