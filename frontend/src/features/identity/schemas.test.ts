import { describe, expect, it } from 'vitest'

import { signInSearchSchema } from './schemas'

const redirectOf = (redirect: string) =>
  signInSearchSchema.parse({ redirect }).redirect

describe('signInSearchSchema', () => {
  it('keeps internal paths', () => {
    expect(redirectOf('/admin/users?page=2')).toBe('/admin/users?page=2')
  })

  it.each(['https://evil.com', '//evil.com', '/\\evil.com', '/ evil'])(
    'drops %s',
    (value) => {
      expect(redirectOf(value)).toBeUndefined()
    },
  )
})
