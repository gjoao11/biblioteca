import { keepPreviousData, useQuery } from '@tanstack/react-query'

import { IdLookupForm } from '@/components/id-lookup-form'
import { SearchInput } from '@/components/search-input'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import { booksQueryOptions } from '../api'
import type { BookSearch } from '../schemas'
import { BookResults } from './book-results'

interface HomePageProps {
  search: BookSearch
  onSearchChange: (search: BookSearch) => void
  // Exemplar e aluno só navegam para o detalhe: a home não busca dados de
  // aluno, então o catalog não depende do student.
  onOpenCopy: (copyId: number) => void
  onOpenStudent: (studentId: number) => void
}

// As três buscas do bibliotecário (P14): livro, exemplar e aluno.
export function HomePage({
  search,
  onSearchChange,
  onOpenCopy,
  onOpenStudent,
}: HomePageProps) {
  const { q, page } = search
  const books = useQuery({
    ...booksQueryOptions({ q, page }),
    enabled: Boolean(q),
    placeholderData: keepPreviousData,
  })

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6">
      <h1 className="text-2xl font-bold">Library Management System</h1>
      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Find copy</CardTitle>
            <CardDescription>
              See the book of a copy and whether it is on loan.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <IdLookupForm
              label="Copy code"
              submitLabel="Open copy"
              onSubmit={onOpenCopy}
            />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Find student</CardTitle>
            <CardDescription>
              See a student and the books they have.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <IdLookupForm
              label="Student ID"
              submitLabel="Open student"
              onSubmit={onOpenStudent}
            />
          </CardContent>
        </Card>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Find book</CardTitle>
          <CardDescription>
            Search by part of the title or author.
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <SearchInput
            label="Title or author"
            defaultValue={q}
            onSearch={(value) =>
              onSearchChange({ q: value || undefined, page: undefined })
            }
          />
          {q && (
            <BookResults
              books={books}
              page={page}
              emptyTitle="No books found"
              emptyDescription="Try another title or author."
            />
          )}
        </CardContent>
      </Card>
    </div>
  )
}
