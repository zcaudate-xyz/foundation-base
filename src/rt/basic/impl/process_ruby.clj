(ns rt.basic.impl.process-ruby
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-basic :as basic]
            [xt.lang.base-repl :as k]
            [std.lang.model.spec-ruby :as spec]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lib :as h]
            [std.string :as str]))

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


(defn return-wrap-invoke
  "wraps forms to be invoked"
  {:added "4.0"}
  [forms]
  (h/$ (. (fn [] ~@forms) (call))))

(defn default-body-transform
  "standard python transforms"
  {:added "4.0"}
  [input mopts]
  (rt/return-transform
   input mopts
   {:wrap-fn return-wrap-invoke}))

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
                       (str/join "\n\n"))]
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
   :emit   {:body  {:transform #'default-body-transform}
            :lang/format :global}
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
