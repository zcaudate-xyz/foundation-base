(ns std.dispatch.common-test
  (:use code.test)
  (:require [std.dispatch.common :refer :all]
            [std.concurrent :as cc]
            [std.lib :as h]
            [std.lib.component :as component]
            [std.protocol.component :as protocol.component]))

^{:refer std.dispatch.common/to-string :added "3.0"}
(fact "returns the executorstring"
  ^:hidden
  
  (to-string (reify protocol.component/IComponentQuery
               (-info [_ _] {:id :test})
               clojure.lang.IPersistentMap
               (valAt [_ k] (get {:type :core :display false} k))
               (valAt [_ k default] (get {:type :core :display false} k default))))
  => "#core.dispatch {:id :test}")

^{:refer std.dispatch.common/info-base :added "3.0"}
(fact "returns base executor info"
  ^:hidden
  
  (info-base (create-map {:options {:pool {:size 1}}}))
  => (contains {:type nil,
                :running false,
                :counter (contains {:submit 0}),
                :options {:pool {:keep-alive 1000, :size 1, :max 1}}}))

^{:refer std.dispatch.common/create-map :added "3.0"}
(fact "creates the base executor map"
  ^:hidden
  
  (create-map {:options {:pool {:size 1}}})
  => (contains {:options {:pool {:keep-alive 1000,
                                 :size 1,
                                 :max 1}},
                :runtime map?}))

^{:refer std.dispatch.common/handle-fn :added "3.0"}
(fact "generic handle function for entry"
  ^:hidden
  
  (let [thunk (handle-fn (-> {:id :hello
                              :handler (fn [{:keys [id]} entry]
                                         {:id id :entry entry})}
                             create-map)
                         {:a 1})]
    (thunk))
  => {:id :hello, :entry {:a 1}})

^{:refer std.dispatch.common/await-termination :added "3.0"}
(fact "generic await termination function for executor"
  ^:hidden
  
  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (await-termination d) => nil?))

^{:refer std.dispatch.common/start-dispatch :added "3.0"}
(fact "generic start function for executor"
  ^:hidden
  
  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (started?-dispatch d) => true
    (stop-dispatch d)
    ;; Ensure we cleanup executor if stop-dispatch failed implicitly (it shouldn't)
    ))

^{:refer std.dispatch.common/stop-dispatch :added "3.0"}
(fact "generic stop function for executor"
  ^:hidden
  
  (let [d (create-map {:options {:pool {:size 1}}})]

    (start-dispatch d)
    (stop-dispatch d)
    (started?-dispatch d) => false))

^{:refer std.dispatch.common/kill-dispatch :added "3.0"}
(fact "generic force kill function for executor"
  ^:hidden
  
  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (kill-dispatch d)
    (started?-dispatch d) => false))

^{:refer std.dispatch.common/started?-dispatch :added "3.0"}
(fact "checks if executor has started"
  ^:hidden

  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (kill-dispatch d)
    (started?-dispatch d) => false))

^{:refer std.dispatch.common/stopped?-dispatch :added "3.0"}
(fact "checks if executor has stopped"
  ^:hidden
  
  (let [d (create-map {:options {:pool {:size 1}}})]
    (start-dispatch d)
    (kill-dispatch d)
    (stopped?-dispatch d) => true))

^{:refer std.dispatch.common/info-dispatch :added "3.0"}
(fact "returns generic executor info"
  ^:hidden

  (let [d (create-map {:options {:pool {:size 1}}})]
    (info-dispatch d))
  => {:type nil, :running false,
      :counter {:submit 0, :queued 0, :process 0, :complete 0, :error 0, :poll 0, :skip 0, :batch 0},
      :options {:pool {:keep-alive 1000, :size 1, :max 1}}})

^{:refer std.dispatch.common/health-dispatch :added "3.0"}
(fact "returns the health of the executor"

  (let [d (create-map {:options {:pool {:size 1}}})]
    (health-dispatch d))
  => {:status :ok})

^{:refer std.dispatch.common/remote?-dispatch :added "3.0"}
(fact "returns whether executor is remote"
  ^:hidden
  
  (let [d (create-map {:options {:pool {:size 1}}})]
    (remote?-dispatch d)) => false)

^{:refer std.dispatch.common/props-dispatch :added "3.0"}
(fact "returns the props of the executor"
  ^:hidden
  
  (let [d (create-map {:options {:pool {:size 1}}})]
    (props-dispatch d)) => map?)

^{:refer std.dispatch.common/check-hooks :added "3.0"}
(fact "Checks that hooks conform to arguments"
  ^:hidden

  (check-hooks {:on-startup (fn [dispatch] )})
  => (contains {:on-startup fn?}))

(comment
  (./import)

  (std.lib/tracked [] :stop))
