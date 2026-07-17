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
       (|| "realtime:Campaign:" campaign-id)
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


^{:refer postgres.gen.rpc/snake-name :added "4.1"}
(fact "normalizes symbols, keywords, and underscores for inference"
  (rpc/snake-name 'Brand_MEMBER_ADD) => "brand-member-add"
  (rpc/snake-name :I_USER_ID) => "i-user-id"
  (rpc/snake-name nil) => nil)

^{:refer postgres.gen.rpc/infer-aggregate :added "4.1"}
(fact "infers aggregate by explicit override, command token, then table prefix"
  (rpc/infer-aggregate {:notify {:entity :Campaign}}) => "Campaign"
  (rpc/infer-aggregate {:function-name 'app.fn/brand-member-add}) => "Brand"
  (rpc/infer-aggregate {:function-name 'app.fn/update-record
                        :source-tables ["AccessRole" "UserProfile"]}) => "User"
  (rpc/infer-aggregate {:function-name 'app.fn/update-record
                        :output-shape {:source-table "TopicEntry"}}) => "Topic"
  (rpc/infer-aggregate {:function-name 'app.fn/update-record}) => nil)

^{:refer postgres.gen.rpc/shape-rows :added "4.1"}
(fact "enumerates root, nested, and collection rows with exact paths"
  (rpc/shape-rows
   {:source-table "Envelope"
    :fields {:users {:items {:shape {:source-table "User" :fields {}}}}
             :role {:shape {:source-table "AccessRole" :fields {}}}}})
  => [["Envelope" [] :one]
      ["User" ["users"] :many]
      ["AccessRole" ["role"] :one]])

^{:refer postgres.gen.rpc/inferred-shape-rows :added "4.1"}
(fact "recovers only uniquely matching symbolic composite fields"
  (rpc/inferred-shape-rows
   {:fields {:role {:kind :symbol}
             :brand {:kind :unknown}
             :ignored {:kind :scalar}}}
   ["AccessRole" "Brand"])
  => [["AccessRole" ["role"] :one]
      ["Brand" ["brand"] :one]]
  (rpc/inferred-shape-rows
   {:fields {:role {:kind :symbol}}}
   ["AccessRole" "OtherAccessRole"])
  => [])

^{:refer postgres.gen.rpc/infer-topic-id :added "4.1"}
(fact "infers explicit, argument, session, and result topic ids in precedence order"
  (rpc/infer-topic-id {:aggregate "Brand" :notify {:path [:brand :id]}})
  => {:kind :result :path ["brand" "id"]}
  (rpc/infer-topic-id {:aggregate "Brand" :notify {:arg :i-brand-id}})
  => {:kind :arg :name "i-brand-id"}
  (rpc/infer-topic-id {:aggregate "User" :function-name 'app.fn/user-update
                       :inputs [{:name "i-user-id"}]})
  => {:kind :session :name "i-user-id"}
  (rpc/infer-topic-id {:aggregate "Campaign"
                       :output-shape {:fields {:topic-id {:kind :scalar}}}})
  => {:kind :result :path ["topic_id"]}
  (rpc/infer-topic-id {:aggregate "Topic"
                       :output-shape {:source-table "Topic"}})
  => {:kind :result :path ["id"]})

^{:refer postgres.gen.rpc/notification-plan :added "4.1"}
(fact "builds mutation plans, selects remove events, and ignores reads"
  (rpc/notification-plan
   {:function-name 'app.fn/user-delete
    :inputs [{:name "i-user-id"}]
    :report {:analysis {:mutating true :source-tables ["User"]}}
    :output-shape {:source-table "User" :fields {}}})
  => {:aggregate "User"
      :topic-id {:kind :session :name "i-user-id"}
      :event "db/remove"
      :rows [["User" [] :one]]
      :private true}
  (rpc/notification-plan
   {:function-name 'app.fn/user-get
    :report {:analysis {:mutating false}}})
  => nil)

^{:refer postgres.gen.rpc/result-path-form :added "4.1"}
(fact "builds nested JSON traversal and uses text extraction only at the leaf"
  (rpc/result-path-form 'o-result [] false) => 'o-result
  (rpc/result-path-form 'o-result ["request" "id"] false)
  => '(:-> (:-> o-result "request") "id")
  (rpc/result-path-form 'o-result ["request" "id"] true)
  => '(:->> (:-> o-result "request") "id"))

^{:refer postgres.gen.rpc/notification-payload :added "4.1"}
(fact "groups rows by table and preserves sync/remove cardinality semantics"
  (rpc/notification-payload
   {:event "db/sync"
    :rows [["User" [] :one]
           ["Role" ["roles"] :many]]}
   'o-result)
  => '{"db/sync" {"Role" (:-> o-result "roles")
                   "User" [o-result]}}
  (rpc/notification-payload
   {:event "db/remove"
    :rows [["User" [] :one]]}
   'o-result)
  => '{"db/remove" {"User" [(:->> o-result "id")]}})

^{:refer postgres.gen.rpc/topic-id-form :added "4.1"}
(fact "materializes session, argument, and result topic strategies"
  (let [forms {:auth-form '(auth/uid)
               :result-sym 'o-result
               :arg-form #(symbol (subs % 2))}]
    (rpc/topic-id-form {:kind :session :name "i-user-id"} forms)
    => '(auth/uid)
    (rpc/topic-id-form {:kind :arg :name "i-brand-id"} forms)
    => 'brand-id
    (rpc/topic-id-form {:kind :result :path ["brand" "id"]} forms)
    => '(:->> (:-> o-result "brand") "id")))

^{:refer postgres.gen.rpc/realtime-send-form :added "4.1"}
(fact "wraps the exact entity topic, payload, and private flag"
  (rpc/realtime-send-form
   {:aggregate "Brand"
    :topic-id {:kind :arg :name "i-brand-id"}
    :event "db/sync"
    :rows [["Brand" [] :one]]}
   {:auth-form '(auth/uid)
    :result-sym 'o-result
    :arg-form #(symbol (subs % 2))})
  => '(s/realtime-send-request
       (|| "realtime:Brand:" brand-id)
       {"db/sync" {"Brand" [o-result]}}
       true))
