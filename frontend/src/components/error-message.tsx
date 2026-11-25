import { CircleAlertIcon } from 'lucide-react'

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { ApiError } from '@/lib/api/errors'

interface ErrorMessageProps {
  error: unknown
  // Sem onRetry, o botão "Try again" não aparece.
  onRetry?: () => void
}

// Só o ApiError traz texto pensado para o usuário (o detail do Problem
// Details). Outros erros (rede, bug) podem ter mensagens técnicas.
function describe(error: unknown) {
  return error instanceof ApiError ? error.message : 'Please try again.'
}

export function ErrorMessage({ error, onRetry }: ErrorMessageProps) {
  return (
    <Alert variant="destructive">
      <CircleAlertIcon aria-hidden />
      <AlertTitle>Something went wrong</AlertTitle>
      <AlertDescription>
        <p>{describe(error)}</p>
        {onRetry && (
          <Button variant="outline" size="sm" onClick={onRetry}>
            Try again
          </Button>
        )}
      </AlertDescription>
    </Alert>
  )
}
