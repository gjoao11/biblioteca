import { createFileRoute, notFound } from '@tanstack/react-router'

import { PagePlaceholder } from '@/components/layout/page-placeholder'

// Provisória: a tela é do J7 (P13). A home já navega para cá pela busca de aluno.
export const Route = createFileRoute('/_auth/students/$studentId')({
  params: {
    parse: ({ studentId }) => ({ studentId: Number(studentId) }),
    stringify: ({ studentId }) => ({ studentId: String(studentId) }),
  },
  loader: ({ params }) => {
    if (!Number.isInteger(params.studentId) || params.studentId < 1) {
      throw notFound()
    }
  },
  component: StudentRoute,
})

function StudentRoute() {
  const { studentId } = Route.useParams()
  return <PagePlaceholder title={`Student #${studentId}`} />
}
