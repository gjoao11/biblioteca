import {
  keepPreviousData,
  useQuery,
  useSuspenseQuery,
} from '@tanstack/react-query'
import { useId } from 'react'

import { ErrorMessage } from '@/components/error-message'
import { RoutePending } from '@/components/layout/route-pending'
import { PaginationNav } from '@/components/pagination-nav'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '@/components/ui/empty'
import { Field, FieldLabel } from '@/components/ui/field'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { cn } from '@/lib/utils'

import { bookCategoriesQueryOptions, booksQueryOptions } from '../api'
import type { AllBooksSearch } from '../schemas'
import { BooksTable } from './books-table'

interface AllBooksPageProps {
  search: AllBooksSearch
  onSearchChange: (search: AllBooksSearch) => void
}

export function AllBooksPage({ search, onSearchChange }: AllBooksPageProps) {
  const { category, page } = search
  const selectId = useId()
  // O loader da rota já garantiu as categorias.
  const { data: categories } = useSuspenseQuery(bookCategoriesQueryOptions)
  const books = useQuery({
    ...booksQueryOptions({ category, page }),
    placeholderData: keepPreviousData,
  })

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6">
      <h1 className="text-2xl font-bold">All books</h1>
      <Field className="max-w-xs">
        <FieldLabel htmlFor={selectId}>Category</FieldLabel>
        <NativeSelect
          id={selectId}
          className="w-full"
          value={category ?? ''}
          onChange={(event) =>
            onSearchChange({
              category: event.target.value
                ? Number(event.target.value)
                : undefined,
              // Filtro novo volta para a primeira página.
              page: undefined,
            })
          }
        >
          {/* O legado não tinha "todas" e abria na primeira categoria. */}
          <NativeSelectOption value="">All categories</NativeSelectOption>
          {categories.map((item) => (
            <NativeSelectOption key={item.id} value={item.id}>
              {item.name}
            </NativeSelectOption>
          ))}
        </NativeSelect>
      </Field>
      {books.isPending ? (
        <RoutePending />
      ) : books.isError ? (
        <ErrorMessage error={books.error} onRetry={() => books.refetch()} />
      ) : books.data.content.length === 0 ? (
        <Empty className="border">
          <EmptyHeader>
            <EmptyTitle>
              {category ? 'No books in this category' : 'No books yet'}
            </EmptyTitle>
            <EmptyDescription>
              New books show up here after they are added.
            </EmptyDescription>
          </EmptyHeader>
        </Empty>
      ) : (
        <div
          aria-busy={books.isPlaceholderData}
          className={cn(
            'flex flex-col gap-4 transition-opacity',
            books.isPlaceholderData && 'opacity-60',
          )}
        >
          <BooksTable books={books.data.content} />
          <PaginationNav page={page ?? 1} totalPages={books.data.totalPages} />
        </div>
      )}
    </div>
  )
}
