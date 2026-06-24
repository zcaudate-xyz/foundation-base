(ns xt.db.poc.browser-auth-debug-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [xt.lang.common-repl :as repl]
            [xt.lang.spec-base :as xt]
            [xt.db.poc.browser-supabase-auth-test :as auth-test]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.browser-auth-debug-test/worker-load-debug
  :added "4.1"}
(fact "manual sharedworker load with error capture"
  (notify/wait-on [:js 60000]
    (var messages [])
    (var blob (new Blob [(@! auth-test/+sharedworker-script+)] {"type" "text/javascript"}))
    (var url (. (!:G URL) (createObjectURL blob)))
    (var shared (new SharedWorker url {"type" "module"}))
    (var port (. shared ["port"]))
    (. port (start))
    (. port (addEventListener
              "message"
              (fn [event]
                (var data (. event ["data"]))
                (. messages (push {"kind" "message" "data" data}))
                (when (== (xt/x:get-key data "signal") "ready")
                  (repl/notify messages)))
              false))
    (. shared (addEventListener
               "error"
               (fn [event]
                 (. messages (push {"kind" "error"
                                    "message" (. event ["message"])
                                    "filename" (. event ["filename"])
                                    "lineno" (. event ["lineno"])
                                    "colno" (. event ["colno"])
                                    "error" (. event ["error"])}))
                 (repl/notify messages))
               false))
    (. shared (addEventListener
               "messageerror"
               (fn [event]
                 (. messages (push {"kind" "messageerror"}))
                 (repl/notify messages))
               false))
    (return shared))
  => (contains-in
      [{"kind" "message" "data" {"signal" "ready"}}]))
