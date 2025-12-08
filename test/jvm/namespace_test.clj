(ns jvm.namespace-test
  (:use code.test)
  (:require [jvm.namespace :as ns]
            [std.lib :refer [definvoke] :as h]
            [std.lib.result :as res]))

^{:refer jvm.namespace/list-aliases :added "3.0"}
(fact "namespace list all aliases task"

  (ns/list-aliases '[jvm.namespace])
  => map?)

^{:refer jvm.namespace/clear-aliases :added "3.0"}
(comment "removes all namespace aliases" ^:hidden

  ;; require std.string
  (require '[std.string :as str])
  => nil

  ;; error if a new namespace is set to the same alias
  (require '[clojure.set :as string])
  => (throws) ;;  Alias string already exists in namespace

  ;; clearing all aliases
  (clear-aliases)

  (ns-aliases *ns*)
  => {}

  ;; okay to require
  (require '[clojure.set :as string])
  => nil)

^{:refer jvm.namespace/list-imports :added "3.0"}
(fact "namespace list all imports task"

  (ns/list-imports '[jvm.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 482}
  => map?)

^{:refer jvm.namespace/list-external-imports :added "3.0"}
(fact "lists all external imports"

  (ns/list-external-imports '[jvm.namespace] {:return :summary})
  => map?)

^{:refer jvm.namespace/clear-external-imports :added "3.0"}
(fact "clears all external imports")

^{:refer jvm.namespace/list-mappings :added "3.0"}
(fact "namespace list all mappings task"

  (ns/list-mappings '[jvm.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3674}
  => map?)

^{:refer jvm.namespace/clear-mappings :added "3.0"}
(comment "removes all mapped vars in the namespace" ^:hidden

  ;; require `join`
  (require '[std.string.base :refer [join]])

  ;; check that it runs
  (join ["a" "b" "c"])
  => "abc"

  ;; clear mappings
  (ns/clear-mappings)

  ;; the mapped symbol is gone
  (join ["a" "b" "c"])
  => (throws) ;; "Unable to resolve symbol: join in this context"
  )

^{:refer jvm.namespace/list-interns :added "3.0"}
(fact "namespace list all interns task"

  (ns/list-interns '[jvm.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
  => map?)

^{:refer jvm.namespace/clear-interns :added "3.0"}
(comment "clears all interned vars in the namespace"

  (ns/clear-interns))

^{:refer jvm.namespace/clear-refers :added "3.0"}
(fact "clears all refers in a namespace")

^{:refer jvm.namespace/list-publics :added "3.0"}
(fact "namespace list all publics task"

  (ns/list-publics '[jvm.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
  => map?)

^{:refer jvm.namespace/list-refers :added "3.0"}
(fact "namespace list all refers task"

  (ns/list-refers '[jvm.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3149}
  => map?)

^{:refer jvm.namespace/clear :added "3.0"}
(comment "namespace clear all mappings and aliases task"

  (ns/clear #{*ns*})
  ;; { .... }
  => map?)

^{:refer jvm.namespace/list-in-memory :added "3.0"}
(fact "namespace list all objects in memory task"

  (ns/list-in-memory 'jvm.namespace)

  (ns/list-in-memory '[jvm.namespace] {:print {:result false :summary false}
                                      :return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :objects 306, :functions 22}
  => map?)

^{:refer jvm.namespace/loaded? :added "3.0"}
(fact "namespace check if namespace is loaded task"

  (ns/loaded? 'jvm.namespace) => true

  (ns/loaded? '[jvm.namespace])
  => map?)

^{:refer jvm.namespace/reset :added "3.0"}
(comment "deletes all namespaces under the root namespace" ^:hidden

  (ns/reset 'hara)

  (ns/reset '[jvm.namespace]))

^{:refer jvm.namespace/unmap :added "3.0"}
(comment "namespace unmap task"

  (ns/unmap :args 'something)

  (ns/unmap 'jvm.namespace :args '[something more]))

^{:refer jvm.namespace/unalias :added "3.0"}
(comment "namespace unalias task"

  (ns/unalias :args 'something)

  (ns/unalias 'jvm.namespace :args '[something more]))

(definvoke check
  "check for namespace task group"
  {:added "3.0"}
  [:pipe {:template :namespace
          :main {:fn (fn [input] (res/result {:status :return
                                              :data [:ok]}))}
          :params {:title "CHECK (task::namespace)"
                   :print {:item true
                           :result true
                           :summary true}}
          :item   {:output  :data}
          :result {:keys    nil
                   :output  :data
                   :columns [{:key    :id
                              :align  :left}
                             {:key    :data
                              :align  :left
                              :length 80
                              :color  #{:yellow}}]}
          :summary nil}])

(definvoke random-test
  "check for namespace task group"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "RANDOM TEST (task::namespace)"
                   :print {:item true
                           :result true
                           :summary true}}
          :main {:fn (fn [input]
                       (if (< 0.5 (rand))
                         (res/result {:status ((fn [] (rand-nth [:info :warn :error :critical])))
                                      :data   :message})
                         (res/result {:status ((fn [] (rand-nth [:return :highlight])))
                                      :data   (vec (range (rand-int 40)))})))}}])

^{:refer jvm.namespace/reload :added "3.0"}
(comment "reloads all listed namespace aliases"

  (ns/reload))

^{:refer jvm.namespace/reload-all :added "3.0"}
(comment "reloads all listed namespaces and dependents"

  (ns/reload-all))

^{:refer jvm.namespace/list-loaded :added "4.0"}
(fact "list all loaded namespaces")

(comment
  (code.manage/import {:write true})

  (random-test '[hara])
  (check '[hara]))
