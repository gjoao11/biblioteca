import { Link } from '@tanstack/react-router'

import { buttonVariants } from '@/components/ui/button'

export function NotFoundPage() {
  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4 p-4 text-center">
      <h1 className="text-2xl font-bold">Page not found</h1>
      <p className="text-muted-foreground">
        The page you are looking for does not exist.
      </p>
      {/* Link com visual de botão: o Button do Base UI poria role="button" no <a>. */}
      <Link to="/" className={buttonVariants()}>
        Go to home
      </Link>
    </div>
  )
}
