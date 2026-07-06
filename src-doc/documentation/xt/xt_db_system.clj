(ns documentation.xt-db-system
  (:use code.test))

[[:hero {:title "xt.db.system"
         :subtitle "Memory, postgres, sqlite, supabase, and websocket systems."
         :lead "`xt.db.system` provides backend implementations for the same database model, from in-memory graphs to postgres, sqlite, Supabase, realtime, and websocket-backed systems."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"The system layer lets tests and applications swap the storage backend while preserving the higher-level database contract. This is why parity tests exist for memory, SQL, and Supabase paths."

[[:chapter {:title "Internal usage" :link "internal"}]]

"The main system namespaces are exercised by `test-lang/xt/db/system/*`. POC tests use these implementations to validate worker, shared-worker, adapter, and Supabase flows."

[[:chapter {:title "API" :link "api"}]]

