import { createFileRoute } from '@tanstack/react-router'

import {
  bookCategoriesQueryOptions,
  booksQueryOptions,
} from '@/features/catalog/api'
import { AllBooksPage } from '@/features/catalog/components/all-books-page'
import { allBooksSearchSchema } from '@/features/catalog/schemas'

export const Route = createFileRoute('/_auth/all-books')({
  validateSearch: allBooksSearchSchema,
  loaderDeps: ({ search }) => search,
  loader: async ({ context, deps }) => {
    // Adianta a lista sem esperar; a tela trata carregando e erro dela.
    void context.queryClient.prefetchQuery(booksQueryOptions(deps))
    await context.queryClient.ensureQueryData(bookCategoriesQueryOptions)
  },
  component: AllBooksRoute,
})

function AllBooksRoute() {
  const search = Route.useSearch()
  const navigate = Route.useNavigate()
  return (
    <AllBooksPage
      search={search}
      onSearchChange={(next) => navigate({ search: next })}
    />
  )
}
