# Ambiente original do projeto: Laravel 4.2 + PHP 5.6 + mcrypt (ver README)
FROM php:5.6-apache

# Debian Stretch saiu de suporte: os pacotes agora ficam em archive.debian.org
RUN sed -i -e 's|deb.debian.org|archive.debian.org|g' \
           -e 's|security.debian.org|archive.debian.org|g' \
           -e '/stretch-updates/d' /etc/apt/sources.list \
 && apt-get -o Acquire::Check-Valid-Until=false update \
 && apt-get install -y --no-install-recommends --allow-unauthenticated \
        libmcrypt-dev git unzip \
 && docker-php-ext-install mcrypt pdo_mysql \
 && a2enmod rewrite \
 && echo 'date.timezone = America/Sao_Paulo' > /usr/local/etc/php/conf.d/timezone.ini \
 && echo 'ServerName localhost' > /etc/apache2/conf-enabled/servername.conf \
 && rm -rf /var/lib/apt/lists/*

# Composer 2.2 LTS é a última versão que ainda roda em PHP 5.3+
COPY --from=composer:2.2 /usr/bin/composer /usr/bin/composer

# A pasta public/ do Laravel é a raiz do site
ENV APACHE_DOCUMENT_ROOT=/var/www/html/public
RUN sed -ri 's!/var/www/html!${APACHE_DOCUMENT_ROOT}!g' /etc/apache2/sites-available/*.conf \
 && printf '<Directory /var/www/html/public>\n    AllowOverride All\n</Directory>\n' \
        > /etc/apache2/conf-enabled/laravel.conf

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

WORKDIR /var/www/html
ENTRYPOINT ["entrypoint.sh"]
CMD ["apache2-foreground"]
