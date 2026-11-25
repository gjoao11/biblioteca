import { createFileRoute, notFound } from '@tanstack/react-router'

import { copyQueryOptions } from '@/features/catalog/api'
import { CopyDetailPage } from '@/features/catalog/components/copy-detail-page'

export const Route = createFileRoute('/_auth/copies/$copyId')({
  params: {
    parse: ({ copyId }) => ({ copyId: Number(copyId) }),
    stringify: ({ copyId }) => ({ copyId: String(copyId) }),
  },
  loader: ({ context, params }) => {
    // /copies/abc ou /copies/0 não chegam a chamar a API.
    if (!Number.isInteger(params.copyId) || params.copyId < 1) throw notFound()
    return context.queryClient.ensureQueryData(copyQueryOptions(params.copyId))
  },
  component: CopyRoute,
})

function CopyRoute() {
  const { copyId } = Route.useParams()
  return <CopyDetailPage copyId={copyId} />
}
