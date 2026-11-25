import { zodResolver } from '@hookform/resolvers/zod'
import { useSuspenseQuery } from '@tanstack/react-query'
import { CircleCheckIcon } from 'lucide-react'
import { useId, useState } from 'react'
import { useForm } from 'react-hook-form'

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { Textarea } from '@/components/ui/textarea'
import { setServerErrors } from '@/lib/api/form-errors'

import {
  bookCategoriesQueryOptions,
  type CreatedBook,
  useCreateBook,
} from '../api'
import {
  type CreateBookInput,
  createBookSchema,
  type CreateBookValues,
} from '../schemas'

// Nomes iguais aos do CreateBookRequest: os erros de campo do servidor
// caem direto no campo certo.
const FIELDS = [
  'title',
  'author',
  'description',
  'categoryId',
  'copies',
] as const

const EMPTY: CreateBookInput = {
  title: '',
  author: '',
  description: '',
  categoryId: '',
  copies: '1',
}

// Não depende de nenhuma lista da tela (o legado dependia, D9-23).
export function AddBookForm() {
  const id = useId()
  // O loader da rota já garantiu as categorias.
  const { data: categories } = useSuspenseQuery(bookCategoriesQueryOptions)
  const createBook = useCreateBook()
  const [created, setCreated] = useState<CreatedBook>()
  const form = useForm<CreateBookInput, unknown, CreateBookValues>({
    resolver: zodResolver(createBookSchema),
    defaultValues: EMPTY,
  })
  const { errors, isSubmitting } = form.formState

  const onSubmit = form.handleSubmit(async (values) => {
    setCreated(undefined)
    try {
      const book = await createBook.mutateAsync({
        ...values,
        // Descrição é opcional: vazia não vai para a API.
        description: values.description || undefined,
      })
      setCreated(book)
      form.reset(EMPTY)
    } catch (error) {
      setServerErrors(error, form.setError, FIELDS)
    }
  })

  return (
    <form onSubmit={onSubmit} noValidate>
      <FieldGroup>
        {created && (
          // P01: um cadastro, N exemplares; os códigos vão nas etiquetas.
          <Alert>
            <CircleCheckIcon aria-hidden />
            <AlertTitle>Book added</AlertTitle>
            <AlertDescription>
              “{created.title}” has {created.copyIds.length}{' '}
              {created.copyIds.length === 1 ? 'copy' : 'copies'}:{' '}
              {created.copyIds.map((copyId) => `#${copyId}`).join(', ')}.
            </AlertDescription>
          </Alert>
        )}
        <Field data-invalid={!!errors.title}>
          <FieldLabel htmlFor={`${id}-title`}>Title</FieldLabel>
          <Input
            id={`${id}-title`}
            autoComplete="off"
            aria-invalid={!!errors.title}
            {...form.register('title')}
          />
          <FieldError errors={[errors.title]} />
        </Field>
        <Field data-invalid={!!errors.author}>
          <FieldLabel htmlFor={`${id}-author`}>Author</FieldLabel>
          <Input
            id={`${id}-author`}
            autoComplete="off"
            aria-invalid={!!errors.author}
            {...form.register('author')}
          />
          <FieldError errors={[errors.author]} />
        </Field>
        <Field data-invalid={!!errors.description}>
          <FieldLabel htmlFor={`${id}-description`}>Description</FieldLabel>
          <Textarea
            id={`${id}-description`}
            rows={4}
            aria-invalid={!!errors.description}
            {...form.register('description')}
          />
          <FieldDescription>Optional.</FieldDescription>
          <FieldError errors={[errors.description]} />
        </Field>
        <Field data-invalid={!!errors.categoryId}>
          <FieldLabel htmlFor={`${id}-category`}>Category</FieldLabel>
          <NativeSelect
            id={`${id}-category`}
            className="w-full"
            aria-invalid={!!errors.categoryId}
            {...form.register('categoryId')}
          >
            <NativeSelectOption value="">Select a category</NativeSelectOption>
            {categories.map((category) => (
              <NativeSelectOption key={category.id} value={category.id}>
                {category.name}
              </NativeSelectOption>
            ))}
          </NativeSelect>
          <FieldError errors={[errors.categoryId]} />
        </Field>
        <Field data-invalid={!!errors.copies}>
          <FieldLabel htmlFor={`${id}-copies`}>Number of copies</FieldLabel>
          <Input
            id={`${id}-copies`}
            type="number"
            min={1}
            max={100}
            aria-invalid={!!errors.copies}
            {...form.register('copies')}
          />
          <FieldError errors={[errors.copies]} />
        </Field>
        <FieldError errors={[errors.root]} />
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Adding…' : 'Add book'}
        </Button>
      </FieldGroup>
    </form>
  )
}
