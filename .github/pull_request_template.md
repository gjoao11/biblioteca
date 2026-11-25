<!-- Título do PR no padrão dos commits: tipo(escopo): descrição -->

## O que muda

<!-- Em 2 ou 3 frases: o que este PR faz e por quê. -->

Closes #

## P e D9 cobertos

<!-- Códigos de docs/paridade.md. Cada código precisa de um teste com ele no nome
     (ex.: P09_rejectsUnapprovedStudent). Se não cobre nenhum, escreva "Nenhum". -->

| Código | Teste |
|---|---|
| P__ | `NomeDoTeste` |
| D9-__ | `NomeDoTeste` |

## Antes vs. Depois

<!-- Como o legado se comportava e como o sistema novo se comporta agora.
     Em tela, um print de cada lado ajuda. Se não se aplica (infra, docs), escreva "Não se aplica". -->

| Legado | Sistema novo |
|---|---|
|  |  |

## Como testei

- [ ] Backend: `./mvnw verify` passou
- [ ] Frontend: `pnpm lint`, `pnpm typecheck` e `pnpm test` passaram
- [ ] Testei manualmente com o backend no ar (descreva abaixo)

<!-- Passos do teste manual, se houver. -->

## Checklist

- [ ] Mudei endpoints e rodei `pnpm gen:api` (o `schema.d.ts` vai neste PR)
- [ ] Endpoint público novo entrou em `PUBLIC_ENDPOINTS` do `EndpointSecurityTests`
- [ ] Migration nova tem número combinado com o grupo
