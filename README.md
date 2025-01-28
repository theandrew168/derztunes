# derztunes

A simple music site based on old school iTunes

## Setup

This project depends on the [Clojure programming language](https://clojure.org/).
I like to use a [POSIX-compatible Makefile](https://pubs.opengroup.org/onlinepubs/9699919799.2018edition/utilities/make.html) to facilitate the various project operations but traditional [clj commands](https://clojure.org/guides/deps_and_cli) will work just as well.

## Building

To build the application into a standalone JAR, run:

```
make
```

The resulting JAR will be placed into the `target/` directory.

## Running

The result of building this project is a single "uberjar" containing all source code, dependencies, and relevant resources (like migrations and public web files).
This JAR can be executed by any Java Runtime Environment ([JRE](https://code.launchpad.net/ubuntu/noble/+package/default-jre-headless)):

```
java -jar derztunes.jar
```

## Configuration

This project's configuration file is written in Extensible Data Notation ([EDN](https://github.com/edn-format/edn)).
The available configuration values are as follows:

| Name     | Description                             | Required? |
| -------- | --------------------------------------- | --------- |
| `db-uri` | PostgreSQL database connection string   | Yes       |
| `s3-uri` | S3-compatible storage connection string | Yes       |

By default, a config file named `derztunes.edn` is looked for in the current working directory from which the program was executed.
This path can be overridden by supplying a `-conf` flag:

```
java -jar derztunes.jar -conf /etc/derztunes.edn
```

## Syncing

To sync the project's PostgreSQL database with the audio files present in the project's S3 bucket, run the program with the `-sync` flag:

```
java -jar derztunes.jar -sync
```

## Local Development

### Services

This project depends on various services.
To develop locally, you'll need to run these services locally somehow or another.
I find [Docker](https://www.docker.com/) to be a nice tool for this but you can do whatever works best.

- [PostgreSQL](https://www.postgresql.org/) - for persistent storage (site data)
- [Minio](https://min.io/) - for object storage (music files)

The following command starts the necessary containers:

```
docker compose up -d
```

These containers can be stopped via:

```
docker compose down
```

### Running

To start the web server:

```
make run
```

To apply any pending database migrations:

```
make migrate
```

### Testing

Unit and integration tests can be ran after starting the aforementioned services:

```
make test
```
