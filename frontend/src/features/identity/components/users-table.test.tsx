import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { http } from '@/test/http'
import { renderWithQueryClient } from '@/test/render'
import { server } from '@/test/server'

import type { User } from '../api'
import { UsersTable } from './users-table'

const admin: User = {
  id: 1,
  username: 'admin',
  name: 'Admin',
  role: 'ADMIN',
  enabled: true,
  createdAt: '2026-11-22T00:00:00Z',
}

const librarian: User = {
  id: 2,
  username: 'maria',
  name: 'Maria Silva',
  role: 'LIBRARIAN',
  enabled: true,
  createdAt: '2026-11-22T00:00:00Z',
}

describe('UsersTable', () => {
  it('only offers actions for librarians', () => {
    renderWithQueryClient(<UsersTable users={[admin, librarian]} />)

    expect(
      screen.getByRole('button', { name: 'Disable Maria Silva' }),
    ).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Admin$/ })).toBeNull()
  })

  it('disables a librarian keeping the name', async () => {
    let received: unknown
    let receivedId: string | undefined
    server.use(
      http.put(
        '/api/admin/users/{id}',
        async ({ params, request, response }) => {
          receivedId = params.id
          received = await request.json()
          return response(200).json({ ...librarian, enabled: false })
        },
      ),
    )
    renderWithQueryClient(<UsersTable users={[librarian]} />)

    await userEvent.click(
      screen.getByRole('button', { name: 'Disable Maria Silva' }),
    )

    await waitFor(() =>
      expect(received).toEqual({ name: 'Maria Silva', enabled: false }),
    )
    expect(receivedId).toBe('2')
  })
})
