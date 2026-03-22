(ns std.fs.common-test
  (:require [std.fs.common :refer :all]
            [std.lib.enum :as enum])
  (:use code.test)
  (:import (java.nio.file AccessMode)))

^{:refer std.fs.common/option :added "3.0" :class [:file]}
(fact "shows all options for file operations"

  (option)
  => (contains [:atomic-move :create-new
                :skip-siblings :read :continue
                :create :terminate :copy-attributes
                :append :truncate-existing :sync
                :follow-links :delete-on-close :write
                :dsync :replace-existing :sparse
                :nofollow-links :skip-subtree])

  (option :read)
  => java.nio.file.StandardOpenOption/READ)

^{:refer std.fs.common/pattern :added "3.0"}
(fact "takes a string as turns it into a regex pattern"

  (pattern ".clj")
  => #"\Q.\Eclj"

  (pattern "src/*")
  => #"src/.+")

^{:refer std.fs.common/tag-filter :added "3.0"}
(fact "adds a tag to the filter to identify the type"

  (tag-filter {:pattern #"hello"})
  => (just {:tag :pattern
            :pattern #"hello"}))

^{:refer std.fs.common/characterise-filter :added "3.0"}
(fact "characterises a filter based on type"

  (characterise-filter "src")
  => (just {:tag :pattern :pattern #"src"})

  (characterise-filter (fn [_] nil))
  => (just {:tag :fn :fn fn?}))

(comment

  (code.manage/import))