import { Alert } from '@mantine/core';
import { IconAlertCircle } from '@tabler/icons-react';
import type { ApiError } from '../api/errors';

type FormErrorAlertProps = {
  error: ApiError | null;
};

export function FormErrorAlert({ error }: FormErrorAlertProps) {
  if (!error) {
    return null;
  }

  return (
    <Alert color="red" icon={<IconAlertCircle size={18} />} title="Request failed">
      {error.message}
    </Alert>
  );
}
