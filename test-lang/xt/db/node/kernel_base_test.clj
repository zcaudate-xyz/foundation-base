(ns xt.db.node.kernel-base-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.kernel-base :as kernel]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.db.helpers.data-main-test :as sample]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})


^{:refer xt.db.node.kernel-base/kernel-create-config :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-check-exists :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-setup-single :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-teardown-single :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-setup-main :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-setup-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-teardown-main :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-teardown-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-init-main :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-init-handler :added "4.1"}
(fact "TODO")
