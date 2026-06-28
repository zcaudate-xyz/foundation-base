(ns hara.runtime.nginx-test
  (:require [lua.nginx :as n]
            [net.http :as http]
            [hara.runtime.nginx :refer :all]
            [hara.runtime.nginx.config :as config]
            [hara.runtime.nginx.script :as script]
            [std.fs :as fs]
            [hara.lang :as l]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib.network :as network]
            [std.lib.os :as os])
  (:use code.test))

(l/script- :lua
  {:runtime :nginx.instance
   :require [[xt.lang.common-lib :as k]
             [lua.nginx.common-cache :as cache]
             [lua.nginx :as n]]
   :test-mode true})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.nginx/error-logs :added "4.0"}
(fact "gets the running nginx error log"
  (with-redefs [l/rt:inner (constantly {:state (atom [nil "path"])})
                slurp (constantly "log")]
    (error-logs)
    => "log"))

^{:refer hara.runtime.nginx/access-logs :added "4.0"}
(fact "gets the running nginx access log"
  (with-redefs [l/rt:inner (constantly {:state (atom [nil "path"])})
                slurp (constantly "log")]
    (access-logs)
    => "log"))

^{:refer hara.runtime.nginx/nginx-conf :added "4.0"}
(fact "accesses the running ngx conf"
  (with-redefs [l/rt:inner (constantly {:state (atom [nil "path" "conf"])})
                slurp (constantly "conf")]
    (nginx-conf)
    => "conf"))

^{:refer hara.runtime.nginx/dir-tree :added "4.0"}
(fact "gets the running nginx access log"
  (with-redefs [l/rt:inner (constantly {:state (atom [nil "path"])})
                fs/list (constantly [])]
    (dir-tree)
    => []))

^{:refer hara.runtime.nginx/all-nginx-ports :added "4.0"}
(fact "gets all nginx ports on the system"
  (with-redefs [os/sh (fn [& _] "COMMAND PID USER FD TYPE DEVICE SIZE/OFF NODE NAME\nnginx 123 root 6u IPv4 1234 0t0 TCP *:80 (LISTEN)")]
    (all-nginx-ports)
    => {80 #{123}}))

^{:refer hara.runtime.nginx/all-nginx-active :added "4.0"}
(fact "gets all active nginx processes for a port"
  (with-redefs [all-nginx-ports (constantly {80 #{123}})]
    (all-nginx-active 80)
    => #{123}))

^{:refer hara.runtime.nginx/make-conf :added "4.0"}
(fact "creates a config"
  (with-redefs [config/create-conf (fn [_] {})
                script/write (constantly "conf")
                env/pl (fn [& _] nil)]
    (make-conf {})
    => "conf"))

^{:refer hara.runtime.nginx/make-temp :added "4.0"}
(fact "makes a temp directory and conf"
  (with-redefs [make-conf (constantly "conf")
                fs/create-tmpdir (constantly "tmp")
                fs/create-tmpfile (constantly "tmp/file")
                fs/create-directory (constantly "tmp/logs")]
    (make-temp {})
    => ["tmp" "tmp/file" "conf"]))

^{:refer hara.runtime.nginx/start-test-server-shell :added "4.0"}
(fact "starts in shell"
  (with-redefs [make-temp (constantly ["tmp" "file" "conf"])
                os/sh (fn [& _] {:exit 0})
                network/wait-for-port (fn [_ _ _] nil)]
    (start-test-server-shell {:port 80})
    => [:started "tmp" "file"]))

^{:refer hara.runtime.nginx/start-test-server-container :added "4.0"}
(fact "starts in container"
  (with-redefs [make-temp (constantly ["tmp" "file" "conf"])
                os/sh (fn [& _] {:exit 0})
                network/wait-for-port (fn [_ _ _] nil)]
    (start-test-server-container {:port 80 :container {:image "img" :exec "exec"}})
    => [:started "tmp" "file"]))

^{:refer hara.runtime.nginx/start-test-server :added "4.0"}
(fact "starts the test server for a given port"
  (with-redefs [network/port:check-available (constantly 80)
                all-nginx-ports (constantly {})
                os/os-arch (constantly "amd64")
                start-test-server-shell (constantly :started)]
    (start-test-server {:port 80})
    => :started))

^{:refer hara.runtime.nginx/kill-single-nginx :added "4.0"}
(fact "kills nginx processes for a single port"
  (with-redefs [all-nginx-ports (constantly {80 #{123}})
                os/sh (fn [& _] :killed)]
    (kill-single-nginx 80)
    => [#{123} :killed]))

^{:refer hara.runtime.nginx/kill-all-nginx :added "4.0"}
(fact "kills all runnig nginx processes"
  (with-redefs [all-nginx-ports (constantly {80 #{123}})
                os/sh (fn [& _] :killed)]
    (kill-all-nginx)
    => [{80 #{123}} :killed]))

^{:refer hara.runtime.nginx/stop-test-server :added "4.0"}
(fact "stops the nginx test server"
  (with-redefs [all-nginx-ports (constantly {80 #{123}})
                os/sh (fn [& _] nil)]
    (stop-test-server {:port 80})
    => [:stopped #{123}]))

^{:refer hara.runtime.nginx/raw-eval-nginx :added "4.0"}
(fact "posts a raw lua string to the dev server"
  (with-redefs [http/post (constantly {:status 200 :body "ok"})]
    (raw-eval-nginx {:host "localhost" :port 80} "body")
    => "ok"))

^{:refer hara.runtime.nginx/invoke-ptr-nginx :added "4.0"}
(fact "evaluates lua ptr and arguments"
  ;; delegates
  )

^{:refer hara.runtime.nginx/nginx:create :added "4.0"}
(fact "creates a dev nginx runtime"
  (with-redefs [network/port:check-available (constantly 1234)]
    (nginx:create {:id "test"})
    => (contains {:id "test" :port 1234})))

^{:refer hara.runtime.nginx/nginx :added "4.0"}
(fact "creates and starts a dev nginx runtime"
  (with-redefs [nginx:create (constantly :created)
                component/start (fn [x] [x :started])]
    (nginx {})
    => [:created :started]))
