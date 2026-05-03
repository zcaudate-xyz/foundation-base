(ns xtgen.gen-common-spec-test
  (:use code.test)
  (:require [clojure.string :as cstr]
            [xtgen.gen-common-spec :refer :all]))

^{:refer xtgen.gen-common-spec/xtalk-entries :added "4.1"}
(fact "TODO")

^{:refer xtgen.gen-common-spec/op-table-vars :added "4.1"}
(fact "finds grammar op table vars"
  (pos? (count (op-table-vars 'hara.lang.base.grammar-spec)))
  => true)

^{:refer xtgen.gen-common-spec/expand-entry-targets :added "4.1"}
(fact "expands alias-bearing grammar entries into explicit targets"
  (->> (op-entries 'hara.lang.base.grammar-spec)
       (filter #(= :defn (:op %)))
       first
       expand-entry-targets
       (mapv :target))
  => '[defabstract defn defn-])

^{:refer xtgen.gen-common-spec/primitive-entries :added "4.1"}
(fact "collects primitive entries from grammar-spec and grammar-macro"
  (let [targets (->> (primitive-entries)
                     (map :target)
                     set)]
    [(every? symbol? targets)
     (contains? targets 'if)
     (contains? targets 'let)
     (contains? targets 'defn)
     (contains? targets 'proto:get)
     (contains? targets '->)
     (contains? targets 'comment)
     (not (contains? targets :=))])
  => [true true true true true true true true])

^{:refer xtgen.gen-common-spec/generate-common-ns-template :added "4.1"}
(fact "creates the namespace prelude"
  (generate-common-ns-template 'xt.lang.spec-primitive)
  => "(ns xt.lang.spec-primitive\n  (:require [hara.lang :as l :refer [defspec.xt]]))\n\n(l/script :xtalk)")

^{:refer xtgen.gen-common-spec/generate-common-type-template :added "4.1"}
(fact "omits spec declarations when no type contract is declared"
  (generate-common-type-template {:target 'if})
  => nil)

^{:refer xtgen.gen-common-spec/generate-common-type :added "4.1"}
(fact "renders explicit primitive spec declarations only"
  (generate-common-type {:target 'if})
  => nil

  (read-string
   (generate-common-type {:target 'x:promise
                          :op-spec {:type '[:fn [[:xt/fn]] :xt/promise]}}))
  => '(defspec.xt x:promise [:fn [[:xt/fn]] :xt/promise]))

^{:refer xtgen.gen-common-spec/generate-common-macro-template :added "4.1"}
(fact "uses declared arglists when present and variadic fallback otherwise"
  [(read-string (generate-common-macro
                 {:target 'proto:get
                  :op-spec {:arglists '([obj])}}))
   (read-string (generate-common-macro {:target 'if}))]
  => '[(defmacro.xt ^{:standalone true}
         proto:get
         ([obj] (list (quote proto:get) obj)))
        (defmacro.xt ^{:standalone true}
          if
         ([x & more] (apply list (quote if) x more)))])

^{:refer xtgen.gen-common-spec/generate-common-macro :added "4.1"}
(fact "TODO")

^{:refer xtgen.gen-common-spec/generate-common-function-template :added "4.1"}
(fact "TODO")

^{:refer xtgen.gen-common-spec/generate-common-function :added "4.1"}
(fact "TODO")

^{:refer xtgen.gen-common-spec/render-lang-primitive-spec :added "4.1"}
(fact "renders the primitive layer with spec and macro pairs"
  (let [output (render-lang-primitive-spec)]
    [(cstr/includes? output "(ns xt.lang.spec-primitive")
     (not (cstr/includes? output "(defspec.xt if"))
     (cstr/includes? output "(defmacro.xt ^{:standalone true} \n  let")
     (not (cstr/includes? output "(defspec.xt proto:get")))])
  => [true true true true])
