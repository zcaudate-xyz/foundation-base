(ns lua.nginx-test
  (:require [rt.nginx :as nginx]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :nginx.instance
   :require [[lua.nginx :as n]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx/req-raw-uri :added "4.0"}
(fact "gets the raw uri"

  (!.lua
   (n/thread-spawn
    (fn []
      (n/thread-spawn
       (fn []
         (n/thread-spawn
          (fn []
            (return (+ 1 2 3))))))))))


^{:refer lua.nginx/req-raw-uri :added "4.0"}
(fact "gets the raw uri"

  (n/req-raw-uri)
  => "eval")

^{:refer lua.nginx/getconf :added "4.0"}
(fact "gets the current from env or global CONFIG"

  (!.lua (:= (. CONFIG ["HELLO"]) "WORLD")
         (n/getconf "HELLO"))
  => "WORLD")

^{:refer lua.nginx/log! :added "4.0"}
(fact "logs out a request with clj line number"

  (!.lua (n/log! "HELLO" {:a 1}))

  (nginx/error-logs)
  => string?)

^{:refer lua.nginx/return-ok :added "4.0"}
(fact "returns an http 200 response"

  ((:template @n/return-ok) "OK")
  => '(do (:= ngx.status ngx.HTTP-OK)
          (ngx.say "OK")
          (return (ngx.exit ngx.HTTP-OK)))

  (!.lua
   ('((fn []
        (n/return-ok "\"OK\"")))))
  => "OK")

^{:refer lua.nginx/return-err :added "4.0"}
(fact "returns an error code"

  ((:template @n/return-err) "NOT-FOUND" 'ngx.HTTP-FORBIDDEN)
  => '(do (:= ngx.status ngx.HTTP-FORBIDDEN)
          (ngx.say "NOT-FOUND")
          (return (ngx.exit ngx.HTTP-FORBIDDEN))))

^{:refer lua.nginx/return-out :added "4.0"}
(fact "returns a message with output"

  ((:template @n/return-out) {:status "ok"})
  => '(do
        (:= ngx.status ngx.HTTP-OK)
        (ngx.say (cjson.encode {:status "ok"}))
        (return (ngx.exit ngx.HTTP-OK)))

  (!.lua
   ('((fn []
        (n/return-out {:status "ok"})))))
  => {"status" "ok"})

^{:refer lua.nginx/http-debug-ws :added "4.0"}
(fact "http debug ws form")

^{:refer lua.nginx/http-debug-es :added "4.0"}
(fact "http debug sse form")

^{:refer lua.nginx/http-debug-call :added "4.0"}
(fact "http call function (for things like upload)")

^{:refer lua.nginx/http-debug-api :added "4.0"}
(fact "embeds the debug api")

^{:refer lua.nginx/http-echo-ws :added "4.0"}
(fact "http echo ws form")

^{:refer lua.nginx/http-setup-global :added "4.0"}
(fact "injects globals used by `lua.nginx` libraries")

^{:refer lua.nginx/start-task :added "4.0"}
(fact "starts a task loop (usually in `init-worker-by-lua-block`) section"

  ((:template @n/start-task) 'f)
  => '(ngx.timer.at 0 (fn [] (ngx.thread.spawn f))))

^{:refer lua.nginx/shared :added "4.0"}
(fact "gets the shared state")

^{:refer lua.nginx/ngx-tmpl :added "4.0"}
(fact "creates an ngx form"

  (n/ngx-tmpl 'OK)
  => '(def$.lua OK ngx.OK))

^{:refer lua.nginx/path :added "4.0"}
(fact "gets the absolute path to a file"

  (n/path "hello")
  ;; "/var/folders/rc/4nxjl26j50gffnkgm65ll8gr0000gp/T/4997056838908143382/hello"
  => string?)

^{:refer lua.nginx/sha1-new :added "4.0"}
(fact "creates a sha1"

  (!.lua
   (do:>
    (local ngxsha1 (require "resty.sha1"))
    (local ngxstr (require "resty.string"))
    (local sha (n/sha1-new))
    (. sha (update "hello"))
    (return (n/to-hex (. sha (final))))))
  => "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d")

^{:refer lua.nginx/md5-new :added "4.0"}
(fact "creates a sha1"

  (!.lua
   (do:>
    (local ngxmd5 (require "resty.md5"))
    (local ngxstr  (require "resty.string"))
    (local sha     (n/md5-new))
    (. sha (update "hello"))
    (return (n/to-hex (. sha (final))))))
  => "5d41402abc4b2a76b9719d911017c592")

^{:refer lua.nginx/sha256-new :added "4.0"}
(fact "creates a sha256"

  (!.lua
   (do:>
    (local ngxsha256 (require "resty.sha256"))
    (local ngxstr  (require "resty.string"))
    (local sha (n/sha256-new))
    (. sha (update "hello"))
    (return (n/to-hex (. sha (final))))))
  => "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824")

(comment

  (rt.nginx/nginx-conf)
  (./import))
