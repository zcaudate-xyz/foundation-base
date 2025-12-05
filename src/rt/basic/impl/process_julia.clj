(ns rt.basic.impl.process-julia
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-basic :as basic]
            [std.lang.model.spec-julia :as julia]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json]))

(def +julia-init+
  (common/put-program-options
   :julia  {:default  {:oneshot     :julia
                       :basic       :julia}
            :env      {:julia     {:exec    "julia"
                                   :flags   {:oneshot   ["-e"]
                                             :basic     ["-e"]}}}}))

;;
;; EVAL
;;

(defn default-body-wrap
  "creates the scaffolding for the runtime eval to work"
  {:added "4.0"}
  [forms]
  (list 'do
        (list 'function (with-meta 'OUT-FN
                          {:inner true})
              []
              (list 'try
                    (concat
                     '(do)
                     (butlast forms)
                     [(list 'return (last forms))])
                    (list 'catch 'e
                          (list 'throw 'e))))
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
  (fn [body]
    (str "import Pkg; Pkg.add(\"JSON\"); using JSON;\n\n"
         "\n\n"
         (impl/emit-as
          :julia [(list 'println body)]))))

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

(def +client-basic-ast+
  '[(using Sockets)
    (using JSON)
    (using Base64)

    (defn main []
      (local client := (connect "localhost" "PORT_PLACEHOLDER"))
      (while true
        (local line := (readline client))
        (if (== line "<PING>")
          (do (println client "<PONG>"))
          (do (local input := (JSON.parse line))
              (local wrap_fn := (fn [f]
                                  (try
                                    (do (local v := (f))
                                        (JSON.print client {:id (get input "id")
                                                            :key (get input "key")
                                                            :type "data"
                                                            :value v})
                                        (println client))
                                    (catch Exception e
                                      (JSON.print client {:type "error"
                                                          :value (string e)})
                                      (println client)))))
              (local out := (include_string Main (get input "body")))
              (out wrap_fn)))))

    (main)])

(def +client-basic+
  (l/emit-as :julia +client-basic-ast+))

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (fn [port & [{:keys [host]}]]
    (str "import Pkg; Pkg.add(\"JSON\"); using JSON; using Sockets;\n\n"
         (str/replace +client-basic+
                      "\"PORT_PLACEHOLDER\"" (str port)))))

(def +julia-basic-config+
  (common/set-context-options
   [:julia :basic :default]
   {:bootstrap #'default-basic-client
    :main  {}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +julia-basic+
  [(rt/install-type!
    :julia :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
