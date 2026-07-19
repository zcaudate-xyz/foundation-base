(ns hara.seedgen.cli-test
  (:require [hara.seedgen.cli :as cli])
  (:use code.test))

^{:refer hara.seedgen.cli/collect-selector :added "4.1"}
(fact "collects :only namespace args into a vector selector"

  (cli/collect-selector [":only" "xt.foo" "xt.bar" "xt.baz"])
  => [['xt.foo 'xt.bar 'xt.baz] []]

  (cli/collect-selector [":only" "xt.foo" "xt.bar" ":with" "[dart ruby]"])
  => [['xt.foo 'xt.bar] [":with" "[dart ruby]"]]

  (cli/collect-selector ["'xt.foo"])
  => ['xt.foo []]

  (cli/collect-selector ["'[xt.foo xt.bar]"])
  => [['xt.foo 'xt.bar] []])

^{:refer hara.seedgen.cli/parse-test-args :added "4.1"}
(fact "parses seedgen test args, including multi-namespace :only"

  (cli/parse-test-args [":only" "xt.foo" "xt.bar" ":with" "[dart ruby]"])
  => {:selector '[xt.foo xt.bar]
      :langs    '[dart ruby]
      :extra    []
      :params   {}}

  (cli/parse-test-args ["'xt.foo" ":with" "[dart]"])
  => {:selector 'xt.foo
      :langs    '[dart]
      :extra    []
      :params   {}}

  (cli/parse-test-args ["':all"])
  => {:selector :all
      :langs    nil
      :extra    []
      :params   {}}

  (cli/parse-test-args ["'xt.foo" ":with" "[dart]" ":metrics" "target/out.json"])
  => {:selector 'xt.foo
      :langs '[dart]
      :extra [":metrics" "target/out.json"]
      :params {:metrics 'target/out.json}})

^{:refer hara.seedgen.cli/parse-command-args :added "4.1"}
(fact "parses seedgen command args, including multi-namespace :only"

  (cli/parse-command-args [":only" "xt.foo" "xt.bar" ":lang" "lua"])
  => {:selector '[xt.foo xt.bar]
      :params   {:lang 'lua}}

  (cli/parse-command-args ["'xt.foo" ":lang" "python"])
  => {:selector 'xt.foo
      :params   {:lang 'python}})


^{:refer hara.seedgen.cli/seedgen-test :added "4.1"}
(fact "is the bench-test runner entry point"
  (var? #'cli/seedgen-test) => true
  (:doc (meta #'cli/seedgen-test)) => "generates and runs xtbench tests for the given selector and languages")

^{:refer hara.seedgen.cli/failure-tree :added "4.1"}
(fact "groups test failures by canonical namespace and function"
  (let [failed {:meta {:ns 'xtbench.lua.lang.sample-test
                       :refer 'xt.lang.sample/parse
                       :path "/repo/test-lang/xtbench/lua/lang/sample_test.clj"
                       :line 42}}
        thrown {:meta {:ns 'xtbench.lua.lang.sample-test
                       :function 'xt.lang.sample/emit
                       :path "test-lang/xtbench/lua/lang/sample_test.clj"
                       :line 51}}
        summary (with-meta {:passed 3 :failed 2 :throw 1 :timeout 0}
                  {:data {:failed [failed failed]
                          :throw [thrown]
                          :timeout []}})]
    (cli/failure-tree summary
                      ['xtbench.lua.lang.load-error-test]
                      {:root "/repo"}))
  => [{:namespace "lang.load-error-test"
       :runtime-namespace "xtbench.lua.lang.load-error-test"
       :counts {:errored 1}
       :functions []
       :namespace-errors [{:type :errored :count 1}]}
      {:namespace "lang.sample-test"
       :runtime-namespace "xtbench.lua.lang.sample-test"
       :counts {:failed 2 :throw 1}
       :functions [{:function "xt.lang.sample/emit"
                    :counts {:throw 1}
                    :locations [{:type :throw :count 1
                                 :path "test-lang/xtbench/lua/lang/sample_test.clj"
                                 :line 51}]}
                   {:function "xt.lang.sample/parse"
                    :counts {:failed 2}
                    :locations [{:type :failed :count 2
                                 :path "test-lang/xtbench/lua/lang/sample_test.clj"
                                 :line 42}]}]
       :namespace-errors []}])

^{:refer hara.seedgen.cli/-main :added "4.1"}
(fact "is the CLI entry point"
  (var? #'cli/-main) => true
  (:doc (meta #'cli/-main)) => "main entry point for lein seedgen")

^{:refer hara.seedgen.cli/seedgen-todos :added "4.1"}
(fact "returns 0 TODO facts for a selector matching no namespaces"
  (cli/seedgen-todos 'nonexistent.namespace.selector)
  => 0

  (cli/seedgen-todos 'nonexistent.namespace.selector {:write true})
  => 0)
