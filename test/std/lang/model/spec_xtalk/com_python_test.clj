(ns std.lang.model.spec-xtalk.com-python-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.com-python :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-return-encode :added "4.0"}
(fact "encodes return value"
  (l/emit-as :python [(python-tf-x-return-encode '[_ "hello" "id" "key"])])
  => #"json.dumps")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-return-wrap :added "4.0"}
(fact "wraps return value"
  (l/emit-as :python [(python-tf-x-return-wrap '[_ (fn [] (return 1)) (fn [x] (return x))])])
  => #"try")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-return-eval :added "4.0"}
(fact "evals return value"
  (l/emit-as :python [(python-tf-x-return-eval '[_ "1 + 1" (fn [x] (return x))])])
  => #"exec")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-socket-connect :added "4.0"}
(fact "connects socket"
  (l/emit-as :python [(python-tf-x-socket-connect '[_ "localhost" 8080 {}])])
  => #"socket.socket")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-socket-send :added "4.0"}
(fact "sends socket"
  (l/emit-as :python [(python-tf-x-socket-send '[_ conn "hello"])])
  => #"sendall")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-socket-close :added "4.0"}
(fact "closes socket"
  (l/emit-as :python [(python-tf-x-socket-close '[_ conn])])
  => #"close")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-ws-connect :added "4.0"}
(fact "connects ws"
  (l/emit-as :python [(python-tf-x-ws-connect '[_ "localhost" 8080 {}])])
  => #"websocket.WebSocketApp")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-ws-send :added "4.0"}
(fact "sends ws"
  (l/emit-as :python [(python-tf-x-ws-send '[_ wb "hello"])])
  => #"send")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-ws-close :added "4.0"}
(fact "closes ws"
  (l/emit-as :python [(python-tf-x-ws-close '[_ wb])])
  => #"close")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-client-basic :added "4.0"}
(fact "basic client"
  (l/emit-as :python [(python-tf-x-client-basic '[_ "localhost" 8080 connect-fn eval-fn])])
  => #"recv")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-client-ws :added "4.0"}
(fact "ws client"
  (l/emit-as :python [(python-tf-x-client-ws '[_ "localhost" 8080 {} connect-fn eval-fn])])
  => #"threading")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-print :added "4.0"}
(fact "prints"
  (l/emit-as :python [(python-tf-x-print '[_ "hello"])])
  => #"print")

^{:refer std.lang.model.spec-xtalk.com-python/python-tf-x-shell :added "4.0"}
(fact "shell"
  (l/emit-as :python [(python-tf-x-shell '[_ "ls" cm])])
  => #"os")
