import { createFileRoute } from '@tanstack/react-router'

import { PagePlaceholder } from '@/components/layout/page-placeholder'

export const Route = createFileRoute('/_auth/currently-issued')({
  component: CurrentlyIssuedRoute,
})

function CurrentlyIssuedRoute() {
  return <PagePlaceholder title="Currently issued books" />
}
