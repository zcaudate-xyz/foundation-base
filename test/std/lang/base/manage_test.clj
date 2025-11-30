(ns std.lang.base.manage-test
  (:use code.test)
  (:require [std.lang.base.manage :refer :all]
            [std.lang :as l]
            [xt.lang.base-lib :as k]
            [std.lang.model.spec-xtalk]))

^{:refer std.lang.base.manage/lib-overview-format :added "4.0"}
(fact "formats the lib overview"
  ^:hidden
  
  (lib-overview-format (l/get-book (l/default-library)
                                   :xtalk))
  => string?)

^{:refer std.lang.base.manage/lib-overview :added "4.0"}
(fact "specifies lib overview task"
  (lib-overview :all) => coll?)

^{:refer std.lang.base.manage/lib-module-env :added "4.0"}
(fact "compiles the lib-module task environment"

  (lib-module-env nil)
  => map?)

^{:refer std.lang.base.manage/lib-module-filter :added "4.0"}
(fact "filters modules based on :lang"
  ^:hidden
  
  (lib-module-filter 'xt.lang.base-lib
                     {:lang :xtalk}
                     (:modules (l/get-book (l/default-library)
                                          :xtalk))
                     nil)
  => map?)

^{:refer std.lang.base.manage/lib-module-overview-format :added "4.0"}
(fact "formats the module overview"
  ^:hidden
  
  (lib-module-overview-format (l/get-module (l/default-library)
                                            :xtalk
                                            'xt.lang.base-lib)
                              {:deps true})
  => string?)

^{:refer std.lang.base.manage/lib-module-overview :added "4.0"}
(fact "lists all modules"
  (lib-module-overview :all) => coll?)

^{:refer std.lang.base.manage/lib-module-entries-format-section :added "4.0"}
(fact "formats either code or fragment section"
  (lib-module-entries-format-section (l/get-module (l/default-library) :xtalk 'xt.lang.base-lib) :code {})
  => string?)

^{:refer std.lang.base.manage/lib-module-entries-format :added "4.0"}
(fact "formats the entries of a module"
  ^:hidden
  
  (lib-module-entries-format (l/get-module (l/default-library)
                                           :xtalk
                                           'xt.lang.base-lib)
                             {})
  => string?)

^{:refer std.lang.base.manage/lib-module-entries :added "4.0"}
(fact "outputs module entries"
  (lib-module-entries :all) => coll?)

^{:refer std.lang.base.manage/lib-module-purge-fn :added "4.0"}
(fact "the purge module function"
  (lib-module-purge-fn 'xt.lang.base-lib {:lang :xtalk} (:modules (l/get-book (l/default-library) :xtalk)) nil)
  => anything)

^{:refer std.lang.base.manage/lib-module-purge :added "4.0"}
(fact "purges modules"
  (lib-module-purge :all {:lang :xtalk}) => coll?)

^{:refer std.lang.base.manage/lib-module-unused-fn :added "4.0"}
(fact "analyzes the module for unused links"
  ^:hidden
  
  (lib-module-unused-fn 'xt.lang.base-lib
                     {:lang :xtalk}
                     (:modules (l/get-book (l/default-library)
                                          :xtalk))
                     nil)
  => vector?)

^{:refer std.lang.base.manage/lib-module-unused :added "4.0"}
(fact "lists unused modules"
  (lib-module-unused :all) => coll?)

^{:refer std.lang.base.manage/lib-module-missing-line-number-fn :added "4.0"}
(fact "helper function to `lib-module-missing-line-number`"
  ^:hidden
  
  (lib-module-missing-line-number-fn 'xt.lang.base-lib
                                     {:lang :xtalk}
                                     (:modules (l/get-book (l/default-library)
                                                           :xtalk))
                                     nil)
  => nil)

^{:refer std.lang.base.manage/lib-module-missing-line-number :added "4.0"}
(fact "lists modules with entries that are missing line numbers (due to inproper macros)"
  (lib-module-missing-line-number :all) => coll?)

^{:refer std.lang.base.manage/lib-module-incorrect-alias-fn :added "4.0"}
(fact "helper function to `lib-module-incorrect-alias`"
  ^:hidden
  
  (lib-module-incorrect-alias-fn 'xt.lang.base-lib
                                 {:lang :xtalk}
                                 (:modules (l/get-book (l/default-library)
                                                       :xtalk))
                                 nil)
  => nil)

^{:refer std.lang.base.manage/lib-module-incorrect-alias :added "4.0"}
(fact "lists modules that have an incorrect alias"
  (lib-module-incorrect-alias :all) => coll?)
