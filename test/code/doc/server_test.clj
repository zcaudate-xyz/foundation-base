(ns code.doc.server-test
  (:use code.test)
  (:require [code.doc.server :refer :all])
  (:import (com.sun.net.httpserver HttpServer)))

^{:refer code.doc.server/stop! :added "4.1"}
(fact "stops the running server and clears the atom"
  (with-redefs [*server* (atom nil)
                println (fn [& _])]
    (start! 0)
    (stop!)
    @*server*)
  => nil)

^{:refer code.doc.server/start! :added "4.1"}
(fact "starts the server on an ephemeral port and returns a running HttpServer"
  (with-redefs [*server* (atom nil)
                println (fn [& _])]
    (let [^HttpServer server (start! 0)]
      (try
        [(some? server)
         (= server @*server*)
         (number? (-> server .getAddress .getPort))]
        (finally
          (.stop server 0)))))
  => [true true true])

^{:refer code.doc.server/-main :added "4.1"}
(fact "parses port from args and starts the server, defaulting to 8080"
  (let [started (atom nil)]
    (with-redefs [start! (fn [port] (reset! started port))
                  promise (fn [] (reify clojure.lang.IDeref (deref [_] nil)))]
      (-main "1234")
      @started))
  => 1234

  (let [started (atom nil)]
    (with-redefs [start! (fn [port] (reset! started port))
                  promise (fn [] (reify clojure.lang.IDeref (deref [_] nil)))]
      (-main)
      @started))
  => 8080)
