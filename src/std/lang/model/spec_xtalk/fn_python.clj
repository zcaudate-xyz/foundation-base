(ns std.lang.model.spec-xtalk.fn-python
  (:require [std.lib.template :as template]))

(defn python-tf-x-del
  [[_ obj]]
  (list 'del obj))

(defn python-tf-x-cat
  [[_ & args]]
  (apply list '+ args))

(defn python-tf-x-len
  [[_ arr]]
  (list 'len arr))

(defn python-tf-x-get-key
  [[_ obj key default]]
  (let [val (list '. obj (list 'get key))]
    (if default
      (list 'or val default)
      val)))

(defn python-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list '== check (list 'x:get-key obj key nil))
    (list 'not= nil (list '. obj (list 'get key)))))

(defn python-tf-x-err
  [[_ msg]]
  (list 'throw (list 'Exception msg)))

(defn python-tf-x-eval
  [[_ s]]
  (list 'eval s))

(defn python-tf-x-apply
  [[_ f args]]
  (list f (list :* args)))

(defn python-tf-x-random
  [_]
  '(. (__import__ "random")
      (random)))

(defn python-tf-x-print
  ([[_ & args]]
   (apply list 'print args)))

(defn python-tf-x-shell
  ([[_ s cm]]
   (template/$ (do (var res (. (__import__ "os") (system ~s)))
            (var f (. ~cm (get "success")))
            (if f
              (return (f res))
              (return res))))))

(defn python-tf-x-type-native
  [[_ obj]]
  (template/$ (cond (isinstance ~obj '(dict))
             (return "object")
             
             (isinstance ~obj '(list))
             (return "array")
             
             (callable ~obj)
             (return "function")

             (== bool (type ~obj))
             (return "boolean")

             (isinstance ~obj '(int float))
             (return "number")
             
             (isinstance ~obj '(str))
             (return "string")
             
             :else
             (return (str (type ~obj))))))

(defn python-tf-x-future-run
  [[_ thunk]]
  (template/$
   (do (var task {"status" "pending"
                  "value" nil
                  "error" nil})
       (try (:= out (~thunk))
            (:= (. task ["status"]) "ok")
            (:= (. task ["value"]) out)
            (catch [Exception :as e]
                (:= (. task ["status"]) "error")
                (:= (. task ["error"]) e)))
       (return task))))

(defn python-tf-x-future-then
  [[_ task on-ok]]
  (template/$
   (if (== "ok" (. ~task (get "status")))
     (do (var out {"status" "pending"
                   "value" nil
                   "error" nil})
         (var v nil)
         (try (:= v (~on-ok (. ~task (get "value"))))
              (:= (. out ["status"]) "ok")
              (:= (. out ["value"]) v)
              (catch [Exception :as e]
                  (:= (. out ["status"]) "error")
                  (:= (. out ["error"]) e)))
         (return out))
     (return ~task))))

(defn python-tf-x-future-catch
  [[_ task on-err]]
  (template/$
   (if (== "error" (. ~task (get "status")))
     (do (var out {"status" "pending"
                   "value" nil
                   "error" nil})
         (var v nil)
         (try (:= v (~on-err (. ~task (get "error"))))
              (:= (. out ["status"]) "ok")
              (:= (. out ["value"]) v)
              (catch [Exception :as e]
                  (:= (. out ["status"]) "error")
                  (:= (. out ["error"]) e)))
         (return out))
     (return ~task))))

(defn python-tf-x-future-finally
  [[_ task on-done]]
  (template/$ (do (~on-done)
            (return ~task))))

(defn python-tf-x-future-cancel
  [[_ task]]
  (template/$ (do (:= (. ~task ["status"]) "cancelled")
            (return ~task))))

(defn python-tf-x-future-status
  [[_ task]]
  (template/$ (. ~task (get "status"))))

(defn python-tf-x-future-await
  [[_ task timeout-ms default]]
  (template/$
   (cond (== "ok" (. ~task (get "status")))
         (return (. ~task (get "value")))
         
         (== "error" (. ~task (get "status")))
         (throw (. ~task (get "error")))
         
         :else
         (return ~default))))

(defn python-tf-x-future-from-async
  [[_ executor]]
  (template/$
   (do (var box {"ok" false
                 "value" nil
                 "error" nil})
       (fn resolve [v]
         (:= (. box ["ok"]) true)
         (:= (. box ["value"]) v))
       (fn reject [e]
         (:= (. box ["error"]) e))
       (~executor resolve reject)
       (if (. box ["ok"])
         (return {"status" "ok"
                  "value" (. box ["value"])
                  "error" nil})
         (return {"status" "error"
                  "value" nil
                  "error" (. box ["error"])})))))

(def +python-core+
  {:x-del            {:macro #'python-tf-x-del    :emit :macro}
   :x-cat            {:macro #'python-tf-x-cat    :emit :macro}
   :x-len            {:macro #'python-tf-x-len    :emit :macro}
   :x-err            {:macro #'python-tf-x-err     :emit :macro}
   :x-eval           {:macro #'python-tf-x-eval    :emit :macro}
   :x-apply          {:macro #'python-tf-x-apply   :emit :macro}
   :x-unpack         {:raw :*  :emit :alias}
   :x-random         {:macro #'python-tf-x-random  :emit :macro}
   :x-print          {:macro #'python-tf-x-print         :emit :macro}
   :x-shell          {:macro #'python-tf-x-shell         :emit :macro}
   :x-now-ms         {:default '(round (* 1000 (. (__import__ "time") (time)))) :emit :unit}
   :x-type-native    {:macro #'python-tf-x-type-native   :emit :macro}
   :x-future-run       {:macro #'python-tf-x-future-run      :emit :macro}
   :x-future-then      {:macro #'python-tf-x-future-then     :emit :macro}
   :x-future-catch     {:macro #'python-tf-x-future-catch    :emit :macro}
   :x-future-finally   {:macro #'python-tf-x-future-finally  :emit :macro}
   :x-future-cancel    {:macro #'python-tf-x-future-cancel   :emit :macro}
   :x-future-status    {:macro #'python-tf-x-future-status   :emit :macro}
    :x-future-await     {:macro #'python-tf-x-future-await    :emit :macro}
    :x-future-from-async {:macro #'python-tf-x-future-from-async :emit :macro}})

;;
;; PROTO
;;

(defn python-tf-x-proto-create
  [[_ m]]
  m)

(defn python-tf-x-proto-get
  [[_ obj _]]
  (template/$ (. ~obj (get "__proto__"))))

(defn python-tf-x-proto-set
  [[_ obj prototype _]]
  (template/$
   (do (:= (. ~obj ["__proto__"]) ~prototype)
       (for:object [[k f] ~prototype]
         (if (callable f)
           (:= (. ~obj [k]) (. (__import__ "types") (MethodType f ~obj)))
           (:= (. ~obj [k]) f)))
       (return ~obj))))

(defn python-tf-x-proto-tostring
  [[_ _]]
  '"__str__")

(def +python-proto+
  {:x-this           {:emit :unit :default 'self}
   :x-proto-create   {:macro #'python-tf-x-proto-create   :emit :macro}
   :x-proto-get      {:macro #'python-tf-x-proto-get      :emit :macro}
   :x-proto-set      {:macro #'python-tf-x-proto-set      :emit :macro}
   :x-proto-tostring {:macro #'python-tf-x-proto-tostring :emit :macro}})


;;
;; GLOBAL
;;

(def +python-global+
  {})

;;
;; CUSTOM
;;

(def +python-custom+
  {:x-get-key        {:macro #'python-tf-x-get-key}
   :x-has-key?       {:macro #'python-tf-x-has-key? :emit :macro}})

;;
;; MATH
;;


(defn python-tf-x-m-abs   [[_ num]] (list 'abs num))
(defn python-tf-x-m-acos  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'acos num)))
(defn python-tf-x-m-asin  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'asin num)))
(defn python-tf-x-m-atan  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'atan num)))
(defn python-tf-x-m-ceil  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'ceil num)))
(defn python-tf-x-m-cos   [[_ num]] (list '. (list '__import__ "math")
                                          (list 'cos num)))
(defn python-tf-x-m-cosh  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'cosh num)))
(defn python-tf-x-m-exp   [[_ num]] (list '. (list '__import__ "math")
                                          (list 'exp num)))
(defn python-tf-x-m-floor [[_ num]] (list '. (list '__import__ "math")
                                          (list 'floor num)))
(defn python-tf-x-m-loge  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'log num)))
(defn python-tf-x-m-log10 [[_ num]] (list '. (list '__import__ "math")
                                          (list 'log10 num)))
(defn python-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn python-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn python-tf-x-m-mod   [[_ num denom]] (list :% num (list :- " % ") denom))
(defn python-tf-x-m-quot  [[_ num denom]] (list :% num (list :- " // ") denom))
(defn python-tf-x-m-pow   [[_ base n]] (list 'pow base n))
(defn python-tf-x-m-sin   [[_ num]] (list '. (list '__import__ "math")
                                          (list 'sin num)))
(defn python-tf-x-m-sinh  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'sinh num)))
(defn python-tf-x-m-sqrt  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'sqrt num)))
(defn python-tf-x-m-tan   [[_ num]] (list '. (list '__import__ "math")
                                          (list 'tan num)))
(defn python-tf-x-m-tanh  [[_ num]] (list '. (list '__import__ "math")
                                          (list 'tanh num)))

(def +python-math+
  {:x-m-abs           {:macro #'python-tf-x-m-abs,                 :emit :macro}
   :x-m-acos          {:macro #'python-tf-x-m-acos,                :emit :macro}
   :x-m-asin          {:macro #'python-tf-x-m-asin,                :emit :macro}
   :x-m-atan          {:macro #'python-tf-x-m-atan,                :emit :macro}
   :x-m-ceil          {:macro #'python-tf-x-m-ceil,                :emit :macro}
   :x-m-cos           {:macro #'python-tf-x-m-cos,                 :emit :macro}
   :x-m-cosh          {:macro #'python-tf-x-m-cosh,                :emit :macro}
   :x-m-exp           {:macro #'python-tf-x-m-exp,                 :emit :macro}
   :x-m-floor         {:macro #'python-tf-x-m-floor,               :emit :macro}
   :x-m-loge          {:macro #'python-tf-x-m-loge,                :emit :macro}
   :x-m-log10         {:macro #'python-tf-x-m-log10,               :emit :macro}
   :x-m-max           {:macro #'python-tf-x-m-max,                 :emit :macro}
   :x-m-min           {:macro #'python-tf-x-m-min,                 :emit :macro}
   :x-m-mod           {:macro #'python-tf-x-m-mod,                 :emit :macro}
   :x-m-pow           {:macro #'python-tf-x-m-pow,                 :emit :macro}
   :x-m-quot          {:macro #'python-tf-x-m-quot,                :emit :macro}
   :x-m-sin           {:macro #'python-tf-x-m-sin,                 :emit :macro}
   :x-m-sinh          {:macro #'python-tf-x-m-sinh,                :emit :macro}
   :x-m-sqrt          {:macro #'python-tf-x-m-sqrt,                :emit :macro}
   :x-m-tan           {:macro #'python-tf-x-m-tan,                 :emit :macro}
   :x-m-tanh          {:macro #'python-tf-x-m-tanh,                :emit :macro}})

;;
;; TYPE
;;

(defn python-tf-x-to-string
  [[_ e]]
  (list 'str e))

(defn python-tf-x-to-number
  [[_ e]]
  (list 'float e))

(defn python-tf-x-is-string?
  [[_ e]]
  (list 'isinstance e 'str))

(defn python-tf-x-is-number?
  [[_ e]]
  (list 'isinstance e ''(int float)))

(defn python-tf-x-is-integer?
  [[_ e]]
  (list 'isinstance e ''(int)))

(defn python-tf-x-is-boolean?
  [[_ e]]
  (list '== 'bool (list 'type e)))

(defn python-tf-x-is-function?
  [[_ e]]
  (list 'callable e))

(defn python-tf-x-is-object?
  [[_ e]]
  (list 'isinstance e 'dict))

(defn python-tf-x-is-array?
  [[_ e]]
  (list 'isinstance e 'list))

(def +python-type+
  {:x-to-string      {:macro #'python-tf-x-to-string :emit :macro}
   :x-to-number      {:macro #'python-tf-x-to-number :emit :macro}
   :x-is-string?     {:macro #'python-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'python-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'python-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'python-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'python-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'python-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'python-tf-x-is-array? :emit :macro}})

;;
;; LU
;;



(defn python-tf-x-lu-create
  "converts map to array"
  {:added "4.0"}
  ([[_]]
   '{}))

(defn python-tf-x-lu-eq
  "converts map to array"
  {:added "4.0"}
  ([[_ o1 o2]]
   (list '== (list 'id o1) (list 'id o2))))

(defn python-tf-x-lu-get
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj]]
   (template/$ (. ~lu (get (id ~obj))))))

(defn python-tf-x-lu-set
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj gid]]
   (template/$ (:= (. ~lu [(id ~obj)]) ~gid))))

(defn python-tf-x-lu-del
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj]]
   (template/$ (del (. ~lu [(id ~obj)])))))

(def +python-lu+
  {:x-lu-create      {:macro #'python-tf-x-lu-create  :emit :macro}
    :x-lu-eq          {:macro #'python-tf-x-lu-eq  :emit :macro}
    :x-lu-get         {:macro #'python-tf-x-lu-get :emit :macro}
    :x-lu-set         {:macro #'python-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'python-tf-x-lu-del :emit :macro}})

;;
;; OBJ
;;

(defn python-tf-x-obj-keys
  [[_ obj]]
  (list 'list (list '. obj '(keys))))

(defn python-tf-x-obj-vals
  [[_ obj]]
  (list 'list (list '. obj '(values))))

(defn python-tf-x-obj-pairs
  "converts map to array"
  {:added "4.0"}
  ([[_ obj]]
   (list 'list (list '. obj '(items)))))

(defn python-tf-x-obj-clone
  [[_ obj]]
  (list '. obj '(copy)))

(def +python-obj+
  {:x-obj-keys    {:macro #'python-tf-x-obj-keys   :emit :macro}
   :x-obj-vals    {:macro #'python-tf-x-obj-vals   :emit :macro}
   :x-obj-pairs   {:macro #'python-tf-x-obj-pairs  :emit :macro}
   :x-obj-clone   {:macro #'python-tf-x-obj-clone  :emit :macro}})

;;
;; ARR
;;

(defn python-tf-x-arr-clone
    [[_ arr]]
    (list '. arr [(list :- ":")]))

  (defn python-tf-x-arr-slice
    [[_ arr start end]]
    (list '. arr [(list :to (list '- start (list 'x:offset))
                         end)]))



(defn python-tf-x-arr-push
  [[_ arr item]]
  (list '. arr (list 'append item)))

(defn python-tf-x-arr-pop
  [[_ arr]]
  (list '. arr (list 'pop)))

(defn python-tf-x-arr-reverse
  [[_ arr]]
  (list 'list (list 'reversed arr)))

(defn python-tf-x-arr-push-first
  [[_ arr item]]
  (list '. arr (list 'insert 0 item)))

(defn python-tf-x-arr-pop-first
  [[_ arr]]
  (list '. arr (list 'pop 0)))

(defn python-tf-x-arr-insert
  [[_ arr idx e]]
  (list '. arr (list 'insert idx e)))

(defn python-tf-x-arr-remove
  [[_ arr idx]]
  (list '. arr (list 'pop idx)))

(defn python-tf-x-arr-sort
  [[_ arr key-fn compare-fn]]
  (list '. arr (list 'sort :key #_key-fn
                     (template/$ (. (__import__ "functools")
                              (cmp_to_key
                               (fn:> [a b]
                                     (:? (~compare-fn
                                          (~key-fn a)
                                          (~key-fn b))
                                         -1 1))))))))

(defn python-tf-x-str-comp
  [[_ a b]]
  (list '< a b))

(def +python-arr+
  {:x-arr-clone       {:macro #'python-tf-x-arr-clone      :emit :macro :type :template}
   :x-arr-slice       {:macro #'python-tf-x-arr-slice      :emit :macro :type :template}
   :x-arr-reverse     {:macro #'python-tf-x-arr-reverse    :emit :macro :type :template}
   :x-arr-push        {:macro #'python-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'python-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'python-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'python-tf-x-arr-pop-first  :emit :macro}
   :x-arr-insert      {:macro #'python-tf-x-arr-insert     :emit :macro}
   :x-arr-remove      {:macro #'python-tf-x-arr-remove     :emit :macro}
   :x-arr-sort        {:macro #'python-tf-x-arr-sort       :emit :macro}
   :x-str-comp        {:macro #'python-tf-x-str-comp       :emit :macro}})


;;
;; STRING
;;

(defn python-tf-x-str-char
  ([[_ s i]]
   (list 'ord (list '. s [(list '- i (list 'x:offset))]))))

(defn python-tf-x-str-split
  ([[_ s tok]]
   (list '. s (list 'split tok))))

(defn python-tf-x-str-join
  ([[_ s arr]]
   (list '. s (list 'join arr))))

(defn python-tf-x-str-index-of
  ([[_ s tok & [start]]]
   (list '+ (list '. s (list 'find tok (or start 0)))
         (list 'x:offset))))

(defn python-tf-x-str-to-fixed
  [[_ num digits]]
  (list '. (list 'x:cat "{:." (list 'str digits) "f}")
        (list 'format num)))

(defn python-tf-x-str-substring
  ([[_ s start & [end]]]
   (template/$ (. ~s [~(list :to (list '- start (list 'x:offset))
                             (or end \0))]))))

(defn python-tf-x-str-to-upper
  ([[_ s]]
   (list '. s '(upper))))

(defn python-tf-x-str-to-lower
  ([[_ s]]
   (list '. s '(lower))))

(defn python-tf-x-str-replace
  ([[_ s tok replacement]]
   (list '. s (list 'replace tok replacement))))

(defn python-tf-x-str-trim
  ([[_ s]]
   (list '. s '(strip))))

(defn python-tf-x-str-trim-left
  ([[_ s]]
   (list '. s '(lstrip))))

(defn python-tf-x-str-trim-right
  ([[_ s]]
   (list '. s '(rstrip))))

(def +python-str+
  {:x-str-char       {:macro #'python-tf-x-str-char      :emit :macro}
   :x-str-split      {:macro #'python-tf-x-str-split      :emit :macro}
   :x-str-join       {:macro #'python-tf-x-str-join       :emit :macro}
   :x-str-index-of   {:macro #'python-tf-x-str-index-of   :emit :macro}
   :x-str-to-fixed   {:macro #'python-tf-x-str-to-fixed   :emit :macro}
   :x-str-substring  {:macro #'python-tf-x-str-substring  :emit :macro}
   :x-str-to-upper   {:macro #'python-tf-x-str-to-upper      :emit :macro}
   :x-str-to-lower   {:macro #'python-tf-x-str-to-lower      :emit :macro}
   :x-str-replace    {:macro #'python-tf-x-str-replace    :emit :macro}
   :x-str-trim       {:macro #'python-tf-x-str-trim       :emit :macro}
   :x-str-trim-left  {:macro #'python-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right {:macro #'python-tf-x-str-trim-right :emit :macro}})

;;
;; JSON
;;

(defn python-tf-x-json-encode
  ([[_ obj]]
   (list '. (list '__import__ "json")
         (list 'dumps obj))))

(defn python-tf-x-json-decode
  ([[_ s]]
   (list '. (list '__import__ "json")
         (list 'loads s))))

(def +python-js+
  {:x-json-encode      {:macro #'python-tf-x-json-encode      :emit :macro}
   :x-json-decode      {:macro #'python-tf-x-json-decode      :emit :macro}})

;;
;; RETURN
;;

(defn python-tf-x-return-encode
  ([[_ out id key]]
   (template/$ (do (:- :import json)
            (try
              (return (json.dumps {:id  ~id
                                   :key ~key
                                   :type  "data"
                                   :value  ~out}))
              (catch Exception
                  (return (json.dumps {:id ~id
                                       :key ~key
                                       :type  "raw"
                                       :value (str ~out)}))))))))

(defn python-tf-x-return-wrap
  ([[_ f encode-fn]]
   (template/$ (do (:- :import json)
            (try (:= out (~f))
                 (catch [Exception :as e]
                     (return (json.dumps {:type "error"
                                          :value (str e)}))))
            (return (~encode-fn out nil nil))))))

(defn python-tf-x-return-eval
  ([[_ s wrap-fn]]
   (template/$ (do (fn thunk []
              (let [g   (globals)]
                (exec ~s g g)
                (return (g.get "OUT"))))
            (return (~wrap-fn thunk))))))

(def +python-return+
  {:x-return-encode  {:macro #'python-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'python-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'python-tf-x-return-eval     :emit :macro}})

;;
;; ITER
;;

(defn python-tf-x-socket-connect
  ([[_ host port opts]]
   (template/$ (do (:- :import socket)
            (var conn   (socket.socket))
            (conn.connect '(host port))
            (return conn)))))

(defn python-tf-x-socket-send
  ([[_ conn s]]
   (template/$ (. ~conn (sendall (. ~s (encode)))))))

(defn python-tf-x-socket-close
  ([[_ conn]]
   (template/$ (. ~conn (close)))))

(def +python-socket+
  {:x-socket-connect      {:macro #'python-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'python-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'python-tf-x-socket-close        :emit :macro}})


;;
;; ITER
;;

(defn python-tf-x-iter-from-obj
  ([[_ obj]]
   (list 'iter (list '. obj '(items)))))

(defn python-tf-x-iter-from-arr
  ([[_ arr]]
   (list 'iter arr)))

(defn python-tf-x-iter-from
  ([[_ x]]
   (list 'iter x)))

(defn python-tf-x-iter-eq
  ([[_ it0 it1 eq-fn]]
   (template/$ (do (for [x0 :in ~it0]
              (try
                (var x1 (next ~it1))
                (if (not (~eq-fn x0 x1))
                  (return false))
                (catch StopIteration (return false))))
            (try
              (next ~it1)
              (return false)
              (catch StopIteration (return true)))))))

(defn python-tf-x-iter-next
  ([[_ it]]
   (list 'next it)))

(defn python-tf-x-iter-has?
  ([[_ x]]
   (list 'hasattr x "__iter__")))

(defn python-tf-x-iter-native?
  ([[_ it]]
   (list 'hasattr it "__next__")))

(def +python-iter+
  {:x-iter-from-obj       {:macro #'python-tf-x-iter-from-obj       :emit :macro}
   :x-iter-from-arr       {:macro #'python-tf-x-iter-from-arr       :emit :macro}
   :x-iter-from           {:macro #'python-tf-x-iter-from           :emit :macro}
   :x-iter-eq             {:macro #'python-tf-x-iter-eq             :emit :macro}
   :x-iter-null           {:default '(if false (yield)) :emit :unit}
   :x-iter-next           {:macro #'python-tf-x-iter-next           :emit :macro}
   :x-iter-has?           {:macro #'python-tf-x-iter-has?           :emit :macro}
   :x-iter-native?        {:macro #'python-tf-x-iter-native?        :emit :macro}})

;;
;; CACHE
;;

(defn python-cache-name
  [cache]
  (or cache "__GLOBAL__"))

(defn python-global-cache-root
  []
  (list '. '(globals) ["__xtalk_cache__"]))

(defn python-global-cache-bucket
  [cache]
  (list '. (python-global-cache-root) [(python-cache-name cache)]))

(defn python-tf-x-cache
  ([[_ name]]
   (template/$ (do (var g (globals))
                   (if (not (. g (get "__xtalk_cache__")))
                     (:= (. g ["__xtalk_cache__"]) {}))
                   (if (not (. (. g ["__xtalk_cache__"]) (get ~name)))
                     (:= (. (. g ["__xtalk_cache__"]) [~name]) {}))
                   (return ~name)))))

(defn python-tf-x-cache-list
  ([[_ cache]]
   (template/$ (do (var g (globals))
                   (if (not (. g (get "__xtalk_cache__")))
                     (:= (. g ["__xtalk_cache__"]) {}))
                   (if (not (. (. g ["__xtalk_cache__"]) (get ~(python-cache-name cache))))
                     (:= (. (. g ["__xtalk_cache__"]) [~(python-cache-name cache)]) {}))
                   (return (list (. ~(python-global-cache-bucket cache) (keys))))))))

(defn python-tf-x-cache-flush
  ([[_ cache]]
   (template/$ (do (var g (globals))
                   (if (not (. g (get "__xtalk_cache__")))
                     (:= (. g ["__xtalk_cache__"]) {}))
                   (:= (. (. g ["__xtalk_cache__"]) [~(python-cache-name cache)]) {})
                   (return ~(python-global-cache-bucket cache))))))

(defn python-tf-x-cache-get
  ([[_ cache key]]
   (template/$ (do (var g (globals))
                   (if (not (. g (get "__xtalk_cache__")))
                     (:= (. g ["__xtalk_cache__"]) {}))
                   (if (not (. (. g ["__xtalk_cache__"]) (get ~(python-cache-name cache))))
                     (:= (. (. g ["__xtalk_cache__"]) [~(python-cache-name cache)]) {}))
                   (return (. ~(python-global-cache-bucket cache) (get ~key)))))))

(defn python-tf-x-cache-set
  ([[_ cache key val]]
   (template/$ (do (var g (globals))
                   (if (not (. g (get "__xtalk_cache__")))
                     (:= (. g ["__xtalk_cache__"]) {}))
                   (if (not (. (. g ["__xtalk_cache__"]) (get ~(python-cache-name cache))))
                     (:= (. (. g ["__xtalk_cache__"]) [~(python-cache-name cache)]) {}))
                   (:= (. ~(python-global-cache-bucket cache) [~key]) ~val)
                   (return ~val)))))

(defn python-tf-x-cache-del
  ([[_ cache key]]
   (template/$ (do (var g (globals))
                   (if (not (. g (get "__xtalk_cache__")))
                     (:= (. g ["__xtalk_cache__"]) {}))
                   (if (not (. (. g ["__xtalk_cache__"]) (get ~(python-cache-name cache))))
                     (:= (. (. g ["__xtalk_cache__"]) [~(python-cache-name cache)]) {}))
                   (. ~(python-global-cache-bucket cache) (pop ~key nil))
                   (return true)))))

(defn python-tf-x-cache-incr
  ([[_ cache key amount]]
   (template/$ (do (var g (globals))
                   (if (not (. g (get "__xtalk_cache__")))
                     (:= (. g ["__xtalk_cache__"]) {}))
                   (if (not (. (. g ["__xtalk_cache__"]) (get ~(python-cache-name cache))))
                     (:= (. (. g ["__xtalk_cache__"]) [~(python-cache-name cache)]) {}))
                   (var prev (or (. ~(python-global-cache-bucket cache) (get ~key))
                                 0))
                   (var curr (+ prev ~amount))
                   (:= (. ~(python-global-cache-bucket cache) [~key]) curr)
                   (return curr)))))

(def +python-cache+
  {:x-cache                 {:macro #'python-tf-x-cache        :emit :macro}
   :x-cache-list            {:macro #'python-tf-x-cache-list   :emit :macro}
   :x-cache-flush           {:macro #'python-tf-x-cache-flush  :emit :macro}
   :x-cache-get             {:macro #'python-tf-x-cache-get    :emit :macro}
   :x-cache-set             {:macro #'python-tf-x-cache-set    :emit :macro}
   :x-cache-del             {:macro #'python-tf-x-cache-del    :emit :macro}
   :x-cache-incr            {:macro #'python-tf-x-cache-incr   :emit :macro}})

;;
;; ASYNC
;;

(defn python-tf-x-thread-spawn
  ([[_ thunk]]
   (template/$ (. (__import__ "threading")
           (Thread :target ~thunk)
           (start)))))

(defn python-tf-x-thread-spawn
  ([[_ thunk]]
   (with-meta
     (template/$ (do (var threading  (__import__ "threading"))
              (var thread := (threading.Thread :target ~thunk))
              (. thread (start))))
     {:assign/template 'thread})))

(defn python-tf-x-thread-join
  ([[_ thread]]
   '(x:error "Thread join not Supported")))

(defn python-tf-x-with-delay
  ([[_ thunk ms]]
   (template/$ (x:thread-spawn
         (fn []
           (return [(. (__import__ "time")
                       (sleep (/ ~ms 1000)))
                    ('(~thunk))]))))))

(def +python-thread+
  {:x-thread-spawn   {:macro #'python-tf-x-thread-spawn  :emit :macro   :type :template}
   :x-thread-join    {:macro #'python-tf-x-thread-join   :emit :macro}
   :x-with-delay     {:macro #'python-tf-x-with-delay    :emit :macro}})

;;
;; FILE
;;

(defn python-tf-x-slurp
  ([[_ filename]]))

(defn python-tf-x-spit
  ([[_ filename s]]))

(def +python-file+
  {:x-slurp          {:macro #'python-tf-x-slurp         :emit :macro}
   :x-spit           {:macro #'python-tf-x-spit          :emit :macro}})

;;
;; BASE 64
;;

(defn python-tf-x-b64-encode
  ([[_ obj]]
   (list '. (list '. (list '__import__ "base64")
                  (list 'b64encode (list 'bytes obj "utf-8")))
         (list 'decode "utf-8"))))

(defn python-tf-x-b64-decode
  ([[_ s]]
   (list '. (list '. (list '__import__ "base64")
            (list 'b64decode s))
         (list 'decode "utf-8"))))

(def +python-b64+
  {:x-b64-decode     {:macro #'python-tf-x-b64-decode         :emit :macro}
   :x-b64-encode     {:macro #'python-tf-x-b64-encode         :emit :macro}})

(def +python+
  (merge +python-core+
         +python-proto+
         +python-global+
         +python-custom+
         +python-math+
         +python-type+
         +python-lu+
         +python-obj+
         +python-arr+
         +python-str+
         +python-js+
         +python-return+
         +python-socket+
         +python-iter+
         +python-cache+
         +python-thread+
         +python-file+
         +python-b64+))


(comment

 

  ;;
  ;; FN
  ;;

  (defn python-tf-x-fn-every
    [[_ arr pred]]
    (template/$ (return (all (map pred arr)))))

  (defn python-tf-x-fn-some
    [[_ arr pred]]
    (template/$ (return (any (map pred arr)))))

  (defn python-tf-x-fn-foldl
    [[_ arr f init]]
    (template/$ (do (:- :import functools)
             (return (functools.reduce ~f ~arr ~init)))))

  (defn python-tf-x-fn-foldr
    [[_ arr f init]]
    (template/$ (do (:- :import functools)
             (return (functools.reduce ~f
                                       (:% ~arr [(:- "::-1")])
                                       ~init)))))

  (def +python-fn+
    {:x-fn-every       {:macro #'python-tf-x-fn-every   :emit :macro}
     :x-fn-some        {:macro #'python-tf-x-fn-some    :emit :macro}
     :x-fn-foldl       {:macro #'python-tf-x-fn-foldl   :emit :macro}
     :x-fn-foldr       {:macro #'python-tf-x-fn-foldr   :emit :macro}})


  (defn python-tf-x-str-pad-left
    ([[_ s n ch]]
     (list '. s (list 'rjust n ch))))

  (defn python-tf-x-str-pad-right
    ([[_ s n ch]]
     (list '. s (list 'ljust n ch))))
  :x-str-pad-left   {:macro #'python-tf-x-str-pad-left   :emit :macro}
  :x-str-pad-right  {:macro #'python-tf-x-str-pad-right  :emit :macro}
  
  
  ;;
  ;; ARR
  ;;

  

  )
