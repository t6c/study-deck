import { Button, Group, Modal, Select, Stack, TextInput, Textarea } from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { useEffect } from 'react';
import type { CreateDeckRequest, DeckResponse, FolderResponse, UpdateDeckRequest } from '../../api/types';

type DeckFormValues = {
  folderId: string | null;
  title: string;
  description: string;
};

type DeckFormModalProps = {
  opened: boolean;
  onClose: () => void;
  onSubmit: (values: CreateDeckRequest | UpdateDeckRequest) => void;
  folders: FolderResponse[];
  deck?: DeckResponse | null;
  loading?: boolean;
};

function toRequest(values: DeckFormValues, editing: boolean): CreateDeckRequest | UpdateDeckRequest {
  const shared = {
    title: values.title.trim(),
    description: values.description.trim() || null,
  };

  if (editing) {
    return shared;
  }

  return {
    ...shared,
    folderId: values.folderId ? Number(values.folderId) : null,
  };
}

export function DeckFormModal({ opened, onClose, onSubmit, folders, deck, loading = false }: DeckFormModalProps) {
  const form = useForm<DeckFormValues>({
    initialValues: {
      folderId: null,
      title: '',
      description: '',
    },
    validate: {
      title: isNotEmpty('Deck title is required'),
    },
  });

  useEffect(() => {
    if (!opened) {
      return;
    }

    form.setValues({
      folderId: deck?.folderId ? String(deck.folderId) : null,
      title: deck?.title ?? '',
      description: deck?.description ?? '',
    });
    form.clearErrors();
    form.resetDirty();
  }, [deck, opened]);

  return (
    <Modal opened={opened} onClose={onClose} title={deck ? 'Edit deck' : 'Create deck'} centered>
      <form onSubmit={form.onSubmit((values) => onSubmit(toRequest(values, Boolean(deck))))}>
        <Stack>
          <Select
            label="Folder"
            placeholder="No folder"
            clearable
            data={folders.map((folder) => ({ value: String(folder.id), label: folder.name }))}
            disabled={Boolean(deck)}
            {...form.getInputProps('folderId')}
          />
          <TextInput label="Title" placeholder="English Vocabulary" withAsterisk {...form.getInputProps('title')} />
          <Textarea label="Description" placeholder="Intermediate words" minRows={3} {...form.getInputProps('description')} />
          <Group justify="flex-end">
            <Button variant="subtle" onClick={onClose} type="button">
              Cancel
            </Button>
            <Button type="submit" loading={loading}>
              {deck ? 'Save deck' : 'Create deck'}
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}
