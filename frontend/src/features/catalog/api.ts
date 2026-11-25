import {
  queryOptions,
  useMutation,
  useQueryClient,
} from '@tanstack/react-query'

import { api } from '@/lib/api/client'
import { unwrap } from '@/lib/api/errors'
import type { components } from '@/lib/api/schema'

export type BookSummary = components['schemas']['BookSummaryResponse']
export type BookPage = components['schemas']['BookPageResponse']
export type PublicBook = components['schemas']['PublicBookResponse']
export type PublicBookPage = components['schemas']['PublicBookPageResponse']
export type BookCategory = components['schemas']['BookCategoryResponse']
export type Copy = components['schemas']['CopyResponse']
export type CreateBookRequest = components['schemas']['CreateBookRequest']
export type CreatedBook = components['schemas']['CreatedBookResponse']

// `page` aqui é o da URL, a partir de 1.
export interface BookFilters {
  q?: string
  category?: number
  page?: number
}

export const bookKeys = {
  all: ['books'] as const,
  list: (filters: BookFilters) => [...bookKeys.all, 'list', filters] as const,
  publicList: (filters: Pick<BookFilters, 'q' | 'page'>) =>
    [...bookKeys.all, 'public', filters] as const,
  categories: ['book-categories'] as const,
  copy: (copyId: number) => ['copies', copyId] as const,
}

// A URL conta as páginas a partir de 1 (mais legível); a API, a partir de 0.
function toApiPage(page = 1) {
  return page - 1
}

export function booksQueryOptions(filters: BookFilters) {
  return queryOptions({
    queryKey: bookKeys.list(filters),
    queryFn: ({ signal }) =>
      unwrap(
        api.GET('/api/books', {
          params: {
            query: {
              q: filters.q,
              category: filters.category,
              page: toApiPage(filters.page),
            },
          },
          signal,
        }),
      ),
  })
}

export function publicBooksQueryOptions(
  filters: Pick<BookFilters, 'q' | 'page'>,
) {
  return queryOptions({
    queryKey: bookKeys.publicList(filters),
    queryFn: ({ signal }) =>
      unwrap(
        api.GET('/api/public/books', {
          params: { query: { q: filters.q, page: toApiPage(filters.page) } },
          signal,
        }),
      ),
  })
}

// Categorias quase nunca mudam: ficam 5 minutos sem buscar de novo.
export const bookCategoriesQueryOptions = queryOptions({
  queryKey: bookKeys.categories,
  queryFn: ({ signal }) => unwrap(api.GET('/api/book-categories', { signal })),
  staleTime: 5 * 60 * 1000,
})

export function copyQueryOptions(copyId: number) {
  return queryOptions({
    queryKey: bookKeys.copy(copyId),
    queryFn: ({ signal }) =>
      unwrap(
        api.GET('/api/copies/{copyId}', {
          params: { path: { copyId } },
          signal,
        }),
      ),
  })
}

export function useCreateBook() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateBookRequest) =>
      unwrap(api.POST('/api/books', { body })),
    // As listas mudam (novo livro, novos totais); a categoria não.
    onSuccess: () => queryClient.invalidateQueries({ queryKey: bookKeys.all }),
  })
}
