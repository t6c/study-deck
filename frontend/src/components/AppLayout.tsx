import {
  ActionIcon,
  AppShell,
  Burger,
  Group,
  NavLink,
  ScrollArea,
  Stack,
  Text,
  Title,
  Tooltip,
  UnstyledButton,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import {
  IconBook2,
  IconChevronLeft,
  IconChevronRight,
  IconCards,
  IconFolder,
  IconHome2,
  IconLogout,
  IconSchool,
  IconStar,
  IconTargetArrow,
  IconUserCircle,
} from '@tabler/icons-react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';

const navSections = [
  {
    label: 'Main',
    items: [
      { label: 'Home', path: '/', icon: IconHome2 },
      { label: 'Library', path: '/library', icon: IconBook2 },
      { label: 'Folders', path: '/folders', icon: IconFolder },
      { label: 'Decks', path: '/decks', icon: IconCards },
      { label: 'Starred', path: '/starred', icon: IconStar },
    ],
  },
  {
    label: 'Study',
    items: [
      { label: 'Spaced Repetition', path: '/srs', icon: IconTargetArrow },
      { label: 'Practice Tests', path: '/practice-tests', icon: IconSchool },
    ],
  },
];

export function AppLayout() {
  const [mobileOpened, { toggle: toggleMobile, close: closeMobile }] = useDisclosure(false);
  const [desktopOpened, { toggle: toggleDesktop }] = useDisclosure(true);
  const location = useLocation();
  const navigate = useNavigate();
  const { logout, user } = useAuth();

  const navbarWidth = desktopOpened ? 260 : 76;

  return (
    <AppShell
      padding="md"
      header={{ height: 60 }}
      navbar={{
        width: navbarWidth,
        breakpoint: 'sm',
        collapsed: { mobile: !mobileOpened, desktop: false },
      }}
    >
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between">
          <Group gap="sm">
            <Burger opened={mobileOpened} onClick={toggleMobile} hiddenFrom="sm" size="sm" aria-label="Toggle navigation" />
            <Title order={3}>Study Deck</Title>
          </Group>
          <Group gap="xs">
            <Text size="sm" c="dimmed" visibleFrom="xs">
              {user?.displayName ?? user?.email}
            </Text>
            <Tooltip label={desktopOpened ? 'Collapse sidebar' : 'Expand sidebar'}>
              <ActionIcon
                aria-label={desktopOpened ? 'Collapse sidebar' : 'Expand sidebar'}
                onClick={toggleDesktop}
                variant="subtle"
                visibleFrom="sm"
              >
                {desktopOpened ? <IconChevronLeft size={18} /> : <IconChevronRight size={18} />}
              </ActionIcon>
            </Tooltip>
          </Group>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="sm">
        <Stack h="100%" gap="sm">
          <ScrollArea flex={1}>
            <Stack gap="md">
              {navSections.map((section) => (
                <Stack gap={4} key={section.label}>
                  {desktopOpened ? (
                    <Text c="dimmed" fw={700} size="xs" tt="uppercase" px="xs">
                      {section.label}
                    </Text>
                  ) : null}
                  {section.items.map((item) => {
                    const Icon = item.icon;
                    const active = item.path === '/' ? location.pathname === '/' : location.pathname.startsWith(item.path);

                    return (
                      <NavLink
                        key={`${section.label}-${item.label}`}
                        component={Link}
                        to={item.path}
                        label={desktopOpened ? item.label : undefined}
                        active={active}
                        leftSection={<Icon size={20} />}
                        onClick={closeMobile}
                        aria-label={item.label}
                      />
                    );
                  })}
                </Stack>
              ))}
            </Stack>
          </ScrollArea>
          <NavLink
            component={Link}
            to="/profile"
            label={desktopOpened ? 'Profile' : undefined}
            active={location.pathname.startsWith('/profile')}
            leftSection={<IconUserCircle size={20} />}
            onClick={closeMobile}
            aria-label="Profile"
          />
          <UnstyledButton
            aria-label="Logout"
            onClick={() => {
              logout();
              navigate('/login', { replace: true });
            }}
          >
            <Group gap="sm" p="xs">
              <IconLogout size={20} />
              {desktopOpened ? <Text size="sm">Logout</Text> : null}
            </Group>
          </UnstyledButton>
        </Stack>
      </AppShell.Navbar>

      <AppShell.Main>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
}
