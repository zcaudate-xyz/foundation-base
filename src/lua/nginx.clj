(ns lua.nginx
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [xt.lang.base-repl :as repl])
  (:refer-clojure :exclude [print flush time re-find]))

(l/script :lua
  {:import [["cjson" :as cjson]
            ["resty.md5" :as ngxmd5]
            ["resty.string" :as ngxstr]
            ["resty.sha1" :as ngxsha1]
            ["resty.sha256" :as ngxsha256]
            ["resty.uuid" :as ngxuuid]
            ["resty.shell" :as ngxshell]]})

(def +core+
  '[OK ERROR AGAIN DONE DECLINED])

(def +http-method+
  '[HTTP_GET
    HTTP_HEAD
    HTTP_PUT
    HTTP_POST
    HTTP_DELETE
    HTTP_OPTIONS 
    HTTP_MKCOL     
    HTTP_COPY      
    HTTP_MOVE      
    HTTP_PROPFIND  
    HTTP_PROPPATCH 
    HTTP_LOCK      
    HTTP_UNLOCK    
    HTTP_PATCH     
    HTTP_TRACE])

(def +http-status+
  '[HTTP_CONTINUE HTTP_SWITCHING_PROTOCOLS
    HTTP_OK HTTP_CREATED HTTP_ACCEPTED
    HTTP_NO_CONTENT HTTP_PARTIAL_CONTENT
    HTTP_SPECIAL_RESPONSE HTTP_MOVED_PERMANENTLY
    HTTP_MOVED_TEMPORARILY HTTP_SEE_OTHER
    HTTP_NOT_MODIFIED HTTP_TEMPORARY_REDIRECT
    HTTP_BAD_REQUEST HTTP_UNAUTHORIZED
    HTTP_PAYMENT_REQUIRED HTTP_FORBIDDEN
    HTTP_NOT_FOUND HTTP_NOT_ALLOWED HTTP_NOT_ACCEPTABLE
    HTTP_REQUEST_TIMEOUT HTTP_CONFLICT HTTP_GONE
    HTTP_UPGRADE_REQUIRED HTTP_TOO_MANY_REQUESTS
    HTTP_CLOSE HTTP_ILLEGAL HTTP_INTERNAL_SERVER_ERROR
    HTTP_METHOD_NOT_IMPLEMENTED HTTP_BAD_GATEWAY
    HTTP_SERVICE_UNAVAILABLE HTTP_GATEWAY_TIMEOUT
    HTTP_VERSION_NOT_SUPPORTED HTTP_INSUFFICIENT_STORAGE])

(def +log+
  '[STDERR
    EMERG
    ALERT
    CRIT
    ERR
    WARN
    NOTICE
    INFO
    DEBUG])

(def$.lua print print)

(def +methods+
  '[ctx
    location.capture
    location.capture_multi
    status
    header
    resp.get_headers
    req.is_internal
    req.start_time
    req.http_version
    req.raw_header
    req.get_method
    req.set_method
    req.set_uri
    req.set_uri_args
    req.get_uri_args
    req.get_post_args
    req.get_headers
    req.set_header
    req.clear_header
    req.read_body
    req.discard_body
    req.get_body_data
    req.get_body_file
    req.set_body_data
    req.set_body_file
    req.init_body
    req.append_body
    req.finish_body
    req.socket
    exec
    redirect
    send_headers
    headers_sent
    print
    say
    log
    flush
    exit
    eof
    sleep
    escape_uri
    unescape_uri
    encode_args
    decode_args
    encode_base64
    decode_base64
    crc32_short
    crc32_long
    hmac_sha1
    md5
    md5_bin
    sha1_bin
    quote_sql_str
    today
    time
    now
    update_time
    localtime
    utctime
    cookie_time
    http_time
    parse_http_time
    is_subrequest
    re.match
    re.find
    re.gmatch
    re.sub
    re.gsub

    socket.udp
    socket.stream
    socket.tcp
    socket.connect
    get_phase
    thread.spawn
    thread.wait
    thread.kill
    on_abort
    timer.at
    timer.running_count
    timer.pending_count
    config.subsystem
    config.debug
    config.prefix
    config.nginx_version
    config.nginx_configure
    config.ngx_lua_version
    worker.exiting
    worker.pid
    worker.count
    worker.id
    semaphore
    balancer
    ssl
    ocsp])

