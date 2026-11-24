# Paridade com o legado

Estas listas definem o escopo da reescrita. Os comportamentos P01 a P16 são o que o sistema legado (prabhakar267/library-management-system, Laravel 4.2) faz e o sistema novo precisa continuar fazendo. Os defeitos D9-01 a D9-27 são problemas do legado que o sistema novo não pode repetir. Vale a intenção de negócio, nunca o mecanismo defeituoso. Todo P ou D9 coberto por uma tarefa ganha um teste com o código no nome (por exemplo `P09_rejectsUnapprovedStudent`).

As evidências apontam arquivos e linhas do legado no commit `64e436a`. "01" e "02" são os relatórios de inventário técnico e de auditoria de riscos da pesquisa sobre o legado; os códigos S, D, T e INV são achados desses relatórios. Fonte: seção 9 do `ESTRUTURA-DE-MODERNIZACAO.md` da pesquisa. As funcionalidades ausentes (A01 a A17) ficam no backlog e não entram aqui.

## Comportamentos a preservar

| # | Comportamento | Evidência |
|---|---|---|
| P01 | Livro (título) é distinto de exemplar físico; o cadastro de livro cria N exemplares de uma vez, de forma atômica. | `BooksController.php:64-105`; 01 seção 6.3 |
| P02 | O identificador digitado no empréstimo e na devolução é o do exemplar, não o do livro. | `app/views/panel/issue-return.blade.php:24`; `LogController.php:67` |
| P03 | Listagem de livros filtrada por categoria, com total de exemplares e disponíveis por livro. | `BooksController.php:17-45` |
| P04 | Busca de livros por trecho do título ou do autor. | `BooksController.php:114-136` |
| P05 | Visitantes podem buscar livros sem login. | `app/routes.php:60`; `script.searchbook.js` |
| P06 | Detalhe de um exemplar mostra o livro e, se emprestado, quem está com ele (só para usuário autenticado; ver D9-02). | `BooksController.php:145-200` |
| P07 | Aluno se autocadastra sem login e fica pendente até aprovação. | `StudentController.php:189-225` |
| P08 | Bibliotecário aprova ou rejeita alunos pendentes; listas separadas de pendentes e aprovados, filtráveis por curso, ano e categoria. | `StudentController.php:11-44`, `:137-153` |
| P09 | Empréstimo verifica, nesta ordem: aluno existe, aluno aprovado, limite da categoria do aluno (`max_allowed`), exemplar existe, exemplar disponível; e registra quem emprestou. | `LogController.php:51-97` |
| P10 | Empréstimo e devolução atualizam juntos o registro de empréstimo, a disponibilidade do exemplar e a contagem do aluno. | `LogController.php:77-97`, `:136-152` |
| P11 | Mensagens de negócio claras quando o empréstimo é recusado (aluno não aprovado, limite atingido, exemplar indisponível). O texto é preservado; o mecanismo (exceção lida do debug) não. | `LogController.php:52,57,65` e seguintes |
| P12 | Lista de empréstimos em aberto com data de empréstimo e prazo de devolução de 14 dias. | `LogController.php:5-36` |
| P13 | Detalhe do aluno com os livros que estão com ele. | `StudentController.php:64-116` |
| P14 | Home do bibliotecário com três buscas: livro, exemplar, aluno. | `app/views/panel/index.blade.php`; `script.mainpage.js` |
| P15 | Login por nome de usuário com opção "lembrar-me", voltando para a página pretendida; após sair, o usuário cai na tela de login. | `AccountController.php:7-41`, `:87-90` |
| P16 | Bibliotecário logado não vê as telas públicas de login e cadastro (redireciona para a home). | `app/filters.php:68-72` |

## Defeitos a não reproduzir

