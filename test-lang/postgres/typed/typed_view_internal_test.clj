(ns postgres.typed.typed-view-internal-test
  (:require [postgres.typed.typed-common :as types]
            [postgres.typed.typed-view-internal :as view])
  (:use code.test))

^{:refer postgres.typed.typed-view-internal/dataview-entry :added "4.1"}
(fact "builds typed dataview entries and rejects invalid schema references"
  (let [table (types/make-table-def
                "fixture" "User"
                [(types/make-column-def :id
                  (types/make-type-ref :primitive nil :uuid))
                 (types/make-column-def :org-id
                  (types/make-type-ref :primitive nil :uuid))
                 (types/make-column-def :handle
                  (types/make-type-ref :primitive nil :citext))]
                :id)
        select-entry (view/dataview-entry
                      :select table
                      {:args [:uuid (symbol "i-org-id")]
                       :select {:org-id (symbol "i-org-id")}})
        return-entry (view/dataview-entry
                      :return table
                      {:args [:uuid (symbol "i-user-id")]
                       :return #{:*/data}})
        unknown-column (try
                         (view/dataview-entry
                          :select table {:select {:missing true}})
                         :accepted
                         (catch clojure.lang.ExceptionInfo ex
                           (select-keys (ex-data ex) [:table :column])))
        undeclared-argument (try
                              (view/dataview-entry
                               :select table {:select {:id (symbol "i-missing")}})
                              :accepted
                              (catch clojure.lang.ExceptionInfo ex
                                (select-keys (ex-data ex) [:symbols :declared])))
        identity-type (try
                        (view/dataview-entry
                         :return table
                         {:args [:text (symbol "i-id")]
                          :return [:id]})
                        :accepted
                        (catch clojure.lang.ExceptionInfo ex
                          (select-keys (ex-data ex) [:expected :actual :column])))
        unknown-identity (try
                           (view/dataview-entry
                            :return table
                            {:args [:uuid (symbol "i-id")]
                             :identity [:missing]
                             :return [:id]})
                           :accepted
                           (catch clojure.lang.ExceptionInfo ex
                             (select-keys (ex-data ex) [:table :column :identity])))]
    [select-entry
     return-entry
     unknown-column
     undeclared-argument
     identity-type
     unknown-identity]
    =>
    [{:input [{:symbol "i_org_id" :type "uuid"}]
      :view {:table "User"
             :type "select"
             :query {"org_id" "{{i_org_id}}"}}}
     {:input [{:symbol "i_user_id" :type "uuid"}]
      :view {:table "User"
             :type "return"
             :query ["*/data"]
             :identity ["id"]}}
     {:table "User" :column "missing"}
     {:symbols ['i-missing] :declared []}
     {:expected :uuid :actual :text :column "id"}
     {:table "User" :column "missing" :identity ["missing"]}]))
