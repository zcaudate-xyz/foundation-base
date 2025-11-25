(ns rt.nginx-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [lua.nginx :as n]
            [rt.nginx :refer :all]
            [std.fs :as fs]
            [net.http :as http]
            [rt.nginx.config :as config]
            [rt.nginx.script :as script]))

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
  (with-redefs [l/rt:inner (constantly {:state (atom [nil "path"])})
                slurp (constantly "log")]
    (error-logs)
    => "log"))

^{:refer rt.nginx/access-logs :added "4.0"}
(fact "gets the running nginx access log"
  ^:hidden
  (with-redefs [l/rt:inner (constantly {:state (atom [nil "path"])})
                slurp (constantly "log")]
    (access-logs)
    => "log"))

^{:refer rt.nginx/nginx-conf :added "4.0"}
(fact "accesses the running ngx conf"
  ^:hidden
  (with-redefs [l/rt:inner (constantly {:state (atom [nil "path" "conf"])})
                slurp (constantly "conf")]
    (nginx-conf)
    => "conf"))

^{:refer rt.nginx/dir-tree :added "4.0"}
(fact "gets the running nginx access log"
  ^:hidden
  (with-redefs [l/rt:inner (constantly {:state (atom [nil "path"])})
                fs/list (constantly [])]
    (dir-tree)
    => []))

^{:refer rt.nginx/all-nginx-ports :added "4.0"}
(fact "gets all nginx ports on the system"
  ^:hidden
  (with-redefs [h/sh (fn [& _] "COMMAND PID USER FD TYPE DEVICE SIZE/OFF NODE NAME\nnginx 123 root 6u IPv4 1234 0t0 TCP *:80 (LISTEN)")]
    (all-nginx-ports)
    => {80 #{123}}))

^{:refer rt.nginx/all-nginx-active :added "4.0"}
(fact "gets all active nginx processes for a port"
  ^:hidden
  (with-redefs [all-nginx-ports (constantly {80 #{123}})]
    (all-nginx-active 80)
    => #{123}))

^{:refer rt.nginx/make-conf :added "4.0"}
(fact "creates a config"
  ^:hidden
  (with-redefs [config/create-conf (fn [_] {})
                script/write (constantly "conf")
                h/pl (fn [& _] nil)]
    (make-conf {})
    => "conf"))

^{:refer rt.nginx/make-temp :added "4.0"}
(fact "makes a temp directory and conf"
  ^:hidden
  (with-redefs [make-conf (constantly "conf")
                fs/create-tmpdir (constantly "tmp")
                fs/create-tmpfile (constantly "tmp/file")
                fs/create-directory (constantly "tmp/logs")]
    (make-temp {})
    => ["tmp" "tmp/file" "conf"]))

^{:refer rt.nginx/start-test-server-shell :added "4.0"}
(fact "starts in shell"
  (with-redefs [make-temp (constantly ["tmp" "file" "conf"])
                h/sh (fn [& _] {:exit 0})
                h/wait-for-port (fn [_ _ _] nil)]
    (start-test-server-shell {:port 80})
    => [:started "tmp" "file"]))

^{:refer rt.nginx/start-test-server-container :added "4.0"}
(fact "starts in container"
  (with-redefs [make-temp (constantly ["tmp" "file" "conf"])
                h/sh (fn [& _] {:exit 0})
                h/wait-for-port (fn [_ _ _] nil)]
    (start-test-server-container {:port 80 :container {:image "img" :exec "exec"}})
    => [:started "tmp" "file"]))

^{:refer rt.nginx/start-test-server :added "4.0"}
(fact "starts the test server for a given port"
  (with-redefs [h/port:check-available (constantly 80)
                all-nginx-ports (constantly {})
                h/os-arch (constantly "amd64")
                start-test-server-shell (constantly :started)]
    (start-test-server {:port 80})
    => :started))

^{:refer rt.nginx/kill-single-nginx :added "4.0"}
(fact "kills nginx processes for a single port"
  (with-redefs [all-nginx-ports (constantly {80 #{123}})
                h/sh (fn [& _] :killed)]
    (kill-single-nginx 80)
    => [#{123} :killed]))

^{:refer rt.nginx/kill-all-nginx :added "4.0"}
(fact "kills all runnig nginx processes"
  (with-redefs [all-nginx-ports (constantly {80 #{123}})
                h/sh (fn [& _] :killed)]
    (kill-all-nginx)
    => [{80 #{123}} :killed]))

^{:refer rt.nginx/stop-test-server :added "4.0"}
(fact "stops the nginx test server"
  (with-redefs [all-nginx-ports (constantly {80 #{123}})
                h/sh (fn [& _] nil)]
    (stop-test-server {:port 80})
    => [:stopped #{123}]))

^{:refer rt.nginx/raw-eval-nginx :added "4.0"}
(fact "posts a raw lua string to the dev server"
  ^:hidden
  (with-redefs [http/post (constantly {:status 200 :body "ok"})]
    (raw-eval-nginx {:host "localhost" :port 80} "body")
    => "ok"))

^{:refer rt.nginx/invoke-ptr-nginx :added "4.0"}
(fact "evaluates lua ptr and arguments"
  ;; delegates
  )

^{:refer rt.nginx/nginx:create :added "4.0"}
(fact "creates a dev nginx runtime"
  (with-redefs [h/port:check-available (constantly 1234)]
    (nginx:create {:id "test"})
    => (contains {:id "test" :port 1234})))

^{:refer rt.nginx/nginx :added "4.0"}
(fact "creates and starts a dev nginx runtime"
  (with-redefs [nginx:create (constantly :created)
                h/start (fn [x] [x :started])]
    (nginx {})
    => [:created :started]))
