(ns js.lib.fastify-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.fastify :as http]
             [js.core :as j]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)]
  :teardown [(l/rt:stop)]})


^{:refer js.lib.fastify/current-servers :added "4.0" :unchecked true}
(fact "gets the current servers"
  ^:hidden
  
  (http/current-servers)
  => map?)

^{:refer js.lib.fastify/wrap-handler :added "4.0" :unchecked true}
(fact "wraps the request into a map"
  ^:hidden
  
  (!.js
    ((http/wrap-handler k/identity)
     {"raw"{"rawHeaders" ["Connection" "Upgrade, HTTP2-Settings",
                          "Content-Length" "0",
                          "HTTP2-Settings" "AAEAAEAAAAIAAAABAAMAAABkAAQBAAAAAAUAAEAA",
                          "Host" "127.0.0.1:3000",
                          "Upgrade" "h2c",
                          "User-Agent" "Java-http-client/11.0.15"]
            "method" "POST",
            "url" "/euoeu/oue?a=2"}
      "params" {"*" "/euoeu/oue"}
      "query" {"a" "2"}}))
  => {"url" "/euoeu/oue?a=2",
      "method" "POST",
      "query" {"a" "2"},
      "path" "/euoeu/oue",
      "headers"
      {"HTTP2-Settings" "AAEAAEAAAAIAAAABAAMAAABkAAQBAAAAAAUAAEAA",
       "User-Agent" "Java-http-client/11.0.15",
       "Content-Length" "0",
       "Connection" "Upgrade, HTTP2-Settings",
       "Upgrade" "h2c",
       "Host" "127.0.0.1:3000"}})

^{:refer js.lib.fastify/start-server :added "4.0" :unchecked true
  :setup [(def +port+
            (h/port:check-available 0))
          (http/stop-server +port+)]
  :teardown [(http/stop-server +port+)]}
(fact "starts a fastify server"
  ^:hidden
  
  (def -server-
    (j/<! (http/start-server (@! +port+) k/identity)))
  
  -server-
  => map?

  (std.json/read (str (h/sh {:args ["curl" (str "http://localhost:" +port+ "/euoeu/oue?a=2")]})))
  => (contains-in
      {"url" "/euoeu/oue?a=2", "method" "GET", "query" {"a" "2"},
       "path" "/euoeu/oue", "headers" {"Accept" "*/*",
                                       "Host" string?}}))

^{:refer js.lib.fastify/stop-server :added "4.0" :unchecked true}
(fact "stops a fastify server")
