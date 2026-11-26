import { act, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { SearchInput } from './search-input'

// fireEvent em vez de userEvent: com timers falsos, o userEvent espera o
// tempo passar sozinho e trava. Aqui o teste controla cada milissegundo.
function typeInto(input: HTMLElement, value: string) {
  fireEvent.change(input, { target: { value } })
}

function wait(ms: number) {
  act(() => vi.advanceTimersByTime(ms))
}

describe('SearchInput', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })
  afterEach(() => {
    vi.useRealTimers()
  })

  it('searches only after the user stops typing', () => {
    const onSearch = vi.fn()
    render(<SearchInput label="Search books" onSearch={onSearch} />)
    const input = screen.getByRole('searchbox', { name: 'Search books' })

    typeInto(input, 'pot')
    wait(200)
    typeInto(input, ' potter ')
    wait(299)
    expect(onSearch).not.toHaveBeenCalled()

    wait(1)
    expect(onSearch).toHaveBeenCalledOnce()
    expect(onSearch).toHaveBeenCalledWith('potter')
  })

  it('starts with the given value', () => {
    render(
      <SearchInput
        label="Search books"
        defaultValue="tolkien"
        onSearch={vi.fn()}
      />,
    )

    expect(screen.getByRole('searchbox', { name: 'Search books' })).toHaveValue(
      'tolkien',
    )
  })

  it('drops a pending search when it leaves the screen', () => {
    const onSearch = vi.fn()
    const { unmount } = render(
      <SearchInput label="Search books" onSearch={onSearch} />,
    )

    typeInto(screen.getByRole('searchbox'), 'potter')
    unmount()
    wait(300)

    expect(onSearch).not.toHaveBeenCalled()
  })

  it('D9-15 gives each search field its own id', () => {
    render(
      <>
        <SearchInput label="Search books" onSearch={vi.fn()} />
        <SearchInput label="Search copies" onSearch={vi.fn()} />
      </>,
    )

    const books = screen.getByRole('searchbox', { name: 'Search books' })
    const copies = screen.getByRole('searchbox', { name: 'Search copies' })
    expect(books.id).not.toBe(copies.id)
  })
})
