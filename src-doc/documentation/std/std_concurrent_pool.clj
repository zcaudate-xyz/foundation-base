(ns documentation.std-concurrent-pool
  (:use code.test))

[[:hero {:title "std.concurrent.pool"
         :subtitle "bounded reusable resource pools with lifecycle and health checks"
         :lead "Manage expensive reusable objects with acquisition tracking, idle cleanup, disposal, and component lifecycle support."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"A pool separates resource construction from resource use. It tracks idle and busy objects, enforces a maximum, records utilization, and can dispose unhealthy or long-idle resources in the background."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Create a pool"}]]

(comment
  (require '[std.concurrent.pool :as pool])

  (def connections
    (pool/pool
     {:size 2
      :max 8
      :keep-alive 30000
      :poll 5000
      :resource {:create (fn [] {:opened-at (System/currentTimeMillis)})
                 :start identity
                 :stop  (fn [_] nil)
                 :health (fn [_] {:status :ok})
                 :initial 0.5}})))

[[:section {:title "Acquire and release safely"}]]

"Use `pool:with-resource` for normal work. It releases the object even when the body throws. Lower-level code can call `pool:acquire` and `pool:release` directly."

(comment
  (pool/pool:with-resource [connection connections]
    (:opened-at connection))

  (let [[id connection] (pool/pool:acquire connections)]
    (try
      connection
      (finally
        (pool/pool:release connections id)))))

[[:section {:title "Inspect and dispose"}]]

(comment
  (pool/pool:info connections)
  (pool/pool:resources:busy connections)
  (pool/pool:resources:idle connections)
  (pool/pool:cleanup connections)
  (pool/pool:stop connections))

"Call `pool:dispose:mark` inside a pooled operation when the current resource should be discarded rather than returned to idle storage."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.pool"}]]
