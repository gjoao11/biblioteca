import { render, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import type { BookSummary } from '../api'
import { BooksTable } from './books-table'

const book: BookSummary = {
  id: 1,
  title: 'The Hobbit',
  author: 'J. R. R. Tolkien',
  categoryId: 2,
  categoryName: 'Fantasy',
  totalCopies: 3,
  availableCopies: 1,
}

describe('BooksTable', () => {
  it('P03 shows available and total copies of each book', () => {
    render(<BooksTable books={[book]} />)

    const row = screen.getByRole('row', { name: /The Hobbit/ })
    const cells = within(row).getAllByRole('cell')
    expect(cells.map((cell) => cell.textContent)).toEqual([
      'The Hobbit',
      'J. R. R. Tolkien',
      'Fantasy',
      '1',
      '3',
    ])
  })

  it('D9-07 renders a book title with HTML as plain text', () => {
    const title = '<script>alert(1)</script>'
    const { container } = render(<BooksTable books={[{ ...book, title }]} />)

    expect(screen.getByRole('cell', { name: title })).toBeInTheDocument()
    expect(container.querySelector('script')).toBeNull()
  })
})
