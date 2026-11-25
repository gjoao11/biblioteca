import { createFileRoute } from '@tanstack/react-router'

import { booksQueryOptions } from '@/features/catalog/api'
import { HomePage } from '@/features/catalog/components/home-page'
import { bookSearchSchema } from '@/features/catalog/schemas'

export const Route = createFileRoute('/_auth/')({
  validateSearch: bookSearchSchema,
  loaderDeps: ({ search }) => search,
  // Só adianta a busca, sem esperar: a tela trata carregando e erro.
  loader: ({ context, deps }) => {
    if (deps.q) void context.queryClient.prefetchQuery(booksQueryOptions(deps))
  },
  component: HomeRoute,
})

function HomeRoute() {
  const search = Route.useSearch()
  const navigate = Route.useNavigate()
  return (
    <HomePage
      search={search}
      onSearchChange={(next) => navigate({ search: next, replace: true })}
      onOpenCopy={(copyId) =>
        navigate({ to: '/copies/$copyId', params: { copyId } })
      }
      onOpenStudent={(studentId) =>
        navigate({ to: '/students/$studentId', params: { studentId } })
      }
    />
  )
}
