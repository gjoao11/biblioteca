import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '@/components/ui/empty'

interface PagePlaceholderProps {
  title: string
}

// Tela provisória das rotas logadas que ainda não foram construídas.
// Quem constrói a tela troca o `component` da rota pela tela real.
export function PagePlaceholder({ title }: PagePlaceholderProps) {
  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6">
      <h1 className="text-2xl font-bold">{title}</h1>
      <Empty className="border">
        <EmptyHeader>
          <EmptyTitle>Coming soon</EmptyTitle>
          <EmptyDescription>This screen is not built yet.</EmptyDescription>
        </EmptyHeader>
      </Empty>
    </div>
  )
}
