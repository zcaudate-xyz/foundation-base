(ns code.test.base.process-test
  (:use code.test)
  (:require [code.test.base.process :refer :all]
            [code.test.base.context :as ctx]
            [code.test.checker.common :as base]
            [code.test.compile :as compile]
            [std.lib :as h]))

^{:refer code.test.base.process/evaluate :added "3.0"}
(fact "converts a form to a result"
  (->> (evaluate {:form '(+ 1 2 3)})
       (into {}))
  => (contains {:status :success, :data 6, :form '(+ 1 2 3), :from :evaluate}))

^{:refer code.test.base.process/process :added "3.0"}
(fact "processes a form or a check"
  (defn view-signal [op]
    (let [output (atom nil)]
      (h/signal:with-temp [:test (fn [{:keys [result]}]
                                   (reset! output (into {} result)))]
                          (process op)
                          @output)))

  (view-signal {:type :form
                :form '(+ 1 2 3)
                :meta {:line 10 :col 3}})
  => (contains {:status :success,
                :data 6,
                :form '(+ 1 2 3),
                :from :evaluate,
                :meta {:line 10, :col 3}})

  ((contains {:status :success,
              :data true,
              :checker base/checker?
              :actual 6,
              :from :verify,
              :meta nil})
   (view-signal {:type :test-equal
                 :input  {:form '(+ 1 2 3)}
                 :output {:form 'even?}}))
  => true)

(fact "attaches metadata to the result"
  (attach-meta {:a 1} {:b 2})
  => {:a 1, :meta {:b 2}}

  (ctx/with-context {:eval-meta {:c 3}}
    (attach-meta {:a 1} {:b 2}))
  => {:a 1, :meta {:c 3}, :original {:b 2}})

(fact "collect function handles mixed results"
  (let [passing (compile/split '[(+ 1 1) => 2])
        failing (compile/split '[(+ 1 1) => 3])]
    (ctx/with-context {:results (atom [])}
      (collect {} (doall (map process (concat passing failing)))) => false)))

(fact "skip function signals a skipped fact"
  (let [output (atom nil)
        id (h/uuid)]
    (ctx/with-context {:run-id id}
      (h/signal:with-temp [:test (fn [data] (reset! output data))]
        (skip {:a 1})))
    @output => (contains {:id id, :test :fact, :meta {:a 1}, :results [], :skipped true})))

(fact "run-single runs a check form"
  (ctx/with-context {:run-id :test, :results (atom []), :settings {:match {:unit "test"}}}
    (run-single {:unit "test"} (compile/split '[(+ 1 1) => 2])))
  => true

  (ctx/with-context {:run-id :test, :settings {:match {:unit "other"}}}
    (run-single {:unit "test"} (compile/split '[(+ 1 1) => 2])))
  => :skipped)
