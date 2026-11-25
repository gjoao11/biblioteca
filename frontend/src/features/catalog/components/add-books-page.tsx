import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import { AddBookForm } from './add-book-form'

export function AddBooksPage() {
  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <h1 className="text-2xl font-bold">Add books</h1>
      <Card>
        <CardHeader>
          <CardTitle>New book</CardTitle>
          <CardDescription>
            All copies are created at once, each with its own code.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <AddBookForm />
        </CardContent>
      </Card>
    </div>
  )
}
