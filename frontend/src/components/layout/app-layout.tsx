import { Link, Outlet } from '@tanstack/react-router'

import { useCurrentUser } from '@/features/identity/api'
import { UserMenu } from '@/features/identity/components/user-menu'

export function AppLayout() {
  const user = useCurrentUser()

  return (
    <div className="min-h-svh">
      <header className="flex items-center justify-between border-b px-4 py-3">
        <nav className="flex items-center gap-6">
          <Link to="/" className="font-semibold">
            Library
          </Link>
          {user.role === 'ADMIN' && (
            <Link
              to="/admin/users"
              className="text-muted-foreground hover:text-foreground text-sm"
              activeProps={{ className: 'text-foreground' }}
            >
              Users
            </Link>
          )}
        </nav>
        <UserMenu />
      </header>
      <main className="p-4">
        <Outlet />
      </main>
    </div>
  )
}
