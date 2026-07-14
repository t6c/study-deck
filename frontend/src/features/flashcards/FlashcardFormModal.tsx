import { Button, Group, Modal, Stack, TextInput, Textarea } from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { useEffect } from 'react';
import type { CreateFlashcardRequest, FlashcardResponse, UpdateFlashcardRequest } from '../../api/types';

type FlashcardFormValues = {
  term: string;
  definition: string;
  termImageUrl: string;
  definitionImageUrl: string;
};

type FlashcardFormModalProps = {
  opened: boolean;
  onClose: () => void;
  onSubmit: (values: CreateFlashcardRequest | UpdateFlashcardRequest) => void;
  flashcard?: FlashcardResponse | null;
  loading?: boolean;
};

function toRequest(values: FlashcardFormValues): CreateFlashcardRequest {
  return {
    term: values.term.trim(),
    definition: values.definition.trim(),
    termImageUrl: values.termImageUrl.trim() || null,
    definitionImageUrl: values.definitionImageUrl.trim() || null,
  };
}

export function FlashcardFormModal({ opened, onClose, onSubmit, flashcard, loading = false }: FlashcardFormModalProps) {
  const form = useForm<FlashcardFormValues>({
    initialValues: {
      term: '',
      definition: '',
      termImageUrl: '',
      definitionImageUrl: '',
    },
    validate: {
      term: isNotEmpty('Term is required'),
      definition: isNotEmpty('Definition is required'),
    },
  });

  useEffect(() => {
    if (!opened) {
      return;
    }

    form.setValues({
      term: flashcard?.term ?? '',
      definition: flashcard?.definition ?? '',
      termImageUrl: flashcard?.termImageUrl ?? '',
      definitionImageUrl: flashcard?.definitionImageUrl ?? '',
    });
    form.clearErrors();
    form.resetDirty();
  }, [flashcard, opened]);

  return (
    <Modal opened={opened} onClose={onClose} title={flashcard ? 'Edit card' : 'Create card'} centered>
      <form onSubmit={form.onSubmit((values) => onSubmit(toRequest(values)))}>
        <Stack>
          <TextInput label="Term" placeholder="abate" withAsterisk {...form.getInputProps('term')} />
          <Textarea label="Definition" placeholder="become less intense" minRows={3} withAsterisk {...form.getInputProps('definition')} />
          <TextInput label="Term image URL" placeholder="https://example.com/term.png" {...form.getInputProps('termImageUrl')} />
          <TextInput
            label="Definition image URL"
            placeholder="https://example.com/definition.png"
            {...form.getInputProps('definitionImageUrl')}
          />
          <Group justify="flex-end">
            <Button variant="subtle" onClick={onClose} type="button">
              Cancel
            </Button>
            <Button type="submit" loading={loading}>
              {flashcard ? 'Save card' : 'Create card'}
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}
