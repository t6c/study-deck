import { Anchor, Button, Container, Paper, PasswordInput, Stack, Text, TextInput, Title } from '@mantine/core';
import { hasLength, isEmail, isNotEmpty, useForm } from '@mantine/form';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../../api/authApi';
import type { ApiError } from '../../api/errors';
import { normalizeApiError } from '../../api/errors';
import { useAuth } from '../../auth/AuthProvider';
import { FormErrorAlert } from '../../components/FormErrorAlert';

export function RegisterPage() {
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);
  const auth = useAuth();
  const navigate = useNavigate();

  const form = useForm({
    mode: 'controlled',
    initialValues: {
      displayName: '',
      email: '',
      password: '',
    },
    validate: {
      displayName: isNotEmpty('Display name is required'),
      email: isEmail('Enter a valid email'),
      password: hasLength({ min: 8 }, 'Use at least 8 characters'),
    },
  });

  return (
    <Container size={460} py={80}>
      <Paper withBorder p="xl" radius="sm">
        <form
          onSubmit={form.onSubmit(async (values) => {
            setError(null);
            setLoading(true);
            try {
              const response = await register(values);
              auth.setSession(response);
              navigate('/', { replace: true });
            } catch (requestError) {
              setError(normalizeApiError(requestError));
            } finally {
              setLoading(false);
            }
          })}
        >
          <Stack>
            <Stack gap={4}>
              <Title order={1}>Create account</Title>
              <Text c="dimmed" size="sm">
                Start building your study library.
              </Text>
            </Stack>
            <FormErrorAlert error={error} />
            <TextInput label="Display name" autoComplete="name" {...form.getInputProps('displayName')} />
            <TextInput label="Email" autoComplete="email" {...form.getInputProps('email')} />
            <PasswordInput label="Password" autoComplete="new-password" {...form.getInputProps('password')} />
            <Button type="submit" loading={loading}>
              Create account
            </Button>
            <Text size="sm" ta="center">
              Already have an account?{' '}
              <Anchor component={Link} to="/login">
                Sign in
              </Anchor>
            </Text>
          </Stack>
        </form>
      </Paper>
    </Container>
  );
}
