import { createFileRoute, redirect, useRouter } from '@tanstack/react-router'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { fetchCurrentUser } from '@/features/identity/api'
import { SignInForm } from '@/features/identity/components/sign-in-form'
import { signInSearchSchema } from '@/features/identity/schemas'

export const Route = createFileRoute('/sign-in')({
  validateSearch: signInSearchSchema,
  beforeLoad: async ({ context, search }) => {
    if (await fetchCurrentUser(context.queryClient)) {
      throw redirect({ href: search.redirect ?? '/' })
    }
  },
  component: SignInPage,
})

function SignInPage() {
  const router = useRouter()
  const { redirect: redirectTo } = Route.useSearch()

  return (
    <div className="flex min-h-svh items-center justify-center p-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>Sign in</CardTitle>
          <CardDescription>Library Management System</CardDescription>
        </CardHeader>
        <CardContent>
          <SignInForm
            onSuccess={() => router.history.push(redirectTo ?? '/')}
          />
        </CardContent>
      </Card>
    </div>
  )
}
