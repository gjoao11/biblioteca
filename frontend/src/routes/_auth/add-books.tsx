import { createFileRoute } from '@tanstack/react-router'

import { bookCategoriesQueryOptions } from '@/features/catalog/api'
import { AddBooksPage } from '@/features/catalog/components/add-books-page'

export const Route = createFileRoute('/_auth/add-books')({
  loader: ({ context }) =>
    context.queryClient.ensureQueryData(bookCategoriesQueryOptions),
  component: AddBooksPage,
})
