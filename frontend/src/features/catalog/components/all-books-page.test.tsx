import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { http, problem } from '@/test/http'
import { createTestQueryClient, renderWithRouter } from '@/test/render'
import { server } from '@/test/server'

import { type BookCategory, bookKeys, type BookSummary } from '../api'
import type { AllBooksSearch } from '../schemas'
import { AllBooksPage } from './all-books-page'

const categories: BookCategory[] = [
  { id: 1, name: 'Computer Science' },
  { id: 2, name: 'Fantasy' },
]

const hobbit: BookSummary = {
  id: 1,
  title: 'The Hobbit',
  author: 'J. R. R. Tolkien',
  categoryId: 2,
  categoryName: 'Fantasy',
  totalCopies: 3,
  availableCopies: 1,
}

function page(content: BookSummary[], totalPages = 1) {
  return {
    content,
    page: 0,
    size: 20,
    totalElements: content.length,
    totalPages,
  }
}

function renderPage(search: AllBooksSearch = {}, onSearchChange = vi.fn()) {
  // Como o loader da rota: as categorias já estão no cache.
  const queryClient = createTestQueryClient()
  queryClient.setQueryData(bookKeys.categories, categories)
  return renderWithRouter(
    <AllBooksPage search={search} onSearchChange={onSearchChange} />,
    { path: '/all-books', queryClient },
  )
}

describe('AllBooksPage', () => {
  it('P03 lists the books of the chosen category', async () => {
    let query: URLSearchParams | undefined
    server.use(
      http.get('/api/books', ({ request, response }) => {
        query = new URL(request.url).searchParams
        return response(200).json(page([hobbit], 3))
      }),
    )
    await renderPage({ category: 2, page: 3 })

    expect(
      await screen.findByRole('cell', { name: 'The Hobbit' }),
    ).toBeVisible()
    expect(screen.getByRole('combobox', { name: 'Category' })).toHaveValue('2')
    expect(query?.get('category')).toBe('2')
    expect(query?.get('page')).toBe('2')
  })

  it('offers every category and goes back to the first page on change', async () => {
    server.use(
      http.get('/api/books', ({ response }) =>
        response(200).json(page([hobbit], 3)),
      ),
    )
    const onSearchChange = vi.fn()
    await renderPage({ category: 2, page: 3 }, onSearchChange)

    await userEvent.selectOptions(
      screen.getByRole('combobox', { name: 'Category' }),
      'All categories',
    )
    expect(onSearchChange).toHaveBeenLastCalledWith({
      category: undefined,
      page: undefined,
    })

    await userEvent.selectOptions(
      screen.getByRole('combobox', { name: 'Category' }),
      'Computer Science',
    )
    expect(onSearchChange).toHaveBeenLastCalledWith({
      category: 1,
      page: undefined,
    })
  })

  it('says when the category has no books', async () => {
    server.use(
      http.get('/api/books', ({ response }) => response(200).json(page([]))),
    )
    await renderPage({ category: 1 })

    expect(
      await screen.findByText('No books in this category'),
    ).toBeInTheDocument()
  })

  it('shows the API error and retries', async () => {
    let attempts = 0
    server.use(
      http.get('/api/books', ({ response }) => {
        attempts += 1
        return attempts === 1
          ? response.untyped(problem({ status: 503, detail: 'Try later' }))
          : response(200).json(page([hobbit]))
      }),
    )
    await renderPage()

    expect(await screen.findByRole('alert')).toHaveTextContent('Try later')
    await userEvent.click(screen.getByRole('button', { name: 'Try again' }))

    await waitFor(() =>
      expect(screen.getByRole('cell', { name: 'The Hobbit' })).toBeVisible(),
    )
  })
})
