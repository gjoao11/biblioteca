#!/bin/sh
set -e
cd /var/www/html

: "${APP_KEY:?defina APP_KEY no .env}"
[ "${#APP_KEY}" -eq 32 ] || { echo "APP_KEY precisa ter 32 caracteres"; exit 1; }
: "${MYSQL_DATABASE:?defina MYSQL_DATABASE no .env}"
: "${MYSQL_USER:?defina MYSQL_USER no .env}"
: "${MYSQL_PASSWORD:?defina MYSQL_PASSWORD no .env}"

[ -d vendor ] || composer install --no-interaction

# troca a chave versionada pela do .env
sed -i "s|'key' => '[^']*'|'key' => '${APP_KEY}'|" app/config/app.php

# gera a configuracao do MySQL a partir do .env
cat > app/config/mysql.config.php <<PHP
<?php

define("MYSQL_HOSTNAME", "db");
define("MYSQL_DATABASE", "${MYSQL_DATABASE}");
define("MYSQL_USERNAME", "${MYSQL_USER}");
define("MYSQL_PASSWORD", "${MYSQL_PASSWORD}");
PHP

chmod -R 777 app/storage
php artisan migrate --force

exec "$@"