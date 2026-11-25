import {
  keepPreviousData,
  useQuery,
  useSuspenseQuery,
} from '@tanstack/react-query'
import { useId } from 'react'

import { Field, FieldLabel } from '@/components/ui/field'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'

import { bookCategoriesQueryOptions, booksQueryOptions } from '../api'
import type { AllBooksSearch } from '../schemas'
import { BookResults } from './book-results'

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
      <BookResults
        books={books}
        page={page}
        emptyTitle={category ? 'No books in this category' : 'No books yet'}
        emptyDescription="New books show up here after they are added."
      />
    </div>
  )
}
