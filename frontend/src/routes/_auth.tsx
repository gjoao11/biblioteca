import { createFileRoute, redirect } from '@tanstack/react-router'

import { AppLayout } from '@/components/layout/app-layout'
import { fetchCurrentUser } from '@/features/identity/api'

export const Route = createFileRoute('/_auth')({
  beforeLoad: async ({ context, location }) => {
    const user = await fetchCurrentUser(context.queryClient)
    if (!user) {
      throw redirect({ to: '/sign-in', search: { redirect: location.href } })
    }
  },
  component: AppLayout,
})
