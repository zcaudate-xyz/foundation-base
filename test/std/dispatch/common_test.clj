(ns std.dispatch.common-test
  (:use code.test)
  (:require [std.dispatch.common :refer :all]
            [std.concurrent :as cc]
            [std.lib :as h]
            [std.lib.component :as component]
            [std.protocol.component :as protocol.component]))

(fact:global
 {:component
  {|dispatch| {:create (create-map {:options {:pool {:size 1}}})
               :setup start-dispatch
               :teardown stop-dispatch}}})

^{:refer std.dispatch.common/to-string :added "3.0"}
(fact "returns the executorstring"
  (with-redefs [protocol.component/-info (constantly {:id :test})]
    (to-string (reify protocol.component/IComponentQuery
                 (-info [_ _] {:id :test})
                 clojure.lang.IPersistentMap
                 (valAt [_ k] (get {:type :core :display false} k))
                 (valAt [_ k default] (get {:type :core :display false} k default)))))
  => "#core.dispatch {:id :test}")

^{:refer std.dispatch.common/info-base :added "3.0"
  :use [|dispatch|]}
(fact "returns base executor info"
  (info-base |dispatch|)
  => (contains {:type nil,
                :running false,
                :counter (contains {:submit 0}),
                :options {:pool {:keep-alive 1000, :size 1, :max 1}}}))

^{:refer std.dispatch.common/create-map :added "3.0"}
(fact "creates the base executor map"

  (create-map {:options {:pool {:size 1}}})
  => (contains {:options {:pool {:keep-alive 1000,
                                 :size 1,
                                 :max 1}},
                :runtime map?}))

^{:refer std.dispatch.common/handle-fn :added "3.0"}
(fact "generic handle function for entry"

  (let [thunk (handle-fn (-> {:id :hello
                              :handler (fn [{:keys [id]} entry]
                                         {:id id :entry entry})}
                             create-map)
                         {:a 1})]
    (thunk))
  => {:id :hello, :entry {:a 1}})

^{:refer std.dispatch.common/await-termination :added "3.0"}
(fact "generic await termination function for executor"
  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (await-termination d) => nil?))

^{:refer std.dispatch.common/start-dispatch :added "3.0"}
(fact "generic start function for executor"
  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (started?-dispatch d) => true
    (stop-dispatch d)
    ;; Ensure we cleanup executor if stop-dispatch failed implicitly (it shouldn't)
    ))

^{:refer std.dispatch.common/stop-dispatch :added "3.0"}
(fact "generic stop function for executor"
  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (stop-dispatch d)
    (started?-dispatch d) => false))

^{:refer std.dispatch.common/kill-dispatch :added "3.0"}
(fact "generic force kill function for executor"
  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (kill-dispatch d)
    (started?-dispatch d) => false))

^{:refer std.dispatch.common/started?-dispatch :added "3.0"
  :use [|dispatch|]}
(fact "checks if executor has started"
  (started?-dispatch |dispatch|) => true)

^{:refer std.dispatch.common/stopped?-dispatch :added "3.0"
  :use [|dispatch|]}
(fact "checks if executor has stopped"
  (stopped?-dispatch |dispatch|) => false)

^{:refer std.dispatch.common/info-dispatch :added "3.0"
  :use [|dispatch|]}
(fact "returns generic executor info"

  (info-dispatch |dispatch|)
  => (contains {:type nil, :running true,
                :counter (contains {:submit 0})
                :options {:pool {:keep-alive 1000, :size 1, :max 1}}}))

^{:refer std.dispatch.common/health-dispatch :added "3.0"
  :use [|dispatch|]}
(fact "returns the health of the executor"
  (health-dispatch |dispatch|) => {:status :ok})

^{:refer std.dispatch.common/remote?-dispatch :added "3.0"
  :use [|dispatch|]}
(fact "returns whether executor is remote"
  (remote?-dispatch |dispatch|) => false)

^{:refer std.dispatch.common/props-dispatch :added "3.0"
  :use [|dispatch|]}
(fact "returns the props of the executor"
  (props-dispatch |dispatch|) => map?)

^{:refer std.dispatch.common/check-hooks :added "3.0"}
(fact "Checks that hooks conform to arguments"
  (check-hooks {:on-startup (fn [dispatch] )})
  => (contains {:on-startup fn?}))

(comment
  (./import)

  (std.lib/tracked [] :stop))
