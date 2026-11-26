import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { renderWithRouter } from '@/test/render'

import { NotFoundPage } from './not-found-page'

describe('NotFoundPage', () => {
  it('offers a link back to home', async () => {
    await renderWithRouter(<NotFoundPage />, { path: '/xyz' })

    expect(
      screen.getByRole('heading', { level: 1, name: 'Page not found' }),
    ).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Go to home' })).toHaveAttribute(
      'href',
      '/',
    )
  })
})
