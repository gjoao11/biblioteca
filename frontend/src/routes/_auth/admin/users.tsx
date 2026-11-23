import { createFileRoute } from '@tanstack/react-router'

import { usersQueryOptions } from '@/features/identity/api'
import { UsersPage } from '@/features/identity/components/users-page'

export const Route = createFileRoute('/_auth/admin/users')({
  loader: ({ context }) =>
    context.queryClient.ensureQueryData(usersQueryOptions),
  component: UsersPage,
})
