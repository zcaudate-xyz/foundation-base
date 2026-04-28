(ns std.lang.model-annex.spec-r.destructure-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-r.rewrite :as rewrite]))

^{:refer std.lang.model-annex.spec-r.rewrite/r-rewrite-stage :added "4.1"}
(fact "rewrites set destructuring lets for R"
  (let [out (rewrite/r-rewrite-stage
             '(let [#{path} opts
                    x 1]
                path)
             nil)
        [_ bindings body] out
        [temp-sym temp-val path-sym path-val x-sym x-val] bindings]
    (and (= 'let (first out))
         (= 'opts temp-val)
         (= 'path path-sym)
         (= (list 'x:get-key temp-sym "path" nil) path-val)
         (= 'x x-sym)
         (= 1 x-val)
         (= 'path body)))
  => true)