| # | Defeito | Evidência | Achado |
|---|---|---|---|
| D9-01 | Qualquer visitante cria conta de bibliotecário; mensagem falsa de e-mail de ativação. | `app/routes.php:22,48`; `AccountController.php:72` | S01, S08 |
| D9-02 | Escrita e detalhe de exemplar sem autenticação no recurso `/books`. | `app/routes.php:67-68` | S02, S05 |
| D9-03 | Devolução por GET (`GET /issue-log/{id}/edit`). | `LogController.php:112-157`; `script.logs.js:65-67` | S06 |
| D9-04 | Escritas AJAX sem token CSRF; logout por GET. | `script.addbook.js:62-64`, `script.logs.js:41-43`, `script.student-approval.js:46-49`; `app/routes.php:122` | S06, S16 |
| D9-05 | Front depende do formato de exceção do modo debug (`error.message`). | `script.logs.js:52`; `app/config/app.php:16` | S07 |
| D9-06 | Contrato fora do padrão REST: busca em `GET /books/{texto}`, `GET /student` lista pendentes e `GET /student/create` lista aprovados, ações vazias expostas como rotas. | 01 seção 4.2 | INV-7 |
| D9-07 | Saída sem escape: `<%= %>` nos templates Underscore, `{{ }}` do Blade 4, `json_encode` cru dentro de `<script>`. | `app/views/underscore/search_book.blade.php:3-5`; `registration.blade.php:16-17`; `allbook.blade.php:50` | S03, S10 |
| D9-08 | `eval` sobre o corpo da resposta. | `script.addbook.js:70`; `script.mainpage.js:55,81` | S11 |
| D9-09 | Chave e credencial versionadas; debug ligado; URL base fixa em `http://localhost:8000`. | `app/config/app.php:16,29,81`; `app/config/local/database.php:25-28` | S04, S07, S15 |
| D9-10 | Data de devolução calculada sobre uma string `d-M` em vez do epoch. | `LogController.php:30-31` | INV-10 |
| D9-11 | Repopulação e erros do campo de usuário lidos de `login` em vez de `username`. | `create.blade.php:16-18`; `signin.blade.php:16-18` | T07 |
| D9-12 | Título "Student Registration Form" copiado na tela de busca pública. | `app/views/public/book-search.blade.php:10` | INV-10 |
| D9-13 | Filtro de curso sem opção "todos"; anos fixos de 2011 a 2015. | `approval.blade.php:14-32`; `students.blade.php:25-32` | INV-10 |
| D9-14 | Grafia `avaliable`/`avaliability` no JSON. | `BooksController.php:39,132` | INV-10 |
| D9-15 | `id` HTML duplicado na home. | `index.blade.php:74,89` | INV-10 |
| D9-16 | `between` aplicado como tamanho de string na validação de aluno; nenhuma verificação de existência de curso ou categoria; sem unicidade de matrícula e e-mail. | `StudentController.php:191-202` | S14 |
| D9-17 | Livros do aluno ordenados por `created_at`, que nunca é gravado; "Date of addition" exibe `updated_at`, que nunca é gravado. | `StudentController.php:99-103`; `search_issue.blade.php:12` | D03, T07 |
| D9-18 | Verificação de disponibilidade e limite fora da transação, sem bloqueio. | `LogController.php:49-97` | D01 |
| D9-19 | Datas como epoch em texto com sentinela `0`; prazo só exibido, nunca gravado. | `LogController.php:83-84,31` | D04 |
| D9-20 | Consultas N+1 e listagens sem paginação. | `BooksController.php:27-42`; `LogController.php:20-27` | S17, T03 |
| D9-21 | Cadastro de usuário sem o campo `name` (NOT NULL), aceito apenas porque o conector desliga o modo estrito. | `AccountController.php:63-66`; CT-07 | D05 |
| D9-22 | Validação só no navegador; nenhuma validação no servidor no cadastro de livro. | `script.addbook.js:53`; `BooksController.php:66` | D06, S14 |
| D9-23 | Script da tela de cadastro de livro depende de elementos que só existem na tela de listagem. | `script.addbook.js:84` | 01 seção 6.3 |
| D9-24 | Bibliotecas front-end em fim de vida copiadas no repositório; jQuery UI carregado sem uso; funções e variáveis globais implícitas; `console.log` de depuração; fonte carregada por `http://`. | 01 seção 8 | T06, T05 |
| D9-25 | Código morto e órfãos (métodos de `BaseController`, `IssueLog`, `Books::issues()`, `cms_user_apis`, configuração de reset de senha, `phpunit.xml`, `app/database/libraries`); `.gitignore` com comentários na mesma linha das regras. | 01 seções 9 e 12 | T05 |
| D9-26 | Controllers instanciam `HomeController` para reaproveitar consultas. | `BooksController.php:227,234,241` | T03 |
| D9-27 | Contadores (`books_issued`, `available_status`) sem reconciliação com o registro de empréstimos. | `LogController.php:94-96,143-145` | D03 |

## Caso em aberto

P16 tem um efeito colateral no legado: o bibliotecário logado não consegue usar a busca pública `/book` (`app/routes.php:16,60`, `app/filters.php:70`). Parece involuntário e não é falha de segurança. No sistema novo, `GET /api/public/books` atende visitante e usuário logado.
