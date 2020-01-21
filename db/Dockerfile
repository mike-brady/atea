# docker run --env-file .db.env --name atea_db -p 3306:3306 -d atea_db:latest

FROM mysql:5.6

# Files in /docker-entrypoint-initdb.d/ will run in alphabetic order
# Use numbered prefixes to set order .sql files will run
COPY build.sql /docker-entrypoint-initdb.d/1_build.sql
COPY words.sql /docker-entrypoint-initdb.d/2_words.sql
COPY abbreviations.sql /docker-entrypoint-initdb.d/3_abbreviations.sql
COPY examples.sql /docker-entrypoint-initdb.d/4_examples.sql
# COPY test_data.sql /docker-entrypoint-initdb.d/5_test_data.sql

ENTRYPOINT ["docker-entrypoint.sh", "mysqld"]
