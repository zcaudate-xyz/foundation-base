(ns rt.nginx-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [lua.nginx :as n]
            [rt.nginx :refer :all]))

(l/script- :lua
  {:runtime :nginx.instance
   :require [[xt.lang.base-lib :as k]
             [xt.sys.cache-common :as cache]
             [lua.nginx :as n]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer rt.nginx/error-logs :added "4.0"}
(fact "gets the running nginx error log"
  ^:hidden
  
  (error-logs)
  => "")

^{:refer rt.nginx/access-logs :added "4.0"}
(fact "gets the running nginx access log"
  ^:hidden
  
  (access-logs)
  => string?)

^{:refer rt.nginx/nginx-conf :added "4.0"}
(fact "accesses the running ngx conf"
  ^:hidden
  
  (nginx-conf)
  => string?)

^{:refer rt.nginx/dir-tree :added "4.0"}
(fact "gets the running nginx access log"
  ^:hidden
  
  (dir-tree)
  => map?)

^{:refer rt.nginx/all-nginx-ports :added "4.0"}
(fact "gets all nginx ports on the system"
  ^:hidden
  
  (all-nginx-ports)
  => map?)

^{:refer rt.nginx/all-nginx-active :added "4.0"}
(fact "gets all active nginx processes for a port"
  ^:hidden
  
  (all-nginx-active 1000)
  => (any vector? nil?))

^{:refer rt.nginx/make-conf :added "4.0"}
(fact "creates a config"
  ^:hidden
  
  (make-conf nil)
  => string?)

^{:refer rt.nginx/make-temp :added "4.0"}
(fact "makes a temp directory and conf"
  ^:hidden
  
  (make-temp {})
  => vector?)

^{:refer rt.nginx/start-test-server-shell :added "4.0"}
(fact "starts in shell")

^{:refer rt.nginx/start-test-server-container :added "4.0"}
(fact "starts in container")

^{:refer rt.nginx/start-test-server :added "4.0"
  :let [|port| (h/port:check-available 0)]
  :teardown [(stop-test-server {:port |port|})]}
(fact "starts the test server for a given port"
  ^:hidden

  
  (start-test-server {:port |port|})
  
  (get (all-nginx-ports) |port|)
  => set?)

^{:refer rt.nginx/kill-single-nginx :added "4.0"}
(fact "kills nginx processes for a single port")

^{:refer rt.nginx/kill-all-nginx :added "4.0"}
(fact "kills all runnig nginx processes")

^{:refer rt.nginx/stop-test-server :added "4.0"}
(fact "stops the nginx test server")

^{:refer rt.nginx/raw-eval-nginx :added "4.0"}
(fact "posts a raw lua string to the dev server"
  ^:hidden
  
  (std.json/read (raw-eval-nginx (l/rt :lua) "return 1 + 2"))
  => {"value" 3, "type" "data"})

^{:refer rt.nginx/invoke-ptr-nginx :added "4.0"}
(fact "evaluates lua ptr and arguments"
  ^:hidden
  
  (invoke-ptr-nginx (l/rt :lua)
                    k/add
                    [1 2])
  => 3
  
  (invoke-ptr-nginx (l/rt :lua)
                    k/add
                    ["hello" 2])
  => (throws)

  (!.lua
   (local g (n/shared :GLOBAL))
   (cache/set g "a" 1)
   (cache/set g "b" 2)
   (cache/list-keys g))
  => ["a" "b"]

  (!.lua
   (cache/list-keys (n/shared :GLOBAL)))
  => ["a" "b"])

^{:refer rt.nginx/nginx:create :added "4.0"}
(fact "creates a dev nginx runtime")

^{:refer rt.nginx/nginx :added "4.0"}
(fact "creates and starts a dev nginx runtime")

(comment
  (h/on:cancel )
  
  (throw (ex-info "ouoeuoe"))
  (l/rt:stop)
  (./import)
  (l/emit :lua )
  (./pull '[compliment "0.3.11"])
  )