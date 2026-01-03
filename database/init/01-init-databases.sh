#!/bin/bash
set -e

echo "Initializing PostgreSQL databases..."


psql --username "$POSTGRES_USER" --dbname postgres <<EOSQL
DO \$\$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_roles WHERE rolname = '${KEYCLOAK_DB_USER}'
  ) THEN
    CREATE ROLE ${KEYCLOAK_DB_USER}
      LOGIN
      PASSWORD '${KEYCLOAK_DB_PASSWORD}';
  END IF;
END
\$\$;
EOSQL

psql --username "$POSTGRES_USER" --dbname postgres <<EOSQL
SELECT format(
  'CREATE DATABASE %I OWNER %I',
  '${KEYCLOAK_DB}',
  '${KEYCLOAK_DB_USER}'
)
WHERE NOT EXISTS (
  SELECT 1 FROM pg_database WHERE datname = '${KEYCLOAK_DB}'
)\gexec
EOSQL


psql --username "$POSTGRES_USER" --dbname postgres <<EOSQL
REVOKE ALL ON DATABASE ${KEYCLOAK_DB} FROM PUBLIC;
GRANT CONNECT ON DATABASE ${KEYCLOAK_DB} TO ${KEYCLOAK_DB_USER};
GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};
EOSQL

echo "Database '${POSTGRES_DB}' e '${KEYCLOAK_DB}' pronti"
echo "Utente '${KEYCLOAK_DB_USER}' configurato correttamente"
