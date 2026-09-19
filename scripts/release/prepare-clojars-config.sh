#!/usr/bin/env bash

set -euo pipefail

mode="${1:-package}"

case "$mode" in
  package)
    ;;
  deploy)
    test -n "${CLOJARS_USERNAME:-}" || {
      echo "CLOJARS_USERNAME is required for Clojars deployment" >&2
      exit 1
    }
    test -n "${CLOJARS_PASSWORD:-}" || {
      echo "CLOJARS_PASSWORD is required for Clojars deployment" >&2
      exit 1
    }
    ;;
  *)
    echo "usage: $0 [package|deploy]" >&2
    exit 2
    ;;
esac

mkdir -p config
umask 077
cat > config/repositories.edn <<'EOF'
{"clojars" {:id "clojars"
            :url "https://repo.clojars.org/"
            :authentication {:username [:env "CLOJARS_USERNAME"]
                             :password [:env "CLOJARS_PASSWORD"]}}}
EOF
