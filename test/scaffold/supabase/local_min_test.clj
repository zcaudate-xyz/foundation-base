(ns scaffold.supabase.local-min-test
  (:use code.test)
  (:require [std.lib.os :as os]
            [std.lib.network :as network]
            [scaffold.supabase.local-min :as local-min]))

^{:refer scaffold.supabase.local-min/start-supabase :added "4.1"}
(fact "recovers when only the database portion of local-min is running"
  (let [calls (atom [])
        results (atom [{:exit 0
                        :out ""
                        :err (str "supabase start is already running.\n"
                                  "Stopped services: [supabase_kong_local-min supabase_auth_local-min]")}
                       {:exit 0 :out "stopped" :err ""}
                       {:exit 0 :out "started" :err ""}])]
    (with-redefs [os/sh (fn [{:keys [args]}]
                         (swap! calls conj args)
                         :process)
                  os/sh-output (fn [_]
                                 (let [result (first @results)]
                                   (swap! results rest)
                                   result))
                  network/wait-for-port (constantly true)
                  local-min/wait-for-auth-ready (constantly true)]
      (local-min/start-supabase)
      => true

      @calls
      => [["supabase" "start" "--workdir" "docker/local-min"]
          ["supabase" "stop" "--workdir" "docker/local-min"]
          ["supabase" "start" "--workdir" "docker/local-min"]])))

^{:refer scaffold.supabase.local-min/stop-supabase :added "4.1"}
(fact "stops the supabase")


^{:refer scaffold.supabase.local-min/shutdown-supabase :added "4.1"}
(fact "TODO")