(defmacro.lua req-raw-uri
  "gets the raw uri"
  {:added "4.0"}
  []
  (list 'string.match
        (list 'ngx.req.raw_header)
        "^%w+ /([^%s]+)"))

(defmacro.lua getconf
  "gets the current from env or global CONFIG"
  {:added "4.0"}
  [s]
  (list 'or
        (list 'os.getenv s)
        (list 'and 'CONFIG
              (list '. 'CONFIG [s]))))

(defmacro.lua log!
  "logs out a request with clj line number"
  {:added "4.0"}
  [message data]
  (let [pos   (meta (l/macro-form))
        nsstr (str (h/ns-sym)
                   [(:line pos)
                    (:column pos)])]
    (list 'ngx.log 'ngx.ALERT
          (list 'cat "\n"
                (list 'table.concat
                      [message
                       nsstr
                       (list 'cjson.encode data)]
                      " - ")
                "\n"))))

(defmacro.lua return-ok
  "returns an http 200 response"
  {:added "4.0"}
  [out]
  (h/$ (do (:= ngx.status ngx.HTTP-OK)
           (ngx.say ~out)
           (return (ngx.exit ngx.HTTP-OK)))))

(defmacro.lua return-err
  "returns an error code"
  {:added "4.0"}
  [err code]
  (h/$ (do (:= ngx.status ~code)
           (ngx.say ~err)
           (return (ngx.exit ~code)))))

(defmacro.lua return-out
  "returns a message with output"
  {:added "4.0"}
  [out]
  (h/$ (do (:= ngx.status ngx.HTTP-OK)
           (ngx.say (cjson.encode ~out))
           (return (ngx.exit ngx.HTTP-OK)))))

(defmacro.lua http-debug-ws
  "http debug ws form"
  {:added "4.0"}
  []
  '(do 
     (local #{ws-handler} DEBUG)
     (ws-handler)))

(defmacro.lua http-debug-es
  "http debug sse form"
  {:added "4.0"}
  []
  '(do 
    (local #{es-handler} DEBUG)
    (es-handler)))

(defmacro.lua http-debug-call
  "http call function (for things like upload)"
  {:added "4.0"}
  []
  '(do 
     (local #{call-handler} DEBUG)
     (call-handler)))

(defmacro.lua http-debug-api
  "embeds the debug api"
  {:added "4.0"}
  []
  (list 'do
        '(ngx.req.read-body)
        '(local body (ngx.req.get-body-data))
        (list `return-ok (list `repl/return-eval 'body))))

(defmacro.lua http-echo-ws
  "http echo ws form"
  {:added "4.0"}
  []
  '(do
     (local ngxwsserver (require "resty.websocket.server"))
     (local conn (. ngxwsserver (new {:timeout 10000
                                      :max-payload-len 65535})))
     (while true
       (local '[data tag err] (. conn (recv-frame)))
       (when (. conn fatal)
         (ngx.log ngx.ERR "failed to receive frame: " err)
         (break))
       
       (cond (not data)
             (do (local '[bytes err] (. conn (send-ping)))
                 (when (not bytes)
                   (ngx.log ngx.ERR "failed to send ping: " err)
                   (break)))
             
             (== tag "close")
             (do (. conn (send-close 1000))
                 (break))

             (== tag "ping")
             (do (local '[bytes err] (. conn (send-pong)))
                 (when (not bytes)
                   (ngx.log ngx.ERR "failed to send ping: " err)
                   (break)))

             (== tag "pong")
             (ngx.log ngx.INFO "pong recieved")

             :else
             (. conn (send-text data))))))

(defmacro.lua http-setup-global
  "injects globals used by `lua.nginx` libraries"
  {:added "4.0"}
  []
  '(do:> (:= cjson (require "cjson"))
         (:= CONFIG {})
         (:= DEBUG  {})))

(defmacro.lua start-task
  "starts a task loop (usually in `init-worker-by-lua-block`) section"
  {:added "4.0"}
  [f]
  (h/$ (ngx.timer.at 0 (fn []
                         (ngx.thread.spawn ~f)))))

(defmacro.lua shared
  "gets the shared state"
  {:added "4.0"}
  ([key]
   (list '. 'ngx.shared [(if (keyword? key)
                           (h/strn key)
                           key)])))

(defn ngx-tmpl
  "creates an ngx form"
  {:added "4.0"}
  [sym]
  (let [nsym (-> (name sym)
                 (.replaceAll "\\." "-")
                 (.replaceAll "_" "-")
                 (symbol))]
    (h/$ (def$.lua ~nsym ~(symbol (str "ngx." sym))))))

(h/template-entries [ngx-tmpl]
  +core+
  +http-method+
  +http-status+
  +log+
  +methods+)

(defmacro.lua path
  "gets the absolute path to a file
 
   (n/path \"hello\")
   ;; \"/var/folders/rc/4nxjl26j50gffnkgm65ll8gr0000gp/T/4997056838908143382/hello\"
   => string?"
  {:added "4.0"}
  [file]
  (h/$ (cat (ngx.config.prefix) ~file)))

;;
;; SHA
;;

(defmacro.lua sha1-new
  "creates a sha1"
  {:added "4.0"}
  []
  '(. ngxsha1 (new)))

(defmacro.lua md5-new
  "creates a sha1"
  {:added "4.0"}
  []
  '(. ngxmd5 (new)))

(defmacro.lua sha256-new
  "creates a sha256"
  {:added "4.0"}
  []
  '(. ngxsha256 (new)))

(def$.lua uuid ngxuuid)

(def$.lua uuid-time ngxuuid.generate-time)

(def$.lua uuid-check ngxuuid.is-valid)

(def$.lua to-hex ngxstr.to-hex)

(def$.lua atoi ngxstr.atoi)
