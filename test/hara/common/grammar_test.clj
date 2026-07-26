tahto/common/grammar_test.clj:1:(ns tahto.common.grammar-test
tahto/common/grammar_test.clj:2:  (:require [tahto.common.grammar :refer :all]
tahto/common/grammar_test.clj:3:            [tahto.common.grammar-spec :as spec])
  (:use code.test))

tahto/common/grammar_test.clj:6:^{:refer tahto.common.grammar/gen-ops :added "4.0"}
(fact "generates ops"

tahto/common/grammar_test.clj:9:  (gen-ops 'tahto.common.grammar-spec "spec")
  => vector?)

tahto/common/grammar_test.clj:12:^{:refer tahto.common.grammar/normalize-op-entry :added "4.1"}
(fact "normalizes shared macro and hard-link defaults"
  (normalize-op-entry
   {:op :x-add
    :emit :macro
tahto/common/grammar_test.clj:17:    :macro #'tahto.common.grammar-xtalk/tf-add})
tahto/common/grammar_test.clj:18:  => (contains {:value/template #'tahto.common.grammar-xtalk/tf-add
                :value/standalone true})

  (normalize-op-entry
   {:op :prototype-create
    :emit :macro
tahto/common/grammar_test.clj:24:    :macro #'tahto.common.grammar-xtalk/tf-add})
tahto/common/grammar_test.clj:25:  => (contains {:value/template #'tahto.common.grammar-xtalk/tf-add
                :value/standalone true})

  (normalize-op-entry
   {:op :helper
    :emit :hard-link
    :raw 'xt.lang.common-data/obj-keys})
  => (contains {:value/standalone 'xt.lang.common-data/obj-keys}))

tahto/common/grammar_test.clj:34:^{:refer tahto.common.grammar/collect-ops :added "4.0"}
(fact "collects alll ops together"

  (collect-ops +op-all+)
  => map?)

tahto/common/grammar_test.clj:40:^{:refer tahto.common.grammar/ops-list :added "4.0"}
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
      :prototype
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
      :xtalk-tahto.core-link-specific
      :xtalk-runtime-specific])

tahto/common/grammar_test.clj:88:^{:refer tahto.common.grammar/ops-symbols :added "4.0"}
(fact "gets a list of symbols"

  (ops-symbols)
  => coll?)

tahto/common/grammar_test.clj:94:^{:refer tahto.common.grammar/ops-summary :added "4.0"}
(fact "gets the symbol and op name for a given category"

  (ops-summary [:macro])
  => '[[:macro {:tfirst #{->}, :tlast #{->>}, :doto #{doto}, :if #{if}, :cond #{cond}, :when #{when}}]]

  (ops-summary [:counter])
  => [[:counter {:incby #{:+=}, :decby #{:-=}, :mulby #{:*=}, :incto #{:++}, :decto #{:--}}]])

tahto/common/grammar_test.clj:103:^{:refer tahto.common.grammar/ops-detail :added "4.0"}
(fact "get sthe detail of the ops"

  (ops-detail :macro-arrow)
  => map?)

tahto/common/grammar_test.clj:109:^{:refer tahto.common.grammar/default-lookup :added "4.1"}
(fact "removes optional categories from the lookup"
  (contains? (set (keys (default-lookup +op-all+)))
             :functional-core)
  => false

  (contains? (set (keys (default-lookup +op-all+)))
             :math)
  => true)

tahto/common/grammar_test.clj:119:^{:refer tahto.common.grammar/build :added "4.1"}
(fact "functional core ops are selected explicitly"

  (build :include [:functional-core])
  => map?)

tahto/common/grammar_test.clj:125:^{:refer tahto.common.grammar/build-min :added "4.0"}
(fact "minimum ops example for a language"

  (build-min)
  => map?)

tahto/common/grammar_test.clj:131:^{:refer tahto.common.grammar/build-xtalk :added "4.0"}
(fact "xtalk ops"

  (build-xtalk)
  => map?)

tahto/common/grammar_test.clj:137:^{:refer tahto.common.grammar/build:override :added "4.0"}
(fact "overrides existing ops in the map"

  (build:override (build-min)
                  {:WRONG {}})
  => (throws)

  (build:override (build-min)
                  {:ret {}})
  => map?)

tahto/common/grammar_test.clj:148:^{:refer tahto.common.grammar/build:extend :added "4.0"}
(fact "adds new  ops in the map"

  (build:extend (build-min)
                {:NEW {}})
  => map?

  (build:extend (build-min)
                {:ret {}})
  => (throws))

tahto/common/grammar_test.clj:159:^{:refer tahto.common.grammar/to-reserved :added "3.0"}
(fact "convert op map to symbol map"

  (to-reserved (build :include [:vars]))
  => '{:=      {:op :seteq, :symbol #{:=}, :emit :assign, :raw "="},
       var     {:op :var,
                :symbol #{var},
                :emit :def-assign,
                :raw "",
                :assign "="}})

tahto/common/grammar_test.clj:170:^{:refer tahto.common.grammar/grammar-structure :added "3.0"}
(fact "returns all the `:block` and `:fn` forms"

  (grammar-structure (build :include [:vars]))
  => {:block #{}, :def #{}, :fn #{}}


  (grammar-structure (build :include [:control-general]))
  => {:block #{:for :while :branch}, :def #{}, :fn #{}}

  (grammar-structure (build :include [:top-base]))
  => {:block #{},
      :def #{:defn :def :defrun},
      :fn #{}})

tahto/common/grammar_test.clj:185:^{:refer tahto.common.grammar/grammar-sections :added "3.0"}
(fact "process sections witihin the grammar"

  (grammar-sections (build :include [:top-base]))
  => #{:code}

  (grammar-sections (build))
  => #{:code})

tahto/common/grammar_test.clj:194:^{:refer tahto.common.grammar/grammar-macros :added "3.0"}
(fact "process macros within the grammar"

  (grammar-macros (build-min))
  => #{:defn :defglobal :def :defrun})

tahto/common/grammar_test.clj:200:^{:refer tahto.common.grammar/grammar? :added "3.0"}
(fact "checks that an object is instance of grammar"

  (grammar? (grammar :test
              (to-reserved (build))
              {}))
  => true)

tahto/common/grammar_test.clj:208:^{:refer tahto.common.grammar/grammar :added "3.0"
  :style/indent 1}
(fact "constructs a grammar"

  (grammar :test
    (to-reserved (build-min))
    {})
  => map?)

(comment
  (./import)
  (./create-tests))