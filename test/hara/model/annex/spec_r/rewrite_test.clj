(ns hara.model.annex.spec-r.rewrite-test
  (:require [clojure.walk]
            [hara.common.emit-preprocess :as preprocess]
            [hara.model.annex.spec-r :as r]
            [hara.model.annex.spec-r.rewrite :as rewrite])
  (:use code.test))

^{:refer hara.model.annex.spec-r/tf-defn :added "4.1"}
(fact "applies inferred optional arguments during defn expansion"
  (preprocess/with:macro-opts [{:module {:id 'xt.event.base-model}}]
    (r/tf-defn '(defn get-output [view dest-key]
                  (return dest-key))))
  => '(def get-output
        (fn [view dest-key := nil]
          (return dest-key))))

^{:refer hara.model.annex.spec-r.rewrite/r-rewrite-expression :added "4.1"}
(fact "rewrites r expressions")

^{:refer hara.model.annex.spec-r.rewrite/r-rewrite-statement :added "4.1"}
(fact "rewrites r statements")

^{:refer hara.model.annex.spec-r.rewrite/r-rewrite-statements :added "4.1"}
(fact "rewrites r statement blocks")

^{:refer hara.model.annex.spec-r.rewrite/r-rewrite-stage :added "4.1"}
(fact "rewrites defgen into an iterator-building defn"
  (let [out      (rewrite/r-rewrite-stage
                  '(defgen hello [xs]
                     (yield 1)
                     (if flag
                       (yield (+ 1 2))
                       (yield 3)))
                  nil)
        [_ name args init & tail] out
        iterator (second init)
        body     (butlast tail)
        final    (last tail)]
     [(= 'defn (first out))
      (= 'hello name)
      (= '[xs] args)
      (symbol? iterator)
      (boolean (re-find #"^hello__iter__" (str iterator)))
      (= '(var ITER (list))
         (clojure.walk/prewalk-replace {iterator 'ITER} init))
     (= '((x:arr-push ITER 1)
          (if flag
            (x:arr-push ITER (+ 1 2))
            (x:arr-push ITER 3)))
        (clojure.walk/prewalk-replace {iterator 'ITER} body))
     (= '(return (x:iter-from-arr ITER))
        (clojure.walk/prewalk-replace {iterator 'ITER} final))])
  => [true true true true true true true true])
