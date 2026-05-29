run-test:
	docker run --rm --name foundation-base-dev --network host -v /var/run/docker.sock:/var/run/docker.sock -v $$(pwd):$$(pwd) -w $$(pwd) ghcr.io/zcaudate-xyz/infra-foundation-dev:main lein test
	
demo-xtdb-backbone:
	./lein trampoline run -m demo-xtdb-backbone.build

demo-xtdb-backbone-sql:
	./lein trampoline run -m demo-xtdb-backbone.build.scratch-v0

demo-xtdb-backbone-supabase:
	cd docker && (npx supabase stop --no-backup || true)
	cd docker && npx supabase start -x logflare --ignore-health-check
	sleep 10
	$(MAKE) demo-xtdb-backbone-sql
	psql "postgresql://postgres:postgres@127.0.0.1:55122/postgres" -v ON_ERROR_STOP=1 -f .build/demo-xtdb-backbone-scratch-v0/sql/scratch_v0.sql
	psql "postgresql://postgres:postgres@127.0.0.1:55122/postgres" -v ON_ERROR_STOP=1 \
		-c 'GRANT USAGE ON SCHEMA scratch_v0 TO anon, authenticated, service_role;' \
		-c 'GRANT SELECT ON TABLE scratch_v0."Log" TO anon, authenticated, service_role;' \
		-c 'GRANT EXECUTE ON FUNCTION scratch_v0.ping() TO anon, authenticated, service_role;' \
		-c 'REVOKE ALL ON FUNCTION scratch_v0.log_append(text) FROM PUBLIC, anon;' \
		-c 'GRANT EXECUTE ON FUNCTION scratch_v0.log_append(text) TO authenticated, service_role;' \
		-c "NOTIFY pgrst, 'reload schema';"
	@supabase_base_url="$$(./lein trampoline run -m clojure.main -e "(do (require 'demo-xtdb-backbone.app.config) (print (demo-xtdb-backbone.app.config/supabase-base-url)))")"; \
	anon_key="$$(./lein trampoline run -m clojure.main -e "(do (require 'demo-xtdb-backbone.app.config) (print (demo-xtdb-backbone.app.config/supabase-api-key)))")"; \
	for attempt in $$(seq 1 90); do \
		status="$$(curl -sS -o /tmp/demo_xtdb_backbone_supabase_probe.json -w '%{http_code}' \
			-H "apikey: $$anon_key" \
			-H "Authorization: Bearer $$anon_key" \
			-H "Accept-Profile: scratch_v0" \
			-H "Content-Profile: scratch_v0" \
			"$$supabase_base_url/rest/v1/Log?select=*&limit=1")"; \
		if [ "$$status" = "200" ]; then \
			echo "scratch_v0 is ready at $$supabase_base_url/rest/v1"; \
			exit 0; \
		fi; \
		sleep 1; \
	done; \
	cat /tmp/demo_xtdb_backbone_supabase_probe.json; \
	exit 1

demo-xtdb-backbone-start:
	$(MAKE) demo-xtdb-backbone
	cd .build/demo-xtdb-backbone && $(MAKE) start

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

.PHONY: demo-xtdb-backbone demo-xtdb-backbone-sql demo-xtdb-backbone-supabase demo-xtdb-backbone-start index index-force index-stats search symbol list-namespaces
	