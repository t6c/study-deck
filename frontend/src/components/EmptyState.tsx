import { Paper, Stack, Text, Title } from '@mantine/core';
import type { ReactNode } from 'react';

type EmptyStateProps = {
  title: string;
  description?: string;
  action?: ReactNode;
};

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <Paper withBorder p="xl" radius="sm">
      <Stack gap="xs" align="center">
        <Title order={3}>{title}</Title>
        {description ? (
          <Text c="dimmed" ta="center">
            {description}
          </Text>
        ) : null}
        {action}
      </Stack>
    </Paper>
  );
}
