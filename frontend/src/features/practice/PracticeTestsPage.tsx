import { Button, Stack, Text } from '@mantine/core';
import { IconCards } from '@tabler/icons-react';
import { Link } from 'react-router-dom';
import { EmptyState } from '../../components/EmptyState';
import { PageHeader } from '../../components/PageHeader';

export function PracticeTestsPage() {
  return (
    <Stack gap="xl">
      <PageHeader title="Practice Tests" description="Start a practice test from any deck." />
      <EmptyState
        title="Choose a deck to create a practice test"
        description="Practice tests are generated from deck flashcards."
        action={
          <Button component={Link} to="/decks" leftSection={<IconCards size={16} />}>
            Browse decks
          </Button>
        }
      />
    </Stack>
  );
}
