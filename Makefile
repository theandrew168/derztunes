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

.PHONY: deploy
deploy: build
	scp target/derztunes.jar derz@derztunes.com:/tmp/derztunes.jar
	ssh -t derz@derztunes.com sudo install /tmp/derztunes.jar /usr/local/bin/derztunes.jar
	ssh -t derz@derztunes.com sudo systemctl restart derztunes

.PHONY: clean
clean:
	clj -T:build clean
