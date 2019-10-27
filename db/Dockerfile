FROM mysql:5.6

# Files in /docker-entrypoint-initdb.d/ will run in alphabetic order
# Use numbered prefixes to set order .sql files will run
COPY build.sql /docker-entrypoint-initdb.d/1_build.sql
COPY test_data.sql /docker-entrypoint-initdb.d/2_test_data.sql

ENTRYPOINT ["docker-entrypoint.sh", "mysqld"]
