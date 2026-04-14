(ns rt.basic.impl.process-ruby
  (:require [clojure.string]
             [rt.basic.type-basic :as basic]
             [rt.basic.type-common :as common]
             [rt.basic.type-oneshot :as oneshot]
             [std.lang.base.impl :as impl]
             [std.lang.base.runtime :as rt]
             [std.lang.model-annex.spec-ruby :as spec]
             [std.lib.collection :as collection]
             [xt.lang.base-repl :as k]))

(def +ruby-init+
  (common/put-program-options
   :ruby  {:default  {:oneshot    :ruby
                      :basic      :ruby}
           :env      {:ruby    {:exec   "ruby"
                                :flags  {:oneshot   ["-e"]
                                         :basic     ["-e"]
                                         :interactive ["-e" "require 'irb'; IRB.start"]}}}}))

;;
;; ONESHOT
;;


(defn default-body-wrap
  "wraps body forms in a top-level `OUT` assignment so Ruby inline `defn`
   forms remain callable within the same eval scope."
  {:added "4.0"}
  [forms]
  (cons 'do
        (concat (butlast forms)
                [(list ':= 'OUT (last forms))])))

(defn normalize-forms
  "normalizes runtime input into a flat sequence of Ruby statements."
  {:added "4.1"}
  [input {:keys [bulk]}]
  (let [forms (if bulk input [input])]
    (if (and (= 1 (count forms))
             (collection/form? (first forms))
             (= 'do (ffirst forms)))
      (rest (first forms))
      forms)))

(defn mark-inline-defs
  "marks inline `defn` forms as inner so Ruby does not namespace-qualify
   helper definitions created within a runtime eval body."
  {:added "4.1"}
  [forms]
  (map (fn [form]
         (if (and (collection/form? form)
                  (= 'defn (first form))
                  (symbol? (second form)))
           (apply list 'defn
                  (with-meta (second form)
                    (assoc (meta (second form)) :inner true))
                  (drop 2 form))
           form))
       forms))

(defn default-body-transform
  "standard ruby transforms"
  {:added "4.0"}
  [input mopts]
  (-> (normalize-forms input mopts)
      (mark-inline-defs)
      (default-body-wrap)))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap  (impl/emit-entry-deps
                    k/return-eval
                    {:lang :ruby
                     :layout :flat})]
    (fn [body]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :ruby [(list 'puts (list 'return-eval body))])))))

(def +ruby-oneshot-config+
  (common/set-context-options
   [:ruby :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +ruby-oneshot+
  [(rt/install-type!
    :ruby :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +client-basic+
  '[(defn client-basic
      [host port opts]
      (:- "require 'socket'")
      (:- "require 'json'")
      (let [conn (TCPSocket.new host port)]
         (while true
            (let [line   (. conn (gets))
                  input  (JSON.parse line)
                  out    (return-eval input)]
              (. conn (puts out))))))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (->> [(impl/emit-entry-deps
                         k/return-eval
                         {:lang :ruby
                          :layout :flat})
                        (impl/emit-as
                         :ruby +client-basic+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :ruby [(list 'client-basic
                       (or host "127.0.0.1")
                       port
                       {})])))))

(def +default-basic-config+
  {:bootstrap #'default-basic-client
   :main   {}
   :emit   {:body  {:transform #'default-body-transform}}
   :json :full
   :encode :json ;; default
   :timeout 2000})

(def +ruby-basic-config+
  (common/set-context-options
   [:ruby :basic :default]
   +default-basic-config+))

(def +ruby-basic+
  [(rt/install-type!
    :ruby :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])

(comment
  )
