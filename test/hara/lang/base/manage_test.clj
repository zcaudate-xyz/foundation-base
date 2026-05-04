(ns hara.lang.base.manage-test
  (:require [xt.lang.common-data]
            [xt.lang.common-lib]
            [hara.lang :as l]
             [hara.common.emit-prep-lua-test :as prep-lua]
             [hara.lang.base.impl :as impl]
             [hara.lang.base.library :as lib]
             [hara.lang.base.library-snapshot :as snap]
             [hara.lang.base.manage :refer :all]
             [xt.lang.common-lib :as k])
  (:use code.test))

(def +library+
  (lib/library {:snapshot (snap/snapshot {:lua {:id :lua
                                                :book prep-lua/+book-min+}})}))

^{:refer hara.lang.base.manage/lib-overview-format :added "4.0"}
(fact "formats the lib overview"

  (lib-overview-format (l/get-book +library+
                                   :lua))
  => string?)

^{:refer hara.lang.base.manage/lib-overview :added "4.0"}
(fact "specifies lib overview task"
  (impl/with:library [+library+]
    (lib-overview :all))
  => coll?)

^{:refer hara.lang.base.manage/lib-module-env :added "4.0"}
(fact "compiles the lib-module task environment"

  (lib-module-env nil)
  => map?)

^{:refer hara.lang.base.manage/lib-module-filter :added "4.0"}
(fact "filters modules based on :lang"

  (lib-module-filter 'L.core
                     {:lang :lua}
                     (:modules (l/get-book +library+
                                           :lua))
                     nil)
  => map?)

^{:refer hara.lang.base.manage/lib-module-overview-format :added "4.0"}
(fact "formats the module overview"

  (lib-module-overview-format (l/get-module +library+
                                            :lua
                                            'L.core)
                              {:deps true})
  => string?)

^{:refer hara.lang.base.manage/lib-module-overview :added "4.0"}
(fact "lists all modules"
  (impl/with:library [+library+]
    (lib-module-overview :all))
  => coll?)

^{:refer hara.lang.base.manage/lib-module-entries-format-section :added "4.0"}
(fact "formats either code or fragment section"
  (lib-module-entries-format-section (l/get-module +library+ :lua 'L.core) :code {})
  => string?)

^{:refer hara.lang.base.manage/lib-module-entries-format :added "4.0"}
(fact "formats the entries of a module"

  (lib-module-entries-format (l/get-module +library+
                                           :lua
                                           'L.core)
                             {})
  => string?)

^{:refer hara.lang.base.manage/lib-module-entries :added "4.0"}
(fact "outputs module entries"
  (impl/with:library [+library+]
    (lib-module-entries :all))
  => coll?)

^{:refer hara.lang.base.manage/lib-module-purge-fn :added "4.0"}
(fact "the purge module function"
  (lib-module-purge-fn 'L.core {:lang :lua} (:modules (l/get-book +library+ :lua)) nil)
  => anything)

^{:refer hara.lang.base.manage/lib-module-purge :added "4.0"}
(fact "purges modules"
  (impl/with:library [+library+]
    (lib-module-purge :all {:lang :lua}))
  => coll?)

^{:refer hara.lang.base.manage/lib-module-unused-fn :added "4.0"}
(fact "analyzes the module for unused links"

  (lib-module-unused-fn 'L.core
                        {:lang :lua}
                        (:modules (l/get-book +library+
                                              :lua))
                        nil)
  => vector?)

^{:refer hara.lang.base.manage/lib-module-unused :added "4.0"}
(fact "lists unused modules"
  (impl/with:library [+library+]
    (lib-module-unused :all))
  => coll?)

^{:refer hara.lang.base.manage/lib-module-missing-line-number-fn :added "4.0"}
(fact "helper function to `lib-module-missing-line-number`"

  (lib-module-missing-line-number-fn 'L.core
                                     {:lang :lua}
                                     (:modules (l/get-book +library+
                                                           :lua))
                                     nil)
  => nil)

^{:refer hara.lang.base.manage/lib-module-missing-line-number :added "4.0"}
(fact "lists modules with entries that are missing line numbers (due to inproper macros)"
  (impl/with:library [+library+]
    (lib-module-missing-line-number :all))
  => coll?)

^{:refer hara.lang.base.manage/lib-module-incorrect-alias-fn :added "4.0"}
(fact "helper function to `lib-module-incorrect-alias`"

  (lib-module-incorrect-alias-fn 'L.core
                                 {:lang :lua}
                                 (:modules (l/get-book +library+
                                                       :lua))
                                 nil)
  => nil)

^{:refer hara.lang.base.manage/lib-module-incorrect-alias :added "4.0"}
(fact "lists modules that have an incorrect alias"
  (impl/with:library [+library+]
    (lib-module-incorrect-alias :all))
  => coll?)
