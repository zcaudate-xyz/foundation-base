(ns std.lang.base.book-loader-test
  (:require [std.lang.base.book :as book]
            [std.lang.base.book-loader :as loader]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.registry :as reg])
  (:use code.test))

^{:refer std.lang.base.registry/registry-book-ns :added "4.1"}
(fact "looks up the namespace for a registered book"
  (reg/registry-book-ns :php)
  => 'std.lang.model-annex.spec-php)

^{:refer std.lang.base.book-loader/ensure-book! :added "4.1"}
(fact "loads a book from the registry into an empty library"
  (let [library (lib/library {:snapshot (snap/snapshot {})})]
    (loader/ensure-book! library :php)
    => book/book?

    (lib/get-book library :xtalk)
    => book/book?))
