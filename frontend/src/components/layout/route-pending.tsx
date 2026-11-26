import { LoaderCircleIcon } from 'lucide-react'

// Aparece só quando o loader demora (defaultPendingMs do router, 1 s).
export function RoutePending() {
  return (
    <div
      role="status"
      className="text-muted-foreground flex items-center gap-2 p-4 text-sm"
    >
      <LoaderCircleIcon aria-hidden className="size-4 animate-spin" />
      Loading…
    </div>
  )
}
