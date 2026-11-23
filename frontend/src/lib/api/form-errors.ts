import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'

import { ApiError } from './errors'

interface FieldProblem {
  field: string
  message: string
}

function isFieldProblem(value: unknown): value is FieldProblem {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as FieldProblem).field === 'string' &&
    typeof (value as FieldProblem).message === 'string'
  )
}

// Erros sem campo conhecido viram erro geral (root).
export function setServerErrors<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
  fields: readonly Path<T>[],
  fallback = 'Something went wrong. Please try again.',
) {
  if (!(error instanceof ApiError)) {
    setError('root', { message: fallback })
    return
  }
  const problems = Array.isArray(error.problem.errors)
    ? error.problem.errors.filter(isFieldProblem)
    : []
  const fieldProblems = problems.filter((problem) =>
    (fields as readonly string[]).includes(problem.field),
  )
  for (const problem of fieldProblems) {
    setError(problem.field as Path<T>, { message: problem.message })
  }
  if (fieldProblems.length === 0) {
    setError('root', { message: error.message })
  }
}
