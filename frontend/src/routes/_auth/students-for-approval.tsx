import { createFileRoute } from '@tanstack/react-router'

import { PagePlaceholder } from '@/components/layout/page-placeholder'

export const Route = createFileRoute('/_auth/students-for-approval')({
  component: StudentsForApprovalRoute,
})

function StudentsForApprovalRoute() {
  return <PagePlaceholder title="Students for approval" />
}
