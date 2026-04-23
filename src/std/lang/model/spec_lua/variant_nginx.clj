(ns std.lang.model.spec-lua.variant-nginx
  (:require [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lang.model.spec-lua :as lua]
            [std.lib.foundation :as f]
            [std.lib.template :as template]))

(defn tf-for-async
  "nginx-specific async transform"
  {:added "4.1"}
  [[_ [[res err] statement] {:keys [success error finally]}]]
  (template/$ (ngx.thread.spawn
               (fn []
                 (for:try [[~res ~err] ~statement]
                          {:success ~success
                           :error ~error})
                 ~@(if finally [finally])))))

(defn lua-tf-x-socket-connect
  [[_ host port opts]]
  (template/$ (do* (local '[conn err])
                   (when (== ~host "host.docker.internal")
                     (local handle (io.popen
                                    (cat "ping host.docker.internal -c 1 -q 2>&1"
                                         " | "
                                         "grep -Po \"(\\d{1,3}\\.){3}\\d{1,3}\"")))
                     (:= ~host (handle:read "*a"))
                     (:= ~host (string.sub ~host 1 (- (len ~host) 1)))
                     (handle:close))
                   (:= conn (ngx.socket.tcp))
                   (:= '[res err] (conn:connect ~host ~port))
                   (return conn err))))

(defn lua-tf-x-cache
  [[_ key]]
  (list '. 'ngx.shared [(if (symbol? key)
                          key
                          (f/strn key))]))

(defn lua-tf-x-cache-list
  [[_ cache]]
  (list '. cache '(get-keys 0)))

(defn lua-tf-x-cache-flush
  [[_ cache]]
  (list '. cache '(flush-all)))

(defn lua-tf-x-cache-get
  [[_ cache key]]
  (list '. cache (list 'get key)))

(defn lua-tf-x-cache-set
  [[_ cache key val]]
  (list '. cache (list 'set key val)))

(defn lua-tf-x-cache-del
  [[_ cache key]]
  (list '. cache (list 'delete key)))

(defn lua-tf-x-cache-incr
  [[_ cache key num]]
  (list '. cache (list 'incr key num)))

(defn lua-tf-x-thread-spawn
  [[_ thunk & [strategy]]]
  (case strategy
    :mock (list 'coroutine.create thunk)
    (list 'ngx.thread.spawn thunk)))

(defn lua-tf-x-thread-join
  [[_ thread & [strategy]]]
  (case strategy
    :mock (list 'coroutine.resume thread)
    (list 'ngx.thread.wait thread)))

(defn lua-tf-x-with-delay
  [[_ thunk ms]]
  (list 'return (list 'ngx.thread.spawn (list 'fn []
                                              (list 'ngx.sleep (list '/ ms 1000))
                                              (list 'var 'f := thunk)
                                              (list 'return (list 'f))))))

(def +meta-delta+
  "Nginx-specific metadata overrides layered onto base Lua."
  {})

(def +grammar-delta+
  "Nginx-specific grammar overrides layered onto base Lua."
  {:for-async      {:macro #'tf-for-async :emit :macro}
   :x-socket-connect {:macro #'lua-tf-x-socket-connect :emit :macro}
   :x-cache          {:macro #'lua-tf-x-cache :emit :macro}
   :x-cache-list     {:macro #'lua-tf-x-cache-list :emit :macro}
   :x-cache-flush    {:macro #'lua-tf-x-cache-flush :emit :macro}
   :x-cache-get      {:macro #'lua-tf-x-cache-get :emit :macro}
   :x-cache-set      {:macro #'lua-tf-x-cache-set :emit :macro}
   :x-cache-del      {:macro #'lua-tf-x-cache-del :emit :macro}
   :x-cache-incr     {:macro #'lua-tf-x-cache-incr :emit :macro}
   :x-thread-spawn   {:macro #'lua-tf-x-thread-spawn :emit :macro}
   :x-thread-join    {:macro #'lua-tf-x-thread-join :emit :macro}
   :x-with-delay     {:macro #'lua-tf-x-with-delay :emit :macro}
   :x-b64-decode     {:emit :alias :raw 'ngx.decode-base64}
   :x-b64-encode     {:emit :alias :raw 'ngx.encode-base64}
   :x-uri-decode     {:emit :alias :raw 'ngx.unescape-uri}
   :x-uri-encode     {:emit :alias :raw 'ngx.escape-uri}})

(def +meta+
  (lua/variant-meta +meta-delta+))

(def +grammar+
  (lua/variant-grammar +grammar-delta+))

(def +book+
  (book/book {:lang :lua.nginx
              :parent :lua
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
