import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'

import { Button } from '@/components/ui/button'
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { setServerErrors } from '@/lib/api/form-errors'

import { useCreateLibrarian } from '../api'
import { createLibrarianSchema, type CreateLibrarianValues } from '../schemas'

const FIELDS = ['username', 'name', 'password'] as const

export function CreateLibrarianForm() {
  const createLibrarian = useCreateLibrarian()
  const form = useForm<CreateLibrarianValues>({
    resolver: zodResolver(createLibrarianSchema),
    defaultValues: { username: '', name: '', password: '' },
  })
  const { errors, isSubmitting } = form.formState

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      await createLibrarian.mutateAsync(values)
      form.reset()
    } catch (error) {
      setServerErrors(error, form.setError, FIELDS)
    }
  })

  return (
    <form onSubmit={onSubmit} noValidate>
      <FieldGroup>
        <Field data-invalid={!!errors.name}>
          <FieldLabel htmlFor="librarian-name">Name</FieldLabel>
          <Input
            id="librarian-name"
            autoComplete="off"
            aria-invalid={!!errors.name}
            {...form.register('name')}
          />
          <FieldError errors={[errors.name]} />
        </Field>
        <Field data-invalid={!!errors.username}>
          <FieldLabel htmlFor="librarian-username">Username</FieldLabel>
          <Input
            id="librarian-username"
            autoComplete="off"
            aria-invalid={!!errors.username}
            {...form.register('username')}
          />
          <FieldError errors={[errors.username]} />
        </Field>
        <Field data-invalid={!!errors.password}>
          <FieldLabel htmlFor="librarian-password">Password</FieldLabel>
          <Input
            id="librarian-password"
            type="password"
            autoComplete="new-password"
            aria-invalid={!!errors.password}
            {...form.register('password')}
          />
          <FieldError errors={[errors.password]} />
        </Field>
        <FieldError errors={[errors.root]} />
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creating…' : 'Create librarian'}
        </Button>
      </FieldGroup>
    </form>
  )
}
