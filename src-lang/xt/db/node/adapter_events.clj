(ns xt.db.node.adapter-events
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.db.system :as xdb]
             [xt.db.system.impl-common :as impl-common]]})

;;
;; The xt.db.node.adapter-events namespace manages entity-topic subscriptions
;; for Supabase Realtime broadcast events.
;;
;; It expects broadcasts with:
;;   event: "xt.db/event"
;;   payload: {"db/sync"   {Table [record]}
;;             "db/remove" {Table [id]}}
;;
;; Topics are entity-scoped: "User:<id>", "Topic:<id>", "Organisation:<id>",
;; etc.
;;

(def$.xt BROADCAST_EVENT "xt.db/event")
(def$.xt DEFAULT_CACHING_SERVICE "db/caching")
(def$.xt DEFAULT_PUBSUB_SERVICE "db/primary")

(defn.xt topic-for
  "builds an entity-scoped realtime topic"
  {:added "4.1"}
  [entity-type entity-id]
  (return (xt/x:cat entity-type ":" entity-id)))

(defn.xt topics-for
  "builds topics for a collection of entities

   entities can be:
     - an array of [entity-type entity-id] pairs
     - an object mapping entity-type to an array of ids"
  {:added "4.1"}
  [entities]
  (var out [])
  (cond (xt/x:is-array? entities)
        (xt/for:array [entry entities]
          (var entity-type (xt/x:first entry))
          (var ids (xt/x:second entry))
          (when (xt/x:is-array? ids)
            (xt/for:array [id ids]
              (xt/x:arr-push out (-/topic-for entity-type id))))
          (when (not (xt/x:is-array? ids))
            (xt/x:arr-push out (-/topic-for entity-type ids))))

        (xt/x:is-object? entities)
        (xt/for:object [[entity-type ids] entities]
          (when (xt/x:is-array? ids)
            (xt/for:array [id ids]
              (xt/x:arr-push out (-/topic-for entity-type id))))
          (when (not (xt/x:is-array? ids))
            (xt/x:arr-push out (-/topic-for entity-type ids)))))
  (return out))

(defn.xt apply-broadcast
  "applies a broadcast payload directly to db/caching"
  {:added "4.1"}
  [node payload]
  (var caching (substrate/get-service node -/DEFAULT_CACHING_SERVICE))
  (when (xt/x:nil? caching)
    (return (promise/x:promise-run
             [false {"status" "error"
                     "tag" "db/caching-not-found"}])))
  (return (promise/x:promise-run (xdb/sync-event caching payload))))

(defn.xt make-broadcast-callback
  "creates a callback for Supabase realtime broadcast events

   The callback receives the raw broadcast payload {db/sync ..., db/remove ...}
   and applies it to db/caching."
  {:added "4.1"}
  [node]
  (return
   (fn [payload]
     (return (-/apply-broadcast node payload)))))

(defn.xt subscribe-topic
  "subscribes a pubsub service to an entity topic"
  {:added "4.1"}
  [node service-id topic opts]
  (var impl (substrate/get-service node service-id))
  (when (xt/x:nil? impl)
    (return (promise/x:promise-run
             {"ok" false
              "error" {"status" "error"
                       "tag" "db/service-not-found"
                       "service_id" service-id}})))
  (var callback (-/make-broadcast-callback node))
  (return (impl-common/subscribe impl topic opts callback)))

(defn.xt unsubscribe-topic
  "unsubscribes a previously obtained topic handle"
  {:added "4.1"}
  [node handle]
  (var impl (or (xt/x:get-key handle "impl")
                (substrate/get-service node -/DEFAULT_PUBSUB_SERVICE)))
  (return (impl-common/unsubscribe impl handle)))

(defn.xt subscribe-entities
  "subscribes to a collection of entity topics

   entities: see topics-for"
  {:added "4.1"}
  [node service-id entities opts]
  (var topics (-/topics-for entities))
  (var handles [])
  (xt/for:array [topic topics]
    (var handle (-/subscribe-topic node service-id topic opts))
    (xt/x:arr-push handles handle))
  (return handles))

(defn.xt apply-broadcast-handler
  "substrate handler for @xt.db/apply-broadcast

   Args: [payload]. Applies the payload to db/caching."
  {:added "4.1"}
  [space args request node]
  (var payload (or (xt/x:first args) {}))
  (return (-/apply-broadcast node payload)))

(defn.xt init-handlers
  "registers adapter-events handlers on a node"
  {:added "4.1"}
  [node]
  (substrate/register-handler node "@xt.db/apply-broadcast" -/apply-broadcast-handler nil)
  (return node))

(defn.xt init-adaptor-events
  "installs adapter-events handlers on an existing node

   Should be called after xt.db.node.adaptor-base/init-adaptor-main so
   that db/caching is present."
  {:added "4.1"}
  [node opts]
  (:= opts (or opts {}))
  (-/init-handlers node)
  (return node))
