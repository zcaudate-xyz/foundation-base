(ns xt.db.runtime.supabase-pull-live-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.db.helpers.supabase-pull-live-test :as live]))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-fetch :as js-fetch]
             [xt.db.instance :as xdb]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]]})

(defn.js make-live-client
  [input]
  (var raw (xt/x:obj-clone (or input {})))
  (xt/x:set-key
   raw
   "request_sync"
   (fn [request _opts]
     (var args ["-sS"
                "-X" (or (xt/x:get-key request "method") "GET")
                "-w" "\n%{http_code}"
                (xt/x:get-key request "url")])
     (var headers (xt/x:obj-clone (or (xt/x:get-key request "headers") {})))
     (when (and (== "GET" (or (xt/x:get-key request "method") "GET"))
                (xt/x:not-nil? (xt/x:get-key headers "Content-Profile"))
                (xt/x:nil? (xt/x:get-key headers "Accept-Profile")))
       (xt/x:set-key headers
                     "Accept-Profile"
                     (xt/x:get-key headers "Content-Profile")))
     (xt/for:object [[k v] headers]
       (xt/x:arr-push args "-H")
       (xt/x:arr-push args (xt/x:cat k ": " v)))
     (when (xt/x:not-nil? (xt/x:get-key request "body"))
       (xt/x:arr-push args "-H")
       (xt/x:arr-push args "Content-Type: application/json")
       (xt/x:arr-push args "-d")
       (xt/x:arr-push args
                      (xt/x:json-encode (xt/x:get-key request "body"))))
     (var proc (. (require "child_process")
                  (spawnSync "curl" args {"encoding" "utf8"})))
     (when (not= 0 (. proc ["status"]))
       (xt/x:err (or (. proc ["stderr"]) "curl failed")))
     (var stdout (or (. proc ["stdout"]) ""))
     (var idx (. stdout (lastIndexOf "\n")))
     (var body (:? (>= idx 0)
                   (. stdout (substring 0 idx))
                   stdout))
     (var status-str (:? (>= idx 0)
                         (. stdout (substring (+ idx 1)))
                         "200"))
     (return {"status" (parseInt status-str 10)
              "body" (js-fetch/decode-body body)})))
  (return (fetch/client-create raw {})))

(fact:global
  {:setup [(l/rt:restart)
           (do (when live/CANARY-SUPABASE-LIVE
                 (live/init-live-postgres-runtime!)
                (l/rt:setup (live/pg-rt) live/+postgres-module+)
                 (live/grant-scratch-schema!)
                (live/reload-postgrest!)
                (live/refresh-live-supabase-config!)
                (live/cleanup-scratch-entry! live/+live-entry-name+))
               true)]
   :teardown [(do (when live/CANARY-SUPABASE-LIVE
                    (live/cleanup-scratch-entry! live/+live-entry-name+)
                    (l/rt:teardown (live/pg-rt) live/+postgres-module+))
                 (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                 (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                 true)
              (l/rt:stop)]})

^{:refer xt.db.instance/db-create :added "4.1.3"}
(fact "creates a live db.supabase instance backed by a js fetch client for scratch-v1"

  (if live/CANARY-SUPABASE-LIVE
    (!.js
     (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
     (var client (-/make-live-client (xt/x:obj-clone (. instance ["client"]))))
     (xt/x:set-key instance "client" client)
     (var db (xdb/db-create instance nil nil {}))
     [(. db ["::"])
      (fetch/client? (. (. db ["instance"]) ["client"]))
      (. (. (. (. db ["instance"]) ["client"]) ["_raw"]) ["schema_name"])])
    :supabase-live-unavailable)
  => (any ["db.supabase" true "scratch"]
          :supabase-live-unavailable))

^{:refer xt.db.instance/db-pull-sync :added "4.1.3"}
(fact "pulls scratch-v1 data through a live supabase instance"

  (if live/CANARY-SUPABASE-LIVE
    (do
      (live/setup-scratch-entry! live/+live-entry-name+ live/+live-entry-tags+)
      (try
        (!.js
         (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
         (var client (-/make-live-client (xt/x:obj-clone (. instance ["client"]))))
         (xt/x:set-key instance "client" client)
         (var db (xdb/db-create instance nil nil {}))
         (xdb/db-pull-sync db nil (@! live/+live-entry-query+)))
        (finally
          (live/cleanup-scratch-entry! live/+live-entry-name+))))
    :supabase-live-unavailable)
  => (any [{"name" "copilot_supabase_pull_live"
            "tags" ["copilot" "supabase" "pull"]}]
          :supabase-live-unavailable))
