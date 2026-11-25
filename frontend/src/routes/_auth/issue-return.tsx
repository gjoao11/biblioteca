import { createFileRoute } from '@tanstack/react-router'

import { PagePlaceholder } from '@/components/layout/page-placeholder'

export const Route = createFileRoute('/_auth/issue-return')({
  component: IssueReturnRoute,
})

function IssueReturnRoute() {
  return <PagePlaceholder title="Issue / Return books" />
}
