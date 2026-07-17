^{:seedgen/skip true}
(ns xt.db.node.example-auth-profile-ui-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [scaffold.supabase.local-min :as local-min]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :test-mode true
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.example-auth-profile :as example]
             [xt.db.node.example-auth-profile-ui :as ui]
             [xt.substrate :as substrate]]})

(defn.js local-client-defaults
  "returns browser client defaults for scaffold/supabase local-min"
  {:added "4.1"}
  []
  (return
   {:host (@! (-> local-min/+config+ :api :hostname))
    :port (@! (-> local-min/+config+ :api :port))
    :secured false
    :apikey (@! (-> local-min/+config+ :api :anon-key))}))

(def ^:private +notify-url+
  (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/"))

(fact:global
 {:setup [(local-min/start-supabase)
          (l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto +notify-url+ 5000)]
  :teardown [(l/rt:stop)
             (local-min/stop-supabase nil)]})

^{:refer xt.db.node.example-auth-profile-ui/mount-playground :added "4.1"}
(fact "mounts the interactive auth/profile UI and preserves its service"
  (notify/wait-on [:js 10000]
    (:= (!:G UI_TITLE) nil)
    (:= (!:G UI_STAGE) nil)
    (:= (!:G React)
        {"createElement" (fn [component props]
                            (return [component props]))})
    (:= window.PLAYGROUND
        {"setTitle" (fn [title] (:= (!:G UI_TITLE) title))
         "setStage" (fn [stage] (:= (!:G UI_STAGE) stage))})
    (var node
         (ui/mount-playground
          (-/local-client-defaults)
          {"space_id" "room/ui"
           "group_id" "auth"}))
    (repl/notify
     [(!:G UI_TITLE)
      (xt/x:is-array? (!:G UI_STAGE))
      (xt/x:not-nil?
       (substrate/get-service node example/DEFAULT_SERVICE_ID))]))
  => ["Supabase auth profile" true true])


^{:refer xt.db.node.example-auth-profile-ui/error-message :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile-ui/refresh-auth-state :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile-ui/run-action :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile-ui/field-style :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile-ui/input-style :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile-ui/button-style :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile-ui/AuthProfileApp :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile-ui/render-playground :added "4.1"}
(fact "TODO")