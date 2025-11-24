(ns std.make.bulk-test
  (:use code.test)
  (:require [std.make.bulk :as bulk]
            [std.make :as make]))


^{:refer std.make.bulk/make-bulk-filter :added "4.0"}
(fact "bulk filter by containers")

^{:refer std.make.bulk/make-bulk-get-keys :added "4.0"}
(fact "bulk get keys")

^{:refer std.make.bulk/make-bulk-build :added "4.0"}
(fact "build make bulk datastructure")

^{:refer std.make.bulk/make-bulk :added "4.0"}
(fact "make bulk")

^{:refer std.make.bulk/make-bulk-gh-init :added "4.0"}
(fact "make bulk init github")

^{:refer std.make.bulk/make-bulk-gh-push :added "4.0"}
(fact "make bulk push github")


^{:refer std.make.bulk/make-bulk-container-filter :added "4.0"}
(fact "filters configs based on container membership")

^{:refer std.make.bulk/make-bulk-container-build :added "4.0"}
(fact "builds containers for bulk configs")