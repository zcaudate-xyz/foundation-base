(ns std.lang.model.spec-js.ts-test
  (:use code.test)
  (:require [clojure.string :as str]
            [std.lang.model.spec-js.ts :refer :all]
            [std.lang.model.spec-xtalk.mixer :as mixer]))

^{:refer std.lang.model.spec-js.ts/valid-ts-ident? :added "4.1"}
(fact "checks if string is valid TypeScript identifier"
  (valid-ts-ident? "hello")
  => true

  (valid-ts-ident? "123hello")
  => false

  (valid-ts-ident? "hello-world")
  => false)

^{:refer std.lang.model.spec-js.ts/sanitize-ts-ident :added "4.1"}
(fact "sanitizes string to valid TypeScript identifier"
  (sanitize-ts-ident "hello-world")
  => "hello_world"

  (sanitize-ts-ident "123hello")
  => "_123hello")

^{:refer std.lang.model.spec-js.ts/named-ts-ident :added "4.1"}
(fact "creates named TypeScript identifier"
  (named-ts-ident "MyType" 'my.ns)
  => "MyType"

  (named-ts-ident 'other.ns/Type 'my.ns)
  => "other_ns_Type")

^{:refer std.lang.model.spec-js.ts/export-ts-ident :added "4.1"}
(fact "creates export TypeScript identifier"
  (export-ts-ident "my-type")
  => "my_type")

^{:refer std.lang.model.spec-js.ts/ns->module-path :added "4.1"}
(fact "converts namespace to module path"
  (ns->module-path 'my.ns)
  => "./my/ns")

^{:refer std.lang.model.spec-js.ts/quoted-prop :added "4.1"}
(fact "quotes property name"
  (quoted-prop "hello-world")
  => "\"hello-world\"")

^{:refer std.lang.model.spec-js.ts/prop-ident :added "4.1"}
(fact "returns property identifier"
  (prop-ident "hello")
  => "hello"

  (prop-ident "hello-world")
  => "\"hello-world\"")

^{:refer std.lang.model.spec-js.ts/unwrap-maybe :added "4.1"}
(fact "unwraps maybe type"
  (unwrap-maybe {:kind :maybe :item {:kind :primitive :name :xt/str}})
  => {:kind :primitive :name :xt/str}

  (unwrap-maybe {:kind :primitive :name :xt/str})
  => {:kind :primitive :name :xt/str})

^{:refer std.lang.model.spec-js.ts/emit-record-fields :added "4.1"}
(fact "emits TypeScript record fields"
  (emit-record-fields [{:name "foo" :type {:kind :primitive :name :xt/str} :optional? false}]
                      'my.ns)
  => "  foo: string;")

^{:refer std.lang.model.spec-js.ts/emit-open-record :added "4.1"}
(fact "emits open record indexer"
  (emit-open-record {:key {:kind :primitive :name :xt/str}
                     :value {:kind :primitive :name :xt/num}}
                    'my.ns)
  => "  [key: string]: number;")

^{:refer std.lang.model.spec-js.ts/emit-ts-type :added "4.1"}
(fact "emits TypeScript type"
  (emit-ts-type {:kind :primitive :name :xt/str} 'my.ns)
  => "string"

  (emit-ts-type {:kind :array :item {:kind :primitive :name :xt/str}} 'my.ns)
  => "Array<string>"

  (emit-ts-type {:kind :named :name 'MyType} 'my.ns)
  => "MyType")

^{:refer std.lang.model.spec-js.ts/collect-type-refs :added "4.1"}
(fact "collects type references"
  (collect-type-refs {:kind :named :name 'other.ns/Type})
  => #{'other.ns/Type}

  (collect-type-refs {:kind :primitive :name :xt/str})
  => #{})

^{:refer std.lang.model.spec-js.ts/fn-type :added "4.1"}
(fact "creates function type"
  (fn-type {:inputs [{:type {:kind :primitive :name :xt/str}}]
            :output {:kind :primitive :name :xt/num}})
  => {:kind :fn
      :inputs [{:kind :primitive :name :xt/str}]
      :output {:kind :primitive :name :xt/num}})

^{:refer std.lang.model.spec-js.ts/analysis-import-groups :added "4.1"}
(fact "groups imports from analysis"
  (analysis-import-groups {:ns 'my.ns
                           :specs []
                           :functions []
                           :values []})
  => [])

^{:refer std.lang.model.spec-js.ts/emit-import-item :added "4.1"}
(fact "emits import item"
  (emit-import-item 'MyType 'my.ns)
  => "MyType"

  (emit-import-item 'other.ns/MyType 'my.ns)
  => "MyType as other_ns_MyType")

^{:refer std.lang.model.spec-js.ts/emit-import-declaration :added "4.1"}
(fact "emits import declaration"
  (emit-import-declaration 'my.ns ['other.ns ['other.ns/Type]])
  => "import type { Type as other_ns_Type } from \"./other/ns\";")


^{:refer std.lang.model.spec-js.ts/emit-imports :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-js.ts/emit-spec-declaration :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-js.ts/emit-function-arg :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-js.ts/emit-function-declaration :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-js.ts/emit-value-declaration :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-js.ts/emit-analysis-declarations :added "4.1"}
(fact "TODO")

(fact "does not duplicate same-name callable specs in declaration output"
  (let [out (-> 'std.lang.model.spec-xtalk-typed-fixture
                mixer/mix-namespace
                emit-analysis-declarations)]
    [(count (re-seq #"export type find_user =" out))
     (str/includes? out "export interface User")])
  => [1 true])

^{:refer std.lang.model.spec-js.ts/declaration-output-path :added "4.1"}
(fact "maps runtime outputs to declaration sidecars"
  [(declaration-output-path "dist/demo.js")
   (declaration-output-path "dist/demo.bundle.min.js")
   (declaration-output-path "dist/demo")]
  => ["dist/demo.d.ts"
      "dist/demo.bundle.min.d.ts"
      "dist/demo.d.ts"])

^{:refer std.lang.model.spec-js.ts/module-dts-artifact :added "4.1"}
(fact "builds declaration artifacts alongside runtime output"
  (let [{:keys [output body]}
        (module-dts-artifact {:main 'std.lang.model.spec-xtalk-typed-fixture
                              :runtime-output "dist/spec_xtalk_typed_fixture.js"})]
    [output
     (boolean (re-find #"export interface User" body))
     (boolean (re-find #"export type find_user" body))])
  => ["dist/spec_xtalk_typed_fixture.d.ts" true true])

^{:refer std.lang.model.spec-js.ts/emit-namespace-declarations :added "4.1"}
(fact "TODO")
