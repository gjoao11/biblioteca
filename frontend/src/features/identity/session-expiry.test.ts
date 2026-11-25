import { MutationCache, QueryCache, QueryClient } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'

import { ApiError } from '@/lib/api/errors'

import { identityKeys } from './api'
import { handleSessionExpiry } from './session-expiry'

const signedInUser = { username: 'ana', name: 'Ana', role: 'LIBRARIAN' }

function setup({ signedIn }: { signedIn: boolean }) {
  const redirectToSignIn = vi.fn()
  const onError = (error: unknown) =>
    handleSessionExpiry(error, queryClient, redirectToSignIn)
  const queryClient: QueryClient = new QueryClient({
    queryCache: new QueryCache({ onError }),
    mutationCache: new MutationCache({ onError }),
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  if (signedIn) queryClient.setQueryData(identityKeys.me, signedInUser)
  queryClient.setQueryData(['books'], ['cached book'])
  return { queryClient, redirectToSignIn }
}

function failingQuery(queryClient: QueryClient, status: number) {
  return queryClient
    .fetchQuery({
      queryKey: ['failing', status],
      queryFn: () => Promise.reject(new ApiError({ status })),
    })
    .catch(() => {})
}

describe('handleSessionExpiry', () => {
  it('redirects to sign-in and clears the cache when a signed-in user gets a 401', async () => {
    const { queryClient, redirectToSignIn } = setup({ signedIn: true })

    await failingQuery(queryClient, 401)

    await vi.waitFor(() => expect(redirectToSignIn).toHaveBeenCalledOnce())
    await vi.waitFor(() =>
      expect(queryClient.getQueryCache().getAll()).toEqual([]),
    )
  })

  it('also reacts to a 401 from a mutation', async () => {
    const { queryClient, redirectToSignIn } = setup({ signedIn: true })

    await queryClient
      .getMutationCache()
      .build(queryClient, {
        mutationFn: () => Promise.reject(new ApiError({ status: 401 })),
      })
      .execute(undefined)
      .catch(() => {})

    await vi.waitFor(() => expect(redirectToSignIn).toHaveBeenCalledOnce())
  })

  it('redirects only once when several requests fail together', async () => {
    const { queryClient, redirectToSignIn } = setup({ signedIn: true })

    await Promise.all([
      failingQuery(queryClient, 401),
      queryClient
        .fetchQuery({
          queryKey: ['other'],
          queryFn: () => Promise.reject(new ApiError({ status: 401 })),
        })
        .catch(() => {}),
    ])

    await vi.waitFor(() => expect(redirectToSignIn).toHaveBeenCalledOnce())
  })

  it('ignores a 401 when nobody is signed in (visitor or wrong password)', async () => {
    const { queryClient, redirectToSignIn } = setup({ signedIn: false })

    await failingQuery(queryClient, 401)

    expect(redirectToSignIn).not.toHaveBeenCalled()
    expect(queryClient.getQueryData(['books'])).toEqual(['cached book'])
  })

  it.each([403, 404, 500])('ignores a %i', async (status) => {
    const { queryClient, redirectToSignIn } = setup({ signedIn: true })

    await failingQuery(queryClient, status)

    expect(redirectToSignIn).not.toHaveBeenCalled()
    expect(queryClient.getQueryData(identityKeys.me)).toEqual(signedInUser)
  })
})
