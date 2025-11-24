(ns code.doc.link.test-test
  (:use code.test)
  (:require [code.doc.link.test :refer :all]))

^{:refer code.doc.link.test/failed-tests :added "3.0"}
(fact "creates a link to all the failed tests in the project"

  (failed-tests [{:meta {:line {:row 1}}
                  :results [{:from :verify :data false
                             :actual {:data 1 :form '(+ 1 1)}
                             :checker {:form 2}
                             :meta {:line {:row 2}}}]}])
  => [{:line {:row 1}
       :output [{:data 1
                 :form '(+ 1 1)
                 :check 2
                 :code {:line {:row 2}}}]}])

^{:refer code.doc.link.test/link-tests :added "3.0"}
(fact "creates a link to all the passed tests in the project"
  (binding [*run-tests* false]
    (link-tests {:articles {"doc" {:elements [{:type :test}]}}} "doc"))
  => {:articles {"doc" {:elements [{:type :test}]}}})
