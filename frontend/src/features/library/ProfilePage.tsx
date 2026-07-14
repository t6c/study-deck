import { Card, Stack, Text } from '@mantine/core';
import { useAuth } from '../../auth/AuthProvider';
import { PageHeader } from '../../components/PageHeader';

export function ProfilePage() {
  const { user } = useAuth();

  return (
    <Stack gap="lg">
      <PageHeader title="Profile" description="Your signed-in study account." />
      <Card withBorder radius="sm">
        <Stack gap={4}>
          <Text fw={700}>{user?.displayName || 'Study Deck user'}</Text>
          <Text c="dimmed">{user?.email}</Text>
        </Stack>
      </Card>
    </Stack>
  );
}
