import { createFileRoute } from '@tanstack/react-router'

import { PagePlaceholder } from '@/components/layout/page-placeholder'

export const Route = createFileRoute('/_auth/registered-students')({
  component: RegisteredStudentsRoute,
})

function RegisteredStudentsRoute() {
  return <PagePlaceholder title="Registered students" />
}
