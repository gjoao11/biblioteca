import { z } from 'zod'

export const signInSchema = z.object({
  username: z.string().trim().min(1, 'Enter your username'),
  password: z.string().min(1, 'Enter your password'),
})

export type SignInValues = z.infer<typeof signInSchema>

// Só caminhos internos: evita que ?redirect= mande o usuário para outro site.
export const signInSearchSchema = z.object({
  redirect: z
    .string()
    .regex(/^\/(?![/\\])[^\\\s]*$/)
    .optional()
    .catch(undefined),
})

export const createLibrarianSchema = z.object({
  username: z
    .string()
    .trim()
    .min(1, 'Enter a username')
    .max(50, 'Use at most 50 characters')
    .regex(
      /^[A-Za-z0-9._-]+$/,
      'Use only letters, numbers, dots, hyphens and underscores',
    ),
  name: z
    .string()
    .trim()
    .min(1, 'Enter a name')
    .max(100, 'Use at most 100 characters'),
  password: z
    .string()
    .min(8, 'Use at least 8 characters')
    .max(72, 'Use at most 72 characters'),
})

export type CreateLibrarianValues = z.infer<typeof createLibrarianSchema>
