import { Link } from '@tanstack/react-router'

import { Button } from '@/components/ui/button'

export function NotFoundPage() {
  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4 p-4 text-center">
      <h1 className="text-2xl font-bold">Page not found</h1>
      <p className="text-muted-foreground">
        The page you are looking for does not exist.
      </p>
      <Button nativeButton={false} render={<Link to="/" />}>
        Go to home
      </Button>
    </div>
  )
}
