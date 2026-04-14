(ns rt.basic.impl-annex.process-erlang
  (:require [clojure.string]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.model-annex.spec-erlang :as spec]
            [xt.lang.base-repl :as k]))

(def +erlang-init+
  (common/put-program-options
   :erlang {:default  {:oneshot     :erlang
                       :basic       :erlang}
            :env      {:erlang    {:exec    "escript"
                                   :flags   {:oneshot   []
                                             :basic     []}}}}))

;;
;; EVAL
;;

(defn default-body-transform
  "standard erlang body transform - returns the last form as a single expression"
  {:added "4.0"}
  [input mopts]
  (rt/return-transform
   input mopts
   {:format-fn identity
    :wrap-fn last}))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (fn [body]
    (let [code (impl/emit-as :erlang body)]
      (str "main(_) -> io:format(\"~p~n\", [begin " code " end])."))))

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

(defn erlang-basic-client-forms
  "emits the Erlang basic client loop as Erlang source."
  {:added "4.1"}
  [host port]
  [(list 'erl-raw "#!/usr/bin/env escript")
   (list 'defn 'main '[_]
         (list 'erl-raw (str "Port = " port))
         (list 'erl-raw (str "{ok, Sock} = gen_tcp:connect(\""
                             host
                             "\", Port, [binary, {active, false}, {packet, line}])"))
         (list 'erl-raw "loop(Sock)"))
   (list 'defn 'loop '[sock]
         (list 'erl-raw "case gen_tcp:recv(Sock, 0) of {ok, Packet} -> S = string:trim(Packet), if S == <<\"<PING>\">> -> gen_tcp:send(Sock, \"<PONG>\\n\"), loop(Sock); true -> Code = json:decode(S), Out = eval_code(<<Code/binary, $.>>), gen_tcp:send(Sock, [Out, \"\\n\"]), loop(Sock) end; {error, closed} -> ok end"))
   (list 'defn 'eval-code '[s]
         (list 'erl-raw "try {ok, Tokens, _} = erl_scan:string(binary_to_list(S)), {ok, Exprs} = erl_parse:parse_exprs(Tokens), {value, Val, _} = erl_eval:exprs(Exprs, []), json:encode(#{<<\"type\">> => <<\"data\">>, <<\"value\">> => Val}) catch error:E -> json:encode(#{<<\"type\">> => <<\"error\">>, <<\"value\">> => list_to_binary(io_lib:format(\"~p\", [E]))}) end"))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (fn [port & [{:keys [host]}]]
    (let [erl-code (impl/emit-as :erlang
                                 (erlang-basic-client-forms
                                  (or host "127.0.0.1")
                                  port))]
      ;; Write the escript to a temp file and run it via sh -c
      ;; (escript does not support inline code as CLI argument; OTP 27 requires shebang for non-.erl files)
      (str "cat > /tmp/erl_client.escript <<'ESCRIPT_END'\n"
           erl-code
           "\nESCRIPT_END\nescript /tmp/erl_client.escript"))))

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
