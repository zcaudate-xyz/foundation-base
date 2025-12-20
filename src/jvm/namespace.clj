(ns jvm.namespace
  (:require [std.pipe :as pipe]
            [jvm.namespace.common :as common]
            [jvm.namespace.eval :as eval]
            [jvm.namespace.context :as context]
            [jvm.namespace.dependent :as dependent]
            [std.lib :as h :refer [definvoke]]))

(h/intern-in eval/eval-ns
             eval/with-ns
             eval/eval-temp-ns
             context/resolve-ns
             context/ns-vars
             context/ns-context
             context/reeval)

(def namespace-template
  {:construct {:input    (fn [_] (.getName *ns*))
               :lookup   (fn [_ _] {})
               :env      (fn [_] {})}
   :arglists '([] [ns])
   :main      {:argcount 1}
   :params    {:print {:function false
                       :item false
                       :result false
                       :summary false}}
   :warning   {:output  :data}
   :error     {:output  :data}
   :item      {:list    (fn [_ _]   (sort (common/ns-list)))
               :pre     (fn [input] (.getName (the-ns input)))
               :post    identity
               :output  identity
               :display identity}
   :result    {:keys    {:count count}
               :ignore  empty?
               :output  identity
               :columns [{:key    :key
                          :align  :left}
                         {:key    :count
                          :length 7
                          :format "(%s)"
                          :align  :center
                          :color  #{:bold}}
                         {:key    :data
                          :align  :left
                          :length 60
                          :color  #{:yellow}}
                         {:key    :time
                          :align  :left
                          :length 10
                          :color  #{:bold}}]}
   :summary   {:aggregate {:total [:count + 0]}}})

(defmethod pipe/pipe-defaults :namespace
  ([_]
   namespace-template))

(defmethod pipe/pipe-defaults :namespace.memory
  ([_]
   (-> namespace-template
       (update-in [:item] merge {:output  common/group-in-memory
                                 :display common/group-in-memory})
       (assoc :params    {:print {:function false
                                  :item     false
                                  :result   true
                                  :summary  true}}
              :result {:keys    {:count     count
                                 :functions common/group-in-memory}
                       :output  common/group-in-memory
                       :columns [{:key    :key
                                  :align  :left}
                                 {:key    :count
                                  :length 7
                                  :format "(%s)"
                                  :align  :right
                                  :color  #{:bold}}
                                 {:key    :functions
                                  :align  :left
                                  :length 80
                                  :color  #{:yellow}}]}
              :summary   {:aggregate {:objects [:count + 0]
                                      :functions [:functions #(+ %1 (count %2)) 0]}}))))

(defmethod pipe/pipe-defaults :namespace.count
  ([_]
   (-> namespace-template
       (assoc :result  {:output  identity
                        :columns [{:key    :key
                                   :align  :left}
                                  {:key    :data
                                   :align  :left
                                   :length 80
                                   :color  #{:yellow}}]}
              :summary   {:aggregate {:total [:data (fn [v _] (inc v)) 0]}}))))

(def return-keys (comp vec sort keys))

(definvoke list-aliases
  "namespace list all aliases task
 
   (ns/list-aliases '[jvm.namespace])"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "NAMESPACE ALIASES"
                   :parallel true}
          :main   {:fn clojure.core/ns-aliases}
          :item   {:post return-keys}}])

(definvoke clear-aliases
  "removes all namespace aliases"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "CLEAR NAMESPACE ALIASES"
                   :parallel true}
          :main {:fn common/ns-clear-aliases}}])

(definvoke list-imports
  "namespace list all imports task
 
   (ns/list-imports '[jvm.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 482}
   => map?"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "NAMESPACE IMPORTS"}
          :main {:fn clojure.core/ns-imports}
          :item {:post return-keys}}])

(definvoke list-external-imports
  "lists all external imports"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "NAMESPACE IMPORTS"
                   :parallel true}
          :main {:fn common/ns-list-external-imports}
          :item {:post return-keys}}])

(definvoke clear-external-imports
  "clears all external imports"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "CLEAR NAMESPACE ALIASES"
                   :parallel true}
          :main {:fn common/ns-clear-external-imports}}])

(definvoke list-mappings
  "namespace list all mappings task
 
   (ns/list-mappings '[jvm.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3674}
   => map?"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "NAMESPACE MAPPINGS"
                   :parallel true}
          :main {:fn clojure.core/ns-map}
          :item {:post return-keys}}])

(definvoke clear-mappings
  "removes all mapped vars in the namespace"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "CLEAR NAMESPACE MAPPINGS"
                   :parallel true}
          :main {:fn common/ns-clear-mappings}}])

(definvoke list-interns
  "namespace list all interns task
 
   (ns/list-interns '[jvm.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
   => map?"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "NAMESPACE INTERNS"
                   :parallel true}
          :main {:fn clojure.core/ns-interns}
          :item {:post return-keys}}])

