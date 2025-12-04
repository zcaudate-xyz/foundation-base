(ns rt.basic.impl.process-erlang
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-basic :as basic]
            [std.lang.model.spec-erlang :as spec]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
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

(def +client-basic+
  "
(fun() ->
    Loop = fun(Sock, Rec) ->
        case gen_tcp:recv(Sock, 0) of
            {ok, Packet} ->
                S = string:trim(Packet),
                if
                    S == \"<PING>\" ->
                        gen_tcp:send(Sock, \"<PONG>\\n\"),
                        Rec(Sock, Rec);
                    true ->
                        Resp = \"{\\\"status\\\": \\\"return\\\", \\\"data\\\": \\\"Erlang Exec Placeholder\\\"}\\n\",
                        gen_tcp:send(Sock, Resp),
                        Rec(Sock, Rec)
                end;
            {error, closed} ->
                ok
        end
    end,
    Start = fun(Port) ->
        {ok, Sock} = gen_tcp:connect(\"localhost\", Port, [{active, false}, {packet, line}]),
        Loop(Sock, Loop)
    end,
    Start(PORT)
end)().")

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (fn [port & [{:keys [host]}]]
    (str/replace +client-basic+ "PORT" (str port))))

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
