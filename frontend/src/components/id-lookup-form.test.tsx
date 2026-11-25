import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { IdLookupForm } from './id-lookup-form'

function renderForm(onSubmit = vi.fn()) {
  render(
    <IdLookupForm label="Copy code" submitLabel="Open" onSubmit={onSubmit} />,
  )
  return onSubmit
}

describe('IdLookupForm', () => {
  it('sends the typed id as a number', async () => {
    const onSubmit = renderForm()

    await userEvent.type(screen.getByLabelText('Copy code'), ' 21 ')
    await userEvent.click(screen.getByRole('button', { name: 'Open' }))

    expect(onSubmit).toHaveBeenCalledWith(21)
  })

  it.each([
    ['', 'Enter a valid ID'],
    ['0', 'Enter a valid ID'],
    ['abc', 'Enter a number'],
    ['2.5', 'Enter a whole number'],
  ])('rejects "%s"', async (typed, message) => {
    const onSubmit = renderForm()

    if (typed) await userEvent.type(screen.getByLabelText('Copy code'), typed)
    await userEvent.click(screen.getByRole('button', { name: 'Open' }))

    expect(await screen.findByText(message)).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })
})
