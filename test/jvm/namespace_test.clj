(ns jvm.namespace-test
  (:require [jvm.namespace :as ns]
            [jvm.namespace.common :as common]
            [jvm.namespace.dependent :as dependent]
            [clojure.string]
            [std.lib.invoke :as invoke]
            [std.lib.result :as res]
            [std.task :as task])
  (:use code.test))

(defmacro with-temp-ns [[sym] & body]
  `(let [~sym (create-ns (gensym "jvm.namespace.test-"))]
     (try
       ~@body
       (finally
         (remove-ns (ns-name ~sym))))))

^{:refer jvm.namespace/list-aliases :added "3.0"}
(fact "namespace list all aliases task"

  (ns/list-aliases '[jvm.namespace])
  => map?)

^{:refer jvm.namespace/clear-aliases :added "3.0"}
(fact "removes all namespace aliases"
  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (alias 'str 'clojure.string))
    (ns/clear-aliases [(ns-name tmp)] {:return :summary})
    => map?))

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
(fact "clears all external imports"
  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (import java.io.File))
    (ns/clear-external-imports [(ns-name tmp)] {:return :summary})
    => map?))

^{:refer jvm.namespace/list-mappings :added "3.0"}
(fact "namespace list all mappings task"

  (ns/list-mappings '[jvm.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3674}
  => map?)

^{:refer jvm.namespace/clear-mappings :added "3.0"}
(fact "removes all mapped vars in the namespace"
  (with-temp-ns [tmp]
    (intern tmp 'join (fn [xs] (apply str xs)))
    (ns/clear-mappings [(ns-name tmp)] {:return :summary})
    => map?))

^{:refer jvm.namespace/list-interns :added "3.0"}
(fact "namespace list all interns task"

  (ns/list-interns '[jvm.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
  => map?)

^{:refer jvm.namespace/clear-interns :added "3.0"}
(fact "clears all interned vars in the namespace"
  (with-temp-ns [tmp]
    (intern tmp 'hello 1)
    (ns/clear-interns [(ns-name tmp)] {:return :summary})
    => map?))

^{:refer jvm.namespace/clear-refers :added "3.0"}
(fact "clears all refers in a namespace"
  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (clojure.core/refer 'clojure.string :only '[join]))
    (ns/clear-refers [(ns-name tmp)] {:return :summary})
    => map?))

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
(fact "namespace clear all mappings and aliases task"
  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (alias 'str 'clojure.string))
    (intern tmp 'hello 1)
    (ns/clear [(ns-name tmp)] {:return :summary})
    => map?))

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
(fact "deletes all namespaces under the root namespace"
  (let [sym (symbol "jvm.namespace.reset-temp")]
    (create-ns sym)
    (ns/reset [sym] {:return :summary})
    => map?))

^{:refer jvm.namespace/unmap :added "3.0"}
(fact "namespace unmap task"
  (with-temp-ns [tmp]
    (intern tmp 'something 1)
    (ns/unmap [(ns-name tmp)] :args '[something])
    => map?))

^{:refer jvm.namespace/unalias :added "3.0"}
(fact "namespace unalias task"
  (with-temp-ns [tmp]
    (binding [*ns* tmp]
      (alias 'something 'clojure.string))
    (ns/unalias [(ns-name tmp)] :args '[something])
    => map?))

(invoke/definvoke check
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

(invoke/definvoke random-test
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
(fact "reloads all listed namespace aliases"
  (with-redefs [ns/reload-task (fn [& _] :reloaded)
                dependent/sort-topo identity]
    (ns/reload '[jvm.namespace.dependent jvm.namespace.common]))
  => :reloaded)

^{:refer jvm.namespace/reload-all :added "3.0"}
(fact "reloads all listed namespaces and dependents"
  (with-redefs [common/ns-reload-all identity]
    (ns/reload-all '[jvm.namespace] {:return :summary}))
  => map?)

^{:refer jvm.namespace/list-loaded :added "4.0"}
(fact "list all loaded namespaces"
  (list (ns/list-loaded '[jvm.namespace]))
  => (contains ['jvm.namespace]))

(comment
  (code.manage/import {:write true})

  (random-test '[hara])
  (check '[hara]))


^{:refer jvm.namespace/reload-task :added "4.1"}
(fact "reload task delegates to namespace reload"
  (with-redefs [common/ns-reload identity]
    (ns/reload-task '[jvm.namespace] {:return :summary}))
  => map?)
