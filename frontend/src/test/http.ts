import { createOpenApiHttp } from 'openapi-msw'

import type { paths } from '@/lib/api/schema'

export const http = createOpenApiHttp<paths>()
