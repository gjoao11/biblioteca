import { useSuspenseQuery } from '@tanstack/react-query'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import { usersQueryOptions } from '../api'
import { CreateLibrarianForm } from './create-librarian-form'
import { UsersTable } from './users-table'

export function UsersPage() {
  const { data: users } = useSuspenseQuery(usersQueryOptions)

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6">
      <h1 className="text-2xl font-bold">Users</h1>
      <div className="grid gap-6 lg:grid-cols-[1fr_20rem]">
        <Card>
          <CardHeader>
            <CardTitle>All users</CardTitle>
            <CardDescription>
              Disabled librarians can no longer sign in.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <UsersTable users={users} />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>New librarian</CardTitle>
          </CardHeader>
          <CardContent>
            <CreateLibrarianForm />
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
