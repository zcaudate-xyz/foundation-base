(ns indigo.server-test
  (:require [indigo.server :refer :all]
            [indigo.server.watcher :as watcher]
            [org.httpkit.server :as http])
  (:use code.test)
  (:import (java.awt Desktop)))

^{:refer indigo.server/start-vite! :added "4.1"}
(fact "start-vite! is not currently defined"
  (nil? (resolve 'indigo.server/start-vite!))
  => true)

^{:refer indigo.server/stop-vite! :added "4.1"}
(fact "stop-vite! is not currently defined"
  (nil? (resolve 'indigo.server/stop-vite!))
  => true)

^{:refer indigo.server/proxy-vite :added "4.1"}
(fact "proxy-vite is not currently defined"
  (nil? (resolve 'indigo.server/proxy-vite))
  => true)

^{:refer indigo.server/wrap-browser-call :added "4.1"}
(fact "wrap-browser-call transforms a handler into a JSON ring response"
  (let [handler (wrap-browser-call (fn [req] (:params req)))
        response (handler {:body "{\"hello\":\"world\"}"})]
    (:status response) => 200
    (get-in response [:headers "Content-Type"]) => "application/json"
    (string? (:body response)) => true))

^{:refer indigo.server/create-routes :added "4.1"}
(fact "create-routes prefixes route keys"
  (create-routes "GET " {"/foo" :foo "/bar" :bar})
  => {"GET /foo" :foo "GET /bar" :bar})

^{:refer indigo.server/repl-handler :added "4.1"}
(fact "repl-handler is a function"
  (fn? repl-handler) => true)

^{:refer indigo.server/dev-handler :added "4.1"}
(fact "dev-handler is a function returning a ring response"
  (fn? dev-handler) => true
  (:status (dev-handler {:uri "/not-found" :request-method :get}))
  => 404)

^{:refer indigo.server/server-stop :added "4.1"}
(fact "server-stop returns nil and clears *instance*"
  (let [orig @*instance*]
    (try
      (reset! *instance* (fn [& _] nil))
      (server-stop) => nil
      @*instance* => nil
      (finally
        (reset! *instance* orig)))))

^{:refer indigo.server/server-start :added "4.1"}
(fact "server-start returns a stop function and sets *instance*"
  (let [orig @*instance*]
    (try
      (reset! *instance* nil)
      (with-redefs [http/run-server (fn [& _] (fn [& _] nil))
                    watcher/start-watcher (fn [] nil)
                    watcher/stop-watcher (fn [] nil)]
        (fn? (server-start)) => true
        (fn? @*instance*) => true)
      (finally
        (reset! *instance* orig)))))

^{:refer indigo.server/server-toggle :added "4.1"}
(fact "server-toggle starts when stopped and stops when started"
  (let [orig @*instance*]
    (try
      (reset! *instance* nil)
      (with-redefs [http/run-server (fn [& _] (fn [& _] nil))
                    watcher/start-watcher (fn [] nil)
                    watcher/stop-watcher (fn [] nil)]
        (fn? (server-toggle)) => true
        (fn? @*instance*) => true
        (nil? (server-toggle)) => true
        (nil? @*instance*) => true)
      (finally
        (reset! *instance* orig)))))

^{:refer indigo.server/server-restart :added "4.1"}
(fact "server-restart stops any running server then starts a new one"
  (let [orig @*instance*]
    (try
      (reset! *instance* (fn [& _] nil))
      (with-redefs [http/run-server (fn [& _] (fn [& _] nil))
                    watcher/start-watcher (fn [] nil)
                    watcher/stop-watcher (fn [] nil)]
        (fn? (server-restart)) => true
        (fn? @*instance*) => true)
      (finally
        (reset! *instance* orig)))))

^{:refer indigo.server/open-client :added "4.1"}
(fact "open-client is a function"
  (fn? open-client) => true
  (when (Desktop/isDesktopSupported)
    (open-client) => nil))
