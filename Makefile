run-test:
	docker run --rm --name foundation-base-dev --network host -v /var/run/docker.sock:/var/run/docker.sock -v $$(pwd):$$(pwd) -w $$(pwd) ghcr.io/zcaudate-xyz/infra-foundation-dev:main lein test
	
start-repl:
	docker run -it --rm --name foundation-base-dev -p 51311:51311 -p 5432:5432 -v /var/run/docker.sock:/var/run/docker.sock -v $$(pwd):$$(pwd) -w $$(pwd) ghcr.io/zcaudate-xyz/infra-foundation-dev:main lein repl
	
start-bash:
	docker run -it --rm --name foundation-base-dev -p 51311:51311 -v /var/run/docker.sock:/var/run/docker.sock -v $$(pwd):$$(pwd) -w $$(pwd) ghcr.io/zcaudate-xyz/infra-foundation-dev:main /bin/bash

start-pg:
	docker run --name foundation-base-pg -e POSTGRES_DB=test -e POSTGRES_PASSWORD=postgres -e POSTGRES_PORT=5432 -e POSTGRES_USER=postgres -d -p 5432:5432 ghcr.io/zcaudate-xyz/infra-db:main

# Foundation Symbol Index Targets
# See README_INDEX.md for documentation

index:
	./bin/foundation-index index

index-force:
	./bin/foundation-index index --force

index-stats:
	./bin/foundation-index stats

search:
	./bin/foundation-index search "$(QUERY)" "$(KIND)" "$(NAMESPACE)" "$(LIMIT)"

symbol:
	./bin/foundation-index symbol "$(NAME)"

list-namespaces:
	./bin/foundation-index list-namespaces

.PHONY: index index-force index-stats search symbol list-namespaces
	