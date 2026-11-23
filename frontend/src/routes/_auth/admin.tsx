import { createFileRoute, redirect } from '@tanstack/react-router'

import { meQueryOptions } from '@/features/identity/api'

export const Route = createFileRoute('/_auth/admin')({
  beforeLoad: async ({ context }) => {
    const user = await context.queryClient.ensureQueryData(meQueryOptions)
    if (user.role !== 'ADMIN') {
      throw redirect({ to: '/' })
    }
  },
})
