import { createOpenApiHttp } from 'openapi-msw'

import type { ProblemDetail } from '@/lib/api/errors'
import type { paths } from '@/lib/api/schema'

export const http = createOpenApiHttp<paths>()

// Resposta de erro no formato Problem Details do backend. O contrato só
// documenta os status de sucesso, então o erro vai por response.untyped(...).
export function problem(body: ProblemDetail) {
  return Response.json(body, {
    status: body.status,
    headers: { 'Content-Type': 'application/problem+json' },
  })
}
