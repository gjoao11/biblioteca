import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { ApiError } from '@/lib/api/errors'

import { ErrorMessage } from './error-message'

describe('ErrorMessage', () => {
  it('shows the detail sent by the API', () => {
    const error = new ApiError({ status: 501, detail: 'Not implemented yet' })

    render(<ErrorMessage error={error} />)

    expect(screen.getByRole('alert')).toHaveTextContent('Not implemented yet')
  })

  it('hides technical messages of other errors', () => {
    render(<ErrorMessage error={new TypeError('Failed to fetch')} />)

    const alert = screen.getByRole('alert')
    expect(alert).toHaveTextContent('Please try again.')
    expect(alert).not.toHaveTextContent('Failed to fetch')
  })

  it('offers a retry only when the screen can retry', async () => {
    const onRetry = vi.fn()
    const { rerender } = render(<ErrorMessage error={new Error()} />)
    expect(screen.queryByRole('button', { name: 'Try again' })).toBeNull()

    rerender(<ErrorMessage error={new Error()} onRetry={onRetry} />)
    await userEvent.click(screen.getByRole('button', { name: 'Try again' }))

    expect(onRetry).toHaveBeenCalledOnce()
  })
})
