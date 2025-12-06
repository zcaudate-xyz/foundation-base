(ns code.manage.fn-format-test
  (:use code.test)
  (:require [code.manage.fn-format :refer :all]
            [std.block.navigate :as nav]
            [std.lib.zip :as zip]))



(fact "debug step-right"
  (let [nav (nav/parse-string "(defn foo [x] 1)")
        nav (nav/down nav)
        nav (zip/step-right nav)]
    (zip/step-right nav))
  => any)



(fact "reproduce step-right failure"
  (let [nav (nav/parse-string "(defn foo [x] 1)")
        nav (nav/down nav)
        nav (nav/right nav) ;; foo
        nav (nav/right nav) ;; [x]
        nav (nav/left nav) ;; foo
        ;; Simulate list-transform logic
        new-list (construct/block (list 'foo))
        nav (assoc nav :right '())
        nav (zip/insert-right nav new-list)]
    (println "DEBUG: nav after insert-right:" nav)
    (zip/step-right nav))
  => any)

^{:refer code.manage.fn-format/list-transform :added "3.0"}


^{:refer code.manage.fn-format/fn:list-forms :added "3.0"}
(fact "query to find `defn` and `defmacro` forms with a vector"
  (-> (nav/parse-string "(defn foo [x] 1)")
      (fn:list-forms)
      (nav/root-string))
  => "(defn foo\n  ([x] 1))")

^{:refer code.manage.fn-format/fn:defmethod-forms :added "3.0"}
(fact "query to find `defmethod` forms with a vector"
  (-> (nav/parse-string "(defmethod foo :x [y] 1)")
      (fn:defmethod-forms)
      (nav/root-string))
  => "(defmethod foo :x\n  ([y] 1))")

^{:refer code.manage.fn-format/fn-format :added "3.0"}
(fact "function to refactor the arglist and body"
  (with-redefs [code.framework/refactor-code (fn [ns params lookup project]
                                               {:updated true})]
    (fn-format 'code.manage.fn-format
               {}
               nil
               nil))
  => {:updated true})


^{:refer code.manage.fn-format/manual-step-right :added "4.1"}
(fact "TODO")

^{:refer code.manage.fn-format/list-transform :added "4.1"}
(fact "TODO")