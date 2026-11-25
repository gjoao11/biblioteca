import { z } from 'zod'

// Search params: o router já converte "?page=2" em número. Valor inválido na
// URL vira undefined (.catch) em vez de quebrar a tela.
const page = z.number().int().min(1).optional().catch(undefined)
// coerce: "?q=1984" chega como número e a busca precisa de texto.
const q = z.coerce.string().trim().max(255).optional().catch(undefined)

export const bookSearchSchema = z.object({ q, page })

export type BookSearch = z.infer<typeof bookSearchSchema>

export const allBooksSearchSchema = z.object({
  category: z.number().int().positive().optional().catch(undefined),
  page,
})

export type AllBooksSearch = z.infer<typeof allBooksSearchSchema>

// Os limites repetem o CreateBookRequest do backend; quem valida de verdade é
// o servidor, aqui é só para avisar antes de enviar.
export const createBookSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, 'Enter the title')
    .max(255, 'Use at most 255 characters'),
  author: z
    .string()
    .trim()
    .min(1, 'Enter the author')
    .max(255, 'Use at most 255 characters'),
  description: z.string().trim().max(5000, 'Use at most 5000 characters'),
  // O <select> e o <input type="number"> entregam texto; coerce vira número.
  categoryId: z.coerce.number<string>().int().positive('Choose a category'),
  copies: z.coerce
    .number<string>({ error: 'Enter a number' })
    .int('Use a whole number')
    .min(1, 'Add at least 1 copy')
    .max(100, 'Add at most 100 copies'),
})

export type CreateBookInput = z.input<typeof createBookSchema>
export type CreateBookValues = z.output<typeof createBookSchema>
