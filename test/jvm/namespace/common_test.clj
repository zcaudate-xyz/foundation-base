(ns jvm.namespace.common-test
  (:require [jvm.namespace.common :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [ns-unmap ns-unalias]))

^{:refer jvm.namespace.common/ns-unalias :added "3.0"}
(comment "removes given aliases in namespaces"

  (ns-unalias *ns* '[hello world])
  => '[hello world])

^{:refer jvm.namespace.common/ns-unmap :added "3.0"}
(comment "removes given mapped elements in namespaces"

  (ns-unmap *ns* '[hello world])
  => '[hello world])

^{:refer jvm.namespace.common/ns-clear-aliases :added "3.0"}
(comment "removes all namespace aliases"

  ;; require std.string
  (require '[std.string :as str])
  => nil

  ;; error if a new namespace is set to the same alias
  (require '[clojure.set :as string])
  => (throws) ;;  Alias string already exists in namespace

  ;; clearing all aliases
  (ns-clear-aliases)

  (ns-aliases *ns*)
  => {}

  ;; okay to require
  (require '[clojure.set :as string])
  => nil)

^{:refer jvm.namespace.common/ns-list-external-imports :added "3.0"}
(fact "lists all external imports"

  (import java.io.File)
  (ns-list-external-imports *ns*)
  => '(File))

^{:refer jvm.namespace.common/ns-clear-external-imports :added "3.0"}
(fact "clears all external imports"

  (ns-clear-external-imports *ns*)
  (ns-list-external-imports *ns*)
  => ())

^{:refer jvm.namespace.common/ns-clear-mappings :added "3.0"}
(comment "removes all mapped vars in the namespace"

  ;; require `join`
  (require '[std.string.base :refer [join]])

  ;; check that it runs
  (join ["a" "b" "c"])
  => "abc"

  ;; clear mappings
  (ns-clear-mappings)

  ;; the mapped symbol is gone
  (join ["a" "b" "c"])
  => (throws) ;; "Unable to resolve symbol: join in this context"
  )

^{:refer jvm.namespace.common/ns-clear-interns :added "3.0"}
(fact "clears all interns in a given namespace"

  (ns-clear-interns *ns*))

^{:refer jvm.namespace.common/ns-clear-refers :added "3.0"}
(fact "clears all refers in a given namespace")

^{:refer jvm.namespace.common/ns-clear :added "3.0"}
(comment "clears all mappings and aliases in a given namespace"

  (ns-clear))

^{:refer jvm.namespace.common/group-in-memory :added "3.0"}
(fact "creates human readable results from the class list"

  (group-in-memory ["code.manage$add$1" "code.manage$add$2"
                    "code.manage$sub$1" "code.manage$sub$2"])
  => '[[add 2]
       [sub 2]])

^{:refer jvm.namespace.common/raw-in-memory :added "3.0"}
(fact "returns a list of keys representing objects"

  (raw-in-memory 'code.manage)
  ;;("code.manage$eval6411" "code.manage$eval6411$loading__5569__auto____6412")
  => coll?)

^{:refer jvm.namespace.common/ns-in-memory :added "3.0"}
(fact "retrieves all the clojure namespaces currently in memory"

  (ns-in-memory 'code.manage)
  ;;[[EVAL 2]]
  => coll?)

^{:refer jvm.namespace.common/ns-loaded? :added "3.0"}
(fact "checks if the namespaces is currently active"

  (ns-loaded? 'jvm.namespace.common)
  => true)

^{:refer jvm.namespace.common/ns-delete :added "3.0"}
(comment "clears all namespace mappings and remove namespace from clojure environment"

  (ns-delete 'jvm.namespace)

  (ns-delete 'jvm.namespace-test))

^{:refer jvm.namespace.common/ns-list :added "3.0"}
(fact "returns all existing clojure namespaces"

  (ns-list)
  => coll?)

^{:refer jvm.namespace.common/ns-reload :added "3.0"}
(fact "reloads aliases in current namespace")

^{:refer jvm.namespace.common/ns-reload-all :added "3.0"}
(fact "reloads aliases and dependents in current namespace")

(comment
  (code.manage/import))