(definvoke list-refers
  "namespace list all refers task
 
   (ns/list-refers '[jvm.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3149}
   => map?"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "NAMESPACE REFERS"
                   :parallel true}
          :main {:fn clojure.core/ns-refers}
          :item {:post return-keys}}])

(definvoke clear-interns
  "clears all interned vars in the namespace
 
   (ns/clear-interns)"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "CLEAR NAMESPACE INTERNS"
                   :parallel true}
          :main {:fn common/ns-clear-interns}}])

(definvoke clear-refers
  "clears all refers in a namespace"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "CLEAR NAMESPACE refers"
                   :parallel true}
          :main {:fn common/ns-clear-refers}}])

(definvoke list-publics
  "namespace list all publics task
 
   (ns/list-publics '[jvm.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
   => map?"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "NAMESPACE PUBLICS"
                   :parallel true}
          :main {:fn clojure.core/ns-publics}
          :item {:post return-keys}}])

(definvoke clear
  "namespace clear all mappings and aliases task
 
   (ns/clear #{*ns*})
   ;; { .... }
   => map?"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "CLEAR NAMESPACE ALIASES AND MAPPINGS"
                   :parallel true}
          :main {:fn common/ns-clear}}])

(definvoke list-in-memory
  "namespace list all objects in memory task
 
   (ns/list-in-memory 'jvm.namespace)
 
   (ns/list-in-memory '[jvm.namespace] {:print {:result false :summary false}
                                       :return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :objects 306, :functions 22}
   => map?"
  {:added "3.0"}
  [:pipe {:template :namespace.memory
          :params {:title "NAMESPACE MEMORY OBJECTS"
                   :parallel true}
          :main   {:fn common/raw-in-memory}}])

(definvoke loaded?
  "namespace check if namespace is loaded task
 
   (ns/loaded? 'jvm.namespace) => true
 
   (ns/loaded? '[jvm.namespace])
   => map?"
  {:added "3.0"}
  [:pipe {:template :namespace.count
          :params {:title "NAMESPACE LOADED?"
                   :parallel true}
          :main {:fn common/ns-loaded?}}])

(definvoke reset
  "deletes all namespaces under the root namespace"
  {:added "3.0"}
  [:pipe {:template :namespace.memory
          :params {:title "RESET NAMESPACE"
                   :parallel true
                   :print {:item true
                           :summary true}}
          :main {:fn common/ns-delete}}])

(definvoke unmap
  "namespace unmap task
 
   (ns/unmap :args 'something)
 
   (ns/unmap 'jvm.namespace :args '[something more])"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "UNMAP NAMESPACE SYMBOL"
                   :parallel true}
          :arglists '([:args syms] [<ns> :args syms])
          :main {:fn common/ns-unmap}}])

(definvoke unalias
  "namespace unalias task
 
   (ns/unalias :args 'something)
 
   (ns/unalias 'jvm.namespace :args '[something more])"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "UNALIAS NAMESPACE SYMBOL"
                   :parallel true
                   :print {:result false
                           :summary false}}
          :arglists '([:args syms] [<ns> :args syms])
          :main {:fn common/ns-unalias}}])

(definvoke reload-task
  "reloads all listed namespace aliases"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "RELOADS NAMESPACE"
                   :parallel false}
          :main {:fn common/ns-reload}}])

(defn reload
  "reloads all listed namespace aliases
 
   (ns/reload)"
  {:added "3.0"}
  ([]
   (reload-task))
  ([inputs]
   (reload-task (dependent/sort-topo inputs))))

(definvoke reload-all
  "reloads all listed namespaces and dependents
 
   (ns/reload-all)"
  {:added "3.0"}
  [:pipe {:template :namespace
          :params {:title "RELOADS NAMESPACE AND DEPENDENCIES"
                   :parallel true}
          :main {:fn common/ns-reload-all}}])

(defn list-loaded
  "list all loaded namespaces"
  {:added "4.0"}
  ([]
   (list-loaded :all))
  ([inputs]
   (let [nss (common/ns-list)]
     (if (= :all inputs)
       nss
       (filter (fn [ns]
                 (some (fn [sym] (.startsWith (str ns)
                                              (str sym)))
                       inputs))
               nss)))))

(comment
  (loaded? '[jvm.namespace])
  (unmap '[jvm.namespace] :args '[ns])
  (list-aliases '[jvm.namespace])
  (list-aliases 'jvm.namespace)
  (list-in-memory '[jvm.namespace])
  (reset '[jvm.namespace-test])
  (list-aliases ['hara] {:print {:item true}})
  (random-test '[hara])
  (check '[*ns*])
  (code.manage/scaffold))
