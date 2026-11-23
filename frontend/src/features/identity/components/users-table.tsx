import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

import { type User, useUpdateUser } from '../api'

interface UsersTableProps {
  users: User[]
}

export function UsersTable({ users }: UsersTableProps) {
  const updateUser = useUpdateUser()

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Name</TableHead>
          <TableHead>Username</TableHead>
          <TableHead>Role</TableHead>
          <TableHead>Status</TableHead>
          <TableHead className="text-right">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {users.map((user) => (
          <TableRow key={user.id}>
            <TableCell className="font-medium">{user.name}</TableCell>
            <TableCell>{user.username}</TableCell>
            <TableCell>
              <Badge variant={user.role === 'ADMIN' ? 'default' : 'secondary'}>
                {user.role === 'ADMIN' ? 'Admin' : 'Librarian'}
              </Badge>
            </TableCell>
            <TableCell>
              <Badge variant={user.enabled ? 'outline' : 'destructive'}>
                {user.enabled ? 'Active' : 'Disabled'}
              </Badge>
            </TableCell>
            <TableCell className="text-right">
              {user.role === 'LIBRARIAN' && (
                <Button
                  variant="outline"
                  size="sm"
                  disabled={updateUser.isPending}
                  aria-label={`${user.enabled ? 'Disable' : 'Enable'} ${user.name}`}
                  onClick={() =>
                    updateUser.mutate({
                      id: user.id,
                      name: user.name,
                      enabled: !user.enabled,
                    })
                  }
                >
                  {user.enabled ? 'Disable' : 'Enable'}
                </Button>
              )}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
