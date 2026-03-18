(ns std.lib.schema-test
  (:use code.test)
  (:require [std.lib.schema :as schema]))


(comment
  ;; PREVIOUS SETUP
  [rt.postgres.grammar.common-application :as app]
  [rt.postgres.script.test.scratch-v1 :as scratch]
  (def -tsch- (get-in (app/app "scratch")
                      [:schema
                       :tree
                       :Task]))
  (def +schema+ (get-in (app/app "scratch")
                        [:schema])))


(def +schema+
  '{:flat
   {:Entry/id
    [{:type :uuid,
      :cardinality :one,
      :primary true,
      :sql {:default (rt.postgres/uuid-generate-v4)},
      :web {:example "00000000-0000-0000-0000-000000000000"},
      :scope :-/id,
      :order 0,
      :ident :Entry/id}],
    :TaskCache/tasks
    [{:ident :TaskCache/tasks,
      :cardinality :many,
      :type :ref,
      :ref
      {:ns :Task,
       :type :reverse,
       :val :tasks,
       :key :Task/_cache,
       :rval :cache,
       :rkey :Task/cache,
       :rident :Task/cache,
       :link
       {:id Task,
        :module rt.postgres.script.test.scratch-v1,
        :lang :postgres,
        :section :code}}}],
    :Task/name
    [{:type :text,
      :cardinality :one,
      :required true,
      :sql {:unique "default", :index {:using :hash}},
      :scope :-/data,
      :order 2,
      :ident :Task/name}],
    :TaskCache/__deleted__
    [{:type :boolean,
      :cardinality :one,
      :scope :-/hidden,
      :sql {:default false},
      :order 5,
      :ident :TaskCache/__deleted__}],
    :Entry/op-created
    [{:type :uuid,
      :cardinality :one,
      :scope :-/system,
      :order 3,
      :ident :Entry/op-created}],
    :TaskCache/time-created
    [{:type :long,
      :cardinality :one,
      :scope :-/data,
      :order 3,
      :ident :TaskCache/time-created}],
    :TaskCache/time-updated
    [{:type :long,
      :cardinality :one,
      :scope :-/data,
      :order 4,
      :ident :TaskCache/time-updated}],
    :Task/status
    [{:type :enum,
      :cardinality :one,
      :required true,
      :scope :-/info,
      :enum {:ns rt.postgres.script.test.scratch-v1/EnumStatus},
      :web {:example "success"},
      :order 1,
      :ident :Task/status}],
    :Task/op-updated
    [{:type :uuid,
      :cardinality :one,
      :scope :-/system,
      :order 5,
      :ident :Task/op-updated}],
    :Task/cache
    [{:type :ref,
      :cardinality :one,
      :required true,
      :ref
      {:ns :TaskCache,
       :link
       {:id TaskCache,
        :module rt.postgres.script.test.scratch-v1,
        :lang :postgres,
        :section :code},
       :rval :tasks,
       :type :forward,
       :key :Task/cache,
       :val :cache,
       :rkey :Task/_cache,
       :rident :TaskCache/tasks},
      :scope :-/ref,
      :order 3,
      :ident :Task/cache}],
    :Task/__deleted__
    [{:type :boolean,
      :cardinality :one,
      :scope :-/hidden,
      :sql {:default false},
      :order 8,
      :ident :Task/__deleted__}],
    :Task/id
    [{:type :uuid,
      :cardinality :one,
      :primary true,
      :sql {:default (rt.postgres/uuid-generate-v4)},
      :web {:example "00000000-0000-0000-0000-000000000000"},
      :scope :-/id,
      :order 0,
      :ident :Task/id}],
    :Task/time-updated
    [{:type :long,
      :cardinality :one,
      :scope :-/data,
      :order 7,
      :ident :Task/time-updated}],
    :Task/time-created
    [{:type :long,
      :cardinality :one,
      :scope :-/data,
      :order 6,
      :ident :Task/time-created}],
    :TaskCache/op-updated
    [{:type :uuid,
      :cardinality :one,
      :scope :-/system,
      :order 2,
      :ident :TaskCache/op-updated}],
    :TaskCache/op-created
    [{:type :uuid,
      :cardinality :one,
      :scope :-/system,
      :order 1,
      :ident :TaskCache/op-created}],
    :TaskCache/id
    [{:type :uuid,
      :cardinality :one,
      :primary true,
      :web {:example "AUD"},
      :sql {:default (rt.postgres/uuid-generate-v4)},
      :scope :-/id,
      :order 0,
      :ident :TaskCache/id}],
    :Entry/tags
    [{:type :array,
      :cardinality :one,
      :required true,
      :sql {:process rt.postgres.script.test.scratch-v1/as-array},
      :scope :-/data,
      :order 2,
      :ident :Entry/tags}],
    :Task/op-created
    [{:type :uuid,
      :cardinality :one,
      :scope :-/system,
      :order 4,
      :ident :Task/op-created}],
    :Entry/op-updated
    [{:type :uuid,
      :cardinality :one,
      :scope :-/system,
      :order 4,
      :ident :Entry/op-updated}],
    :Entry/time-updated
    [{:type :long,
      :cardinality :one,
      :scope :-/data,
      :order 6,
      :ident :Entry/time-updated}],
    :Entry/time-created
    [{:type :long,
      :cardinality :one,
      :scope :-/data,
      :order 5,
      :ident :Entry/time-created}],
    :Entry/name
    [{:type :text,
      :cardinality :one,
      :required true,
      :sql {:unique "default", :index {:using :hash}},
      :scope :-/data,
      :order 1,
      :ident :Entry/name}],
    :Entry/__deleted__
    [{:type :boolean,
      :cardinality :one,
      :scope :-/hidden,
      :sql {:default false},
      :order 7,
      :ident :Entry/__deleted__}]},
   :tree
   {:Entry
    {:id
     [{:type :uuid,
       :cardinality :one,
       :primary true,
       :sql {:default (rt.postgres/uuid-generate-v4)},
       :web {:example "00000000-0000-0000-0000-000000000000"},
       :scope :-/id,
       :order 0,
       :ident :Entry/id}],
     :op-created
     [{:type :uuid,
       :cardinality :one,
       :scope :-/system,
       :order 3,
       :ident :Entry/op-created}],
     :tags
     [{:type :array,
       :cardinality :one,
       :required true,
       :sql {:process rt.postgres.script.test.scratch-v1/as-array},
       :scope :-/data,
       :order 2,
       :ident :Entry/tags}],
     :op-updated
     [{:type :uuid,
       :cardinality :one,
       :scope :-/system,
       :order 4,
       :ident :Entry/op-updated}],
     :time-updated
     [{:type :long,
       :cardinality :one,
       :scope :-/data,
       :order 6,
       :ident :Entry/time-updated}],
     :time-created
     [{:type :long,
       :cardinality :one,
       :scope :-/data,
       :order 5,
       :ident :Entry/time-created}],
     :name
     [{:type :text,
       :cardinality :one,
       :required true,
       :sql {:unique "default", :index {:using :hash}},
       :scope :-/data,
       :order 1,
       :ident :Entry/name}],
     :__deleted__
     [{:type :boolean,
       :cardinality :one,
       :scope :-/hidden,
       :sql {:default false},
       :order 7,
       :ident :Entry/__deleted__}]},
    :TaskCache
    {:tasks
     [{:ident :TaskCache/tasks,
       :cardinality :many,
       :type :ref,
       :ref
       {:ns :Task,
        :type :reverse,
        :val :tasks,
        :key :Task/_cache,
        :rval :cache,
        :rkey :Task/cache,
        :rident :Task/cache,
        :link
        {:id Task,
         :module rt.postgres.script.test.scratch-v1,
         :lang :postgres,
         :section :code}}}],
     :__deleted__
     [{:type :boolean,
       :cardinality :one,
       :scope :-/hidden,
       :sql {:default false},
       :order 5,
       :ident :TaskCache/__deleted__}],
     :time-created
     [{:type :long,
       :cardinality :one,
       :scope :-/data,
       :order 3,
       :ident :TaskCache/time-created}],
     :time-updated
     [{:type :long,
       :cardinality :one,
       :scope :-/data,
       :order 4,
       :ident :TaskCache/time-updated}],
     :op-updated
     [{:type :uuid,
       :cardinality :one,
       :scope :-/system,
       :order 2,
       :ident :TaskCache/op-updated}],
     :op-created
     [{:type :uuid,
       :cardinality :one,
       :scope :-/system,
       :order 1,
       :ident :TaskCache/op-created}],
     :id
     [{:type :uuid,
       :cardinality :one,
       :primary true,
       :web {:example "AUD"},
       :sql {:default (rt.postgres/uuid-generate-v4)},
       :scope :-/id,
       :order 0,
       :ident :TaskCache/id}]},
    :Task
    {:__deleted__
     [{:type :boolean,
       :cardinality :one,
       :scope :-/hidden,
       :sql {:default false},
       :order 8,
       :ident :Task/__deleted__}],
     :name
     [{:type :text,
       :cardinality :one,
       :required true,
       :sql {:unique "default", :index {:using :hash}},
       :scope :-/data,
       :order 2,
       :ident :Task/name}],
     :op-updated
     [{:type :uuid,
       :cardinality :one,
       :scope :-/system,
       :order 5,
       :ident :Task/op-updated}],
     :time-updated
     [{:type :long,
       :cardinality :one,
       :scope :-/data,
       :order 7,
       :ident :Task/time-updated}],
     :time-created
     [{:type :long,
       :cardinality :one,
       :scope :-/data,
       :order 6,
       :ident :Task/time-created}],
     :cache
     [{:type :ref,
       :cardinality :one,
       :required true,
       :ref
       {:ns :TaskCache,
        :link
        {:id TaskCache,
         :module rt.postgres.script.test.scratch-v1,
         :lang :postgres,
         :section :code},
        :rval :tasks,
        :type :forward,
        :key :Task/cache,
        :val :cache,
        :rkey :Task/_cache,
        :rident :TaskCache/tasks},
       :scope :-/ref,
       :order 3,
       :ident :Task/cache}],
     :status
     [{:type :enum,
       :cardinality :one,
       :required true,
       :scope :-/info,
       :enum {:ns rt.postgres.script.test.scratch-v1/EnumStatus},
       :web {:example "success"},
       :order 1,
       :ident :Task/status}],
     :id
     [{:type :uuid,
       :cardinality :one,
       :primary true,
       :sql {:default (rt.postgres/uuid-generate-v4)},
       :web {:example "00000000-0000-0000-0000-000000000000"},
       :scope :-/id,
       :order 0,
       :ident :Task/id}],
     :op-created
     [{:type :uuid,
       :cardinality :one,
       :scope :-/system,
       :order 4,
       :ident :Task/op-created}]}},
   :lu
   {:Entry/id :Entry/id,
    :TaskCache/tasks :TaskCache/tasks,
    :Task/name :Task/name,
    :Task/_cache :TaskCache/tasks,
    :TaskCache/__deleted__ :TaskCache/__deleted__,
    :Entry/op-created :Entry/op-created,
    :TaskCache/time-created :TaskCache/time-created,
    :TaskCache/time-updated :TaskCache/time-updated,
    :Task/status :Task/status,
    :Task/op-updated :Task/op-updated,
    :Task/cache :Task/cache,
    :Task/__deleted__ :Task/__deleted__,
    :Task/id :Task/id,
    :Task/time-updated :Task/time-updated,
    :Task/time-created :Task/time-created,
    :TaskCache/op-updated :TaskCache/op-updated,
    :TaskCache/op-created :TaskCache/op-created,
    :TaskCache/id :TaskCache/id,
    :Entry/tags :Entry/tags,
    :Task/op-created :Task/op-created,
    :Entry/op-updated :Entry/op-updated,
    :Entry/time-updated :Entry/time-updated,
    :Entry/time-created :Entry/time-created,
    :Entry/name :Entry/name,
    :Entry/__deleted__ :Entry/__deleted__},
   :vec
   [:TaskCache
    [:id
     {:type :uuid,
      :primary true,
      :web {:example "AUD"},
      :sql {:default (rt.postgres/uuid-generate-v4)},
      :scope :-/id}
     :op-created
     {:type :uuid, :scope :-/system}
     :op-updated
     {:type :uuid, :scope :-/system}
     :time-created
     {:type :long, :scope :-/data}
     :time-updated
     {:type :long, :scope :-/data}
     :__deleted__
     {:type :boolean, :scope :-/hidden, :sql {:default false}}]
    :Task
    [:id
     {:type :uuid,
      :primary true,
      :sql {:default (rt.postgres/uuid-generate-v4)},
      :web {:example "00000000-0000-0000-0000-000000000000"},
      :scope :-/id}
     :status
     {:type :enum,
      :required true,
      :scope :-/info,
      :enum {:ns rt.postgres.script.test.scratch-v1/EnumStatus},
      :web {:example "success"}}
     :name
     {:type :text,
      :required true,
      :sql {:unique "default", :index {:using :hash}},
      :scope :-/data}
     :cache
     {:type :ref,
      :required true,
      :ref
      {:ns :TaskCache,
       :link
       {:id TaskCache,
        :module rt.postgres.script.test.scratch-v1,
        :lang :postgres,
        :section :code}},
      :scope :-/ref}
     :op-created
     {:type :uuid, :scope :-/system}
     :op-updated
     {:type :uuid, :scope :-/system}
     :time-created
     {:type :long, :scope :-/data}
     :time-updated
     {:type :long, :scope :-/data}
     :__deleted__
     {:type :boolean, :scope :-/hidden, :sql {:default false}}]
    :Entry
    [:id
     {:type :uuid,
      :primary true,
      :sql {:default (rt.postgres/uuid-generate-v4)},
      :web {:example "00000000-0000-0000-0000-000000000000"},
      :scope :-/id}
     :name
     {:type :text,
      :required true,
      :sql {:unique "default", :index {:using :hash}},
      :scope :-/data}
     :tags
     {:type :array,
      :required true,
      :sql {:process rt.postgres.script.test.scratch-v1/as-array},
      :scope :-/data}
     :op-created
     {:type :uuid, :scope :-/system}
     :op-updated
     {:type :uuid, :scope :-/system}
     :time-created
     {:type :long, :scope :-/data}
     :time-updated
     {:type :long, :scope :-/data}
     :__deleted__
     {:type :boolean, :scope :-/hidden, :sql {:default false}}]]})

