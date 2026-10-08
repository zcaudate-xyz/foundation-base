(ns lang-demos.js-007-statstrade-substrate.example-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[lang-demos.js-007-statstrade-substrate.fixture :as fixture]
             [lang-demos.js-007-statstrade-substrate.link :as link]
             [lang-demos.js-007-statstrade-substrate.model :as model]
             [lang-demos.js-007-statstrade-substrate.hook :as hook]
             [js.react.ext-model :as ext-model]
             [xt.event.base-model :as event-model]
             [xt.event.base-listener :as listener]
             [xt.substrate :as substrate]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(defn.js with-fixture
  [f]
  (return (. (fixture/bootstrap)
             (then (fn [state]
                     (return (. (promise/x:promise (fn [] (return (f state))))
                                (finally (fn [] (return (fixture/close state)))))))))))

^{:refer lang-demos.js-007-statstrade-substrate.fixture/bootstrap}
(fact "list and detail traverse the memory transport and return topic data"
  (notify/wait-on [:js 10000]
    (. (-/with-fixture
        (fn [state]
          (var context (. state context))
          (return (. (promise/x:promise-all
                      [(link/call context "topic/list" [])
                       (link/call context "topic/detail" ["topic-2"])])
                     (then (fn [[rows detail]] (return {:rows rows :detail detail})))))))
       (then (fn [value] (repl/notify value)))))
  => {"rows" [{"id" "topic-1" "title" "Will turnout increase?" "revision" 1}
              {"id" "topic-2" "title" "Will the bill pass?" "revision" 1}]
      "detail" {"id" "topic-2" "title" "Will the bill pass?" "revision" 1}})

^{:refer lang-demos.js-007-statstrade-substrate.link/action-fn}
(fact "action-fn transforms arguments with context, wraps the function, and transforms output"
  (notify/wait-on [:js 10000]
    (var order [])
    (var action
         (link/action-fn
          {:fn (fn [x] (. order (push "call")) (return (* x 2)))
           :input (fn [[x] context] (. order (push "input")) (return [(+ x (. context bias))]))
           :wrap (fn [f]
                   (. order (push "wrap"))
                   (return (fn [x] (return (+ 3 (f x))))))
           :output (fn [x] (. order (push "output")) (return (+ "result=" x)))}
          {:bias 4}))
    (. (action 5)
       (then (fn [value] (repl/notify {:value value :order order})))))
  => {"value" "result=21" "order" ["wrap" "input" "call" "output"]})

^{:refer lang-demos.js-007-statstrade-substrate.link/action-fn
  :id synchronous-action-rejection}
(fact "synchronous action errors remain rejected promises"
  (notify/wait-on [:js 10000]
    (var action (link/action-fn {:fn (fn [] (throw (new Error "failed")))} {}))
    (. (action)
       (then (fn [] (repl/notify {:rejected false})))
       (catch (fn [err] (repl/notify {:rejected true :message (. err message)})))))
  => {"rejected" true "message" "failed"})

^{:refer lang-demos.js-007-statstrade-substrate.link/make-actions}
(fact "creating actions does not mutate; submitting changes the record once"
  (notify/wait-on [:js 10000]
    (. (-/with-fixture
        (fn [state]
          (var context (. state context))
          (var actions (link/make-actions model/TOPIC context))
          (return (. (link/call context "topic/detail" ["topic-1"])
                     (then (fn [before]
                             (return (. (. actions (modify "topic-1" "  Revised question?  "))
                                        (then (fn [after] (return {:before before :after after})))))))))))
       (then (fn [value] (repl/notify value)))))
  => {"before" {"id" "topic-1" "title" "Will turnout increase?" "revision" 1}
      "after" {"id" "topic-1" "title" "Revised question?" "revision" 2}})

^{:refer lang-demos.js-007-statstrade-substrate.fixture/install
  :id rejected-modification}
(fact "invalid mutations reject without publishing an event or changing the record"
  (notify/wait-on [:js 10000]
    (. (-/with-fixture
        (fn [state]
          (var context (. state context))
          (var count 0)
          (var cleanup (link/addRawListener (. model/TOPIC event-sync)
                                           (fn [] (:= count (+ count 1))) context "negative"))
          (return (. (link/call context "topic/modify" ["topic-1" " "])
                     (then (fn [] (return {:rejected false})))
                     (catch (fn [err]
                              (return (. (link/call context "topic/detail" ["topic-1"])
                                         (then (fn [row]
                                                 (cleanup)
                                                 (return {:rejected true :message (. err message)
                                                          :events count :revision (. row revision)})))))))))))
       (then (fn [value] (repl/notify value)))))
  => {"rejected" true "message" "Title is required" "events" 0 "revision" 1})

^{:refer lang-demos.js-007-statstrade-substrate.hook/attachViewEvents}
(fact "a modify event refreshes list and detail without a manual refresh"
  (notify/wait-on [:js 10000]
    (. (-/with-fixture
        (fn [state]
          (var context (. state context))
          (var list-view (hook/makeListView model/TOPIC {} context))
          (var detail-view (hook/makeSingleView model/TOPIC {} context))
          (var clean-list (hook/attachViewEvents list-view (. model/TOPIC event-sync) context "list" false))
          (var clean-detail (hook/attachViewEvents detail-view (. model/TOPIC event-sync) context "detail" true))
          (return (. (promise/x:promise-all
                      [(ext-model/refresh-args list-view [])
                       (ext-model/refresh-args detail-view ["topic-1"])])
                     (then (fn [] (return (link/call context "topic/modify" ["topic-1" "Updated?"]))))
                     (then (fn [] (return (promise/x:promise-all
                                           [(. list-view event_refresh) (. detail-view event_refresh)]))))
                     (then (fn []
                             (clean-list)
                             (clean-detail)
                             (return {:list-title (. (event-model/get-current list-view) [0] title)
                                      :detail-title (. (event-model/get-current detail-view) title)
                                      :listeners (listener/list-keyed-listeners
                                                  (. context node)
                                                  (link/event-key (. context space) "topic/changed"))})))))))
       (then (fn [value] (repl/notify value)))))
  => {"list_title" "Updated?" "detail_title" "Updated?" "listeners" []})

^{:refer lang-demos.js-007-statstrade-substrate.link/addRawListener}
(fact "listeners coexist, filter spaces and predicates, and clean up independently"
  (notify/wait-on [:js 10000]
    (. (-/with-fixture
        (fn [state]
          (var context (. state context))
          (var seen [])
          (var first (link/addRawListener
                      {:signal "topic/changed" :check (fn [frame] (return (== (. frame data id) "topic-1")))}
                      (fn [frame] (. seen (push (+ "first:" (. frame data id))))) context "first"))
          (var second (link/addRawListener (. model/TOPIC event-sync)
                                          (fn [frame] (. seen (push (+ "second:" (. frame data id))))) context "second"))
          (return (. (substrate/publish (. context node) "other/space" "topic/changed" {:id "topic-1"} {})
                     (then (fn [] (return (substrate/publish (. context node) (. context space)
                                                           "other/signal" {:id "topic-1"} {}))))
                     (then (fn [] (return (link/call context "topic/modify" ["topic-2" "Two?"]))))
                     (then (fn [] (first) (return (link/call context "topic/modify" ["topic-1" "One?"]))))
                     (then (fn [] (second) (return seen)))))))
       (then (fn [value] (repl/notify value)))))
  => ["second:topic-2" "second:topic-1"])

^{:refer lang-demos.js-007-statstrade-substrate.hook/attachViewEvents
  :id detail-record-filter}
(fact "an unrelated record event leaves the selected detail unchanged"
  (notify/wait-on [:js 10000]
    (. (-/with-fixture
        (fn [state]
          (var context (. state context))
          (var view (hook/makeSingleView model/TOPIC {} context))
          (var cleanup (hook/attachViewEvents view (. model/TOPIC event-sync) context "detail" true))
          (return (. (ext-model/refresh-args view ["topic-1"])
                     (then (fn [] (return (link/call context "topic/modify" ["topic-2" "Other?"]))))
                     (then (fn []
                             (cleanup)
                             (return {:title (. (event-model/get-current view) title)
                                      :refreshed (not= nil (. view event_refresh))})))))))
       (then (fn [value] (repl/notify value)))))
  => {"title" "Will turnout increase?" "refreshed" false})
