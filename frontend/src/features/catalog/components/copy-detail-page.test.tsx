import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { createTestQueryClient, renderWithQueryClient } from '@/test/render'

import { bookKeys, type Copy } from '../api'
import { CopyDetailPage } from './copy-detail-page'

const copy: Copy = {
  id: 21,
  bookId: 7,
  title: 'The Hobbit',
  author: 'J. R. R. Tolkien',
  categoryName: 'Fantasy',
  status: 'AVAILABLE',
}

function renderPage(data: Copy) {
  // Como o loader da rota: o exemplar já está no cache.
  const queryClient = createTestQueryClient()
  queryClient.setQueryData(bookKeys.copy(data.id), data)
  return renderWithQueryClient(<CopyDetailPage copyId={data.id} />, {
    queryClient,
  })
}

describe('CopyDetailPage', () => {
  it('P06 shows the book of the copy', () => {
    renderPage(copy)

    expect(
      screen.getByRole('heading', { level: 1, name: 'Copy #21' }),
    ).toBeInTheDocument()
    expect(screen.getByText('The Hobbit')).toBeInTheDocument()
    expect(screen.getByText('J. R. R. Tolkien')).toBeInTheDocument()
    expect(screen.getByText('Fantasy')).toBeInTheDocument()
    expect(screen.getByText('Available')).toBeInTheDocument()
  })

  it('P06 tells when the copy is on loan', () => {
    renderPage({ ...copy, status: 'LOANED' })

    expect(screen.getByText('On loan')).toBeInTheDocument()
    expect(screen.queryByText('Available')).toBeNull()
  })

  it('D9-07 renders a book title with HTML as plain text', () => {
    const title = '<b onclick="alert(1)">Bold</b>'
    const { container } = renderPage({ ...copy, title })

    expect(screen.getByText(title)).toBeInTheDocument()
    expect(container.querySelector('b')).toBeNull()
  })
})
