(ns lua.nginx.http-fetch-test
  (:require [lua.nginx.http-fetch]
            [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(fact:global
 {:skip (not (env/program-exists? "resty"))})

^{:refer lua.nginx.http-fetch/create :added "4.1"}
(fact "creates an http fetch client"

  (let [out (l/emit-as :lua.nginx ['(lua.nginx.http-fetch/create {:timeout 5000} [])])]
    (boolean (re-find #"lua\.nginx\.http_fetch\.create\(\{timeout=5000\}," out)))
  => true)

^{:refer lua.nginx.http-fetch/request-http-raw :added "4.1"}
(fact "prepares a raw http request"

  (let [out (l/emit-as :lua.nginx ['(lua.nginx.http-fetch/request-http-raw client input)])]
    (boolean (re-find #"lua\.nginx\.http_fetch\.request_http_raw\(client,\s*input\)" out)))
  => true)

^{:refer lua.nginx.http-fetch/request-http :added "4.1"}
(fact "prepares an http request through middleware"

  (let [out (l/emit-as :lua.nginx ['(lua.nginx.http-fetch/request-http client input)])]
    (boolean (re-find #"lua\.nginx\.http_fetch\.request_http\(client,\s*input\)" out)))
  => true)
