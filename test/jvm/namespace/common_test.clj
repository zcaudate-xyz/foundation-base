(ns jvm.namespace.common-test
  (:require [jvm.namespace.common :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [ns-unmap ns-unalias]))

(defmacro with-temp-ns [[sym] & body]
  `(let [~sym (create-ns (gensym "jvm.namespace.common-test-"))]
     (try
       ~@body
       (finally
         (remove-ns (ns-name ~sym))))))

^{:refer jvm.namespace.common/ns-unalias :added "3.0"}
(fact "removes given aliases in namespaces"

  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (alias 'str 'clojure.string))
    [(ns-unalias tmp 'str)
     (ns-aliases tmp)])
  => ['str {}])

^{:refer jvm.namespace.common/ns-unmap :added "3.0"}
(fact "removes given mapped elements in namespaces"

  (with-temp-ns [tmp]
    (intern tmp 'hello 1)
    [(ns-unmap tmp 'hello)
     (contains? (ns-map tmp) 'hello)])
  => ['hello false])

^{:refer jvm.namespace.common/ns-clear-aliases :added "3.0"}
(fact "removes all namespace aliases"

  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (alias 'str 'clojure.string)
      (alias 'set 'clojure.set))
    [(set (ns-clear-aliases tmp))
     (ns-aliases tmp)])
  => [#{'str 'set} {}])

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
(fact "removes all mapped vars in the namespace"

  (with-temp-ns [tmp]
    (intern tmp 'join (fn [xs] (apply str xs)))
    [(some #{'join} (ns-clear-mappings tmp))
     (contains? (ns-map tmp) 'join)])
  => ['join false])

^{:refer jvm.namespace.common/ns-clear-interns :added "3.0"}
(fact "clears all interns in a given namespace"

  (with-temp-ns [tmp]
    (intern tmp 'hello 1)
    [(ns-clear-interns tmp)
     (ns-interns tmp)])
  => [['hello] {}])

^{:refer jvm.namespace.common/ns-clear-refers :added "3.0"}
(fact "clears all refers in a given namespace"
  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (clojure.core/refer 'clojure.string :only '[join]))
    [(ns-clear-refers tmp)
     (ns-refers tmp)])
  => [['join] {}])

^{:refer jvm.namespace.common/ns-clear :added "3.0"}
(fact "clears all mappings and aliases in a given namespace"

  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (alias 'str 'clojure.string)
      (clojure.core/refer 'clojure.string :only '[join])
      (import java.io.File))
    (intern tmp 'hello 1)
    (do (doall (ns-clear tmp))
        [(ns-aliases tmp)
         (ns-refers tmp)
         (ns-interns tmp)
         (ns-list-external-imports tmp)]))
  => [{} {} {} ()])

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
(fact "clears all namespace mappings and remove namespace from clojure environment"

  (let [sym (gensym "jvm.namespace.delete-test-")
        tmp (create-ns sym)]
    (intern tmp 'hello 1)
    (ns-delete sym)
    (contains? (set (ns-list)) sym))
  => false)

^{:refer jvm.namespace.common/ns-list :added "3.0"}
(fact "returns all existing clojure namespaces"

  (ns-list)
  => coll?)

^{:refer jvm.namespace.common/ns-reload :added "3.0"}
(fact "reloads aliases in current namespace"
  (do (ns-reload 'jvm.namespace.common)
      (ns-loaded? 'jvm.namespace.common))
  => true)

^{:refer jvm.namespace.common/ns-reload-all :added "3.0"}
(fact "reloads aliases and dependents in current namespace"
  (do (ns-reload-all 'jvm.namespace.common)
      (ns-loaded? 'jvm.namespace.common))
  => true)

(comment
  (code.manage/import))
