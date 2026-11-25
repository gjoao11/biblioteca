import './index.css'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createRouter, RouterProvider } from '@tanstack/react-router'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import { NotFoundPage } from './components/layout/not-found-page.tsx'
import { RouteError } from './components/layout/route-error.tsx'
import { RoutePending } from './components/layout/route-pending.tsx'
import { ApiError } from './lib/api/errors.ts'
import { routeTree } from './routeTree.gen.ts'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) =>
        !(error instanceof ApiError && error.status < 500) && failureCount < 3,
    },
  },
})
const router = createRouter({
  routeTree,
  context: { queryClient },
  defaultErrorComponent: RouteError,
  defaultPendingComponent: RoutePending,
  defaultNotFoundComponent: NotFoundPage,
})

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
)
