(ns std.lang.model.spec-xtalk.fn-lua
  (:require [std.lib :as h]))

;;
;; CORE
;;

(defn lua-tf-x-del
  [[_ obj]]
  (list := obj nil))

(defn lua-tf-x-cat
  [[_ & args]]
  (apply list 'cat args))

(defn lua-tf-x-eval
  [[_ s]]
  (list (list 'loadstring (list 'cat "return " s))))

(defn lua-tf-x-apply
  [[_ f args]]
  (list f (list 'unpack args)))

(defn lua-tf-x-err
  [[_ s & [data]]]
  (if data
    (h/$ (error (cjson.encode ~[s data])))
    (h/$ (error ~s))))

(defn lua-tf-x-shell
  ([[_ s cm]]
   (h/$ (do* (var handle (io.popen ~s))
             (var res (handle:read "*a"))
             (var f (. ~cm ["success"]))
             (if f
               (return (f res))
               (return res))))))

(defn lua-tf-x-hash-id
  [[_ obj]]
  (h/$ (return nil)))

(defn lua-tf-x-type-native
  [[_ obj]]
  (h/$ (do (local t := (type ~obj))
           (if (== t "table")
             (if (== nil (. '(~obj) [1]))
               (return "object")
               (return "array"))
             (return t)))))

(def +lua-core+
  {:x-del            {:macro #'lua-tf-x-del  :emit :macro}
   :x-cat            {:macro #'lua-tf-x-cat  :emit :macro}
   :x-len            {:emit :alias :raw 'len}
   :x-err            {:macro #'lua-tf-x-err    :emit :macro}
   :x-eval           {:macro #'lua-tf-x-eval   :emit :macro}
   :x-apply          {:macro #'lua-tf-x-apply  :emit :macro}
   :x-unpack         {:emit :alias :raw 'unpack}
   :x-print          {:emit :alias :raw 'print}
   
   :x-random         {:emit :alias :raw 'math.random}
   :x-shell          {:macro #'lua-tf-x-shell         :emit :macro}
   :x-now-ms         {:default '(math.floor (* 1000 (os.time)))   :emit :unit}
   :x-type-native    {:macro #'lua-tf-x-type-native  :emit :macro}})

(defn lua-tf-x-proto-create
  [[_ m]]
  (h/$
   (do (var mt ~m)
       (:= (. mt __index) mt)
       (return mt))))

(def +lua-proto+
  {:x-this            {:emit :unit  :default 'self}
   :x-proto-get       {:emit :alias :raw 'getmetatable}
   :x-proto-set       {:emit :alias :raw 'setmetatable}
   :x-proto-create    {:macro #'lua-tf-x-proto-create  :emit :macro}
   :x-proto-tostring  {:emit :unit  :default "__tostring"}})

;;
;; GLOBAL
;;

(defn lua-tf-x-global-has?
  [[_ sym]]
  (list 'not= (symbol sym) nil))

(defn lua-tf-x-global-set
  [[_ sym val]]
  (list ':= (symbol sym) val))

(defn lua-tf-x-global-del
  [[_ sym]]
  (list ':= (symbol sym) nil))

(def +lua-global+
  {:x-global-has?   {:macro #'lua-tf-x-global-has?  :emit :macro}
   :x-global-set    {:macro #'lua-tf-x-global-set   :emit :macro}
   :x-global-del    {:macro #'lua-tf-x-global-del   :emit :macro}})

;;
;; CUSTOM
;;

(def +lua-custom+
  {})

;;
;; MATH
;;

(defn lua-tf-x-m-quot  [[_ num denom]] (list 'math.floor
                                             (list '/ num denom)))

(def +lua-math+
  {:x-m-abs           {:emit :alias :raw 'math.abs}
   :x-m-acos          {:emit :alias :raw 'math.acos}
   :x-m-asin          {:emit :alias :raw 'math.asin}
   :x-m-atan          {:emit :alias :raw 'math.atan}
   :x-m-ceil          {:emit :alias :raw 'math.ceil}
   :x-m-cos           {:emit :alias :raw 'math.cos}
   :x-m-cosh          {:emit :alias :raw 'math.cosh}
   :x-m-exp           {:emit :alias :raw 'math.exp}
   :x-m-floor         {:emit :alias :raw 'math.floor}
   :x-m-loge          {:emit :alias :raw 'math.log}
   :x-m-log10         {:emit :alias :raw 'math.log10}
   :x-m-max           {:emit :alias :raw 'math.max}
   :x-m-min           {:emit :alias :raw 'math.min}
   :x-m-mod           {:emit :alias :raw 'math.mod}
   :x-m-pow           {:emit :alias :raw 'math.pow}
   :x-m-quot          {:macro #'lua-tf-x-m-quot,     :emit :macro}
   :x-m-sin           {:emit :alias :raw 'math.sin}
   :x-m-sinh          {:emit :alias :raw 'math.sinh}
   :x-m-sqrt          {:emit :alias :raw 'math.sqrt}
   :x-m-tan           {:emit :alias :raw 'math.tan}
   :x-m-tanh          {:emit :alias :raw 'math.tanh}})


;;
;; TYPE
;;

(defn lua-tf-x-is-string?
  [[_ e]]
  (list '== "string" (list 'type e)))

(defn lua-tf-x-is-number?
  [[_ e]]
  (list '== "number" (list 'type e)))

(defn lua-tf-x-is-integer?
  [[_ e]]
  (list '== 0 (list 'mod e 1)))

(defn lua-tf-x-is-boolean?
  [[_ e]]
  (list '== "boolean" (list 'type e)))

(defn lua-tf-x-is-function?
  [[_ e]]
  (list '== "function" (list 'type e)))

(defn lua-tf-x-is-object?
  [[_ e]]
  (h/$ (or (and (== "table" (type ~e))
                (== nil (. '(~e) [1])))
           (== "object" (type ~e)))))

(defn lua-tf-x-is-array?
  [[_ e]]
  (h/$ (or (and (== "table" (type ~e))
                (not= nil (. '(~e) [1])))
           (== "array" (type ~e)))))

(def +lua-type+
  {:x-to-string      {:emit :alias :raw 'tostring}
   :x-to-number      {:emit :alias :raw 'tonumber}
   :x-is-string?     {:macro #'lua-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'lua-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'lua-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'lua-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'lua-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'lua-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'lua-tf-x-is-array? :emit :macro}})

;;
;; LU
;;

(defn lua-tf-x-lu-create
  ([[_]]
   (list 'setmetatable {} {"__mode" "k"})))

(defn lua-tf-x-lu-get
  ([[_ lu obj]]
   (h/$ (. ~lu [(tostring ~obj)]))))

(defn lua-tf-x-lu-set
  ([[_ lu obj gid]]
   (h/$ (:= (. ~lu [(tostring ~obj)]) ~gid))))

(defn lua-tf-x-lu-del
  ([[_ lu obj]]
   (h/$ (:= (. ~lu [(tostring ~obj)]) nil))))

(def +lua-lu+
  {:x-lu-create      {:macro #'lua-tf-x-lu-create :emit :macro}
   :x-lu-get         {:macro #'lua-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'lua-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'lua-tf-x-lu-del :emit :macro}})


;;
;; BIT
;;

(defn lua-tf-x-bit-and
  "lookup equals transform"
  {:added "4.0"}
  [[_ i1 i2]]
  (list 'bit.band i1 i2))

(defn lua-tf-x-bit-or
  "lookup equals transform"
  {:added "4.0"}
  [[_ i1 i2]]
  (list 'bit.bor i1 i2))

(defn lua-tf-x-bit-lshift
  "lookup equals transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'bit.lshift x n))

(defn lua-tf-x-bit-rshift
  "lookup equals transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'bit.rshift x n))

(defn lua-tf-x-bit-xor
  "lookup equals transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'bit.bxor x n))

(def +lua-bit+
  {:x-bit-and         {:macro #'lua-tf-x-bit-and    :emit :macro}
   :x-bit-or          {:macro #'lua-tf-x-bit-or     :emit :macro}
   :x-bit-lshift      {:macro #'lua-tf-x-bit-lshift :emit :macro}
   :x-bit-rshift      {:macro #'lua-tf-x-bit-rshift :emit :macro}
   :x-bit-xor         {:macro #'lua-tf-x-bit-xor :emit :macro}})

;;
;; OBJ
;;

(def +lua-obj+
  {})

;;
;; ARR
;;

(defn lua-tf-x-arr-clone
  [[_ arr]]
  [(list 'unpack arr)])

(defn lua-tf-x-arr-slice
  [[_ arr start end]]
  [(list 'unpack arr (list 'x:offset start) end)])

(defn lua-tf-x-arr-remove
  [[_ arr i]]
  (list 'table.remove arr (list 'x:offset i)))

(defn lua-tf-x-arr-push-first
  [[_ arr item]]
  (list 'table.insert arr 1 item))

(defn lua-tf-x-arr-pop-first
  [[_ arr]]
  (list 'table.remove arr 1))

(defn lua-tf-x-arr-insert
  [[_ arr idx item]]
  (list 'table.insert arr idx item))

(defn lua-tf-x-arr-sort
  [[_ arr key-fn comp-fn]]
  (list 'table.sort arr
        (h/$ (fn [a b]
               (return (~comp-fn
                        (~key-fn a)
                        (~key-fn b)))))))

(def +lua-arr+
  {:x-arr-clone       {:macro #'lua-tf-x-arr-clone      :emit :macro  :type :template}
   :x-arr-slice       {:macro #'lua-tf-x-arr-slice      :emit :macro  :type :template}
   :x-arr-push        {:emit :alias :raw 'table.insert}
   :x-arr-pop         {:emit :alias :raw 'table.remove}
   :x-arr-remove      {:macro #'lua-tf-x-arr-remove     :emit :macro  :type :template}
   :x-arr-find        {:macro #'lua-tf-x-arr-remove     :emit :macro  :type :template}
   :x-arr-push-first  {:macro #'lua-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'lua-tf-x-arr-pop-first  :emit :macro}
   :x-arr-insert      {:macro #'lua-tf-x-arr-insert     :emit :macro}
   :x-arr-sort        {:macro #'lua-tf-x-arr-sort       :emit :macro}
   :x-arr-str-comp    {:emit :alias :raw '<}})

;;
;; STRING
;;

(defn lua-tf-x-str-split
  ([[_ s tok]]
   (h/$ ('((fn [s tok]
             (var out := {})
             (string.gsub s
                          (string.format "([^%s]+)" tok)
                          (fn [c]
                            (table.insert out c)))
             (return out)))
         ~s ~tok))))

(defn lua-tf-x-str-join
  ([[_ s arr]]
   (list 'table.concat arr s)))

(defn lua-tf-x-str-index-of
  ([[_ s tok]]
   (list 'or (list 'string.find s tok) -1)))

(defn lua-tf-x-str-to-fixed
  ([[_ num digits]]
   (list 'string.format (list 'cat "%." digits "f")  num)))

(defn lua-tf-x-str-replace
  ([[_ s tok replacement]]
   (list 'string.gsub s tok replacement)))

(defn lua-tf-x-str-trim
  ([[_ s]]
   (list 'string.gsub s "^%s*(.-)%s*$" "%1")))

(defn lua-tf-x-str-trim-left
  ([[_ s]]
   (list 'string.gsub s "^%s*" "")))

(defn lua-tf-x-str-trim-right
  ([[_ s]]
   (list 'string.gsub s "^(%s*.-)%s*$" "%1")))

(def +lua-str+
  {:x-str-char       {:emit :alias :raw 'string.byte}
   :x-str-split      {:macro #'lua-tf-x-str-split      :emit :macro}
   :x-str-join       {:macro #'lua-tf-x-str-join       :emit :macro}
   :x-str-index-of   {:macro #'lua-tf-x-str-index-of   :emit :macro}
   :x-str-substring  {:emit :alias :raw 'string.sub}
   :x-str-to-upper   {:emit :alias :raw 'string.upper}
   :x-str-to-lower   {:emit :alias :raw 'string.lower}
   :x-str-to-fixed   {:macro #'lua-tf-x-str-to-fixed   :emit :macro}
   :x-str-replace    {:macro #'lua-tf-x-str-replace    :emit :macro}
   :x-str-trim       {:macro #'lua-tf-x-str-trim       :emit :macro}
   :x-str-trim-left  {:macro #'lua-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right {:macro #'lua-tf-x-str-trim-right :emit :macro}})

;;
;; JSON
;;

(def +lua-js+
  {:x-json-encode      {:emit :alias :raw 'cjson.encode}
   :x-json-decode      {:emit :alias :raw 'cjson.decode}})

;;
;; RETURN
;;

(defn lua-tf-x-return-encode
  ([[_ out id key]]
   (h/$ (do (do (local ret nil)
                (local '[r-ok r-err]
                       (pcall (fn []
                                (cond (== nil ~out)
                                      (:= ret (cjson.encode {:id  ~id
                                                             :key ~key
                                                             :type "data"
                                                             :value (. cjson ["null"])}))
                                      
                                      :else
                                      (:= ret (cjson.encode {:id  ~id
                                                             :key ~key
                                                             :type "data"
                                                             :value ~out}))))))
                (cond r-err
                      (return (cjson.encode {:id  ~id
                                             :key ~key
                                             :type "raw"
                                             :error (tostring r-err)
                                             :value (tostring ~out)}))
                      
                      :else
                      (return ret)))))))

(defn lua-tf-x-return-wrap
  ([[_ f encode-fn]]
   (h/$ (do (local out)
            (local '[o-ok o-err] (pcall (fn [] (:= out (~f)))))
            (cond o-err
                  (return (cjson.encode {:type "error"
                                         :value o-err}))
                  
                  :else
                  (return (~encode-fn out)))))))

(defn lua-tf-x-return-eval
  ([[_ s wrap-fn]]
   (h/$ (return (~wrap-fn
                 (fn []
                   (local load-fn (or loadstring load))
                   (local '[f err] (load-fn ~s))
                   (if err
                     (error err)
                     (return (f)))))))))

(def +lua-return+
  {:x-return-encode  {:macro #'lua-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'lua-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'lua-tf-x-return-eval     :emit :macro}})

;;
;; SOCKET
;;

(defn lua-tf-x-socket-connect
  ([[_ host port opts]]
   (h/$ (do* (local '[conn err])
             (local '[ok res] (pcall (fn:> [] (not= nil ngx))))
             (when (== ~host "host.docker.internal")
               (local handle (io.popen
                              (cat "ping host.docker.internal -c 1 -q 2>&1"
                                   " | "
                                   "grep -Po \"(\\d{1,3}\\.){3}\\d{1,3}\"")))
               
               (:= ~host (handle:read "*a"))
               (:= ~host (string.sub ~host 1 (- (len ~host) 1)))
               (handle:close))
             (if (and ok res)
               (do (:= conn (ngx.socket.tcp))
                   (:= '[res err]  (conn:connect ~host ~port)))
               (do (local socket (require "socket"))
                   (:= '[conn err] (socket.connect ~host ~port))))
             (return conn err)))))

(defn lua-tf-x-socket-send
  ([[_ conn s]]
   (h/$ (. ~conn (send ~s)))))

(defn lua-tf-x-socket-close
  ([[_ conn]]
   (h/$ (. ~conn (close)))))

(def +lua-socket+
  {:x-socket-connect      {:macro #'lua-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'lua-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'lua-tf-x-socket-close        :emit :macro}})


;;
;; ITER
;;

(defn lua-tf-x-iter-from-obj
  ([[_ obj]]
   (h/$ (coroutine.wrap
         (fn [] (for [k v :in (pairs ~obj)]
                  (coroutine.yield [k v])))))))

(defn lua-tf-x-iter-from-arr
  ([[_ arr]]
   (h/$ (coroutine.wrap
          (fn [] (for [_ v :in (ipairs ~arr)]
                   (coroutine.yield v)))))))

(defn lua-tf-x-iter-from
  ([[_ obj]]
   (h/$ (coroutine.wrap
         (fn [] (for [e :in (. ~obj ["iterator"])]
                  (coroutine.yield v)))))))

(defn lua-tf-x-iter-eq
  ([[_ it0 it1 eq-fn]]
   (h/$ (do (for [x0 :in ~it0]
              (var x1 (~it1))
              (when (not (~eq-fn x0 x1))
                (return false)))
            (return (== nil (~it1)))))))

(defn lua-tf-x-iter-next
  ([[_ it]]
   (list it)))

(defn lua-tf-x-iter-has?
  ([[_ obj]]
   (h/$ (and (== "table" (type ~obj))
             (== "function" (. '(~obj) ["iterator"]))))))

(defn lua-tf-x-iter-native?
  ([[_ it]]
   (list '== "function" (list 'type it))))

(def +lua-iter+
  {:x-iter-from-obj       {:macro #'lua-tf-x-iter-from-obj       :emit :macro}
   :x-iter-from-arr       {:macro #'lua-tf-x-iter-from-arr       :emit :macro}
   :x-iter-from           {:macro #'lua-tf-x-iter-from           :emit :macro}
   :x-iter-eq             {:macro #'lua-tf-x-iter-eq             :emit :macro}
   :x-iter-next           {:macro #'lua-tf-x-iter-next           :emit :macro}
   :x-iter-has?           {:macro #'lua-tf-x-iter-has?           :emit :macro}
   :x-iter-native?        {:macro #'lua-tf-x-iter-native?        :emit :macro}})

;;
;; CACHE
;;

(defn lua-tf-x-cache
  ([[_ key]]
   (list '. 'ngx.shared [(if (symbol? key)
                           key
                           (h/strn key))])))

(defn lua-tf-x-cache-list
  ([[_ cache]]
   (list '. cache '(get-keys 0))))

(defn lua-tf-x-cache-flush
  ([[_ cache]]
   (list '. cache '(flush-all))))

(defn lua-tf-x-cache-get
  ([[_ cache key]]
   (list '. cache (list 'get key))))

(defn lua-tf-x-cache-set
  ([[_ cache key val]]
   (list '. cache (list 'set key val))))

(defn lua-tf-x-cache-del
  ([[_ cache key]]
   (list '. cache (list 'delete key))))

(defn lua-tf-x-cache-incr
  ([[_ cache key num]]
   (list '. cache (list 'incr key num))))

(def +lua-cache+
  {:x-cache                 {:macro #'lua-tf-x-cache          :emit :macro}
   :x-cache-list            {:macro #'lua-tf-x-cache-list      :emit :macro}
   :x-cache-flush           {:macro #'lua-tf-x-cache-flush     :emit :macro}
   :x-cache-get             {:macro #'lua-tf-x-cache-get       :emit :macro}
   :x-cache-set             {:macro #'lua-tf-x-cache-set       :emit :macro}
   :x-cache-del             {:macro #'lua-tf-x-cache-del       :emit :macro}
   :x-cache-incr            {:macro #'lua-tf-x-cache-incr      :emit :macro}})


;;
;; THREAD
;;

(defn lua-tf-x-thread-spawn
  ([[_ thunk & [strategy]]]
   (case strategy
     :mock  (list 'coroutine.create thunk)
     (list 'ngx.thread.spawn  thunk))))

(defn lua-tf-x-thread-join
  ([[_ thread & [strategy]]]
   (case strategy
     :mock (list  'coroutine.resume thread)
     (list 'ngx.thread.wait thread))))

(defn lua-tf-x-with-delay
  ([[_ thunk ms]]
   (list 'return (list 'ngx.thread.spawn (list 'fn []
                                              (list 'ngx.sleep (list '/ ms 1000))
                                              (list 'var 'f := thunk)
                                              (list 'return (list 'f)))))))

(def +lua-thread+
  {:x-thread-spawn   {:macro #'lua-tf-x-thread-spawn  :emit :macro}
   :x-thread-join    {:macro #'lua-tf-x-thread-join   :emit :macro}
   :x-with-delay     {:macro #'lua-tf-x-with-delay    :emit :macro}})

;;
;; BASE 64
;;

(def +lua-b64+
  {:x-b64-decode     {:emit :alias :raw 'ngx.decode-base64}
   :x-b64-encode     {:emit :alias :raw 'ngx.encode-base64}})


;;
;; URI 
;;

(def +lua-uri+
  {:x-uri-decode     {:emit :alias :raw 'ngx.unescape-uri}
   :x-uri-encode     {:emit :alias :raw 'ngx.escape-uri}})


;;
;; FILE
;;

(defn lua-tf-x-slurp
  ([[_ filename]]))

(defn lua-tf-x-spit
  ([[_ filename s]]))

(def +lua-file+
  {:x-slurp          {:macro #'lua-tf-x-slurp         :emit :macro}
   :x-spit           {:macro #'lua-tf-x-spit          :emit :macro}})

;;
;; SPECIAL
;;

(def +lua-special+
  {})

(def +lua+
  (merge +lua-core+
         +lua-proto+
         +lua-global+
         +lua-custom+
         +lua-math+
         +lua-type+
         +lua-bit+
         +lua-lu+
         +lua-obj+
         +lua-arr+
         +lua-str+
         +lua-js+
         +lua-return+
         +lua-socket+
         +lua-iter+
         +lua-cache+
         +lua-thread+
         +lua-file+
         +lua-b64+
         +lua-uri+
         +lua-special+))
