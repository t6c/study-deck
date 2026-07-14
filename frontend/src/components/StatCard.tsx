import { Paper, Stack, Text, Title } from '@mantine/core';
import type { ReactNode } from 'react';

type StatCardProps = {
  label: string;
  value: ReactNode;
  icon?: ReactNode;
};

export function StatCard({ label, value, icon }: StatCardProps) {
  return (
    <Paper withBorder p="md" radius="sm">
      <Stack gap={4}>
        {icon}
        <Text c="dimmed" size="sm">
          {label}
        </Text>
        <Title order={2}>{value}</Title>
      </Stack>
    </Paper>
  );
}
