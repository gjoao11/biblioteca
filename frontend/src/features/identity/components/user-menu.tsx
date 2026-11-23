import { Button } from '@/components/ui/button'

import { useCurrentUser, useLogout } from '../api'

export function UserMenu() {
  const user = useCurrentUser()
  const logout = useLogout()

  return (
    <div className="flex items-center gap-3">
      <span className="text-muted-foreground text-sm">{user.name}</span>
      <Button
        variant="outline"
        size="sm"
        disabled={logout.isPending}
        onClick={() => logout.mutate()}
      >
        Sign out
      </Button>
    </div>
  )
}
