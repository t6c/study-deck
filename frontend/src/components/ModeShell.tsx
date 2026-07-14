import { Stack } from '@mantine/core';
import type { PropsWithChildren } from 'react';
import { PageHeader } from './PageHeader';

type ModeShellProps = PropsWithChildren<{
  title: string;
  description?: string;
}>;

export function ModeShell({ title, description, children }: ModeShellProps) {
  return (
    <Stack gap="lg">
      <PageHeader title={title} description={description} />
      {children}
    </Stack>
  );
}
