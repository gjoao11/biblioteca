import createClient, { type Middleware } from 'openapi-fetch'

import type { paths } from './schema'

const UNSAFE_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE'])

function readCookie(name: string) {
  const prefix = `${name}=`
  const cookie = document.cookie.split('; ').find((c) => c.startsWith(prefix))
  return cookie && decodeURIComponent(cookie.slice(prefix.length))
}

const csrfMiddleware: Middleware = {
  onRequest({ request }) {
    if (UNSAFE_METHODS.has(request.method)) {
      const token = readCookie('XSRF-TOKEN')
      if (token) request.headers.set('X-XSRF-TOKEN', token)
    }
    return request
  },
}

export const api = createClient<paths>({ baseUrl: window.location.origin })
api.use(csrfMiddleware)
