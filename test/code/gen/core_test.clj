(ns test.code.gen.core-test
  (:use code.test)
  (:require [code.gen.core :as c]
            [std.block :as b]
            [std.lib :as h]
            [clojure.string :as str]))

^{:refer code.gen.core/walk-and-substitute :added "4.0"}
(fact "walks the template AST and substitutes data from the bindings map"
  ;; This is an internal function, tested indirectly via `generate`.
  true => true)

^{:refer code.gen.core/load-template :added "4.0"
  :setup [(def ^:dynamic *template*
            (c/load-template "code/templates/def_simple.block"))]}
(fact "loads a template file from resources and parses it with std.block"

  (b/block? *template*)
  => true
  
  (b/string *template*)
  => "~namespace\n\n;; :def-basic\n(def ~name ~value)"
  
  
  (c/load-template "resources/test/non-existent.block.clj")
  => (throws))

^{:refer code.gen.core/generate :added "4.0"}
(fact "generates code from a template with substitutions"
  (c/generate "code/templates/def_simple.block" {:name 'my-var :value 10})
  => "(def my-var 10)"

  (let [preprocess-fn (fn [b] (assoc b :value (* 2 (:value b))))]
    (c/generate "resources/test/simple.block.clj" {:name 'my-var :value 10} {:preprocess-fn preprocess-fn}))
  => "(def my-var 20)"

  (let [preprocess-fn (fn [b] (assoc b :value (* 3 (:value b))))]
    (c/generate "resources/test/simple.block.clj" {:name 'my-var :value 10 :preprocess-fn preprocess-fn}))
  => "(def my-var 30)")

^{:refer code.gen.core/template-generator :added "4.0"}
(fact "returns a template function suitable for use with std.lib.foundation/template-entries"
  (let [tmpl-fn (c/template-generator "resources/test/simple.block.clj")]
    (tmpl-fn {:name 'another-var :value "test"}))
  => "(def another-var \"test\")")

^{:refer code.gen.core/gen-namespace-block :added "4.0"}
(fact "generates a std.block AST for a Clojure namespace form"
  (-> (c/gen-namespace-block 'my.generated.core {}) b/string)
  => "(ns my.generated.core)"

  (-> (c/gen-namespace-block 'my.generated.core {'clojure.string 'str 'std.lib 'h}) b/string)
  => "(ns my.generated.core\n  (:require\n    [clojure.string :as str]\n    [std.lib :as h]))")
