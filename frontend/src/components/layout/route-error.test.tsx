import { QueryClientProvider } from '@tanstack/react-query'
import {
  createMemoryHistory,
  createRootRoute,
  createRouter,
  RouterProvider,
} from '@tanstack/react-router'
import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { ApiError } from '@/lib/api/errors'
import { createTestQueryClient } from '@/test/render'

import { RouteError } from './route-error'

describe('RouteError', () => {
  it('runs the loader again on "Try again"', async () => {
    let attempts = 0
    const rootRoute = createRootRoute({
      loader: () => {
        attempts += 1
        if (attempts === 1) {
          throw new ApiError({ status: 503, detail: 'Service unavailable' })
        }
      },
      component: () => <p>Loaded</p>,
      errorComponent: RouteError,
    })
    const router = createRouter({
      routeTree: rootRoute,
      history: createMemoryHistory({ initialEntries: ['/'] }),
    })
    await act(() => router.load())
    render(
      <QueryClientProvider client={createTestQueryClient()}>
        <RouterProvider router={router} />
      </QueryClientProvider>,
    )

    expect(screen.getByRole('alert')).toHaveTextContent('Service unavailable')
    await userEvent.click(screen.getByRole('button', { name: 'Try again' }))

    expect(await screen.findByText('Loaded')).toBeInTheDocument()
    expect(attempts).toBe(2)
  })
})
