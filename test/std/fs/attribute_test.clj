(ns std.fs.attribute-test
  (:use code.test)
  (:require [std.fs.attribute :refer :all])
  (:import (java.nio.file.attribute FileAttribute FileTime)))

^{:refer std.fs.attribute/to-mode-string :added "3.0"}
(fact "transforms mode numbers to mode strings"

  (to-mode-string "455")
  => "r--r-xr-x"

  (to-mode-string "777")
  => "rwxrwxrwx")

^{:refer std.fs.attribute/to-mode-number :added "3.0"}
(fact "transforms mode numbers to mode strings"

  (to-mode-number "r--r-xr-x")
  => "455"

  (to-mode-number "rwxrwxrwx")
  => "777")

^{:refer std.fs.attribute/to-permissions :added "3.0"}
(fact "transforms mode to permissions"

  (to-permissions "455")
  => (contains [:owner-read
                :group-read
                :group-execute
                :others-read
                :others-execute] :in-any-order))

^{:refer std.fs.attribute/from-permissions :added "3.0"}
(fact "transforms permissions to mode"

  (from-permissions [:owner-read
                     :group-read
                     :group-execute
                     :others-read
                     :others-execute])
  => "455")

^{:refer std.fs.attribute/owner :added "3.0"}
(fact "returns the owner of the file"

  (owner "project.clj")
  => string?)

^{:refer std.fs.attribute/lookup-owner :added "3.0"}
(fact "lookup the user registry for the name"

  (lookup-owner "WRONG")
  => (throws))

^{:refer std.fs.attribute/set-owner :added "3.0"}
(fact "sets the owner of a particular file"

  (set-owner "test" "WRONG")
  => (throws))

^{:refer std.fs.attribute/lookup-group :added "3.0"}
(fact "lookup the user registry for the name"

  (lookup-group "WRONG")
  => (throws))

^{:refer std.fs.attribute/attr :added "3.0"}
(fact "creates an attribute for input to various functions"
  (attr "test" 1)
  => (partial instance? FileAttribute))

^{:refer std.fs.attribute/attr-value :added "3.0"}
(fact "adjusts the attribute value for input"
  (every? #(instance? java.nio.file.attribute.PosixFilePermission %)
          (attr-value :permissions "rwxrwxrwx"))
  => true)

^{:refer std.fs.attribute/map->attr-array :added "3.0"}
(fact "converts a clojure map to an array of attrs"
  (map->attr-array {:permissions "rwxrwxrwx"})
  => (partial instance? (class (make-array FileAttribute 0))))

^{:refer std.fs.attribute/attrs->map :added "3.0"}
(fact "converts the map of attributes into a clojure map"
  (attrs->map {"owner" "user" "permissions" #{}})
  => map?)

^{:refer std.fs.attribute/attributes :added "3.0" :class [:attribute]}
(fact "shows all attributes for a given path"

  (attributes "project.clj")
  ;;    {:owner "chris",
  ;;     :group "staff",
  ;;     :permissions "rw-r--r--",
  ;;     :file-key "(dev=1000004,ino=2351455)",
  ;;     :ino 2351455,
  ;;     :is-regular-file true.
  ;;     :is-directory false, :uid 501,
  ;;     :is-other false, :mode 33188, :size 4342,
  ;;     :gid 20, :ctime 1476755481000,
  ;;     :nlink 1,
  ;;     :last-access-time 1476755481000,
  ;;     :is-symbolic-link false,
  ;;     :last-modified-time 1476755481000,
  ;;     :creation-time 1472282953000,
  ;;     :dev 16777220, :rdev 0}
  => map?)

^{:refer std.fs.attribute/set-attributes :added "3.0" :class [:attribute]}
(fact "sets all attributes for a given path"

  (do (set-attributes "project.clj"
                  {:last-modified-time 0})
      (-> (attributes "project.clj")
          :last-modified-time))
  => 0)
