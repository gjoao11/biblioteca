import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { http } from '@/test/http'
import { renderWithRouter } from '@/test/render'
import { server } from '@/test/server'

import type { BookSummary } from '../api'
import type { BookSearch } from '../schemas'
import { HomePage } from './home-page'

const hobbit: BookSummary = {
  id: 1,
  title: 'The Hobbit',
  author: 'J. R. R. Tolkien',
  categoryId: 2,
  categoryName: 'Fantasy',
  totalCopies: 3,
  availableCopies: 1,
}

function renderHome(search: BookSearch = {}) {
  const handlers = {
    onSearchChange: vi.fn(),
    onOpenCopy: vi.fn(),
    onOpenStudent: vi.fn(),
  }
  return {
    handlers,
    rendered: renderWithRouter(<HomePage search={search} {...handlers} />),
  }
}

describe('HomePage', () => {
  it('P14 offers book, copy and student searches', async () => {
    await renderHome().rendered

    expect(
      screen.getByRole('searchbox', { name: 'Title or author' }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('textbox', { name: 'Copy code' }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('textbox', { name: 'Student ID' }),
    ).toBeInTheDocument()
  })

  it('P14 opens the copy and the student by their codes', async () => {
    const { handlers, rendered } = renderHome()
    await rendered

    await userEvent.type(screen.getByLabelText('Copy code'), '21')
    await userEvent.click(screen.getByRole('button', { name: 'Open copy' }))
    await userEvent.type(screen.getByLabelText('Student ID'), '5')
    await userEvent.click(screen.getByRole('button', { name: 'Open student' }))

    expect(handlers.onOpenCopy).toHaveBeenCalledWith(21)
    expect(handlers.onOpenStudent).toHaveBeenCalledWith(5)
  })

  it('P14 lists the books found by title or author', async () => {
    let query: URLSearchParams | undefined
    server.use(
      http.get('/api/books', ({ request, response }) => {
        query = new URL(request.url).searchParams
        return response(200).json({
          content: [hobbit],
          page: 0,
          size: 20,
          totalElements: 1,
          totalPages: 1,
        })
      }),
    )
    await renderHome({ q: 'tolkien' }).rendered

    expect(
      await screen.findByRole('cell', { name: 'The Hobbit' }),
    ).toBeVisible()
    expect(query?.get('q')).toBe('tolkien')
  })

  it('waits for some text before searching books', async () => {
    let requests = 0
    server.use(
      http.get('/api/books', ({ response }) => {
        requests += 1
        return response(200).json({
          content: [],
          page: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0,
        })
      }),
    )
    await renderHome().rendered

    // Dá tempo para uma requisição indevida chegar ao handler.
    await new Promise((resolve) => setTimeout(resolve, 50))
    expect(requests).toBe(0)
  })

  it('sends the typed book search to the URL', async () => {
    const { handlers, rendered } = renderHome()
    await rendered

    await userEvent.type(
      screen.getByRole('searchbox', { name: 'Title or author' }),
      'hobbit',
    )

    await waitFor(() =>
      expect(handlers.onSearchChange).toHaveBeenCalledWith({
        q: 'hobbit',
        page: undefined,
      }),
    )
  })

  it('D9-15 keeps every id unique', async () => {
    const { container } = await renderHome().rendered

    const ids = [...container.querySelectorAll('[id]')].map(
      (element) => element.id,
    )
    expect(ids.length).toBeGreaterThan(0)
    expect(new Set(ids).size).toBe(ids.length)
  })
})
