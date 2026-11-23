import {
  type QueryClient,
  queryOptions,
  useMutation,
  useQueryClient,
  useSuspenseQuery,
} from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'

import { api } from '@/lib/api/client'
import { ApiError, unwrap } from '@/lib/api/errors'
import type { components } from '@/lib/api/schema'

export type CurrentUser = components['schemas']['CurrentUserResponse']
export type LoginRequest = components['schemas']['LoginRequest']

export const identityKeys = {
  me: ['me'] as const,
}

// Só muda no login e no logout, que atualizam o cache.
export const meQueryOptions = queryOptions({
  queryKey: identityKeys.me,
  queryFn: ({ signal }) => unwrap(api.GET('/api/me', { signal })),
  staleTime: Infinity,
})

export async function fetchCurrentUser(
  queryClient: QueryClient,
): Promise<CurrentUser | null> {
  try {
    return await queryClient.ensureQueryData(meQueryOptions)
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) return null
    throw error
  }
}

// Só dentro do layout _auth, que garante o usuário antes de renderizar.
export function useCurrentUser() {
  return useSuspenseQuery(meQueryOptions).data
}

export function useLogin() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: LoginRequest) =>
      unwrap(api.POST('/api/auth/login', { body })),
    onSuccess: (user) => {
      queryClient.clear()
      queryClient.setQueryData(identityKeys.me, user)
    },
  })
}

export function useLogout() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  return useMutation({
    mutationFn: () => unwrap(api.POST('/api/auth/logout')),
    onSuccess: async () => {
      // Antes de navegar, senão o /sign-in acharia o usuário no cache e voltaria para /.
      queryClient.removeQueries({ queryKey: identityKeys.me })
      await navigate({ to: '/sign-in' })
      queryClient.clear()
    },
  })
}

export type User = components['schemas']['UserResponse']
export type CreateLibrarianRequest =
  components['schemas']['CreateLibrarianRequest']
export type UpdateUserRequest = components['schemas']['UpdateUserRequest']

export const userKeys = {
  all: ['users'] as const,
}

export const usersQueryOptions = queryOptions({
  queryKey: userKeys.all,
  queryFn: ({ signal }) => unwrap(api.GET('/api/admin/users', { signal })),
})

export function useCreateLibrarian() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateLibrarianRequest) =>
      unwrap(api.POST('/api/admin/users', { body })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: userKeys.all }),
  })
}

export function useUpdateUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, ...body }: UpdateUserRequest & { id: number }) =>
      unwrap(
        api.PUT('/api/admin/users/{id}', { params: { path: { id } }, body }),
      ),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: userKeys.all }),
  })
}
