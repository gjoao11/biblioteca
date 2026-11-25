import { createFileRoute } from '@tanstack/react-router'

import { publicBooksQueryOptions } from '@/features/catalog/api'
import { PublicBookSearchPage } from '@/features/catalog/components/public-book-search-page'
import { bookSearchSchema } from '@/features/catalog/schemas'

// Fora do _auth: visitante busca sem login (P05).
export const Route = createFileRoute('/book')({
  validateSearch: bookSearchSchema,
  loaderDeps: ({ search }) => search,
  // Só adianta a busca, sem esperar: a tela trata carregando e erro.
  loader: ({ context, deps }) => {
    if (deps.q)
      void context.queryClient.prefetchQuery(publicBooksQueryOptions(deps))
  },
  component: BookRoute,
})

function BookRoute() {
  const search = Route.useSearch()
  const navigate = Route.useNavigate()
  return (
    <PublicBookSearchPage
      search={search}
      // replace: digitar não enche o histórico do navegador.
      onSearchChange={(next) => navigate({ search: next, replace: true })}
    />
  )
}
