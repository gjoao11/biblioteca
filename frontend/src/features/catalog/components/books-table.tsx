import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

import type { BookSummary } from '../api'

interface BooksTableProps {
  books: BookSummary[]
}

// Tabela do bibliotecário: inclui os totais de exemplares (P03).
export function BooksTable({ books }: BooksTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Title</TableHead>
          <TableHead>Author</TableHead>
          <TableHead>Category</TableHead>
          <TableHead className="text-right">Available</TableHead>
          <TableHead className="text-right">Total</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {books.map((book) => (
          <TableRow key={book.id}>
            {/* Texto via JSX: o React escapa o HTML do título (D9-07). */}
            <TableCell className="font-medium whitespace-normal">
              {book.title}
            </TableCell>
            <TableCell className="whitespace-normal">{book.author}</TableCell>
            <TableCell>{book.categoryName}</TableCell>
            <TableCell className="text-right">{book.availableCopies}</TableCell>
            <TableCell className="text-right">{book.totalCopies}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
