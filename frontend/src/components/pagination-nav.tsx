import { Link } from '@tanstack/react-router'
import { cn } from 'cn'
import { ChevronLeftIcon, ChevronRightIcon } from 'lucide-react'

import { buttonVariants } from '@/components/ui/button'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
} from '@/components/ui/pagination'

interface PaginationNavProps {
  // Página atual, começando em 1 (como na URL, não como na API).
  page: number
  totalPages: number
}

// Até 7 páginas mostra todas; acima disso, a primeira, a última e as
// vizinhas da atual, com reticências no meio.
function pageItems(page: number, totalPages: number) {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }
  const start = Math.max(2, page - 1)
  const end = Math.min(totalPages - 1, page + 1)
  const items: (number | 'ellipsis')[] = [1]
  if (start > 2) items.push('ellipsis')
  for (let item = start; item <= end; item++) items.push(item)
  if (end < totalPages - 1) items.push('ellipsis')
  items.push(totalPages)
  return items
}

// Usa o <Link> do router com o visual de botão, e não o PaginationLink do
// shadcn: o Button do Base UI põe role="button" no <a>, e um link deve ser
// anunciado como link.
const stepClass = cn(buttonVariants({ variant: 'ghost' }), 'gap-1')
const disabledClass = cn(stepClass, 'pointer-events-none opacity-50')

// Os links só trocam o `page` da URL (to="." é a própria tela) e mantêm os
// outros filtros; a tela lê o `page` no validateSearch e o põe na queryKey.
export function PaginationNav({ page, totalPages }: PaginationNavProps) {
  if (totalPages <= 1) return null

  const previous = (
    <>
      <ChevronLeftIcon aria-hidden />
      <span className="hidden sm:block">Previous</span>
    </>
  )
  const next = (
    <>
      <span className="hidden sm:block">Next</span>
      <ChevronRightIcon aria-hidden />
    </>
  )

  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          {page > 1 ? (
            <Link
              to="."
              search={(prev) => ({ ...prev, page: page - 1 })}
              aria-label="Go to previous page"
              className={stepClass}
            >
              {previous}
            </Link>
          ) : (
            <span aria-disabled className={disabledClass}>
              {previous}
            </span>
          )}
        </PaginationItem>
        {pageItems(page, totalPages).map((item, index) => (
          <PaginationItem
            key={item === 'ellipsis' ? `ellipsis-${index}` : item}
          >
            {item === 'ellipsis' ? (
              <PaginationEllipsis />
            ) : (
              <Link
                to="."
                search={(prev) => ({ ...prev, page: item })}
                aria-label={`Page ${item}`}
                // O Link já põe aria-current="page" no item ativo.
                className={buttonVariants({
                  variant: item === page ? 'outline' : 'ghost',
                  size: 'icon',
                })}
              >
                {item}
              </Link>
            )}
          </PaginationItem>
        ))}
        <PaginationItem>
          {page < totalPages ? (
            <Link
              to="."
              search={(prev) => ({ ...prev, page: page + 1 })}
              aria-label="Go to next page"
              className={stepClass}
            >
              {next}
            </Link>
          ) : (
            <span aria-disabled className={disabledClass}>
              {next}
            </span>
          )}
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  )
}
