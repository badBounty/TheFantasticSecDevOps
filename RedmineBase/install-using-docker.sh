# Install postgres
docker run -d --name postgres-vm -e POSTGRES_PASSWORD=secret -e POSTGRES_USER=redmine -p 5432:5432 postgres

# Install redmine
docker run -d --name redmine-vm -e REDMINE_DB_POSTGRES=postgres-vm -e REDMINE_DB_USERNAME=redmine -e REDMINE_DB_PASSWORD=secret -p 3000:3000 --link postgres-vm:postgres redmine

# Copy sql inserts to redmine
docker cp redmine_inserts.sql postgres-vm:/redmine_inserts.sql

# Config redmine
docker exec -it postgres-vm psql -U redmine -d redmine -a -f /redmine_inserts.sql