.DEFAULT_GOAL := help

LEIN ?= lein
LEIN_FLAGS ?=
PYTHON ?= python3
PORT ?= 8000
MANAGE_PORT ?= 8130
BIND ?= 127.0.0.1
WATCH_FLAGS ?=

.PHONY: help all plan analyse preview generate build check test test-headless watch serve manage

help:
	@printf '%s\n' \
	  'Dupasupa — app.manage workflow' \
	  '  make plan           Show target dependency order' \
	  '  make analyse        Validate and render in memory' \
	  '  make preview        Preview generated-file changes' \
	  '  make generate       Write generated files without running checks' \
	  '  make build          Generate and run declared DSL integration checks' \
	  '  make check          Check generated files against fresh output' \
	  '  make test           Run integration and live database/model/worker checks' \
	  '  make test-headless  Build and run declared DSL integration checks only' \
	  '  make watch          Build, then watch/reload/rebuild source changes' \
	  '  make serve          Build and run Next.js/React + local PostgreSQL on port 8000' \
	  '  make manage         Start the management UI at localhost:8130' \
	  'Use LEIN_FLAGS=-o for cached dependencies; WATCH_FLAGS=--no-tests skips watch checks.'

all: build

plan:
	$(LEIN) $(LEIN_FLAGS) app plan

analyse:
	$(LEIN) $(LEIN_FLAGS) app analyse

preview:
	$(LEIN) $(LEIN_FLAGS) app generate

generate:
	$(LEIN) $(LEIN_FLAGS) app generate --write

build:
	$(LEIN) $(LEIN_FLAGS) app build

check:
	$(LEIN) $(LEIN_FLAGS) app check

test:
	$(LEIN) $(LEIN_FLAGS) app test

test-headless:
	$(LEIN) $(LEIN_FLAGS) app test

watch:
	$(LEIN) $(LEIN_FLAGS) app watch $(WATCH_FLAGS)

serve: build
	$(LEIN) $(LEIN_FLAGS) run -m dupasupa.dev

manage:
	$(LEIN) $(LEIN_FLAGS) manage --port $(MANAGE_PORT)
