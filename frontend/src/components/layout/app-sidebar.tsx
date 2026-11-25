import { Link, type LinkProps, useMatchRoute } from '@tanstack/react-router'
import {
  ArrowLeftRightIcon,
  BookPlusIcon,
  HouseIcon,
  LibraryIcon,
  ListChecksIcon,
  type LucideIcon,
  ShieldIcon,
  UserCheckIcon,
  UsersIcon,
} from 'lucide-react'

import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from '@/components/ui/sidebar'
import { useCurrentUser } from '@/features/identity/api'

interface NavItem {
  label: string
  to: LinkProps['to']
  icon: LucideIcon
}

interface NavGroup {
  label?: string
  items: NavItem[]
}

// Mesmas telas do menu do legado (template_leftbar.blade.php), agrupadas por domínio.
const navGroups: NavGroup[] = [
  { items: [{ label: 'Home', to: '/', icon: HouseIcon }] },
  {
    label: 'Catalog',
    items: [
      { label: 'All books', to: '/all-books', icon: LibraryIcon },
      { label: 'Add books', to: '/add-books', icon: BookPlusIcon },
    ],
  },
  {
    label: 'Loans',
    items: [
      {
        label: 'Issue / Return',
        to: '/issue-return',
        icon: ArrowLeftRightIcon,
      },
      {
        label: 'Currently issued',
        to: '/currently-issued',
        icon: ListChecksIcon,
      },
    ],
  },
  {
    label: 'Students',
    items: [
      {
        label: 'Students for approval',
        to: '/students-for-approval',
        icon: UserCheckIcon,
      },
      {
        label: 'Registered students',
        to: '/registered-students',
        icon: UsersIcon,
      },
    ],
  },
]

const adminGroup: NavGroup = {
  label: 'Admin',
  items: [{ label: 'Users', to: '/admin/users', icon: ShieldIcon }],
}

export function AppSidebar() {
  const user = useCurrentUser()
  const matchRoute = useMatchRoute()
  const { setOpenMobile } = useSidebar()
  const groups = user.role === 'ADMIN' ? [...navGroups, adminGroup] : navGroups

  return (
    <Sidebar>
      <SidebarHeader>
        <Link to="/" className="px-2 py-1 text-lg font-semibold">
          Library
        </Link>
      </SidebarHeader>
      <SidebarContent>
        {groups.map((group, index) => (
          <SidebarGroup key={group.label ?? index}>
            {group.label && (
              <SidebarGroupLabel>{group.label}</SidebarGroupLabel>
            )}
            <SidebarGroupContent>
              <SidebarMenu>
                {group.items.map((item) => (
                  <SidebarMenuItem key={item.label}>
                    <SidebarMenuButton
                      // fuzzy: /all-books continua ativo em subrotas. O "/" nunca casa
                      // por prefixo no TanStack Router, então a Home não precisa de exact.
                      isActive={Boolean(
                        matchRoute({ to: item.to, fuzzy: true }),
                      )}
                      render={
                        <Link
                          to={item.to}
                          // No celular o menu é uma gaveta: fecha ao navegar.
                          onClick={() => setOpenMobile(false)}
                        />
                      }
                    >
                      <item.icon aria-hidden />
                      <span>{item.label}</span>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        ))}
      </SidebarContent>
    </Sidebar>
  )
}
