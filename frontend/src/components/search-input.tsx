import { SearchIcon } from 'lucide-react'
import { useEffect, useId, useRef } from 'react'

import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from '@/components/ui/input-group'
import { Label } from '@/components/ui/label'

interface SearchInputProps {
  label: string
  // Valor inicial, normalmente o termo que já está na URL.
  defaultValue?: string
  placeholder?: string
  // Espera, em ms, depois da última tecla antes de buscar.
  delay?: number
  onSearch: (value: string) => void
}

// Campo de busca com debounce: só chama onSearch quando o usuário para de
// digitar, para não disparar uma requisição por tecla.
export function SearchInput({
  label,
  defaultValue = '',
  placeholder,
  delay = 300,
  onSearch,
}: SearchInputProps) {
  // useId evita id repetido quando a tela tem várias buscas (a home do
  // legado repetia ids, D9-15).
  const id = useId()
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined)

  // Saiu da tela antes do prazo: a busca pendente não roda mais.
  useEffect(() => () => clearTimeout(timer.current), [])

  return (
    <div className="flex flex-col gap-2">
      <Label htmlFor={id}>{label}</Label>
      <InputGroup>
        <InputGroupInput
          id={id}
          type="search"
          defaultValue={defaultValue}
          placeholder={placeholder}
          onChange={(event) => {
            const value = event.target.value.trim()
            clearTimeout(timer.current)
            timer.current = setTimeout(() => onSearch(value), delay)
          }}
        />
        <InputGroupAddon>
          <SearchIcon aria-hidden />
        </InputGroupAddon>
      </InputGroup>
    </div>
  )
}
