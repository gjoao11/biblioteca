import { useSuspenseQuery } from '@tanstack/react-query'
import type { ReactNode } from 'react'

import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'

import { copyQueryOptions } from '../api'

interface CopyDetailPageProps {
  copyId: number
  // Espaço para o empréstimo atual (quem está com o exemplar, P06). Esse dado
  // é do domínio loan, que o catalog não pode importar: a rota compõe.
  children?: ReactNode
}

export function CopyDetailPage({ copyId, children }: CopyDetailPageProps) {
  // O loader da rota já garantiu o exemplar (ou mostrou o erro, ex.: 404).
  const { data: copy } = useSuspenseQuery(copyQueryOptions(copyId))
  const onLoan = copy.status === 'LOANED'

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <div className="flex items-center gap-3">
        <h1 className="text-2xl font-bold">Copy #{copy.id}</h1>
        <Badge variant={onLoan ? 'outline' : 'secondary'}>
          {onLoan ? 'On loan' : 'Available'}
        </Badge>
      </div>
      <Card>
        <CardContent>
          <dl className="grid gap-x-6 gap-y-3 sm:grid-cols-[8rem_1fr]">
            <dt className="text-muted-foreground">Title</dt>
            {/* Texto via JSX: o React escapa o HTML do título (D9-07). */}
            <dd className="font-medium">{copy.title}</dd>
            <dt className="text-muted-foreground">Author</dt>
            <dd>{copy.author}</dd>
            <dt className="text-muted-foreground">Category</dt>
            <dd>{copy.categoryName}</dd>
          </dl>
        </CardContent>
      </Card>
      {children}
    </div>
  )
}
