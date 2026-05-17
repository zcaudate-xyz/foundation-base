(ns xt.event.node-wire
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.event.node-transport-json :as json-transport]]})

(defspec.xt MemoryWireEndpoint
  [:xt/record
   ["meta" [:xt/dict :xt/str :xt/any]]
   ["write_fn" json-transport/TextEndpointWriteFn]
   ["start_fn" json-transport/TextEndpointStartFn]
   ["stop_fn" json-transport/TextEndpointStopFn]])

(defspec.xt MemoryWirePair
  [:xt/record
   ["left" MemoryWireEndpoint]
   ["right" MemoryWireEndpoint]])

(defspec.xt memory-endpoint
  [:fn [[:xt/dict :xt/str :xt/any]] MemoryWireEndpoint])

(defspec.xt memory-pair
  [:fn [[:xt/maybe [:xt/dict :xt/str :xt/any]]] MemoryWirePair])

(defn.xt memory-endpoint
  "creates an in-memory text endpoint that forwards writes to its peer listener"
  {:added "4.1"}
  [state]
  (var write-fn
       (fn [text]
         (var peer (xt/x:get-key state "peer"))
         (when (xt/x:nil? peer)
           (xt/x:err "wire endpoint missing peer"))
         (var listener (xt/x:get-key peer "listener"))
         (when (xt/x:nil? listener)
           (xt/x:err "wire peer not started"))
         (return
          (listener
           {"text" text}
           {"wire" (xt/x:get-key state "id")
            "peer" (xt/x:get-key peer "id")}))))
  (var start-fn
       (fn [listener]
         (xt/x:set-key state "listener" listener)
         (return state)))
  (var stop-fn
       (fn [_]
         (xt/x:set-key state "listener" nil)
         (return true)))
  (return
   {"meta" {"kind" "wire.memory"
            "id" (xt/x:get-key state "id")}
    "write_fn" write-fn
    "start_fn" start-fn
    "stop_fn" stop-fn}))

(defn.xt memory-pair
  "creates a bidirectional in-memory text wire"
  {:added "4.1"}
  [opts]
  (var config (or opts {}))
  (var left-state {"id" (or (xt/x:get-key config "left_id")
                            "left")
                   "listener" nil
                   "peer" nil})
  (var right-state {"id" (or (xt/x:get-key config "right_id")
                             "right")
                    "listener" nil
                    "peer" nil})
  (xt/x:set-key left-state "peer" right-state)
  (xt/x:set-key right-state "peer" left-state)
  (return
   {"left" (-/memory-endpoint left-state)
    "right" (-/memory-endpoint right-state)}))
