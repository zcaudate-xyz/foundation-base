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
      :extra    []}

  (cli/parse-test-args ["'xt.foo" ":with" "[dart]"])
  => {:selector 'xt.foo
      :langs    '[dart]
      :extra    []}

  (cli/parse-test-args ["':all"])
  => {:selector :all
      :langs    nil
      :extra    []})

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

^{:refer hara.seedgen.cli/-main :added "4.1"}
(fact "is the CLI entry point"
  (var? #'cli/-main) => true
  (:doc (meta #'cli/-main)) => "main entry point for lein seedgen")

^{:refer hara.seedgen.cli/seedgen-todos :added "4.1"}
(fact "TODO")