(def -tsch-
  '{:__deleted__
    [{:type :boolean,
      :cardinality :one,
      :scope :-/hidden,
      :sql {:default false},
      :order 8,
      :ident :Task/__deleted__}],
    :name
    [{:type :text,
      :cardinality :one,
      :required true,
      :sql {:unique "default", :index {:using :hash}},
      :scope :-/data,
      :order 2,
      :ident :Task/name}],
    :op-updated
    [{:type :uuid,
      :cardinality :one,
      :scope :-/system,
      :order 5,
      :ident :Task/op-updated}],
    :time-updated
    [{:type :long,
      :cardinality :one,
      :scope :-/data,
      :order 7,
      :ident :Task/time-updated}],
    :time-created
    [{:type :long,
      :cardinality :one,
      :scope :-/data,
      :order 6,
      :ident :Task/time-created}],
    :cache
    [{:type :ref,
      :cardinality :one,
      :required true,
      :ref
      {:ns :TaskCache,
       :link
       {:id TaskCache,
        :module rt.postgres.script.test.scratch-v1,
        :lang :postgres,
        :section :code},
       :rval :tasks,
       :type :forward,
       :key :Task/cache,
       :val :cache,
       :rkey :Task/_cache,
       :rident :TaskCache/tasks},
      :scope :-/ref,
      :order 3,
      :ident :Task/cache}],
    :status
    [{:type :enum,
      :cardinality :one,
      :required true,
      :scope :-/info,
      :enum {:ns rt.postgres.script.test.scratch-v1/EnumStatus},
      :web {:example "success"},
      :order 1,
      :ident :Task/status}],
    :id
    [{:type :uuid,
      :cardinality :one,
      :primary true,
      :sql {:default (rt.postgres/uuid-generate-v4)},
      :web {:example "00000000-0000-0000-0000-000000000000"},
      :scope :-/id,
      :order 0,
      :ident :Task/id}],
    :op-created
    [{:type :uuid,
      :cardinality :one,
      :scope :-/system,
      :order 4,
      :ident :Task/op-created}]})

