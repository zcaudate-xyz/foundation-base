^{:no-test true}
(ns std.lang.model.spec-xtalk.fn-js
  (:require [std.lib :as h]))

;;
;; CORE
;;

(defn js-tf-x-len
  [[_ arr]]
  (list '. arr 'length))

(defn js-tf-x-cat
  [[_ & args]]
  (apply list '+ args))

(defn js-tf-x-apply
  [[_ f args]]
  (list '. f (list 'apply nil args)))

(defn js-tf-x-shell
  ([[_ s opts]]
   (h/$ (do (var p (require "child_process"))
            (p.exec ~s (fn [err res]
                         (if err
                           (if (. ~opts ["error"])
                             (return (. ~opts (error err))))
                           (if (. ~opts ["success"])
                             (return (. ~opts (success res)))))))
            (return ["async"])))))

(defn js-tf-x-random
  [_]
  '(Math.random))

(defn js-tf-x-type-native
  [[_ obj]]
  (h/$ (do (when (== ~obj nil)
             (return nil))
           (var t := (typeof ~obj))
           (if (== t "object")
             (cond (Array.isArray ~obj)
                   (return "array")
                   
                   :else
                   (do (var tn := (. ~obj ["constructor"] ["name"]))
                       (if (== tn "Object")
                         (return "object")
                         (return tn))))
             (return t)))))

(def +js-core+
  {:x-del            {:emit :alias :raw 'delete}
   :x-cat            {:macro #'js-tf-x-cat  :emit :macro :value true
                      :raw "(function (...args) {return args.join('')})"}
   :x-len            {:macro #'js-tf-x-len  :emit :macro}
   :x-err            {:emit :alias :raw 'throw}
   :x-eval           {:emit :alias :raw 'eval}
   :x-apply          {:macro #'js-tf-x-apply   :emit :macro}
   :x-unpack         {:emit :alias :raw :..}
   :x-print          {:emit :alias :raw 'console.log :value true}
   :x-random         {:emit :alias :raw 'Math.random :value true}
   :x-shell          {:macro #'js-tf-x-shell         :emit :macro}
   :x-now-ms         {:emit :alias :raw 'Date.now}
   :x-type-native    {:macro #'js-tf-x-type-native   :emit :macro}})

(defn js-tf-x-proto-get
  [[_ obj]]
  (list 'Object.getPrototypeOf obj))

(defn js-tf-x-proto-set
  [[_ obj prototype]]
  (list 'Object.setPrototypeOf obj prototype))

(defn js-tf-x-proto-create
  [[_ m]]
  (h/$
   (do (var out {})
       (for:object
        [[k f] ~m]
        (if (x:is-function? f)
          (:= (. out [k])
              (fn:> [...args]
                (f this ...args)))
          (:= (. out [k]) f)))
       (return out))))

(def +js-proto+
  {:x-this            {:emit :unit :default 'this}
   :x-proto-get       {:macro #'js-tf-x-proto-get     :emit :macro}
   :x-proto-set       {:macro #'js-tf-x-proto-set     :emit :macro}
   :x-proto-create    {:macro #'js-tf-x-proto-create  :emit :macro}
   :x-proto-tostring  {:emit :unit  :default "toString"}})


(def +js-global+
  {})

(def +js-custom+
  {:x-callback       {:emit :throw}})

;;
;; MATH
;;

(defn js-tf-x-m-max   [[_ & args]] (apply list 'Math.max args))
(defn js-tf-x-m-min   [[_ & args]] (apply list 'Math.min args))
(defn js-tf-x-m-mod   [[_ num denom]] (list 'mod num denom))
(defn js-tf-x-m-quot  [[_ num denom]] (list 'Math.floor (list '/  num denom)))

(def +js-math+
  {:x-m-abs           {:emit :alias :raw 'Math.abs  :value true}
   :x-m-acos          {:emit :alias :raw 'Math.acos :value true}
   :x-m-asin          {:emit :alias :raw 'Math.asin :value true}
   :x-m-atan          {:emit :alias :raw 'Math.atan :value true}
   :x-m-ceil          {:emit :alias :raw 'Math.ceil :value true}
   :x-m-cos           {:emit :alias :raw 'Math.cos  :value true}
   :x-m-cosh          {:emit :alias :raw 'Math.cosh :value true}
   :x-m-exp           {:emit :alias :raw 'Math.exp  :value true}
   :x-m-floor         {:emit :alias :raw 'Math.floor :value true}
   :x-m-loge          {:emit :alias :raw 'Math.log  :value true}
   :x-m-log10         {:emit :alias :raw 'Math.log10 :value true}
   :x-m-max           {:macro #'js-tf-x-m-max,      :raw 'Math.max :emit :macro :value true}
   :x-m-min           {:macro #'js-tf-x-m-min,      :raw 'Math.min :emit :macro :value true}
   :x-m-mod           {:macro #'js-tf-x-m-mod,      :emit :macro}
   :x-m-pow           {:emit :alias :raw 'Math.pow  :value true}
   :x-m-quot          {:macro #'js-tf-x-m-quot,                :emit :macro}
   :x-m-sin           {:emit :alias :raw 'Math.sin  :value true}
   :x-m-sinh          {:emit :alias :raw 'Math.sinh :value true}
   :x-m-sqrt          {:emit :alias :raw 'Math.sqrt :value true}
   :x-m-tan           {:emit :alias :raw 'Math.tan  :value true}
   :x-m-tanh          {:emit :alias :raw 'Math.tanh :value true}})

;;
;; TYPE
;;

(defn js-tf-x-is-string?
  [[_ e]]
  (list '== "string" (list 'typeof e)))

(defn js-tf-x-is-number?
  [[_ e]]
  (list '== "number" (list 'typeof e)))

(defn js-tf-x-is-integer?
  [[_ e]]
  (list 'Number.isInteger e))

(defn js-tf-x-is-boolean?
  [[_ e]]
  (list '== "boolean" (list 'typeof e)))

(defn js-tf-x-is-object?
  [[_ e]]
  (list 'and
        (list 'not= nil e)
        (list '== "object" (list 'typeof e))
        (list 'not (list 'Array.isArray e))))

(defn js-tf-x-is-function?
  [[_ e]]
  (list '== "function" (list 'typeof e)))

(def +js-type+
  {:x-to-string      {:emit :alias :raw 'String}
   :x-to-number      {:emit :alias :raw 'Number}
   :x-is-string?     {:macro #'js-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'js-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'js-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'js-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'js-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'js-tf-x-is-object? :emit :macro}
   :x-is-array?      {:emit :alias :raw 'Array.isArray}})

;;
;; LU
;;

(defn js-tf-x-lu-get
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj]]
   (h/$ (. ~lu (get ~obj)))))

(defn js-tf-x-lu-set
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj gid]]
   (h/$ (. ~lu (set ~obj ~gid)))))

(defn js-tf-x-lu-del
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj]]
   (h/$ (. ~lu (delete ~obj)))))

(def +js-lu+
  {:x-lu-create      {:default '(new WeakMap)}
   :x-lu-get         {:macro #'js-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'js-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'js-tf-x-lu-del :emit :macro}})

(defn js-tf-x-obj-keys
  [[_ obj]]
  (list 'return (list 'Object.keys obj)))

(defn js-tf-x-obj-vals
  [[_ obj]]
  (list 'return (list 'Object.values obj)))

(defn js-tf-x-obj-pairs
  "converts map to array"
  {:added "4.0"}
  ([[_ m]]
   (list 'return (list 'Object.entries m))))

(defn js-tf-x-obj-clone
  [[_ m]]
  (list 'return (list 'Object.assign {} m)))

(defn js-tf-x-obj-assign
  [[_ obj m]]
  (list 'return (list 'Object.assign obj m)))

(def +js-obj+
  {:x-obj-keys      {:macro #'js-tf-x-obj-keys    :emit :macro  :type :template}
   :x-obj-vals      {:macro #'js-tf-x-obj-vals    :emit :macro  :type :template}
   :x-obj-pairs     {:macro #'js-tf-x-obj-pairs   :emit :macro  :type :template}
   :x-obj-clone     {:macro #'js-tf-x-obj-clone   :emit :macro  :type :template}
   :x-obj-assign    {:macro #'js-tf-x-obj-assign  :emit :macro  :type :template}})

;;
;; ARR
;;


(defn js-tf-x-arr-slice
  [[_ arr start end]]
  (list '. arr (list 'slice start end)))

(defn js-tf-x-arr-reverse
  [[_ arr]]
  (list '. arr (list 'slice) (list 'reverse)))



(defn js-tf-x-arr-push
  [[_ arr item]]
  (list '. arr (list 'push item)))

(defn js-tf-x-arr-pop
  [[_ arr]]
  (list '. arr (list 'pop)))

(defn js-tf-x-arr-push-first
  [[_ arr item]]
  (list '. arr (list 'unshift item)))

(defn js-tf-x-arr-pop-first
  [[_ arr]]
  (list '. arr (list 'shift)))

(defn js-tf-x-arr-insert
  [[_ arr idx e]]
  (list '. arr (list 'splice idx 0 e)))

(defn js-tf-x-arr-remove
  [[_ arr idx]]
  (list '. arr (list 'splice idx 1)))

(defn js-tf-x-arr-sort
  [[_ arr key-fn comp-fn]]
  (list '. arr (list 'sort
                     (h/$ (fn [a b]
                            (return (:? (~comp-fn
                                         (~key-fn a)
                                         (~key-fn b))
                                        -1 1)))))))

(defn js-tf-x-arr-str-comp
  [[_ a b]]
  (list '> 0 (list '. a (list 'localeCompare b))))

(def +js-arr+
  {:x-arr-clone       {:emit :alias :raw 'Array.from}
   :x-arr-slice       {:macro #'js-tf-x-arr-slice      :emit :macro   :type :template}
   :x-arr-reverse     {:macro #'js-tf-x-arr-reverse    :emit :macro   :type :template}
   :x-arr-push        {:macro #'js-tf-x-arr-push       :emit :macro   :type :template}
   :x-arr-pop         {:macro #'js-tf-x-arr-pop        :emit :macro   :type :template}
   :x-arr-push-first  {:macro #'js-tf-x-arr-push-first :emit :macro   :type :template}
   :x-arr-pop-first   {:macro #'js-tf-x-arr-pop-first  :emit :macro   :type :template}
   :x-arr-remove      {:macro #'js-tf-x-arr-remove     :emit :macro   :type :template}
   :x-arr-insert      {:macro #'js-tf-x-arr-insert     :emit :macro   :type :template}
   :x-arr-sort        {:macro #'js-tf-x-arr-sort       :emit :macro}
   :x-arr-str-comp    {:macro #'js-tf-x-arr-str-comp   :emit :macro}})

;;
;; STRING
;;

(defn js-tf-x-str-char
  ([[_ s i]]
   (list '. s (list 'charCodeAt i))))

(defn js-tf-x-str-split
  ([[_ s tok]]
   (list '. s (list 'split tok))))

(defn js-tf-x-str-join
  ([[_ s arr]]
   (list '. arr (list 'join s))))

(defn js-tf-x-str-index-of
  ([[_ s tok]]
   (list '. s (list 'indexOf tok))))

(defn js-tf-x-str-substring
  ([[_ s start & args]]
   (list '. s (apply list 'substring start args))))

(defn js-tf-x-str-to-upper
  ([[_ s]]
   (list '. s '(toUpperCase))))

(defn js-tf-x-str-to-lower
  ([[_ s]]
   (list '. s '(toLowerCase))))

(defn js-tf-x-str-to-fixed
  ([[_ n digits]]
   (list '. n (list 'toFixed digits))))

(defn js-tf-x-str-replace
  ([[_ s tok replacement]]
   (list '. s (list 'replace (list 'new 'RegExp tok "g") replacement))))

(defn js-tf-x-str-trim
  ([[_ s]]
   (list '. s (list 'trim))))

(defn js-tf-x-str-trim-left
  ([[_ s]]
   (list '. s (list 'trimLeft))))

(defn js-tf-x-str-trim-right
  ([[_ s]]
   (list '. s (list 'trimRight))))

(def +js-str+
  {:x-str-char        {:macro #'js-tf-x-str-char       :emit :macro}
   :x-str-split       {:macro #'js-tf-x-str-split      :emit :macro}
   :x-str-join        {:macro #'js-tf-x-str-join       :emit :macro}
   :x-str-index-of    {:macro #'js-tf-x-str-index-of   :emit :macro}
   :x-str-substring   {:macro #'js-tf-x-str-substring  :emit :macro}
   :x-str-to-upper    {:macro #'js-tf-x-str-to-upper   :emit :macro}
   :x-str-to-lower    {:macro #'js-tf-x-str-to-lower   :emit :macro}
   :x-str-to-fixed    {:macro #'js-tf-x-str-to-fixed   :emit :macro}
   :x-str-replace     {:macro #'js-tf-x-str-replace    :emit :macro}
   :x-str-trim        {:macro #'js-tf-x-str-trim       :emit :macro}
   :x-str-trim-left   {:macro #'js-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right  {:macro #'js-tf-x-str-trim-right :emit :macro}})

;;
;; JSON
;;

(def +js-js+
  {:x-json-encode      {:emit :alias :raw 'JSON.stringify}
   :x-json-decode      {:emit :alias :raw 'JSON.parse}})

;;
;; COM
;;

(defn js-tf-x-return-encode
  ([[_ out id key]]
   (h/$ (do (var type-fn (fn [x]
                           (let [name (typeof x)]
                             (return (:? (== name "object") (:? x x.constructor.name name) name)))))
            (var tb (typeof ~out))
            (cond (== "function" tb)
                  (return (JSON.stringify {:id     ~id
                                           :key    ~key
                                           :type   "raw"
                                           :return "function"
                                           :value  (. ~out (toString))}))
                  
                  
                  (not= "object" tb)
                  (return (JSON.stringify {:id     ~id
                                           :key    ~key
                                           :type "data"
                                           :return tb
                                           :value ~out}))

                  (== nil ~out)
                  (return (JSON.stringify {:id     ~id
                                           :key    ~key
                                           :type "data"
                                           :return "nil"
                                           :value ~out}))
                  
                  :else
                  (do (var ts (type-fn ~out))
                      (try
                        (if (or (== ts "Object")
                                (== ts "Array"))
                          (return (JSON.stringify {:id     ~id
                                                   :key    ~key
                                                   :type "data"
                                                   :value ~out}))
                          (return (JSON.stringify {:id     ~id
                                                   :key    ~key
                                                   :type  "raw"
                                                   :return ts
                                                   :value (. ~out (toString))})))
                        (catch e (return (JSON.stringify {:id     id
                                                          :key    key
                                                          :type   "raw"
                                                          :return ts
                                                          :value (. ~out (toString))}))))))))))

(defn js-tf-x-return-wrap
  ([[_ f encode-fn]]
   (h/$ (try (var out := (~f))
             (return (~encode-fn  out))
             (catch e (let [err (:? (== "string" (typeof e)) e {:message (. e ["message"]) :stack (. e ["stack"])})]
                        (return (JSON.stringify {:type "error"
                                                 :value err}))))))))

(defn js-tf-x-return-eval
  ([[_ s wrap-fn]]
   (h/$ (return (~wrap-fn
                 (fn []
                   (return (eval ~s))))))))

(def +js-return+
  {:x-return-encode  {:macro #'js-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'js-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'js-tf-x-return-eval     :emit :macro}})

(defn js-tf-x-socket-connect
  ([[_ host port opts cb]]
   (h/$ (do* (var net (eval "require('net')"))
             (var rl  (eval  "require('readline')"))
             (var conn (new net.Socket))
             (return (conn.connect
                      port host (fn []
                                  (cb nil conn))))))))

(defn js-tf-x-socket-send
  ([[_ conn s]]
   (h/$ (. ~conn (write ~s)))))

(defn js-tf-x-socket-close
  ([[_ conn]]
   (h/$ (. ~conn (end)))))

(def +js-socket+
  {:x-socket-connect      {:macro #'js-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'js-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'js-tf-x-socket-close        :emit :macro}})

;;
;; ITER
;;

(defn js-tf-x-iter-from-obj
  ([[_ obj]]
   (list (list '. (list 'Object.entries obj) '[Symbol.iterator]))))

(defn js-tf-x-iter-from-arr
  ([[_ arr]]
   (list (list '. arr '[Symbol.iterator]))))

(defn js-tf-x-iter-from
  ([[_ obj]]
   (list (list '. obj '[Symbol.iterator]))))

(defn js-tf-x-iter-eq
  ([[_ it0 it1 eq-fn]]
   (h/$ (do (for [x0 :of ~it0]
              (var r1 (. ~it1 (next)))
              (cond (. r1 done)
                    (return false)

                    (not (~eq-fn x0 (. r1 value)))
                    (return false)))
            (return (. ~it1 (next) done))))))

(defn js-tf-x-iter-next
  ([[_ it]]
   (list '. it (list 'next))))

(defn js-tf-x-iter-has?
  ([[_ obj]]
   (list 'not= nil (list '. obj '[Symbol.iterator]))))

(defn js-tf-x-iter-native?
  ([[_ it]]
   (list '== "function" (list 'typeof (list '. it ["next"])))))

(def +js-iter+
  {:x-iter-from-obj    {:macro #'js-tf-x-iter-from-obj       :emit :macro}
   :x-iter-from-arr    {:macro #'js-tf-x-iter-from-arr       :emit :macro}
   :x-iter-from        {:macro #'js-tf-x-iter-from           :emit :macro}
   :x-iter-eq          {:macro #'js-tf-x-iter-eq             :emit :macro}
   :x-iter-next        {:macro #'js-tf-x-iter-next           :emit :macro}
   :x-iter-has?        {:macro #'js-tf-x-iter-has?           :emit :macro}
   :x-iter-native?     {:macro #'js-tf-x-iter-native?        :emit :macro}})

;;
;; CACHE
;;

(defn js-tf-x-cache
  ([[_ name]]
   (if (= (h/strn name) "GLOBAL")
     'window.localStorage
     'window.sessionStorage)))

(defn js-tf-x-cache-list
  ([[_ cache]]
   (list 'or
         (list '. cache ["_keys"])
         (list 'Object.keys cache))))

(defn js-tf-x-cache-flush
  ([[_ cache]]
   (list '. cache '(clear))))

(defn js-tf-x-cache-get
  ([[_ cache key]]
   (list '. cache (list 'getItem key))))

(defn js-tf-x-cache-set
  ([[_ cache key val]]
   (list '. cache (list 'setItem key val))))

(defn js-tf-x-cache-del
  ([[_ cache key]]
   (list '. cache (list 'removeItem key))))

(defn js-tf-x-cache-incr
  ([[_ cache key num]]
   (h/$ (do:> (var prev (Number (. ~cache (getItem ~key))))
              (var curr (+ prev ~num))
              (. ~cache (setItem ~key curr))
              (return curr)))))

(def +js-cache+
  {:x-cache                 {:macro #'js-tf-x-cache           :emit :macro}
   :x-cache-flush           {:macro #'js-tf-x-cache-flush     :emit :macro}
   :x-cache-list            {:macro #'js-tf-x-cache-list      :emit :macro}
   :x-cache-get             {:macro #'js-tf-x-cache-get       :emit :macro}
   :x-cache-set             {:macro #'js-tf-x-cache-set       :emit :macro}
   :x-cache-del             {:macro #'js-tf-x-cache-del       :emit :macro}
   :x-cache-incr            {:macro #'js-tf-x-cache-incr      :emit :macro}})

;;
;; FILE
;;

(defn js-tf-x-slurp
  ([[_ filename]]))

(defn js-tf-x-spit
  ([[_ filename s]]))

(def +js-file+
  {:x-slurp          {:macro #'js-tf-x-slurp         :emit :macro}
   :x-spit           {:macro #'js-tf-x-spit          :emit :macro}})

;;
;; THREAD
;;

(defn js-tf-x-thread-spawn
  ([[_ thunk]]
   (h/$ (new Promise (fn [resolve reject]
                       (resolve (~thunk)))))))

(defn js-tf-x-thread-join
  ([[_ thread]]
   '(x:error "Thread join not Supported")))

(defn js-tf-x-with-delay
  ([[_ thunk ms]]
   (h/$ (setTimeout (fn []
                      (new Promise (fn [resolve reject]
                                     (resolve (~thunk)))))
                    ~ms))))

(defn js-tf-x-start-interval
  ([[_ thunk ms]]
   (h/$ (setInterval (fn []
                      (new Promise (fn [resolve reject]
                                     (resolve (~thunk)))))
                    ~ms))))

(defn js-tf-x-stop-interval
  ([[_ instance]]
   (h/$ (clearInterval instance))))

(def +js-thread+
  {:x-thread-spawn   {:macro #'js-tf-x-thread-spawn   :emit :macro}
   :x-thread-join    {:macro #'js-tf-x-thread-join    :emit :macro}
   :x-with-delay     {:macro #'js-tf-x-with-delay     :emit :macro}
   :x-start-interval {:macro #'js-tf-x-start-interval :emit :macro}
   :x-stop-interval  {:macro #'js-tf-x-stop-interval  :emit :macro}})

(def +js-b64+
  {:x-b64-decode     {:emit :alias :raw 'atob}
   :x-b64-encode     {:emit :alias :raw 'btoa}})

(def +js-uri+
  {:x-uri-decode     {:emit :alias :raw 'decodeURIComponent}
   :x-uri-encode     {:emit :alias :raw 'encodeURIComponent}})

(defn js-tf-x-notify-http
  ([[_ host port value id key opts]]
   (h/$ (try
          (var #{path scheme} (or ~opts {}))
          (fetch (+ (or scheme "http") "://" ~host ":" ~port "/" (or path ""))
                 {:method "POST"
                  :body (xt.lang.base-repl/return-encode ~value ~id ~key)})
          (return ["async"])
          (catch e (return ["unable to connect"]))))))

(def +js-special+
  {:x-notify-http   {:macro #'js-tf-x-notify-http    :emit :macro   :type :template}})

(def +js+
  (merge +js-core+
         +js-proto+
         +js-global+
         +js-custom+
         +js-math+
         +js-type+
         +js-lu+
         +js-obj+
         +js-arr+
         +js-str+
         +js-js+
         +js-return+
         +js-socket+
         +js-iter+
         +js-cache+
         +js-thread+
         +js-file+
         +js-b64+
         +js-uri+
         +js-special+))
