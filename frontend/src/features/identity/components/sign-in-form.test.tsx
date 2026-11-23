import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { http } from '@/test/http'
import { renderWithQueryClient } from '@/test/render'
import { server } from '@/test/server'

import { identityKeys } from '../api'
import { SignInForm } from './sign-in-form'

async function fillAndSubmit(username: string, password: string) {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText('Username'), username)
  await user.type(screen.getByLabelText('Password'), password)
  await user.click(screen.getByRole('button', { name: 'Sign in' }))
}

describe('SignInForm', () => {
  it('validates required fields without calling the API', async () => {
    const onSuccess = vi.fn()
    renderWithQueryClient(<SignInForm onSuccess={onSuccess} />)

    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByText('Enter your username')).toBeInTheDocument()
    expect(screen.getByText('Enter your password')).toBeInTheDocument()
    expect(onSuccess).not.toHaveBeenCalled()
  })

  it('signs in and caches the current user', async () => {
    server.use(
      http.post('/api/auth/login', async ({ request, response }) => {
        const body = await request.json()
        return response(200).json({
          username: body.username,
          name: 'Admin',
          role: 'ADMIN',
        })
      }),
    )
    const onSuccess = vi.fn()
    const { queryClient } = renderWithQueryClient(
      <SignInForm onSuccess={onSuccess} />,
    )

    await fillAndSubmit('admin', 'secret')

    await waitFor(() =>
      expect(onSuccess).toHaveBeenCalledWith({
        username: 'admin',
        name: 'Admin',
        role: 'ADMIN',
      }),
    )
    expect(queryClient.getQueryData(identityKeys.me)).toEqual({
      username: 'admin',
      name: 'Admin',
      role: 'ADMIN',
    })
  })

  it('shows the server message when the credentials are wrong', async () => {
    server.use(
      http.post('/api/auth/login', ({ response }) =>
        response.untyped(
          Response.json(
            { status: 401, detail: 'Invalid username or password' },
            {
              status: 401,
              headers: { 'Content-Type': 'application/problem+json' },
            },
          ),
        ),
      ),
    )
    const onSuccess = vi.fn()
    renderWithQueryClient(<SignInForm onSuccess={onSuccess} />)

    await fillAndSubmit('admin', 'wrong')

    expect(
      await screen.findByText('Invalid username or password'),
    ).toBeInTheDocument()
    expect(onSuccess).not.toHaveBeenCalled()
  })
})
