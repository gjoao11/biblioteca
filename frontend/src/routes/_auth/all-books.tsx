import { createFileRoute } from '@tanstack/react-router'

import { PagePlaceholder } from '@/components/layout/page-placeholder'

export const Route = createFileRoute('/_auth/all-books')({
  component: AllBooksRoute,
})

function AllBooksRoute() {
  return <PagePlaceholder title="All books" />
}
