(ns postgres.typed.export-test
  (:require [postgres.typed.typed-common :as types])
  (:use code.test))

(def +shape-fn+
  (types/make-fn-def
   "demo.core"
   "create-user"
   [(types/->FnArg 'm :jsonb [:jsonb] :payload)]
   :jsonb
   {:raw-body '[(pg/t:insert 'UserAccount {:id "u1"})]}
   nil))

(def +manual-sync-fn+
  (types/make-fn-def
   "demo.core"
   "create-user-manual"
   []
   :jsonb
   {:raw-body '[(pg/t:insert 'UserAccount {:id "u1"})]
    :sync/mode :manual
    :sync/tables ['UserAccount 'UserProfile]}
   nil))

(def +read-fn+
  (types/make-fn-def
   "demo.core"
   "get-user"
   []
   :jsonb
   {:raw-body '[(pg/t:get 'UserAccount {:where {:id "u1"}})]}
   nil))

(defn ensure-fixtures!
  []
  (swap! types/*type-registry*
         assoc
         'demo.core/UserAccount
         (types/make-table-def 'demo.core 'UserAccount [] :id nil nil nil)))

(ensure-fixtures!)
