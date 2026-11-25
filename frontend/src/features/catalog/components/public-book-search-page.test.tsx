import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { http, problem } from '@/test/http'
import { renderWithRouter } from '@/test/render'
import { server } from '@/test/server'

import type { PublicBook } from '../api'
import type { BookSearch } from '../schemas'
import { PublicBookSearchPage } from './public-book-search-page'

const hobbit: PublicBook = {
  title: 'The Hobbit',
  author: 'J. R. R. Tolkien',
  available: true,
}

function page(content: PublicBook[], totalPages = 1) {
  return {
    content,
    page: 0,
    size: 20,
    totalElements: content.length,
    totalPages,
  }
}

function renderPage(search: BookSearch = {}, onSearchChange = vi.fn()) {
  return renderWithRouter(
    <PublicBookSearchPage search={search} onSearchChange={onSearchChange} />,
    { path: '/book' },
  )
}

describe('PublicBookSearchPage', () => {
  it('D9-12 has its own title', async () => {
    await renderPage()

    expect(
      screen.getByRole('heading', { level: 1, name: 'Search books' }),
    ).toBeInTheDocument()
  })

  it('P05 lists books for visitors without asking who is signed in', async () => {
    // Só a busca pública tem handler: uma chamada a /api/me faria o teste falhar.
    server.use(
      http.get('/api/public/books', ({ response }) =>
        response(200).json(page([hobbit])),
      ),
    )
    await renderPage({ q: 'hobbit' })

    expect(
      await screen.findByRole('cell', { name: 'The Hobbit' }),
    ).toBeVisible()
    expect(screen.getByText('Available')).toBeInTheDocument()
  })

  it('P04 asks the API for the typed title or author', async () => {
    let query: URLSearchParams | undefined
    server.use(
      http.get('/api/public/books', ({ request, response }) => {
        query = new URL(request.url).searchParams
        return response(200).json(page([hobbit]))
      }),
    )
    await renderPage({ q: 'tolk', page: 2 })

    await waitFor(() => expect(query).toBeDefined())
    expect(query?.get('q')).toBe('tolk')
    // A URL conta a partir de 1; a API, a partir de 0.
    expect(query?.get('page')).toBe('1')
  })

  it('starts a new search on the first page', async () => {
    server.use(
      http.get('/api/public/books', ({ response }) =>
        response(200).json(page([hobbit], 5)),
      ),
    )
    const onSearchChange = vi.fn()
    await renderPage({ q: 'hobbit', page: 3 }, onSearchChange)
    const input = screen.getByRole('searchbox', { name: 'Title or author' })

    await userEvent.clear(input)
    await userEvent.type(input, 'tolkien')

    await waitFor(() =>
      expect(onSearchChange).toHaveBeenCalledWith({
        q: 'tolkien',
        page: undefined,
      }),
    )
  })

  it('waits for some text before searching', async () => {
    let requests = 0
    server.use(
      http.get('/api/public/books', ({ response }) => {
        requests += 1
        return response(200).json(page([]))
      }),
    )
    await renderPage()

    expect(screen.getByText('Start typing to search')).toBeInTheDocument()
    // Dá tempo para uma requisição indevida chegar ao handler.
    await new Promise((resolve) => setTimeout(resolve, 50))
    expect(requests).toBe(0)
  })

  it('says when nothing is found', async () => {
    server.use(
      http.get('/api/public/books', ({ response }) =>
        response(200).json(page([])),
      ),
    )
    await renderPage({ q: 'zzz' })

    expect(await screen.findByText('No books found')).toBeInTheDocument()
  })

  it('shows the API error', async () => {
    server.use(
      http.get('/api/public/books', ({ response }) =>
        response.untyped(
          problem({ status: 400, detail: 'Search is too long' }),
        ),
      ),
    )
    await renderPage({ q: 'x' })

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Search is too long',
    )
  })

  it('D9-07 renders a book title with HTML as plain text', async () => {
    const title = '<img src=x onerror="alert(1)">'
    server.use(
      http.get('/api/public/books', ({ response }) =>
        response(200).json(page([{ ...hobbit, title }])),
      ),
    )
    const { container } = await renderPage({ q: 'img' })

    expect(await screen.findByRole('cell', { name: title })).toBeVisible()
    expect(container.querySelector('img')).toBeNull()
  })
})
