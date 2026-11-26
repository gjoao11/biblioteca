import type { QueryClient } from '@tanstack/react-query'

import { ApiError } from '@/lib/api/errors'

import { identityKeys } from './api'

/**
 * Ligado ao onError do QueryCache e do MutationCache: um 401 de quem estava logado
 * significa que a sessão expirou ou que o usuário foi desativado.
 */
export async function handleSessionExpiry(
  error: unknown,
  queryClient: QueryClient,
  redirectToSignIn: () => Promise<void> | void,
) {
  if (!(error instanceof ApiError) || error.status !== 401) return
  // Sem usuário no cache, o 401 é esperado: visitante, senha errada no login ou
  // outra requisição que já tratou a expiração.
  if (queryClient.getQueryData(identityKeys.me) === undefined) return

  // Mesma ordem do logout: limpar tudo antes de navegar faria as queries da
  // tela atual buscarem de novo.
  queryClient.removeQueries({ queryKey: identityKeys.me })
  await redirectToSignIn()
  queryClient.clear()
}
