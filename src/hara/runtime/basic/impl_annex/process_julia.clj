(ns hara.runtime.basic.impl-annex.process-julia
  (:require [clojure.string]
            [hara.runtime.basic.type-basic :as basic]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.runtime.basic.type-verify :as type-verify]
            [std.json :as json]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.model.annex.spec-julia :as spec]
            [xt.lang.common-lib :as lib]))

(def +julia-init+
  (common/put-program-options
   :julia  {:default  {:oneshot     :julia
                       :verify      :julia
                       :basic       :julia}
            :env      {:julia     {:exec    "julia"
                                   :pipe    true
                                   :flags   {:oneshot   ["-e"]
                                             :verify    ["-e" "try; Meta.parseall(String(read(stdin))); catch; exit(1); end;"]
                                             :basic     ["-e"]}}}}))

(def ^:private +julia-oneshot-prefix+
  "using JSON;\nusing Sockets;\nusing Printf;\nXT_GLOBALS = Dict()\n\n")

(def ^:private +julia-basic-prefix+
  "using JSON; using Sockets; using Printf;\nXT_GLOBALS = Dict()\n\n")

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
                   lib/return-eval
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

(def +julia-verify-config+
  (common/set-context-options
   [:julia :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-oneshot}))

(def +julia-oneshot+
  [(rt/install-type!
    :julia :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +julia-verify+
  [(rt/install-type!
    :julia :verify
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
                         lib/return-eval
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
   :container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-basic-julia:latest"}
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
