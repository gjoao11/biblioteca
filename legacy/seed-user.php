<?php

// Executar exclusivamente no ambiente local de demonstracao.
// Roda dentro do conteiner, na raiz do projeto Laravel (/var/www/html).
// Cria o usuario de teste do legado, se ainda nao existir.

require __DIR__ . '/bootstrap/autoload.php';
$app = require __DIR__ . '/bootstrap/start.php';

$username = 'bibliotecario';

if (DB::table('users')->where('username', $username)->exists()) {
    echo "[seed] Usuario bibliotecario ja existe; nenhuma alteracao feita.\n";
    exit(0);
}

$now = date('Y-m-d H:i:s');

DB::table('users')->insert(array(
    'name' => 'Bibliotecario de Teste',
    'username' => $username,
    'password' => Hash::make('DemoBiblioteca123'),
    'verification_status' => 0,
    'remember_token' => '',
    'created_at' => $now,
    'updated_at' => $now
));

echo "[seed] Usuario bibliotecario criado.\n";