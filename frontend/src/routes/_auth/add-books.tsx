import { createFileRoute } from '@tanstack/react-router'

import { PagePlaceholder } from '@/components/layout/page-placeholder'

export const Route = createFileRoute('/_auth/add-books')({
  component: AddBooksRoute,
})

function AddBooksRoute() {
  return <PagePlaceholder title="Add books" />
}
