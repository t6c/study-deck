import { Button, Card, Group, Stack, Text, Title } from '@mantine/core';
import { IconArrowLeft, IconCheck } from '@tabler/icons-react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ModeShell } from '../../components/ModeShell';

export function getSrsIntroStorageKey(deckId: number) {
  return `studyDeck.srsIntroAcknowledged.${deckId}`;
}

export function SrsIntroPage() {
  const { deckId } = useParams();
  const parsedDeckId = Number(deckId);
  const navigate = useNavigate();

  function continueToSrs() {
    if (Number.isFinite(parsedDeckId)) {
      localStorage.setItem(getSrsIntroStorageKey(parsedDeckId), 'true');
      navigate(`/decks/${parsedDeckId}/srs`);
    }
  }

  return (
    <ModeShell title="Spaced repetition" description="Review cards when they are due, then rate how well you remembered them.">
      <Button component={Link} to={`/decks/${parsedDeckId}`} variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to deck
      </Button>

      <Card withBorder radius="sm" p="xl">
        <Stack gap="md">
          <Title order={2}>How SRS works</Title>
          <Text>
            New cards enter a short learning path. Each review schedules the card again based on your rating, so easy cards
            appear less often and missed cards return sooner.
          </Text>
          <Text c="dimmed">
            Use Again, Hard, Good, and Easy honestly. The schedule gets more useful when your ratings match your recall.
          </Text>
          <Group justify="flex-end">
            <Button leftSection={<IconCheck size={16} />} onClick={continueToSrs}>
              Continue to SRS
            </Button>
          </Group>
        </Stack>
      </Card>
    </ModeShell>
  );
}
