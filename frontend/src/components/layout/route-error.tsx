import { useQueryErrorResetBoundary } from '@tanstack/react-query'
import { type ErrorComponentProps, useRouter } from '@tanstack/react-router'
import { useEffect } from 'react'

import { ErrorMessage } from '@/components/error-message'

// Erro de loader ou de useSuspenseQuery numa rota. "Try again" roda os
// loaders de novo; o reset libera as queries que falharam para buscar outra vez.
export function RouteError({ error }: ErrorComponentProps) {
  const router = useRouter()
  const queryErrorResetBoundary = useQueryErrorResetBoundary()

  useEffect(() => {
    queryErrorResetBoundary.reset()
  }, [queryErrorResetBoundary])

  return <ErrorMessage error={error} onRetry={() => router.invalidate()} />
}
