import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { http } from '@/test/http'
import { renderWithQueryClient } from '@/test/render'
import { server } from '@/test/server'

import { CreateLibrarianForm } from './create-librarian-form'

async function fillAndSubmit() {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText('Name'), 'Maria Silva')
  await user.type(screen.getByLabelText('Username'), 'maria')
  await user.type(screen.getByLabelText('Password'), 'librarian-pass')
  await user.click(screen.getByRole('button', { name: 'Create librarian' }))
}

describe('CreateLibrarianForm', () => {
  it('validates the fields before calling the API', async () => {
    renderWithQueryClient(<CreateLibrarianForm />)
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Username'), 'has space')
    await user.type(screen.getByLabelText('Password'), 'short')
    await user.click(screen.getByRole('button', { name: 'Create librarian' }))

    expect(await screen.findByText('Enter a name')).toBeInTheDocument()
    expect(
      screen.getByText(
        'Use only letters, numbers, dots, hyphens and underscores',
      ),
    ).toBeInTheDocument()
    expect(screen.getByText('Use at least 8 characters')).toBeInTheDocument()
  })

  it('creates the librarian and clears the form', async () => {
    let received: unknown
    server.use(
      http.post('/api/admin/users', async ({ request, response }) => {
        received = await request.json()
        return response(201).json({
          id: 1,
          username: 'maria',
          name: 'Maria Silva',
          role: 'LIBRARIAN',
          enabled: true,
          createdAt: '2026-11-22T00:00:00Z',
        })
      }),
    )
    renderWithQueryClient(<CreateLibrarianForm />)

    await fillAndSubmit()

    await waitFor(() => expect(screen.getByLabelText('Name')).toHaveValue(''))
    expect(received).toEqual({
      username: 'maria',
      name: 'Maria Silva',
      password: 'librarian-pass',
    })
  })

  it('shows a taken username on the username field', async () => {
    server.use(
      http.post('/api/admin/users', ({ response }) =>
        response.untyped(
          Response.json(
            {
              status: 409,
              detail: 'Username is already taken',
              errors: [{ field: 'username', message: 'is already taken' }],
            },
            {
              status: 409,
              headers: { 'Content-Type': 'application/problem+json' },
            },
          ),
        ),
      ),
    )
    renderWithQueryClient(<CreateLibrarianForm />)

    await fillAndSubmit()

    expect(await screen.findByText('is already taken')).toBeInTheDocument()
    expect(screen.getByLabelText('Username')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
  })
})
