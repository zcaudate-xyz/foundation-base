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
  [[_ host port opts cb]]
  (template/$ (do* (local '[conn res err])
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
                   (if err
                     (return (~cb err nil))
                     (return (~cb nil conn))))))

(defn lua-tf-x-with-delay
  [[_ thunk ms]]
  (list 'return (list 'ngx.thread.spawn
                      (list 'fn []
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
