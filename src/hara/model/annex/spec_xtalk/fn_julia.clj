(ns hara.model.annex.spec-xtalk.fn-julia
  (:require [hara.common.util :as ut]
            [std.lib.template :as template]))

(defn julia-free-infix
  [a op b]
  (list ':- \( (list '% a) (str " " op " ") (list '% b) \)))

(defn julia-global-key
  [key]
  (cond (symbol? key)  (ut/sym-default-str key)
        (keyword? key) (name key)
        :else          key))

(defn julia-free-try-catch
  [body err handler]
  (list ':- "try\n"
        (list '% body)
        "\ncatch " err "\n"
        (list '% handler)
        "\nend"))

(defn julia-free-iife
  [body]
  (list ':- "(function()\n"
        (list '% body)
        "\nend)()"))

(defn julia-promise-native-check
  [value]
  (list 'and
        (list 'isa value 'AbstractDict)
        (list '== "xt.promise" (list 'get value "__type__" nil))))

(defn julia-promise-resolve-form
  [value]
  {"__type__" "xt.promise"
   "status" "resolved"
   "value" value})

(defn julia-promise-reject-form
  [err]
  {"__type__" "xt.promise"
   "status" "rejected"
   "error" err})

(defn julia-promise-wrap-expr
  [value]
  (list 'if (julia-promise-native-check value)
        value
        (julia-promise-resolve-form value)))

(defn julia-error-value-expr
  [err]
  (list 'if (list 'isa err 'AbstractString)
        err
        (list 'if (list 'isa err 'ErrorException)
              (list 'getfield err :msg)
              (list 'sprint 'showerror err))))

(defn julia-shell-read-expr
  [command root]
  (list ':- "cd(() -> read(Cmd([\"sh\", \"-lc\", "
        (list '% command)
        "]), String), "
        (list '% root)
        ")"))

(defn julia-tf-x-del
  [[_ obj]]
  (if (and (seq? obj)
           (= '. (first obj))
           (= 3 (count obj))
           (vector? (nth obj 2))
           (= 1 (count (nth obj 2))))
    (let [[_ target [key]] obj]
      (list 'delete! target key))
    (list 'delete! obj)))

(defn julia-tf-x-get-key
  [[_ obj key & [default]]]
  (list 'get obj key default))

(defn julia-tf-x-eval
  [[_ s]]
  (list 'eval (list 'Meta.parse s)))

(defn julia-tf-x-apply
  [[_ f args]]
  (list f (list '... args)))

(defn julia-tf-x-unpack
  [[_ arr]]
  (list '... arr))

(defn julia-tf-x-type-native
  [[_ obj]]
  (template/$
   (cond (== ~obj nil)
         (return nil)

         (isa ~obj Dict)
         (return "object")

         (isa ~obj AbstractArray)
         (return "array")

         (isa ~obj Function)
         (return "function")

         (isa ~obj Bool)
         (return "boolean")

         (isa ~obj Number)
         (return "number")

         (isa ~obj AbstractString)
         (return "string")

         :else
         (return (string (typeof ~obj))))))

(def +julia-core+
  {:x-del         {:macro #'julia-tf-x-del         :emit :macro}
   :x-eval        {:macro #'julia-tf-x-eval        :emit :macro}
   :x-apply       {:macro #'julia-tf-x-apply       :emit :macro}
   :x-unpack      {:macro #'julia-tf-x-unpack      :emit :macro}
   :x-type-native {:macro #'julia-tf-x-type-native :emit :macro}
   :x-get-key     {:macro #'julia-tf-x-get-key     :emit :macro}
   :x-cat         {:raw "*" :emit :infix}
   :x-len         {:raw "length" :emit :invoke}
   :x-err         {:raw "error" :emit :invoke}
   :x-random      {:default '(rand) :emit :unit}
   :x-print       {:raw "println" :emit :invoke}
   :x-now-ms      {:default '(round (* 1000 (time))) :emit :unit}})

;;
;; GLOBAL
;;

(def +julia-global+
  {})

;;
;; MATH
;;

(defn julia-tf-x-m-ceil  [[_ num]] (list 'ceil 'Int num))
(defn julia-tf-x-m-floor [[_ num]] (list 'floor 'Int num))

(def +julia-math+
  (merge {:x-m-ceil  {:macro #'julia-tf-x-m-ceil  :emit :macro}
          :x-m-floor {:macro #'julia-tf-x-m-floor :emit :macro}}
         {:x-m-abs   {:raw "abs" :emit :invoke}
          :x-m-acos  {:raw "acos" :emit :invoke}
          :x-m-asin  {:raw "asin" :emit :invoke}
          :x-m-atan  {:raw "atan" :emit :invoke}
          :x-m-cos   {:raw "cos" :emit :invoke}
          :x-m-cosh  {:raw "cosh" :emit :invoke}
          :x-m-exp   {:raw "exp" :emit :invoke}
          :x-m-loge  {:raw "log" :emit :invoke}
          :x-m-log10 {:raw "log10" :emit :invoke}
          :x-m-max   {:raw "max" :emit :invoke}
          :x-m-min   {:raw "min" :emit :invoke}
          :x-m-mod   {:raw "mod" :emit :invoke}
          :x-m-pow   {:raw "^" :emit :bi}
          :x-m-quot  {:raw "div" :emit :invoke}
          :x-m-sin   {:raw "sin" :emit :invoke}
          :x-m-sinh  {:raw "sinh" :emit :invoke}
          :x-m-sqrt  {:raw "sqrt" :emit :invoke}
          :x-m-tan   {:raw "tan" :emit :invoke}
          :x-m-tanh  {:raw "tanh" :emit :invoke}}))

;;
;; TYPE
;;

(defn julia-tf-x-to-number
  [[_ e]]
  (list 'parse 'Float64 e))

(defn julia-tf-x-is-string?
  [[_ e]]
  (list 'isa e 'String))

(defn julia-tf-x-is-number?
  [[_ e]]
  (list 'isa e 'Number))

(defn julia-tf-x-is-integer?
  [[_ e]]
  (list 'isa e 'Integer))

(defn julia-tf-x-is-boolean?
  [[_ e]]
  (list 'isa e 'Bool))

(defn julia-tf-x-is-function?
  [[_ e]]
  (list 'isa e 'Function))

(defn julia-tf-x-is-object?
  [[_ e]]
  (list 'isa e 'Dict))

(defn julia-tf-x-is-array?
  [[_ e]]
  (list 'isa e 'AbstractArray))

(def +julia-type+
  {:x-to-string      {:raw "string" :emit :invoke}
   :x-to-number      {:macro #'julia-tf-x-to-number :emit :macro}
   :x-is-string?     {:macro #'julia-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'julia-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'julia-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'julia-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'julia-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'julia-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'julia-tf-x-is-array? :emit :macro}})

;;
;; LU
;;

(defn julia-tf-x-lu-get
  [[_ lu obj]]
  (list 'get lu obj nil))

(defn julia-tf-x-lu-create
  [_]
  (list ':- "IdDict()"))

(defn julia-tf-x-lu-set
  [[_ lu obj gid]]
  (list ':= (list '. lu [obj]) gid))

(defn julia-tf-x-lu-del
  [[_ lu obj]]
  (list 'delete! lu obj))

(defn julia-tf-x-lu-eq
  [[_ a b]]
  (list ':- \( (list '% a) " === " (list '% b) \)))

(def +julia-lu+
  {:x-lu-create      {:macro #'julia-tf-x-lu-create :emit :macro}
   :x-lu-get         {:macro #'julia-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'julia-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'julia-tf-x-lu-del :emit :macro}
   :x-lu-eq          {:macro #'julia-tf-x-lu-eq :emit :macro}})

;;
;; OBJ
;;

(defn julia-tf-x-obj-keys
  [[_ obj]]
  (list 'collect (list 'keys obj)))

(defn julia-tf-x-obj-vals
  [[_ obj]]
  (list 'collect (list 'values obj)))

(defn julia-tf-x-obj-pairs
  [[_ obj]]
  (template/$
   (collect
    (map (fn [pair]
           (return [(first pair) (last pair)]))
         (collect ~obj)))))

(defn julia-tf-x-obj-assign
  [[_ obj m]]
  (julia-free-iife
   (template/$
    (do (var out (if (== ~obj nil)
                   (Dict)
                   (copy ~obj)))
        (if (not (== ~m nil))
          (for [pair :in (collect ~m)]
            (:= (. out [(first pair)]) (last pair)))
          nil)
        (return out)))))

(def +julia-obj+
  {:x-obj-keys    {:macro #'julia-tf-x-obj-keys   :emit :macro}
   :x-obj-vals    {:macro #'julia-tf-x-obj-vals   :emit :macro}
   :x-obj-pairs   {:macro #'julia-tf-x-obj-pairs  :emit :macro}
   :x-obj-clone   {:raw "copy" :emit :invoke}
   :x-obj-assign  {:macro #'julia-tf-x-obj-assign :emit :macro}})

;;
;; ARR
;;

(defn julia-tf-x-arr-slice
  [[_ arr start end]]
  (list '. arr [(list 'to (list 'x:offset start) 1 end)]))

(defn julia-tf-x-arr-insert
  [[_ arr idx e]]
  (list 'insert! arr idx e))

(defn julia-tf-x-arr-remove
  [[_ arr idx]]
  (list 'splice! arr (list 'x:offset idx)))

(defn julia-tf-x-arr-sort
  [[_ arr key-fn compare-fn]]
  (list ':- "sort!(" (list '% arr)
        ", by = " (list '% key-fn)
        ", lt = " (list '% compare-fn) ")"))

(defn julia-tf-x-arr-foldr
  [[_ arr f init]]
  (list ':- "foldl(" (list '% f)
        ", Iterators.reverse(" (list '% arr) "); init = " (list '% init) ")"))

(def +julia-arr+
  (merge {:x-arr-slice  {:macro #'julia-tf-x-arr-slice  :emit :macro}
          :x-arr-remove {:macro #'julia-tf-x-arr-remove :emit :macro}
          :x-arr-insert {:macro #'julia-tf-x-arr-insert :emit :macro}
          :x-arr-sort   {:macro #'julia-tf-x-arr-sort   :emit :macro}
          :x-arr-foldr  {:macro #'julia-tf-x-arr-foldr  :emit :macro}
          :x-arr-clone      {:raw "copy" :emit :invoke}
          :x-arr-reverse    {:raw "reverse" :emit :invoke}
          :x-arr-push       {:raw "push!" :emit :invoke}
          :x-arr-pop        {:raw "pop!" :emit :invoke}
          :x-arr-push-first {:raw "pushfirst!" :emit :invoke}
          :x-arr-pop-first  {:raw "popfirst!" :emit :invoke}}))

;;
;; STRING
;;

(defn julia-tf-x-str-char
  ([[_ s i]]
   (list 'Int (list 'x:get-idx s i))))

(defn julia-tf-x-str-index-of
  ([[_ s tok & [start]]]
   (template/$
    (do (var start-idx (:? (or (== ~start nil)
                               (< ~start 1))
                           1
                           ~start))
        (var idx (findnext ~tok ~s start-idx))
        (return (:? (== idx nothing)
                    -1
                    (:? (isa idx Integer)
                        (Int idx)
                        (Int (first idx)))))))))

(defn julia-tf-x-str-substring
  ([[_ s start & [end]]]
   (list '. s [(list 'to
                     (list 'max 1 start)
                     1
                     (or end (list 'lastindex s)))])))

(defn julia-tf-x-str-join
  [[_ sep arr]]
  (list 'join arr sep))

(defn julia-tf-x-str-replace
  [[_ s tok replacement]]
  (list 'replace s (list '=> tok replacement)))

(defn julia-tf-x-str-to-fixed
  [[_ n digits]]
  (list 'Printf.format
        (list 'Printf.Format (list 'string "%." digits "f"))
        n))

(def +julia-str+
  (merge {:x-str-char      {:macro #'julia-tf-x-str-char      :emit :macro}
           :x-str-join      {:macro #'julia-tf-x-str-join      :emit :macro}
           :x-str-index-of  {:macro #'julia-tf-x-str-index-of  :emit :macro}
           :x-str-pad-left  {:raw "lpad"                       :emit :invoke}
           :x-str-pad-right {:raw "rpad"                       :emit :invoke}
           :x-str-replace   {:macro #'julia-tf-x-str-replace   :emit :macro}
           :x-str-substring {:macro #'julia-tf-x-str-substring :emit :macro}
           :x-str-to-fixed  {:macro #'julia-tf-x-str-to-fixed  :emit :macro}}
         {:x-str-split      {:raw "split" :emit :invoke}
          :x-str-to-upper   {:raw "uppercase" :emit :invoke}
          :x-str-to-lower   {:raw "lowercase" :emit :invoke}
          :x-str-trim       {:raw "strip" :emit :invoke}
          :x-str-trim-left  {:raw "lstrip" :emit :invoke}
          :x-str-trim-right {:raw "rstrip" :emit :invoke}
          :x-str-comp       {:raw "<" :emit :infix}}))

;;
;; JSON
;;

(def +julia-js+
  {:x-json-encode      {:raw "JSON.json" :emit :invoke}
   :x-json-decode      {:raw "JSON.parse" :emit :invoke}})

(defn julia-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list '== check (list 'get obj key nil))
    (list 'haskey obj key)))

(defn julia-tf-x-global-set
  [[_ key value]]
  (list ':= (list '. 'XT_GLOBALS [(julia-global-key key)]) value))

(defn julia-tf-x-global-del
  [[_ key]]
  (list 'delete! 'XT_GLOBALS (julia-global-key key)))

(defn julia-tf-x-global-has?
  [[_ key]]
  (list 'haskey 'XT_GLOBALS (julia-global-key key)))

(def +julia-custom+
  {:x-has-key?    {:macro #'julia-tf-x-has-key?    :emit :macro}
   :x-global-set  {:macro #'julia-tf-x-global-set  :emit :macro}
   :x-global-del  {:macro #'julia-tf-x-global-del  :emit :macro}
   :x-global-has? {:macro #'julia-tf-x-global-has? :emit :macro}})

;;
;; SOCKET
;;

(defn julia-tf-x-socket-connect
  [[_ host port opts cb]]
  (let [err-form (julia-error-value-expr 'e)]
    (julia-free-try-catch
     (list 'do
           (list 'var 'conn (list 'connect host port))
           (list 'return (list cb nil 'conn)))
     "e"
     (list 'return (list cb err-form nil)))))

(defn julia-tf-x-socket-send
  [[_ conn s]]
  (template/$
   (do (write ~conn ~s)
       (flush ~conn))))

(defn julia-tf-x-socket-close
  [[_ conn]]
  (template/$
   (close ~conn)))

(def +julia-socket+
  {:x-socket-connect {:macro #'julia-tf-x-socket-connect :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-socket-send    {:macro #'julia-tf-x-socket-send    :emit :macro}
   :x-socket-close   {:macro #'julia-tf-x-socket-close   :emit :macro}})

;;
;; HTTP
;;

(defn julia-tf-x-notify-http
  [[_ host port value id key opts]]
  (let [err-form (julia-error-value-expr 'e)]
    (julia-free-try-catch
     (template/$
       (do (var output (x:return-encode ~value ~id ~key))
           (var path (if (or (== ~opts nil)
                             (== (get ~opts "path" nil) nil))
                       "/"
                       (get ~opts "path" nil)))
           (var conn (connect ~host ~port))
           (write conn
                  (x:cat "POST "
                         path
                        " HTTP/1.0\r\n"
                        "Host: "
                        ~host
                        ":"
                        (x:to-string ~port)
                        "\r\n"
                        "Content-Length: "
                        (x:to-string (x:str-len output))
                        "\r\n"
                        "\r\n"
                        output))
          (flush conn)
          (sleep 0.05)
          (close conn)
          (return ["async"])))
     "e"
     (list 'return ["unable to connect" err-form]))))

(def +julia-http+
  {:x-notify-http {:macro #'julia-tf-x-notify-http :emit :macro
                   :op-spec {:allow-blocks true}}})

;;
;; ITER
;;

(defn julia-tf-x-iter-from-obj
  [[_ obj]]
  (template/$
   (Iterators.Stateful
    (map (fn [pair]
           (return [(first pair) (last pair)]))
          (collect ~obj)))))

(defn julia-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (julia-free-iife
   (template/$
    (do (var left (collect ~it0))
        (var right (collect ~it1))
        (if (not (== (length left) (length right)))
          (return false)
          (do (for [i :in (to 1 1 (length left))]
                (if (not (~eq-fn (. left [i])
                                 (. right [i])))
                  (return false)))
              (return true)))))))

(defn julia-tf-x-iter-has?
  [[_ obj]]
  (template/$
   (or (x:iter-native? ~obj)
       (x:is-array? ~obj))))

(defn julia-tf-x-iter-native?
  [[_ it]]
  (list 'isa it 'Base.Iterators.Stateful))

(def +julia-iter+
  {:x-iter-from-arr  {:raw "Iterators.Stateful" :emit :invoke}
   :x-iter-from-obj  {:macro #'julia-tf-x-iter-from-obj :emit :macro}
   :x-iter-from      {:raw "Iterators.Stateful" :emit :invoke}
   :x-iter-eq        {:macro #'julia-tf-x-iter-eq :emit :macro}
   :x-iter-next      {:raw "popfirst!" :emit :invoke}
   :x-iter-has?      {:macro #'julia-tf-x-iter-has? :emit :macro}
   :x-iter-null      {:default '(Iterators.Stateful []) :emit :unit}
   :x-iter-native?   {:macro #'julia-tf-x-iter-native? :emit :macro}})


(def +python-promise+
  {:x-promise          {:emit :hard-link :raw 'xt.lang.common-promise/promise}
   :x-promise-all      {:emit :hard-link :raw 'xt.lang.common-promise/promise-all}
   :x-promise-then     {:emit :hard-link :raw 'xt.lang.common-promise/promise-then}
   :x-promise-catch    {:emit :hard-link :raw 'xt.lang.common-promise/promise-catch}
   :x-promise-finally  {:emit :hard-link :raw 'xt.lang.common-promise/promise-finally}
   :x-promise-native?  {:emit :hard-link :raw 'xt.lang.common-promise/promise-native?}
   :x-with-delay       {:emit :hard-link :raw 'xt.lang.common-promise/with-delay}})


;;
;; SHELL
;;

(defn julia-tf-x-shell
  [[_ s root cb]]
  (let [read-form (julia-shell-read-expr s root)
        err-form  (julia-error-value-expr 'e)]
    (julia-free-try-catch
     (list 'do
           (list 'var 'out read-form)
           (list cb nil 'out)
           (list 'return ["async"]))
     "e"
     (list 'do
           (list cb {"code" 1
                     "err" err-form
                     "out" ""}
                 nil)
           (list 'return ["async"])))))

(def +julia-shell+
  {:x-pwd   {:default '(get ENV "PWD" (pwd)) :emit :unit}
   :x-shell {:macro #'julia-tf-x-shell :emit :macro
             :op-spec {:allow-blocks true}}})

;;
;; FILE
;;

(defn julia-tf-x-file-resolve
  [[_ root path]]
  (list 'abspath (list 'joinpath root path)))

(defn julia-tf-x-file-slurp
  [[_ filename cb]]
  (let [err-form (julia-error-value-expr 'e)]
    (julia-free-try-catch
     (list 'do
           (list cb nil (list 'read filename 'String))
           (list 'return ["async"]))
     "e"
     (list 'do
           (list cb err-form nil)
           (list 'return ["async"])))))

(defn julia-tf-x-file-spit
  [[_ filename content cb]]
  (let [err-form (julia-error-value-expr 'e)]
    (julia-free-try-catch
     (list 'do
           (list 'write filename content)
           (list cb nil filename)
           (list 'return ["async"]))
     "e"
     (list 'do
           (list cb err-form nil)
           (list 'return ["async"])))))

(def +julia-file+
  {:x-file-resolve   {:macro #'julia-tf-x-file-resolve :emit :macro}
   :x-file-slurp     {:macro #'julia-tf-x-file-slurp   :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-file-spit      {:macro #'julia-tf-x-file-spit    :emit :macro
                      :op-spec {:allow-blocks true}}})

(def +julia-bit+
  {:x-bit-and    {:raw "&" :emit :bi}
   :x-bit-or     {:raw "|" :emit :bi}
   :x-bit-lshift {:raw "<<" :emit :bi}
   :x-bit-rshift {:raw ">>" :emit :bi}
   :x-bit-xor    {:raw "⊻" :emit :bi}})

;;
;; RETURN
;;

(defn julia-tf-x-return-encode
  ([[_ out id key]]
   (julia-free-iife
    (template/$
     (do (var type-fn
              (fn [obj]
                (cond (== obj nil)
                      (return "nil")

                      (isa obj Dict)
                      (return "object")

                      (isa obj AbstractArray)
                      (return "array")

                      (isa obj Function)
                      (return "function")

                      (isa obj Bool)
                      (return "boolean")

                      (isa obj Number)
                      (return "number")

                      (isa obj AbstractString)
                      (return "string")

                      :else
                      (return (string (typeof obj))))))
         (var ts (type-fn ~out))
         (if (== ts "function")
           (return (JSON.json {:id ~id
                               :key ~key
                               :type "raw"
                               :return ts
                               :value (string ~out)}))
           (return (JSON.json {:id ~id
                               :key ~key
                               :type "data"
                               :return ts
                               :value ~out}))))))))

(defn julia-tf-x-return-wrap
  ([[_ f encode-fn]]
   (let [out 'out]
     (julia-free-try-catch
      (list 'do
            (list 'var out (list f))
            (list 'if (list 'applicable encode-fn out)
                  (list 'return (list encode-fn out))
                  (list 'return (list encode-fn out nil nil))))
      "e"
      '(return (JSON.json {:type "error"
                           :value (sprint showerror e)}))))))

(defn julia-tf-x-return-eval
  ([[_ s wrap-fn]]
   (template/$
    (return (~wrap-fn
             (fn []
               (return (include_string Main ~s))))))))

(def +julia-return+
  {:x-return-encode  {:macro #'julia-tf-x-return-encode   :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-return-wrap    {:macro #'julia-tf-x-return-wrap     :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-return-eval    {:macro #'julia-tf-x-return-eval     :emit :macro
                      :op-spec {:allow-blocks true}}})

(def +julia+
  (merge +julia-core+
         +julia-custom+
         +julia-global+
         +julia-math+
         +julia-type+
         +julia-lu+
         +julia-obj+
         +julia-arr+
         +julia-str+
         +julia-js+
         +julia-socket+
         +julia-http+
         +julia-iter+
         #_+julia-promise+
         +julia-shell+
         +julia-file+
         +julia-bit+
         +julia-return+))
