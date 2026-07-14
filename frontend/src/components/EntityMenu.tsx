import { ActionIcon, Menu } from '@mantine/core';
import { IconDotsVertical, IconEdit, IconTrash } from '@tabler/icons-react';

type EntityMenuProps = {
  onEdit: () => void;
  onDelete: () => void;
};

export function EntityMenu({ onEdit, onDelete }: EntityMenuProps) {
  return (
    <Menu shadow="md" width={160}>
      <Menu.Target>
        <ActionIcon aria-label="Open actions menu" variant="subtle">
          <IconDotsVertical size={18} />
        </ActionIcon>
      </Menu.Target>
      <Menu.Dropdown>
        <Menu.Item leftSection={<IconEdit size={16} />} onClick={onEdit}>
          Edit
        </Menu.Item>
        <Menu.Item color="red" leftSection={<IconTrash size={16} />} onClick={onDelete}>
          Delete
        </Menu.Item>
      </Menu.Dropdown>
    </Menu>
  );
}
