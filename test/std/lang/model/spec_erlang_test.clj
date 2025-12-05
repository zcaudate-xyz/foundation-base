(ns std.lang.model.spec-erlang-test
  (:use code.test)
  (:require [std.lang.model.spec-erlang :as erl]
            [std.lang.base.emit-common :as common]
            [std.lang :as l]
            [std.lib :as h]))

(fact "emits erlang defn"
  (l/emit-as :erlang ['(defn foo [a b] (+ a b))])
  => "foo(A, B) -> A + B.")

(fact "emits erlang case"
  (l/emit-as :erlang ['(case x 1 2 3 4)])
  => "case x of 1 -> 2; 3 -> 4 end")

(fact "emits erlang tuple"
  (l/emit-as :erlang ['(tuple 1 2 3)])
  => "{1, 2, 3}")

(fact "emits erlang var"
  (l/emit-as :erlang ['(var x 1)])
  => "X = 1")

(fact "emits erlang multi-clause function (manual)"
  (l/emit-as :erlang ['(defn bar [x] (case x 1 2 3 4))])
  => "bar(X) -> case X of 1 -> 2; 3 -> 4 end.")

(fact "end-to-end erlang execution"
  ^:unchecked
  (if (not-empty (try (h/sh "which" "erl" {:wrap false}) (catch Throwable _ nil)))
    (do
      (let [code "-module(test).
-export([main/0]).

main() ->
    io:format(\"~p~n\", [add(2, 3)]).

add(A, B) -> A + B."
            _ (spit "test.erl" code)
            _ (h/sh "erlc" "test.erl" {:wrap false})
            out (h/sh "erl" "-noshell" "-s" "test" "main" "-s" "init" "stop" {:wrap false})]
        (str out) => (contains "5")))
    "skipped"))
