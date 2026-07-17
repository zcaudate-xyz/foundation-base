(ns postgres.gen.rpc-test
  (:use code.test)
  (:require [postgres.gen.rpc :as rpc]))

(fact "typed output shapes enumerate root and nested returned rows"
  (rpc/shape-rows
   {:fields
    {:request {:shape {:source-table "AccessRequest" :fields {}}}
     :role {:shape {:source-table "AccessRole" :fields {}}}}})
  => [["AccessRequest" ["request"] :one]
      ["AccessRole" ["role"] :one]])

(fact "entity aggregate and topic ids are inferred by convention"
  (rpc/notification-plan
   {:function-name 'app.fn/brand-member-add
    :inputs [{:name "i-user-id"} {:name "i-brand-id"}]
    :report {:analysis {:mutating true
                        :source-tables ["Brand" "AccessRole"]}}
    :output-shape {:source-table "AccessRole" :fields {}}})
  => {:aggregate "Brand"
      :topic-id {:kind :arg :name "i-brand-id"}
      :event "db/sync"
      :rows [["AccessRole" [] :one]]
      :private true})

(fact "super-user convention targets the affected user"
  (:topic-id
   (rpc/notification-plan
    {:function-name 'app.fn/super-user-update
     :inputs [{:name "i-user-id"} {:name "i-target-id"}]
     :report {:analysis {:mutating true :source-tables ["User"]}}
     :output-shape {:source-table "User" :fields {}}}))
  => {:kind :arg :name "i-target-id"})

(fact "canonical requests are private entity-topic db sync forms"
  (rpc/realtime-send-form
   {:aggregate "Campaign"
    :topic-id {:kind :arg :name "i-campaign-id"}
    :event "db/sync"
    :rows [["AccessRequest" ["request"] :one]
           ["AccessRole" ["role"] :one]]
    :private true}
   {:auth-form '(auth/uid)
    :result-sym 'o-result
    :arg-form #(symbol (subs % 2))})
  => '(s/realtime-send-request
       (|| "Campaign:" campaign-id)
       {"db/sync"
        {"AccessRequest" [(:-> o-result "request")]
         "AccessRole" [(:-> o-result "role")]}}
       true))

(fact "notification payloads retain every row path for the same table"
  (= '{"db/sync"
       {"Prospect" [(:-> (:-> o-result "prospects") "winning")
                    (:-> (:-> o-result "prospects") "losing")]}}
     (rpc/notification-payload
      {:event "db/sync"
       :rows [["Prospect" ["prospects" "winning"] :one]
              ["Prospect" ["prospects" "losing"] :one]]}
      'o-result))
  => true)

(fact "array rows stay flat and removals extract every id"
  [(rpc/notification-payload
    {:event "db/sync"
     :rows [["Prospect" ["winning"] :many]
            ["Prospect" ["losing"] :many]]}
    'o-result)
   (rpc/notification-payload
    {:event "db/remove"
     :rows [["RevLog" ["logs"] :many]]}
    'o-result)]
  => '[{"db/sync" {"Prospect" (|| (:-> o-result "winning")
                                      (:-> o-result "losing"))}}
       {"db/remove" {"RevLog" (pg/jsonb-path-query-array
                                  (:-> o-result "logs")
                                  "$[*].id")}}])
