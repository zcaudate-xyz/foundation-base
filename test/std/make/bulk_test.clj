(ns std.make.bulk-test
  (:use code.test)
  (:require [std.make.bulk :as bulk]
            [std.make :as make]
            [std.make.project :as project]))

^{:refer std.make.bulk/make-bulk-filter :added "4.0"}
(fact "bulk filter by containers")

^{:refer std.make.bulk/make-bulk-get-keys :added "4.0"}
(fact "bulk get keys"

  (bulk/make-bulk-get-keys {:a true} {:a {:deps []}})
  => '[(:a) (:a)])

^{:refer std.make.bulk/make-bulk-build :added "4.0"}
(fact "build make bulk datastructure"

  (bulk/make-bulk-build {:configs {}})
  => [{} {}])

^{:refer std.make.bulk/make-bulk :added "4.0"}
(fact "make bulk"

  (bulk/make-bulk {:name "test" :configs {} :actions {}})
  => (contains {:built {} :bulked {} :total number?}))

^{:refer std.make.bulk/make-bulk-gh-init :added "4.0"}
(fact "make bulk init github"

  (bulk/make-bulk-gh-init {})
  => {})

^{:refer std.make.bulk/make-bulk-gh-push :added "4.0"}
(fact "make bulk push github"

  (bulk/make-bulk-gh-push {})
  => {})

^{:refer std.make.bulk/make-bulk-container-filter :added "4.0"}
(fact "filters configs based on container membership"

  (bulk/make-bulk-container-filter {} [])
  => {})

^{:refer std.make.bulk/make-bulk-container-build :added "4.0"}
(fact "builds containers for bulk configs"

  (bulk/make-bulk-container-build {} [])
  => (contains {:built {} :bulked {} :total number?}))
