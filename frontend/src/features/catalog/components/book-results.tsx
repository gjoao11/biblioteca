import type { UseQueryResult } from '@tanstack/react-query'

import { ErrorMessage } from '@/components/error-message'
import { RoutePending } from '@/components/layout/route-pending'
import { PaginationNav } from '@/components/pagination-nav'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '@/components/ui/empty'
import { cn } from '@/lib/utils'

import type { BookPage } from '../api'
import { BooksTable } from './books-table'

interface BookResultsProps {
  // Resultado do useQuery com placeholderData: keepPreviousData.
  books: UseQueryResult<BookPage>
  // Página atual da URL (a partir de 1).
  page?: number
  emptyTitle: string
  emptyDescription: string
}

// Carregando, erro, vazio ou tabela com paginação: os estados de toda lista
// de livros do bibliotecário.
export function BookResults({
  books,
  page,
  emptyTitle,
  emptyDescription,
}: BookResultsProps) {
  if (books.isPending) return <RoutePending />
  if (books.isError) {
    return <ErrorMessage error={books.error} onRetry={() => books.refetch()} />
  }
  if (books.data.content.length === 0) {
    return (
      <Empty className="border">
        <EmptyHeader>
          <EmptyTitle>{emptyTitle}</EmptyTitle>
          <EmptyDescription>{emptyDescription}</EmptyDescription>
        </EmptyHeader>
      </Empty>
    )
  }
  return (
    <div
      // Enquanto a próxima página chega, a anterior fica visível e esmaecida.
      aria-busy={books.isPlaceholderData}
      className={cn(
        'flex flex-col gap-4 transition-opacity',
        books.isPlaceholderData && 'opacity-60',
      )}
    >
      <BooksTable books={books.data.content} />
      <PaginationNav page={page ?? 1} totalPages={books.data.totalPages} />
    </div>
  )
}
