import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { SidebarProvider } from '@/components/ui/sidebar'
import { type CurrentUser, identityKeys } from '@/features/identity/api'
import { createTestQueryClient, renderWithRouter } from '@/test/render'

import { AppSidebar } from './app-sidebar'

const librarian: CurrentUser = {
  username: 'maria',
  name: 'Maria Silva',
  role: 'LIBRARIAN',
}

function renderSidebar(user: CurrentUser, path = '/') {
  const queryClient = createTestQueryClient()
  queryClient.setQueryData(identityKeys.me, user)
  return renderWithRouter(
    <SidebarProvider>
      <AppSidebar />
    </SidebarProvider>,
    { path, queryClient },
  )
}

describe('AppSidebar', () => {
  it('links to every logged-in screen', async () => {
    await renderSidebar(librarian)

    const expected = {
      Home: '/',
      'All books': '/all-books',
      'Add books': '/add-books',
      'Issue / Return': '/issue-return',
      'Currently issued': '/currently-issued',
      'Students for approval': '/students-for-approval',
      'Registered students': '/registered-students',
    }
    for (const [name, href] of Object.entries(expected)) {
      expect(screen.getByRole('link', { name })).toHaveAttribute('href', href)
    }
  })

  it('shows Users only to admins', async () => {
    const { unmount } = await renderSidebar(librarian)
    expect(screen.queryByRole('link', { name: 'Users' })).toBeNull()
    unmount()

    await renderSidebar({ ...librarian, role: 'ADMIN' })
    expect(screen.getByRole('link', { name: 'Users' })).toHaveAttribute(
      'href',
      '/admin/users',
    )
  })

  // aria-current vem do Link (leitor de tela); data-active, do destaque visual.
  function expectCurrent(name: string, current: boolean) {
    const link = screen.getByRole('link', { name })
    if (current) {
      expect(link).toHaveAttribute('aria-current', 'page')
      expect(link).toHaveAttribute('data-active')
    } else {
      expect(link).not.toHaveAttribute('aria-current')
      expect(link).not.toHaveAttribute('data-active')
    }
  }

  it('marks only the current page', async () => {
    await renderSidebar(librarian, '/all-books')

    expectCurrent('All books', true)
    expectCurrent('Home', false)
  })

  it('marks Home only on the exact path', async () => {
    await renderSidebar(librarian, '/')

    expectCurrent('Home', true)
    expectCurrent('All books', false)
  })
})
