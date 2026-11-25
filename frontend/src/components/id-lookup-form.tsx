import { zodResolver } from '@hookform/resolvers/zod'
import { useId } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'

import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'

const lookupSchema = z.object({
  id: z.coerce
    .number<string>({ error: 'Enter a number' })
    .int('Enter a whole number')
    .positive('Enter a valid ID'),
})

type LookupInput = z.input<typeof lookupSchema>
type LookupValues = z.output<typeof lookupSchema>

interface IdLookupFormProps {
  label: string
  submitLabel: string
  // Recebe o id já validado; normalmente navega para a página de detalhe.
  onSubmit: (id: number) => void
}

// "Digite um código e abra o detalhe": a busca de exemplar e a de aluno da
// home do legado. useId evita id repetido com vários na mesma tela (D9-15).
export function IdLookupForm({
  label,
  submitLabel,
  onSubmit,
}: IdLookupFormProps) {
  const inputId = useId()
  const form = useForm<LookupInput, unknown, LookupValues>({
    resolver: zodResolver(lookupSchema),
    defaultValues: { id: '' },
  })
  const error = form.formState.errors.id

  return (
    <form
      onSubmit={form.handleSubmit((values) => onSubmit(values.id))}
      noValidate
    >
      <Field data-invalid={!!error}>
        <FieldLabel htmlFor={inputId}>{label}</FieldLabel>
        <div className="flex gap-2">
          <Input
            id={inputId}
            inputMode="numeric"
            autoComplete="off"
            aria-invalid={!!error}
            {...form.register('id')}
          />
          <Button type="submit" variant="outline">
            {submitLabel}
          </Button>
        </div>
        <FieldError errors={[error]} />
      </Field>
    </form>
  )
}
