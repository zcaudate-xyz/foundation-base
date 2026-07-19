(ns code.doc.check-test
  (:require [code.doc.check :as check])
  (:use code.test))

^{:refer code.doc.check/check-api-element :added "4.1"}
(fact "flags namespaces missing from the project lookup"

  (check/check-api-element {:lookup {}}
                           {:type :api :namespace "std.lib.missing"})
  => [{:type :missing-namespace :namespace 'std.lib.missing}])

^{:refer code.doc.check/check-api-element :id check-api-entries :added "4.1"}
(fact "flags entries with missing source or examples, and typo'd `:only` vars"

  (check/check-api-element
   {:lookup {'std.lib.collection "src/std/lib/collection.clj"}}
   {:type :api :namespace "std.lib.collection"
    :table {'map-keys {:source {:code "(defn map-keys ...)"}
                       :test   {:code nil}}
            'map-vals {:source {:code nil}
                       :test   {:code "(fact ...)"}}}
    :only ["map-keys" "map-indexed"]})
  => [{:type :missing-example :namespace 'std.lib.collection :var 'map-keys}
      {:type :missing-source :namespace 'std.lib.collection :var 'map-vals}
      {:type :missing-only-var :namespace 'std.lib.collection :var 'map-indexed}])

^{:refer code.doc.check/check-api-element :id check-api-generated :added "4.1"}
(fact "does not flag runtime-generated vars"

  (check/check-api-element
   {:lookup {'std.lib.bin "src/std/lib/bin.clj"}}
   {:type :api :namespace "std.lib.bin"
    :table {'double-buffer {:source {:generated true}
                            :test {:code "(fact ...)"}}}})
  => [])

^{:refer code.doc.check/check-reference-element :added "4.1"}
(fact "flags the missing reference placeholder"

  (check/check-reference-element
   {:type :reference
    :refer "std.lib.collection/missing"
    :code "MISSING REFERENCE {:mode :source :refer std.lib.collection/missing}"})
  => [{:type :missing-reference :refer "std.lib.collection/missing"}]

  (check/check-reference-element
   {:type :reference :refer "std.lib.collection/map-keys" :code "(defn map-keys ...)"})
  => nil)

^{:refer code.doc.check/check-element :added "4.1"}
(fact "flags data directive errors and failed tests"

  (check/check-element {} {:type :related :error "code.doc data: no related entries"})
  => [{:type :unknown-data-group :error "code.doc data: no related entries"}]

  (check/check-element {} {:type :test :failed {:output [{} {}]} :line {:row 5}})
  => [{:type :failed-test :line 5 :failures 2}]

  (check/check-element {} {:type :paragraph :text "hello"})
  => nil)

^{:refer code.doc.check/select-pages :added "4.1"}
(fact "selects pages by site prefix"

  (check/select-pages {'core/getting-started {} 'core/index {} 'std/jvm-monitor {}}
                      '[core])
  => '[core/getting-started core/index]

  (check/select-pages {'core/getting-started {} 'std/jvm-monitor {}}
                      :all)
  => '[core/getting-started std/jvm-monitor])
