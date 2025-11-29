(ns code.test.base.process-test
  (:use code.test)
  (:require [code.test.base.process :refer :all]
            [code.test.base.runtime :as rt]
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

^{:refer code.test.base.process/attach-meta :added "3.0"}
(fact "attaches metadata to the result")

^{:refer code.test.base.process/collect :added "3.0"}
(fact "makes sure that all returned verified results are true"
  (->> (compile/rewrite-top-level '[(+ 1 1) => 2
                                    (+ 1 2) => 3])
       (mapv process)
       (collect {}))
  => true)

^{:refer code.test.base.process/skip-check :added "3.0"}
(fact "returns the form with no ops evaluated"
  (skip-check {}) => :skipped)

^{:refer code.test.base.process/run-check :added "3.0"}
(fact "runs a single check form"

  (binding [rt/*eval-mode* false]
    (run-check {:unit #{:foo}} []))
  => :skipped

  (run-check {} [])
  => true)
