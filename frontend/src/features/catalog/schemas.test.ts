import { describe, expect, it } from 'vitest'

import {
  allBooksSearchSchema,
  bookSearchSchema,
  createBookSchema,
} from './schemas'

describe('bookSearchSchema', () => {
  it('keeps a valid search', () => {
    expect(bookSearchSchema.parse({ q: ' tolkien ', page: 2 })).toEqual({
      q: 'tolkien',
      page: 2,
    })
  })

  it('reads a numeric search as text', () => {
    expect(bookSearchSchema.parse({ q: 1984 })).toEqual({ q: '1984' })
  })

  it('drops an invalid page instead of failing', () => {
    expect(bookSearchSchema.parse({ page: 0 })).toEqual({})
    expect(bookSearchSchema.parse({ page: 'abc' })).toEqual({})
  })
})

describe('allBooksSearchSchema', () => {
  it('drops an invalid category instead of failing', () => {
    expect(allBooksSearchSchema.parse({ category: 'abc', page: 3 })).toEqual({
      page: 3,
    })
  })
})

describe('createBookSchema', () => {
  const valid = {
    title: ' The Hobbit ',
    author: 'J. R. R. Tolkien',
    description: '',
    categoryId: '2',
    copies: '3',
  }

  it('turns the form text into the request values', () => {
    expect(createBookSchema.parse(valid)).toEqual({
      title: 'The Hobbit',
      author: 'J. R. R. Tolkien',
      description: '',
      categoryId: 2,
      copies: 3,
    })
  })

  it('requires title, author and category', () => {
    const result = createBookSchema.safeParse({
      ...valid,
      title: ' ',
      author: '',
      categoryId: '',
    })

    expect(result.success).toBe(false)
    expect(result.error?.flatten().fieldErrors).toMatchObject({
      title: ['Enter the title'],
      author: ['Enter the author'],
      categoryId: ['Choose a category'],
    })
  })

  it.each([
    ['0', 'Add at least 1 copy'],
    ['101', 'Add at most 100 copies'],
    ['1.5', 'Use a whole number'],
    ['abc', 'Enter a number'],
  ])('rejects %s copies', (copies, message) => {
    const result = createBookSchema.safeParse({ ...valid, copies })

    expect(result.error?.flatten().fieldErrors.copies).toEqual([message])
  })
})
