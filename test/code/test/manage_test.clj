(ns code.test.manage-test
  (:require [code.test.manage :refer :all]
            [code.test.base.runtime :as rt]
            [code.test :refer [fact]]))

(fact "fact:global-map function"
  (rt/with-context {:global (atom {})}
    (fact:global-map *ns* {:a 1})
    (rt/get-global *ns*) => {:a 1}))

(fact "fact:global macro"
  (rt/with-context {:global (atom {})}
    (fact:global :set {:a 1})
    (fact:global :get) => {:a 1}))

(fact "fact:ns macro and helpers"
  (let [ns 'code.test.manage-test-sample]
    (try
      (fact:ns :import ns)
      (ns-aliases ns) => not-empty?
      (fact:ns :unimport ns)
      (ns-aliases ns) => empty?
      (finally
       (remove-ns ns)))))

(fact "fact:ns-load and fact:ns-unload"
  (let [ns 'code.test.manage-test-sample]
    (try
      (eval `(fact "" (fact:ns-load ~ns) => ~ns))
      (find-ns ns) => not-nil?
      (eval `(fact "" (fact:ns-unload ~ns) => ~ns))
      (find-ns ns) => nil?
      (finally
       (remove-ns ns)))))
