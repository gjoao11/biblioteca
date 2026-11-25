import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { renderWithRouter } from '@/test/render'

import { PaginationNav } from './pagination-nav'

function pageLinks() {
  return screen
    .queryAllByRole('link', { name: /^Page / })
    .map((link) => link.textContent)
}

describe('PaginationNav', () => {
  it('is hidden when there is a single page', async () => {
    await renderWithRouter(<PaginationNav page={1} totalPages={1} />)

    expect(screen.queryByRole('navigation')).toBeNull()
  })

  it('cannot go back from the first page', async () => {
    await renderWithRouter(<PaginationNav page={1} totalPages={3} />)

    expect(
      screen.queryByRole('link', { name: 'Go to previous page' }),
    ).toBeNull()
    expect(
      screen.getByRole('link', { name: 'Go to next page' }),
    ).toHaveAttribute('href', '/?page=2')
  })

  it('cannot go forward from the last page', async () => {
    await renderWithRouter(<PaginationNav page={3} totalPages={3} />, {
      path: '/?page=3',
    })

    expect(screen.queryByRole('link', { name: 'Go to next page' })).toBeNull()
    expect(
      screen.getByRole('link', { name: 'Go to previous page' }),
    ).toHaveAttribute('href', '/?page=2')
  })

  it('keeps the other search params and marks the current page', async () => {
    await renderWithRouter(<PaginationNav page={2} totalPages={3} />, {
      path: '/?category=4&page=2',
    })

    expect(screen.getByRole('link', { name: 'Page 3' })).toHaveAttribute(
      'href',
      '/?category=4&page=3',
    )
    expect(screen.getByRole('link', { name: 'Page 2' })).toHaveAttribute(
      'aria-current',
      'page',
    )
    expect(screen.getByRole('link', { name: 'Page 1' })).not.toHaveAttribute(
      'aria-current',
    )
  })

  it('shows every page up to seven', async () => {
    await renderWithRouter(<PaginationNav page={1} totalPages={7} />)

    expect(pageLinks()).toEqual(['1', '2', '3', '4', '5', '6', '7'])
  })

  it('collapses distant pages into ellipses', async () => {
    await renderWithRouter(<PaginationNav page={10} totalPages={20} />, {
      path: '/?page=10',
    })

    expect(pageLinks()).toEqual(['1', '9', '10', '11', '20'])
    expect(screen.getAllByText('More pages')).toHaveLength(2)
  })
})