^{:refer std.lib.schema/expand-scopes :added "4.0"}
(fact "expand `*` keys into `-` keys"
  ^:hidden
  
  (schema/expand-scopes :*/data)
  => #{:-/info :-/id :-/data :-/key}

  (schema/expand-scopes :*/info)
  => #{:-/info :-/id :-/key})

^{:refer std.lib.schema/linked-primary :added "4.0"}
(fact "gets the linked primary column"
  ^:hidden
  
  (schema/linked-primary -tsch-
                         :cache
                         +schema+)
  => '{:type :uuid, :cardinality :one, :primary true,
       :web {:example "AUD"},
       :sql {:default (rt.postgres/uuid-generate-v4)}, :scope :-/id, :order 0, :ident :TaskCache/id})

^{:refer std.lib.schema/order-keys :added "4.0"}
(fact "order keys given schema"
  ^:hidden
  
  (schema/order-keys -tsch-
                     [:cache
                      :status
                      :id
                      :WRONG])
  => '(:id :status :cache :WRONG))

^{:refer std.lib.schema/get-defaults :added "4.0"}
(fact "get defaults in the schema"
  ^:hidden
  
  (schema/get-defaults -tsch-)
  => '{:__deleted__ false, :id (rt.postgres/uuid-generate-v4)})

^{:refer std.lib.schema/check-valid-columns :added "4.0"}
(fact "check if columns are valid"
  ^:hidden
  
  (schema/check-valid-columns -tsch- [:id :status])
  => [true]

  (schema/check-valid-columns -tsch- [:id :WRONG])
  => (contains-in [false {:not-allowed #{:WRONG}}]))

^{:refer std.lib.schema/check-missing-columns :added "4.0"}
(fact "check if columns are missing"
  ^:hidden
  
  (schema/check-missing-columns -tsch-
                         [:status]
                         :required)
  => [false {:missing #{:name :cache},
             :required #{:name :cache :status}}]

  (schema/check-missing-columns -tsch-
                         [:status :name :cache]
                         :required)
  => [true])

^{:refer std.lib.schema/check-fn-columns :added "4.0"}
(fact "perform a check using the `:check` key"

  (schema/check-fn-columns -tsch- {:status 'a})
  => {})

^{:refer std.lib.schema/get-returning :added "4.0"}
(fact "collects the returning ids and columns"
  
  (->> (schema/get-returning -tsch-
                      [:*/data :cache])
       (map first))
  => '(:id :status :name :cache :time-created :time-updated))
