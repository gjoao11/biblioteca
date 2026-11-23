# Legado (Laravel 4.2) em Docker

Ambiente para rodar o sistema legado de biblioteca (Laravel 4.2, PHP 5.6, MySQL 5.7), usado na demo lado a lado e como referência de paridade (P01 a P16 e D9-01 a D9-27).

- Código do legado: https://github.com/prabhakar267/library-management-system, no commit `64e436a`.
- O código **não** é copiado para este repositório: o `setup.sh` clona em `legacy/app/` (pasta ignorada pelo Git).
- Todos os dados são fictícios e o sistema só escuta em `127.0.0.1`. Não exponha este ambiente na rede.

## Pré-requisitos

- Docker Desktop aberto e com o motor rodando (`docker info` deve responder).
- Git e um terminal bash (Git Bash no Windows).

## Como subir

Dentro de `legacy/`:

1. Clonar o legado no commit certo:

   ```bash
   ./setup.sh
   ```

2. Criar o `.env` a partir do modelo:

   ```bash
   cp .env.example .env
   ```

3. Editar o `.env`:

   - `APP_KEY`: chave de **exatamente 32 caracteres** (letras e números). Gere uma nova, não reutilize a do repositório original:

     ```bash
     LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 32; echo
     ```

     No PowerShell:

     ```powershell
     -join ((48..57)+(65..90)+(97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})
     ```

   - `MYSQL_ROOT_PASSWORD` e `MYSQL_PASSWORD`: senhas só com letras e números (aspas, `$` e `\` quebram a configuração gerada).

4. Subir os contêineres:

   ```bash
   docker compose up -d --build
   docker compose logs -f app
   ```

   Na primeira vez, o app instala as dependências (`composer install`) e roda as migrations. Espere a linha `resuming normal operations` do Apache.

5. Abrir http://127.0.0.1:8080.

## O que o ambiente faz sozinho

Ao subir, o `entrypoint.sh`:

- troca a chave `'key'` de `app/config/app.php` pela `APP_KEY` do `.env`, então a chave antiga do repositório original nunca é usada;
- gera `app/config/mysql.config.php` a partir das variáveis do `.env` (host `db`);
- roda `php artisan migrate`.

O `.env` e a pasta `app/` ficam fora do Git.

## Usuário de teste

Credenciais fictícias para uso exclusivo na demonstração local:

- Usuário: `bibliotecario`
- Senha: `DemoBiblioteca123`
- Login: http://127.0.0.1:8080/sign-in

Após subir os contêineres e concluir as migrations, execute dentro
de `legacy/`:

    docker compose cp seed-user.php app:/tmp/seed-user.php
    docker compose exec app sh -c 'cd /var/www/html && cp /tmp/seed-user.php ./seed-user.php && php ./seed-user.php'

O script cria o usuário apenas se o login ainda não existir.
Se já existir, não altera a senha nem os demais dados.

Não reutilize essas credenciais fora do ambiente local.

## Telas

Endereço base: http://127.0.0.1:8081

| Nº | Tela | URL | Acesso pelo filtro de rota |
|---|---|---|---|
| 01 | Login | http://127.0.0.1:8081/sign-in | Sem sessão |
| 02 | Criação de conta | http://127.0.0.1:8081/create | Sem sessão |
| 03 | Cadastro de estudante | http://127.0.0.1:8081/student-registration | Sem sessão |
| 04 | Busca de livros | http://127.0.0.1:8081/book | Sem sessão |
| 05 | Painel inicial | http://127.0.0.1:8081/ | Autenticado |
| 06 | Cadastro de livros | http://127.0.0.1:8081/add-books | Autenticado |
| 07 | Listagem de livros | http://127.0.0.1:8081/all-books | Autenticado |
| 08 | Estudantes cadastrados | http://127.0.0.1:8081/registered-students | Autenticado |
| 09 | Aprovação de estudantes | http://127.0.0.1:8081/students-for-approval | Autenticado |
| 10 | Empréstimo e devolução | http://127.0.0.1:8081/issue-return | Autenticado |
| 11 | Empréstimos em andamento | http://127.0.0.1:8081/currently-issued | Autenticado |

As URLs foram identificadas no arquivo de rotas.
A existência das rotas não substitui a validação funcional das telas.

As quatro primeiras páginas usam o filtro `guest`: para testá-las,
saia da conta ou abra uma janela anônima.

## Portas

| serviço | endereço |
|---|---|
| Aplicação (Apache + PHP) | http://127.0.0.1:8081 |
| MySQL | 127.0.0.1:3307 |

## Derrubar e recomeçar

Parar os contêineres, mantendo o banco:

```bash
docker compose down
```

Parar e apagar o banco (recomeça do zero):

```bash
docker compose down -v
```

Para trocar a chave, edite o `APP_KEY` no `.env` e rode `docker compose up -d`.

## Problemas comuns

- `failed to connect to the docker API`: o Docker Desktop não está aberto. Abra e espere o motor subir.
- `APP_KEY precisa ter 32 caracteres`: ajuste o `.env`.
- `exec format error` no `entrypoint.sh`: o arquivo está com BOM ou finais de linha CRLF. Rode `sed -i 's/\r$//' entrypoint.sh` e `docker compose up -d --build`.
- Porta em uso: troque a porta do serviço `app` em `compose.yaml` (por exemplo, `"127.0.0.1:8081:80"`) e atualize este README.