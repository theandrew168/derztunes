.POSIX:
.SUFFIXES:

.PHONY: default
default: build

.PHONY: build
build:
	clj -T:build jar

.PHONY: run
run:
	clj -M:run

.PHONY: repl
repl:
	clj -M:repl

.PHONY: migrate
migrate:
	psql -f resources/migration/0001_create_track_table.sql postgresql://postgres:postgres@localhost:5432/postgres

.PHONY: test
test:
	clj -M:test

.PHONY: clean
clean:
	clj -T:build clean
