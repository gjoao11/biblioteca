import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  createMemoryHistory,
  createRootRoute,
  createRouter,
  RouterProvider,
} from '@tanstack/react-router'
import { act, render } from '@testing-library/react'
import type { ReactElement } from 'react'

export function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
}

export function renderWithQueryClient(ui: ReactElement) {
  const queryClient = createTestQueryClient()
  return {
    queryClient,
    ...render(
      <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>,
    ),
  }
}

interface RenderWithRouterOptions {
  // Caminho inicial, com search params (ex.: '/all-books?page=2').
  path?: string
  // Para preencher o cache antes de renderizar (ex.: o usuário atual).
  queryClient?: QueryClient
}

// Router mínimo em memória: só a rota raiz, que renderiza `ui` em qualquer
// caminho. Basta para <Link> gerar href e saber se está ativo.
export async function renderWithRouter(
  ui: ReactElement,
  {
    path = '/',
    queryClient = createTestQueryClient(),
  }: RenderWithRouterOptions = {},
) {
  const rootRoute = createRootRoute({ component: () => ui })
  const router = createRouter({
    routeTree: rootRoute,
    history: createMemoryHistory({ initialEntries: [path] }),
  })
  await act(() => router.load())
  return {
    queryClient,
    router,
    ...render(
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>,
    ),
  }
}
