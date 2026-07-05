(ns net.smoke-test
  (:use code.test)
  (:require [net.http]
            [net.http.api]
            [net.http.client]
            [net.http.common]
            [net.http.router]
            [net.http.websocket]
            [net.openapi.call]
            [net.openapi.read]
            [net.resp.connection]
            [net.resp.node]
            [net.resp.pool]
            [net.resp.wire]
            [xt.net.http-fetch]
            [xt.net.http-util]
            [xt.net.ws-native]
            [xt.net.ws-phoenix]
            [xt.net.conn-redis]
            [xt.net.conn-sql]
            [xt.net.addon-supabase]
            [js.net.http-fetch]
            [js.net.conn-redis]
            [js.net.conn-postgres]
            [js.net.conn-sqlite]
            [js.net.ws-native]
            [python.net.http-fetch]
            [python.net.conn-redis]
            [python.net.conn-postgres]
            [python.net.conn-sqlite]
            [dart.net.http-fetch]
            [dart.net.conn-postgres]
            [dart.net.conn-sqlite]))

^{:refer net.smoke-test/all-net-namespaces-load :added "4.1"}
(fact "all clojure/jvm net namespaces load without error"
  (mapv find-ns '[net.http
                  net.http.api
                  net.http.client
                  net.http.common
                  net.http.router
                  net.http.websocket
                  net.openapi.call
                  net.openapi.read
                  net.resp.connection
                  net.resp.node
                  net.resp.pool
                  net.resp.wire])
  => (partial every? #(instance? clojure.lang.Namespace %)))

^{:refer net.smoke-test/all-xt-net-namespaces-load :added "4.1"}
(fact "all xtalk net namespaces load without error"
  (mapv find-ns '[xt.net.http-fetch
                  xt.net.http-util
                  xt.net.ws-native
                  xt.net.ws-phoenix
                  xt.net.conn-redis
                  xt.net.conn-sql
                  xt.net.addon-supabase])
  => (partial every? #(instance? clojure.lang.Namespace %)))

^{:refer net.smoke-test/all-js-net-namespaces-load :added "4.1"}
(fact "all js net namespaces load without error"
  (mapv find-ns '[js.net.http-fetch
                  js.net.conn-redis
                  js.net.conn-postgres
                  js.net.conn-sqlite
                  js.net.ws-native])
  => (partial every? #(instance? clojure.lang.Namespace %)))

^{:refer net.smoke-test/all-python-net-namespaces-load :added "4.1"}
(fact "all python net namespaces load without error"
  (mapv find-ns '[python.net.http-fetch
                  python.net.conn-redis
                  python.net.conn-postgres
                  python.net.conn-sqlite])
  => (partial every? #(instance? clojure.lang.Namespace %)))

^{:refer net.smoke-test/all-dart-net-namespaces-load :added "4.1"}
(fact "all dart net namespaces load without error"
  (mapv find-ns '[dart.net.http-fetch
                  dart.net.conn-postgres
                  dart.net.conn-sqlite])
  => (partial every? #(instance? clojure.lang.Namespace %)))
