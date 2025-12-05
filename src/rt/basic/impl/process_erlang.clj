(ns rt.basic.impl.process-erlang
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-basic :as basic]
            [std.lang.model.spec-erlang :as spec]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(def +erlang-init+
  (common/put-program-options
   :erlang {:default  {:oneshot     :erlang
                       :basic       :erlang}
            :env      {:erlang    {:exec    "erl"
                                   :flags   {:oneshot   ["-noshell" "-eval"]
                                             :basic     ["-noshell" "-eval"]}}}}))

;;
;; EVAL
;;

(defn default-body-wrap
  "creates the scaffolding for the runtime eval to work"
  {:added "4.0"}
  [forms]
  forms)

(defn default-body-transform
  "standard erlang transforms"
  {:added "4.0"}
  [input mopts]
  (rt/return-transform
   input mopts
   {:format-fn identity
    :wrap-fn default-body-wrap}))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (fn [body]
    (let [code (impl/emit-as :erlang body)]
      (str "io:format(\"~p~n\", [begin " code " end]), init:stop()."))))

(def +erlang-oneshot-config+
  (common/set-context-options
   [:erlang :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +erlang-oneshot+
  [(rt/install-type!
    :erlang :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +client-basic-ast+
  '[
    (erl-def main [_]
      (erl-assign Port (erl-raw "PORT_PLACEHOLDER"))
      (erl-assign (erl-tuple ok Sock) (gen_tcp/connect "localhost" Port [(erl-tuple 'active false) (erl-tuple 'packet 'line) (erl-tuple 'binary true)]))
      (loop Sock))

    (erl-def loop [Sock]
      (erl-case (gen_tcp/recv Sock 0)
        (erl-tuple ok Packet)
        (do (erl-assign S (string/trim Packet))
            (erl-case (eq S (erl-raw "<<\"<PING>\">>"))
               true
               (do (gen_tcp/send Sock "<PONG>\n")
                   (loop Sock))

               false
               (do (erl-assign Input (json/decode S))
                   (erl-assign WrapFn (fn [F]
                            (try
                              (do (erl-assign V (F))
                                  (json/encode {(erl-raw "<<\"id\">>") (maps/get (erl-raw "<<\"id\">>") Input)
                                                (erl-raw "<<\"key\">>") (maps/get (erl-raw "<<\"key\">>") Input)
                                                (erl-raw "<<\"type\">>") (erl-raw "<<\"data\">>")
                                                (erl-raw "<<\"value\">>") V}))
                              (catch :error E
                                (json/encode {(erl-raw "<<\"type\">>") (erl-raw "<<\"error\">>")
                                              (erl-raw "<<\"value\">>") (list_to_binary (io_lib/format "~p" [E]))})))))
                   (erl-assign Out (eval_helper (maps/get (erl-raw "<<\"body\">>") Input) WrapFn))
                   (gen_tcp/send Sock [Out "\n"])
                   (loop Sock))))

        (erl-tuple error closed)
        ok))

    (erl-def eval_helper [S WrapFn]
      (erl-assign (erl-tuple ok Tokens _) (erl_scan/string (binary_to_list S)))
      (erl-assign (erl-tuple ok Exprs) (erl_parse/parse_exprs Tokens))
      (erl-assign Bindings [(erl-tuple 'EvalHelper (erl-fun eval_helper 2))])
      (erl-assign (erl-tuple value Val _) (erl_eval/exprs Exprs Bindings))
      (WrapFn (fn [] Val)))
    ])

(def +client-basic+
  (l/emit-as :erlang +client-basic-ast+))

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (fn [port & [{:keys [host]}]]
    (str "main(_) -> " (str/replace +client-basic+
                                    "PORT_PLACEHOLDER" (str port)) ", init:stop().")))

(def +erlang-basic-config+
  (common/set-context-options
   [:erlang :basic :default]
   {:bootstrap #'default-basic-client
    :main  {}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +erlang-basic+
  [(rt/install-type!
    :erlang :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
