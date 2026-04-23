(ns std.lang.base.grammar-test
  (:require [std.lang.base.grammar :refer :all]
             [std.lang.base.grammar-spec :as spec])
  (:use code.test))

(defn grammar-rewrite-probe
  [form _ _]
  form)

^{:refer std.lang.base.grammar/gen-ops :added "4.0"}
(fact "generates ops"

  (gen-ops 'std.lang.base.grammar-spec "spec")
  => vector?)

^{:refer std.lang.base.grammar/collect-ops :added "4.0"}
(fact "collects alll ops together"

  (collect-ops +op-all+)
  => map?)

^{:refer std.lang.base.grammar/ops-list :added "4.0"}
(fact "lists all ops in the grammar"

  (vec (ops-list))
  => [:builtin
      :builtin-global
      :builtin-module
      :builtin-helper
      :free-control
      :free-literal
      :math
      :compare
      :logic
      :counter
      :return
      :throw
      :await
      :async
      :data-table
      :data-shortcuts
      :data-range
      :vars
      :bit
      :pointer
      :fn
      :block
      :control-base
      :control-general
      :control-try-catch
      :top-base
      :top-global
      :class
      :for
      :coroutine
      :functional-core
      :macro
      :macro-arrow
      :macro-let
      :macro-xor
      :macro-case
      :macro-forange
      :xtalk-common
      :xtalk-functional
      :xtalk-language-specific
      :xtalk-std-lang-link-specific
      :xtalk-runtime-specific])

^{:refer std.lang.base.grammar/ops-symbols :added "4.0"}
(fact "gets a list of symbols"

  (ops-symbols)
  => coll?)

^{:refer std.lang.base.grammar/ops-summary :added "4.0"}
(fact "gets the symbol and op name for a given category"

  (ops-summary [:macro])
  => '[[:macro {:tfirst #{->}, :tlast #{->>}, :doto #{doto}, :if #{if}, :cond #{cond}, :when #{when}}]]

  (ops-summary [:counter])
  => [[:counter {:incby #{:+=}, :decby #{:-=}, :mulby #{:*=}, :incto #{:++}, :decto #{:--}}]])

^{:refer std.lang.base.grammar/ops-detail :added "4.0"}
(fact "get sthe detail of the ops"

  (ops-detail :macro-arrow)
  => map?)

^{:refer std.lang.base.grammar/build :added "3.0"}
(fact "selector for picking required ops in grammar"

  (build :include [:vars])
  => map?)

^{:refer std.lang.base.grammar/build-min :added "4.0"}
(fact "minimum ops example for a language"

  (build-min)
  => map?)

^{:refer std.lang.base.grammar/build-xtalk :added "4.0"}
(fact "xtalk ops"

  (build-xtalk)
  => map?)

^{:refer std.lang.base.grammar/build-functional-core :added "4.1"}
(fact "functional core ops"

  (build-functional-core)
  => map?)

^{:refer std.lang.base.grammar/build:override :added "4.0"}
(fact "overrides existing ops in the map"

  (build:override (build-min)
                  {:WRONG {}})
  => (throws)

  (build:override (build-min)
                  {:ret {}})
  => map?)

^{:refer std.lang.base.grammar/build:extend :added "4.0"}
(fact "adds new  ops in the map"

  (build:extend (build-min)
                {:NEW {}})
  => map?

  (build:extend (build-min)
                {:ret {}})
  => (throws))

^{:refer std.lang.base.grammar/to-reserved :added "3.0"}
(fact "convert op map to symbol map"

  (to-reserved (build :include [:vars]))
  => '{:=      {:op :seteq, :symbol #{:=}, :emit :assign, :raw "="},
       var     {:op :var,
                :symbol #{var},
                :emit :def-assign,
                :raw "",
                :assign "="}})

^{:refer std.lang.base.grammar/grammar-structure :added "3.0"}
(fact "returns all the `:block` and `:fn` forms"

  (grammar-structure (build :include [:vars]))
  => {:block #{}, :def #{}, :fn #{}}


  (grammar-structure (build :include [:control-general]))
  => {:block #{:for :while :branch}, :def #{}, :fn #{}}

  (grammar-structure (build :include [:top-base]))
  => {:block #{},
      :def #{:defn :def :defrun},
      :fn #{}})

^{:refer std.lang.base.grammar/grammar-sections :added "3.0"}
(fact "process sections witihin the grammar"

  (grammar-sections (build :include [:top-base]))
  => #{:code}

  (grammar-sections (build))
  => #{:code})

^{:refer std.lang.base.grammar/grammar-macros :added "3.0"}
(fact "process macros within the grammar"

  (grammar-macros (build-min))
  => #{:defn :defglobal :def :defrun})

^{:refer std.lang.base.grammar/grammar? :added "3.0"}
(fact "checks that an object is instance of grammar"

  (grammar? (grammar :test
              (to-reserved (build))
              {}))
  => true)

^{:refer std.lang.base.grammar/grammar :added "3.0"
  :style/indent 1}
(fact "constructs a grammar"

  (grammar :test
    (to-reserved (build-min))
    {})
  => map?

  (select-keys
   (grammar :test
     (to-reserved (build-min))
     {:rewrite [#'grammar-rewrite-probe]})
   [:tag :rewrite])
  => {:tag :test
      :rewrite [#'grammar-rewrite-probe]})

(comment
  (./import)
  (./create-tests))


^{:refer std.lang.base.grammar/normalize-op-entry :added "4.1"}
(fact "normalizes shared macro and hard-link defaults"
  (normalize-op-entry
   {:op :x-add
    :emit :macro
    :macro #'std.lang.base.grammar-xtalk/tf-add})
  => (contains {:value/template #'std.lang.base.grammar-xtalk/tf-add
                :value/standalone true})

  (normalize-op-entry
   {:op :helper
    :emit :hard-link
    :raw 'xt.lang.common-data/obj-keys})
  => (contains {:value/standalone 'xt.lang.common-data/obj-keys}))


^{:refer std.lang.base.grammar/default-lookup :added "4.1"}
(fact "TODO")
