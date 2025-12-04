(ns rt.basic.impl.process-erlang
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-basic :as basic]
            [xt.lang.base-repl :as k]
            [std.lang.model.spec-erlang :as spec]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lib :as h]
            [std.string :as str]))

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

(def +client-basic-baked+
  "
main(_) ->
    Port = PORT_PLACEHOLDER,
    {ok, Sock} = gen_tcp:connect(\"localhost\", Port, [{active, false}, {packet, line}, {binary, true}]),
    loop(Sock).

loop(Sock) ->
    case gen_tcp:recv(Sock, 0) of
        {ok, Packet} ->
            S = string:trim(Packet),
            if
                S == <<\"<PING>\">> ->
                    gen_tcp:send(Sock, \"<PONG>\\n\"),
                    loop(Sock);
                true ->
                    Input = json:decode(S),
                    WrapFn = fun(F) ->
                        try
                            V = F(),
                            json:encode(#{
                                <<\"id\">> => maps:get(<<\"id\">>, Input),
                                <<\"key\">> => maps:get(<<\"key\">>, Input),
                                <<\"type\">> => <<\"data\">>,
                                <<\"value\">> => V
                            })
                        catch error:E ->
                            json:encode(#{
                                <<\"type\">> => <<\"error\">>,
                                <<\"value\">> => list_to_binary(io_lib:format(\"~p\", [E]))
                            })
                        end
                    end,
                    Out = eval_helper(maps:get(<<\"body\">>, Input), WrapFn),
                    gen_tcp:send(Sock, [Out, \"\\n\"]),
                    loop(Sock)
            end;
        {error, closed} ->
            ok
    end.

eval_helper(S, WrapFn) ->
    {ok, Tokens, _} = erl_scan:string(binary_to_list(S)),
    {ok, Exprs} = erl_parse:parse_exprs(Tokens),
    Bindings = [{'EvalHelper', fun eval_helper/2}],
    {value, Val, _} = erl_eval:exprs(Exprs, Bindings),
    WrapFn(fun() -> Val end).
")

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (fn [port & [{:keys [host]}]]
    (str/replace +client-basic-baked+ "PORT_PLACEHOLDER" (str port))))

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
