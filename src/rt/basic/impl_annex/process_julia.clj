(ns rt.basic.impl-annex.process-julia
  (:require [clojure.string]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.json :as json]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.model-annex.spec-julia :as spec]
            [xt.lang.base-repl :as k]))

(def +julia-init+
  (common/put-program-options
   :julia  {:default  {:oneshot     :julia
                       :basic       :julia}
            :env      {:julia     {:exec    "julia"
                                    :flags   {:oneshot   ["-e"]
                                              :basic     ["-e"]}}}}))

(def ^:private +julia-oneshot-prefix+
  "using JSON;\n\n")

(def ^:private +julia-basic-prefix+
  "using JSON; using Sockets;\n\n")

;;
;; EVAL
;;

(defn default-body-wrap
  "creates the scaffolding for the runtime eval to work"
  {:added "4.0"}
  [forms]
  (list 'do
        (apply list 'defn (with-meta 'OUT-FN
                            {:inner true})
               []
               (concat (butlast forms)
                       [(list 'return (last forms))]))
        (list ':= 'OUT (list 'OUT-FN))))

(defn default-body-transform
  "standard julia transforms"
  {:added "4.0"}
  [input mopts]
  (rt/return-transform
   input mopts
   {:format-fn identity
    :wrap-fn default-body-wrap}))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap (impl/emit-entry-deps
                   k/return-eval
                   {:lang :julia
                    :layout :flat})]
    (fn [body]
      (str +julia-oneshot-prefix+
            bootstrap
            "\n\n"
            (impl/emit-as
             :julia [(list 'println (list 'return-eval body))])))))

(def +julia-oneshot-config+
  (common/set-context-options
   [:julia :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +julia-oneshot+
  [(rt/install-type!
    :julia :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +client-basic+
  '[(defn client-basic
      [host port opts]
      (let [conn   (connect host port)]
        (while true
          (let [line  (readline conn)]
            (cond (== line "<PING>")
                  (do (write conn "<PONG>\n")
                      (flush conn))

                  :else
                  (let [input (x:json-decode line)
                        out   (return-eval input)]
                    (write conn (string out "\n"))
                    (flush conn)))))))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (->> [(impl/emit-entry-deps
                         k/return-eval
                         {:lang :julia
                          :layout :flat})
                        (impl/emit-as
                         :julia +client-basic+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str +julia-basic-prefix+
            bootstrap
            "\n\n"
            (impl/emit-as
             :julia [(list 'client-basic
                           (or host "127.0.0.1")
                           port
                           {})])))))

(def +default-julia-basic-config+
  {:bootstrap #'default-basic-client
   :main  {}
   :container {:image "foundation-base/rt-basic-julia:latest"}
   :container-backup true
   :emit  {:body  {:transform #'default-body-transform}}
   :json :full
   :encode :json
   :timeout 2000})

(def +julia-basic-config+
  (common/set-context-options
   [:julia :basic :default]
   +default-julia-basic-config+))

(def +julia-basic+
  [(rt/install-type!
    :julia :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
