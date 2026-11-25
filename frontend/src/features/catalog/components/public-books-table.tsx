import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

import type { PublicBook } from '../api'

interface PublicBooksTableProps {
  books: PublicBook[]
}

export function PublicBooksTable({ books }: PublicBooksTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Title</TableHead>
          <TableHead>Author</TableHead>
          <TableHead>Status</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {books.map((book, index) => (
          // A busca pública não expõe o id do livro; título + autor + posição
          // identificam a linha dentro da página.
          <TableRow key={`${book.title}|${book.author}|${index}`}>
            {/* Texto via JSX: o React escapa o HTML do título (D9-07). */}
            <TableCell className="font-medium whitespace-normal">
              {book.title}
            </TableCell>
            <TableCell className="whitespace-normal">{book.author}</TableCell>
            <TableCell>
              <Badge variant={book.available ? 'secondary' : 'outline'}>
                {book.available ? 'Available' : 'Not available'}
              </Badge>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
