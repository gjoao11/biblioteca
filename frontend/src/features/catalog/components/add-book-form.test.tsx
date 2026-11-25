import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { http, problem } from '@/test/http'
import { createTestQueryClient, renderWithQueryClient } from '@/test/render'
import { server } from '@/test/server'

import { type BookCategory, bookKeys } from '../api'
import { AddBookForm } from './add-book-form'

const categories: BookCategory[] = [
  { id: 1, name: 'Computer Science' },
  { id: 2, name: 'Fantasy' },
]

function renderForm() {
  // Como o loader da rota: as categorias já estão no cache.
  const queryClient = createTestQueryClient()
  queryClient.setQueryData(bookKeys.categories, categories)
  return renderWithQueryClient(<AddBookForm />, { queryClient })
}

async function fill({ copies = '3' } = {}) {
  await userEvent.type(screen.getByLabelText('Title'), 'The Hobbit')
  await userEvent.type(screen.getByLabelText('Author'), 'J. R. R. Tolkien')
  await userEvent.selectOptions(screen.getByLabelText('Category'), 'Fantasy')
  const copiesInput = screen.getByLabelText('Number of copies')
  await userEvent.clear(copiesInput)
  await userEvent.type(copiesInput, copies)
}

function submit() {
  return userEvent.click(screen.getByRole('button', { name: 'Add book' }))
}

describe('AddBookForm', () => {
  it('validates the fields before calling the API', async () => {
    let requests = 0
    server.use(
      http.post('/api/books', ({ response }) => {
        requests += 1
        return response.untyped(problem({ status: 500 }))
      }),
    )
    renderForm()

    await submit()

    expect(await screen.findByText('Enter the title')).toBeInTheDocument()
    expect(screen.getByText('Enter the author')).toBeInTheDocument()
    expect(screen.getByText('Choose a category')).toBeInTheDocument()
    expect(requests).toBe(0)
  })

  it('P01 creates the book with N copies at once', async () => {
    let received: unknown
    let requests = 0
    server.use(
      http.post('/api/books', async ({ request, response }) => {
        requests += 1
        received = await request.json()
        return response(201).json({
          id: 7,
          title: 'The Hobbit',
          author: 'J. R. R. Tolkien',
          categoryId: 2,
          categoryName: 'Fantasy',
          copyIds: [21, 22, 23],
        })
      }),
    )
    renderForm()

    await fill({ copies: '3' })
    await submit()

    expect(await screen.findByText('Book added')).toBeInTheDocument()
    expect(screen.getByText(/has 3 copies: #21, #22, #23/)).toBeInTheDocument()
    expect(requests).toBe(1)
    expect(received).toEqual({
      title: 'The Hobbit',
      author: 'J. R. R. Tolkien',
      categoryId: 2,
      copies: 3,
    })
    expect(screen.getByLabelText('Title')).toHaveValue('')
  })

  it('shows server field errors on the fields', async () => {
    server.use(
      http.post('/api/books', ({ response }) =>
        response.untyped(
          problem({
            status: 400,
            detail: 'One or more fields are invalid',
            errors: [{ field: 'categoryId', message: 'does not exist' }],
          }),
        ),
      ),
    )
    renderForm()

    await fill()
    await submit()

    expect(await screen.findByText('does not exist')).toBeInTheDocument()
    expect(screen.getByLabelText('Category')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
  })

  it('shows other server errors on the form', async () => {
    server.use(
      http.post('/api/books', ({ response }) =>
        response.untyped(
          problem({ status: 501, detail: 'Not implemented yet' }),
        ),
      ),
    )
    renderForm()

    await fill()
    await submit()

    expect(await screen.findByText('Not implemented yet')).toBeInTheDocument()
    // Erro mantém o que foi digitado.
    expect(screen.getByLabelText('Title')).toHaveValue('The Hobbit')
  })

  it('D9-23 works on its own, without loading the book list', async () => {
    let listRequests = 0
    server.use(
      http.get('/api/books', ({ response }) => {
        listRequests += 1
        return response(200).json({
          content: [],
          page: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0,
        })
      }),
      http.post('/api/books', ({ response }) =>
        response(201).json({
          id: 7,
          title: 'The Hobbit',
          author: 'J. R. R. Tolkien',
          categoryId: 2,
          categoryName: 'Fantasy',
          copyIds: [21],
        }),
      ),
    )
    renderForm()

    await fill({ copies: '1' })
    await submit()

    expect(await screen.findByText(/has 1 copy: #21/)).toBeInTheDocument()
    await waitFor(() => expect(listRequests).toBe(0))
  })
})
