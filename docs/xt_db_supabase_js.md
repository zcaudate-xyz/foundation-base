# Native supabase-js adapter for xt.db

The existing "supabase" adapter uses Foundation's HTTP/PostgREST client. The
JavaScript-only "supabase-js" adapter accepts an already-created
@supabase/supabase-js client and translates the existing xt.db selection tree
into the native query builder.

That makes it suitable for browser pages that already own a Supabase session,
such as statstrade-v1. Pass the shared client through the primary database
configuration:

\`\`\`clojure
(var supabase-client (sb/getClient))
(main/create-impl
 "supabase-js"
 {"client" supabase-client}
 schema
 lookup)
\`\`\`

In statstrade-v1, sb/getClient can remain the singleton used by the page
queries. The adapter does not create a second auth session when "client" is
provided.

The adapter currently implements ISourceRemote and the listener storage
protocols. Use a "memory" caching service for page-model cache/sync flows.
Realtime subscriptions remain on the existing HTTP adapter until a native
Realtime implementation is added.
