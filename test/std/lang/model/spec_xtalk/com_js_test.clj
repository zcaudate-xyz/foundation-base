(ns std.lang.model.spec-xtalk.com-js-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.com-js :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-return-encode :added "4.0"}
(fact "encodes return value"
  (l/emit-as :js ['(fn [] (x:return-encode "hello" "id" "key"))])
  => #"JSON.stringify")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-return-wrap :added "4.0"}
(fact "wraps return value"
  (l/emit-as :js ['(fn [] (x:return-wrap (fn [] (return 1)) (fn [x] (return x))))])
  => #"try")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-return-eval :added "4.0"}
(fact "evals return value"
  (l/emit-as :js ['(fn [] (x:return-eval "1 + 1" (fn [x] (return x))))])
  => #"eval")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-socket-connect :added "4.0"}
(fact "connects socket"
  (l/emit-as :js [(js-tf-x-socket-connect '[_ "localhost" 8080 {} (fn [])])])
  => #"net.Socket")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-socket-send :added "4.0"}
(fact "sends socket"
  (l/emit-as :js [(js-tf-x-socket-send '[_ conn "hello"])])
  => #"conn.write")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-socket-close :added "4.0"}
(fact "closes socket"
  (l/emit-as :js [(js-tf-x-socket-close '[_ conn])])
  => #"conn.end")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-ws-connect :added "4.0"}
(fact "connects ws"
  (l/emit-as :js [(js-tf-x-ws-connect '[_ "localhost" 8080 {}])])
  => #"WebSocket")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-ws-send :added "4.0"}
(fact "sends ws"
  (l/emit-as :js [(js-tf-x-ws-send '[_ wb "hello"])])
  => #"wb.send")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-ws-close :added "4.0"}
(fact "closes ws"
  (l/emit-as :js [(js-tf-x-ws-close '[_ wb])])
  => #"wb.close")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-notify-socket :added "4.0"}
(fact "notifies socket"
  (l/emit-as :js [(js-tf-x-notify-socket '[_ "localhost" 8080 "val" "id" "key" connect-fn encode-fn])])
  => #"connect_fn")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-notify-http :added "4.0"}
(fact "notifies http"
  (l/emit-as :js [(js-tf-x-notify-http '[_ "localhost" 8080 "val" "id" "key" encode-fn])])
  => #"fetch")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-client-basic :added "4.0"}
(fact "basic client"
  (l/emit-as :js [(js-tf-x-client-basic '[_ "localhost" 8080 connect-fn eval-fn])])
  => #"createInterface")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-client-ws :added "4.0"}
(fact "ws client"
  (l/emit-as :js [(js-tf-x-client-ws '[_ "localhost" 8080 {} connect-fn eval-fn])])
  => #"onmessage")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-print :added "4.0"}
(fact "prints"
  (l/emit-as :js [(js-tf-x-print '[_ "hello"])])
  => #"console.log")

^{:refer std.lang.model.spec-xtalk.com-js/js-tf-x-shell :added "4.0"}
(fact "shell"
  (l/emit-as :js [(js-tf-x-shell '[_ "ls" cm])])
  => #"child_process")
