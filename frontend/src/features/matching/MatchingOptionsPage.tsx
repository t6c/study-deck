import { Alert, Button, Checkbox, Group, NumberInput, Paper, Stack } from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconAlertCircle, IconArrowLeft, IconPlayerPlay } from '@tabler/icons-react';
import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { createMatchingSession } from '../../api/matchingApi';
import type { CreateMatchingSessionRequest } from '../../api/types';
import { ModeShell } from '../../components/ModeShell';

export function MatchingOptionsPage() {
  const { deckId } = useParams();
  const parsedDeckId = Number(deckId);
  const navigate = useNavigate();
  const form = useForm<CreateMatchingSessionRequest>({
    initialValues: {
      cardCount: 6,
      starredOnly: false,
    },
    validate: {
      cardCount: (value) => (value < 1 ? 'Choose at least one card' : null),
    },
  });
  const startSession = useMutation({
    mutationFn: (values: CreateMatchingSessionRequest) => createMatchingSession(parsedDeckId, values),
    onSuccess: (session) => navigate(`/matching-sessions/${session.id}`),
  });
  const disabled = startSession.isPending || !Number.isFinite(parsedDeckId);

  return (
    <ModeShell title="Matching options" description="Pair terms with their definitions.">
      <Button component={Link} to={`/decks/${parsedDeckId}`} variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to deck
      </Button>

      {startSession.isError || !Number.isFinite(parsedDeckId) ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not start matching">
          Check that the deck has cards, then try again.
        </Alert>
      ) : null}

      <Paper withBorder radius="sm" p="lg">
        <form onSubmit={form.onSubmit((values) => startSession.mutate(values))}>
          <Stack gap="lg">
            <NumberInput label="Card count" min={1} max={50} allowDecimal={false} {...form.getInputProps('cardCount')} />
            <Checkbox label="Starred only" {...form.getInputProps('starredOnly', { type: 'checkbox' })} />
            <Group justify="flex-end">
              <Button type="submit" leftSection={<IconPlayerPlay size={16} />} loading={startSession.isPending} disabled={disabled}>
                Start matching
              </Button>
            </Group>
          </Stack>
        </form>
      </Paper>
    </ModeShell>
  );
}
