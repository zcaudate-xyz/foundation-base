tahto/model/annex/spec_r/destructure_test.clj:1:(ns tahto.model.annex.spec-r.destructure-test
  (:use code.test)
tahto/model/annex/spec_r/destructure_test.clj:3:  (:require [tahto.model.annex.spec-r.rewrite :as rewrite]))

tahto/model/annex/spec_r/destructure_test.clj:5:^{:refer tahto.model.annex.spec-r.rewrite/r-rewrite-stage :added "4.1"}
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
