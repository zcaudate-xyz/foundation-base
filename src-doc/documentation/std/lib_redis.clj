(ns documentation.lib-redis
  (:use code.test))

[[:hero {:title "lib.redis"
         :subtitle "pooled Redis clients, scripts, events, and lifecycle integration"
         :lead "Create RESP connection pools and compose them with optional local services, event listeners, notifications, and script state."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"A Redis client map contains identity, host and port settings, runtime script and listener state, and an active connection pool after startup. Component wrappers coordinate supporting lifecycle steps."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Create and start a client"}]]

(comment
  (require '[lib.redis :as redis])

  (def client
    (redis/client-create
     {:host "localhost"
      :port 6379
      :mode :eval}))

  (def started
    (redis/client:start client)))

[[:section {:title "Inspect and close"}]]

(comment
  (redis/client-string started)
  (:pool started)
  @(:runtime started)
  (redis/client:stop started))

[[:chapter {:title "Supporting namespaces" :link "supporting"}]]

"`lib.redis.event` manages listeners and notification loops. `lib.redis.script` and `lib.redis.extension` support script registration and command extensions. `lib.redis.bench` integrates local test services."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.redis"}]]
[[:api {:namespace "lib.redis.event"}]]
[[:api {:namespace "lib.redis.script"}]]
[[:api {:namespace "lib.redis.extension"}]]
