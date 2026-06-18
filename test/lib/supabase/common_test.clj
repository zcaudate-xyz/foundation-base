(ns lib.supabase.common-test
  (:use code.test)
  (:require [lib.supabase.common :as common]
            [scaffold.supabase.local-min :as local-min]
            [hara.lang :as l]))

(l/script- :postgres
  {:runtime :jdbc.client
   :test-mode true
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

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer lib.supabase.common/callback :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/authorize :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/admin-generate-link :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/settings :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/health :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/otp :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/verify-post :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/token-password :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/admin-create-user :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/invite :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/recovery :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/signup :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/user-get :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/token-refresh :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/admin-get-user :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/admin-delete-user :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/user-put :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/logout :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/admin-update-user :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/admin-list-users :added "4.1"}
(fact "TODO")

^{:refer lib.supabase.common/verify-get :added "4.1"}
(fact "TODO")
