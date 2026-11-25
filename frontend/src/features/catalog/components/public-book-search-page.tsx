import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'

import { ErrorMessage } from '@/components/error-message'
import { RoutePending } from '@/components/layout/route-pending'
import { PaginationNav } from '@/components/pagination-nav'
import { SearchInput } from '@/components/search-input'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '@/components/ui/empty'
import { cn } from '@/lib/utils'

import { publicBooksQueryOptions } from '../api'
import type { BookSearch } from '../schemas'
import { PublicBooksTable } from './public-books-table'

interface PublicBookSearchPageProps {
  search: BookSearch
  onSearchChange: (search: BookSearch) => void
}

// Tela pública (P05): fora do _auth, sem menu e sem chamar /api/me.
export function PublicBookSearchPage({
  search,
  onSearchChange,
}: PublicBookSearchPageProps) {
  const { q, page } = search
  const books = useQuery({
    ...publicBooksQueryOptions({ q, page }),
    // Sem texto não busca: a API devolveria o acervo inteiro.
    enabled: Boolean(q),
    // Mantém a lista anterior enquanto a nova chega, sem piscar.
    placeholderData: keepPreviousData,
  })

  return (
    <div className="mx-auto flex min-h-svh max-w-3xl flex-col gap-6 p-4">
      <header className="flex items-center justify-between">
        <span className="font-semibold">Library</span>
        <Link
          to="/sign-in"
          className="text-muted-foreground hover:text-foreground text-sm"
        >
          Librarian sign in
        </Link>
      </header>
      <div className="flex flex-col gap-1">
        {/* D9-12: o legado mostrava "Student Registration Form" aqui. */}
        <h1 className="text-2xl font-bold">Search books</h1>
        <p className="text-muted-foreground">
          Find a book by part of its title or author.
        </p>
      </div>
      <SearchInput
        label="Title or author"
        defaultValue={q}
        onSearch={(value) =>
          // Busca nova volta para a primeira página.
          onSearchChange({ q: value || undefined, page: undefined })
        }
      />
      {!q ? (
        <Empty className="border">
          <EmptyHeader>
            <EmptyTitle>Start typing to search</EmptyTitle>
            <EmptyDescription>
              Results show up as soon as you stop typing.
            </EmptyDescription>
          </EmptyHeader>
        </Empty>
      ) : books.isPending ? (
        <RoutePending />
      ) : books.isError ? (
        <ErrorMessage error={books.error} onRetry={() => books.refetch()} />
      ) : books.data.content.length === 0 ? (
        <Empty className="border">
          <EmptyHeader>
            <EmptyTitle>No books found</EmptyTitle>
            <EmptyDescription>Try another title or author.</EmptyDescription>
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
          <PublicBooksTable books={books.data.content} />
          <PaginationNav page={page ?? 1} totalPages={books.data.totalPages} />
        </div>
      )}
    </div>
  )
}
