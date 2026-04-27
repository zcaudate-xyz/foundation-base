(ns std.lang.model-annex.spec-xtalk.fn-julia
  (:require [std.lang.base.util :as ut]
            [std.lib.template :as template]))

(defn julia-free-call
  [fname & args]
  (apply list ':- (concat [fname \(]
                          (->> args
                               (map (fn [arg] (list '% arg)))
                               (interpose \,))
                          [\)])))

(defn julia-free-getindex
  [target idx]
  (list ':- (list '% target) \[ (list '% idx) \]))

(defn julia-free-setindex
  [target idx value]
  (list ':- (list '% target) \[ (list '% idx) \] "=" (list '% value)))

(defn julia-free-slice
  [target start end]
  (list ':- (list '% target) \[ (list '% start) \: (list '% end) \]))

(defn julia-free-range
  [start end]
  (list ':- \( (list '% start) \) \: \( (list '% end) \)))

(defn julia-free-unpack
  [arr]
  (list '... arr))

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

(defn julia-tf-x-del
  [[_ obj]]
  (if (and (seq? obj)
           (= '. (first obj))
           (= 3 (count obj))
           (vector? (nth obj 2))
           (= 1 (count (nth obj 2))))
    (let [[_ target [key]] obj]
      (julia-free-call "delete!" target key))
    (julia-free-call "delete!" obj)))

(defn julia-tf-x-cat
  [[_ & args]]
  (apply list '* args))

(defn julia-tf-x-len
  [[_ arr]]
  (list 'length arr))

(defn julia-tf-x-get-key
  [[_ obj key default]]
  (list 'get obj key default))

(defn julia-tf-x-get-idx
  [[_ arr idx]]
  (julia-free-getindex arr (list '+ idx 1)))

(defn julia-tf-x-set-idx
  [[_ arr idx value]]
  (julia-free-setindex arr (list '+ idx 1) value))

(defn julia-tf-x-err
  [[_ msg]]
  (list 'error msg))

(defn julia-tf-x-eval
  [[_ s]]
  (list 'eval (list 'Meta.parse s)))

(defn julia-tf-x-apply
  [[_ f args]]
  (list ':- \( (list '% f) \) \( (list '% (julia-free-unpack args)) \)))

(defn julia-tf-x-unpack
  [[_ arr]]
  (julia-free-unpack arr))

(defn julia-tf-x-random
  [_]
  (list 'rand))

(defn julia-tf-x-print
  ([[_ & args]]
   (apply list 'println args)))

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
  {:x-del            {:macro #'julia-tf-x-del      :emit :macro}
   :x-cat            {:macro #'julia-tf-x-cat      :emit :macro}
   :x-len            {:macro #'julia-tf-x-len      :emit :macro}
   :x-err            {:macro #'julia-tf-x-err      :emit :macro}
   :x-eval           {:macro #'julia-tf-x-eval     :emit :macro}
   :x-apply          {:macro #'julia-tf-x-apply    :emit :macro}
   :x-unpack         {:macro #'julia-tf-x-unpack :emit :macro}
   :x-random         {:macro #'julia-tf-x-random   :emit :macro}
   :x-print          {:macro #'julia-tf-x-print    :emit :macro}
   :x-now-ms         {:default '(round (* 1000 (time))) :emit :unit}
   :x-get-key        {:macro #'julia-tf-x-get-key  :emit :macro}
   :x-get-idx        {:macro #'julia-tf-x-get-idx  :emit :macro}
   :x-set-idx        {:macro #'julia-tf-x-set-idx  :emit :macro}
   :x-type-native    {:macro #'julia-tf-x-type-native :emit :macro}})

;;
;; GLOBAL
;;

(def +julia-global+
  {})

;;
;; MATH
;;

(defn julia-tf-x-m-abs   [[_ num]] (list 'abs num))
(defn julia-tf-x-m-acos  [[_ num]] (list 'acos num))
(defn julia-tf-x-m-asin  [[_ num]] (list 'asin num))
(defn julia-tf-x-m-atan  [[_ num]] (list 'atan num))
(defn julia-tf-x-m-cos   [[_ num]] (list 'cos num))
(defn julia-tf-x-m-cosh  [[_ num]] (list 'cosh num))
(defn julia-tf-x-m-exp   [[_ num]] (list 'exp num))
(defn julia-tf-x-m-loge  [[_ num]] (list 'log num))
(defn julia-tf-x-m-log10 [[_ num]] (list 'log10 num))
(defn julia-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn julia-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn julia-tf-x-m-ceil  [[_ num]] (list 'ceil 'Int num))
(defn julia-tf-x-m-floor [[_ num]] (list 'floor 'Int num))
(defn julia-tf-x-m-mod   [[_ num denom]] (list 'mod num denom))
(defn julia-tf-x-m-pow   [[_ base n]] (list (symbol "^") base n))
(defn julia-tf-x-m-quot  [[_ num denom]] (list 'div num denom))
(defn julia-tf-x-m-sin   [[_ num]] (list 'sin num))
(defn julia-tf-x-m-sinh  [[_ num]] (list 'sinh num))
(defn julia-tf-x-m-sqrt  [[_ num]] (list 'sqrt num))
(defn julia-tf-x-m-tan   [[_ num]] (list 'tan num))
(defn julia-tf-x-m-tanh  [[_ num]] (list 'tanh num))

(def +julia-math+
  {:x-m-abs           {:macro #'julia-tf-x-m-abs,                 :emit :macro}
   :x-m-acos          {:macro #'julia-tf-x-m-acos,                :emit :macro}
   :x-m-asin          {:macro #'julia-tf-x-m-asin,                :emit :macro}
   :x-m-atan          {:macro #'julia-tf-x-m-atan,                :emit :macro}
   :x-m-ceil          {:macro #'julia-tf-x-m-ceil,                :emit :macro}
   :x-m-cos           {:macro #'julia-tf-x-m-cos,                 :emit :macro}
   :x-m-cosh          {:macro #'julia-tf-x-m-cosh,                :emit :macro}
   :x-m-exp           {:macro #'julia-tf-x-m-exp,                 :emit :macro}
   :x-m-floor         {:macro #'julia-tf-x-m-floor,               :emit :macro}
   :x-m-loge          {:macro #'julia-tf-x-m-loge,                :emit :macro}
   :x-m-log10         {:macro #'julia-tf-x-m-log10,               :emit :macro}
   :x-m-max           {:macro #'julia-tf-x-m-max,                 :emit :macro}
   :x-m-min           {:macro #'julia-tf-x-m-min,                 :emit :macro}
   :x-m-mod           {:macro #'julia-tf-x-m-mod,                 :emit :macro}
   :x-m-pow           {:macro #'julia-tf-x-m-pow,                 :emit :macro}
   :x-m-quot          {:macro #'julia-tf-x-m-quot,                :emit :macro}
   :x-m-sin           {:macro #'julia-tf-x-m-sin,                 :emit :macro}
   :x-m-sinh          {:macro #'julia-tf-x-m-sinh,                :emit :macro}
   :x-m-sqrt          {:macro #'julia-tf-x-m-sqrt,                :emit :macro}
   :x-m-tan           {:macro #'julia-tf-x-m-tan,                 :emit :macro}
   :x-m-tanh          {:macro #'julia-tf-x-m-tanh,                :emit :macro}})

;;
;; TYPE
;;

(defn julia-tf-x-to-string
  [[_ e]]
  (list 'string e))

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
  {:x-to-string      {:macro #'julia-tf-x-to-string :emit :macro}
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
  (julia-free-setindex lu obj gid))

(defn julia-tf-x-lu-del
  [[_ lu obj]]
  (julia-free-call "delete!" lu obj))

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
  (list 'collect obj))

(defn julia-tf-x-obj-clone
  [[_ obj]]
  (list 'copy obj))

(defn julia-tf-x-obj-assign
  [[_ obj m]]
  (template/$
   (do (var out (if (== ~obj nil)
                  (Dict())
                  ~obj))
        (if (not= nil ~m)
          (for [k :in (keys ~m)]
            (x:set-key out k (x:get-key ~m k nil))))
        (return out))))

(def +julia-obj+
  {:x-obj-keys    {:macro #'julia-tf-x-obj-keys   :emit :macro}
   :x-obj-vals    {:macro #'julia-tf-x-obj-vals   :emit :macro}
   :x-obj-pairs   {:macro #'julia-tf-x-obj-pairs  :emit :macro}
   :x-obj-clone   {:macro #'julia-tf-x-obj-clone  :emit :macro}
   :x-obj-assign  {:macro #'julia-tf-x-obj-assign :emit :macro
                   :raw nil
                   :value/standalone true
                   :value/template #'julia-tf-x-obj-assign}})

;;
;; ARR
;;

(defn julia-tf-x-arr-clone
  [[_ arr]]
  (list 'copy arr))

(defn julia-tf-x-arr-slice
  [[_ arr start end]]
  (list 'getindex arr (julia-free-range (list '+ start 1) end)))

(defn julia-tf-x-arr-push
  [[_ arr item]]
  (julia-free-call "push!" arr item))

(defn julia-tf-x-arr-pop
  [[_ arr]]
  (julia-free-call "pop!" arr))

(defn julia-tf-x-arr-reverse
  [[_ arr]]
  (list 'reverse arr))

(defn julia-tf-x-arr-push-first
  [[_ arr item]]
  (julia-free-call "pushfirst!" arr item))

(defn julia-tf-x-arr-pop-first
  [[_ arr]]
  (julia-free-call "popfirst!" arr))

(defn julia-tf-x-arr-insert
  [[_ arr idx e]]
  (julia-free-call "insert!" arr (list '+ idx 1) e))

(defn julia-tf-x-arr-remove
  [[_ arr idx]]
  (julia-free-call "splice!" arr (list '+ idx 1)))

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
  {:x-arr-clone       {:macro #'julia-tf-x-arr-clone      :emit :macro}
   :x-arr-slice       {:macro #'julia-tf-x-arr-slice      :emit :macro}
   :x-arr-reverse     {:macro #'julia-tf-x-arr-reverse    :emit :macro}
   :x-arr-push        {:macro #'julia-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'julia-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'julia-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'julia-tf-x-arr-pop-first  :emit :macro}
   :x-arr-remove      {:macro #'julia-tf-x-arr-remove     :emit :macro}
   :x-arr-insert      {:macro #'julia-tf-x-arr-insert     :emit :macro}
   :x-arr-sort        {:macro #'julia-tf-x-arr-sort       :emit :macro}
   :x-arr-foldr       {:macro #'julia-tf-x-arr-foldr      :emit :macro
                       :raw nil
                       :value/standalone true
                       :value/template #'julia-tf-x-arr-foldr}})

;;
;; STRING
;;

(defn julia-tf-x-str-char
  ([[_ s i]]
   (list 'Int (list 'getindex s (list '+ i 1)))))

(defn julia-tf-x-str-split
  ([[_ s tok]]
   (list 'split s tok)))

(defn julia-tf-x-str-join
  ([[_ s arr]]
   (list 'join arr s)))

(defn julia-tf-x-str-index-of
  ([[_ s tok & [start]]]
   (template/$
    (do (var idx (findnext ~tok ~s (+ 1 ~(or start 0))))
        (if (== idx nothing)
          (return nil)
          (return (- (Int (first idx)) 1)))))))

(defn julia-tf-x-str-substring
  ([[_ s start & [end]]]
   (list 'getindex s
         (julia-free-range (list '+ start 1)
                           (or end (list 'lastindex s))))))

(defn julia-tf-x-str-to-upper
  ([[_ s]]
   (list 'uppercase s)))

(defn julia-tf-x-str-to-lower
  ([[_ s]]
   (list 'lowercase s)))

(defn julia-tf-x-str-replace
  ([[_ s tok replacement]]
    (list 'replace s (list '=> tok replacement))))

(defn julia-tf-x-str-to-fixed
  [[_ n digits]]
  (list 'Printf.format
        (list 'Printf.Format (list 'string "%." digits "f"))
        n))

(defn julia-tf-x-str-trim
  [[_ s]]
  (list 'strip s))

(defn julia-tf-x-str-trim-left
  [[_ s]]
  (list 'lstrip s))

(defn julia-tf-x-str-trim-right
  [[_ s]]
  (list 'rstrip s))

(defn julia-tf-x-str-comp
  [[_ a b]]
  (list '< a b))

(def +julia-str+
  {:x-str-char       {:macro #'julia-tf-x-str-char      :emit :macro}
   :x-str-split      {:macro #'julia-tf-x-str-split      :emit :macro}
   :x-str-join       {:macro #'julia-tf-x-str-join       :emit :macro}
   :x-str-index-of   {:macro #'julia-tf-x-str-index-of   :emit :macro
                      :value/standalone true
                      :value/template #'julia-tf-x-str-index-of}
   :x-str-substring  {:macro #'julia-tf-x-str-substring  :emit :macro}
   :x-str-to-upper   {:macro #'julia-tf-x-str-to-upper      :emit :macro}
   :x-str-to-lower   {:macro #'julia-tf-x-str-to-lower      :emit :macro}
   :x-str-to-fixed   {:macro #'julia-tf-x-str-to-fixed   :emit :macro}
   :x-str-replace    {:macro #'julia-tf-x-str-replace    :emit :macro}
   :x-str-trim       {:macro #'julia-tf-x-str-trim       :emit :macro}
   :x-str-trim-left  {:macro #'julia-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right {:macro #'julia-tf-x-str-trim-right :emit :macro}
   :x-str-comp       {:macro #'julia-tf-x-str-comp       :emit :macro}})

;;
;; JSON
;;

(defn julia-tf-x-json-encode
  ([[_ obj]]
   (list 'JSON.json obj)))

(defn julia-tf-x-json-decode
  ([[_ s]]
   (list 'JSON.parse s)))

(def +julia-js+
  {:x-json-encode      {:macro #'julia-tf-x-json-encode      :emit :macro}
   :x-json-decode      {:macro #'julia-tf-x-json-decode      :emit :macro}})

(defn julia-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list '== check (list 'get obj key nil))
    (list 'haskey obj key)))

(defn julia-tf-x-global-set
  [[_ key value]]
  (julia-free-setindex 'XT_GLOBALS (julia-global-key key) value))

(defn julia-tf-x-global-del
  [[_ key]]
  (julia-free-call "delete!" 'XT_GLOBALS (julia-global-key key)))

(defn julia-tf-x-global-has?
  [[_ key]]
  (list 'haskey 'XT_GLOBALS (julia-global-key key)))

(def +julia-custom+
  {:x-has-key?    {:macro #'julia-tf-x-has-key?    :emit :macro}
   :x-global-set  {:macro #'julia-tf-x-global-set  :emit :macro
                   :value/template #'julia-tf-x-global-set}
   :x-global-del  {:macro #'julia-tf-x-global-del  :emit :macro
                   :value/template #'julia-tf-x-global-del}
   :x-global-has? {:macro #'julia-tf-x-global-has? :emit :macro
                   :value/template #'julia-tf-x-global-has?}})

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

(defn julia-tf-x-iter-from-arr
  [[_ arr]]
  (list 'Iterators.Stateful arr))

(defn julia-tf-x-iter-from
  [[_ obj]]
  (list 'Iterators.Stateful obj))

(defn julia-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (template/$
   (do (for [x0 :in ~it0]
         (if (isempty ~it1)
           (return false))
         (if (not (~eq-fn x0 (x:iter-next ~it1)))
           (return false)))
        (return (isempty ~it1)))))

(defn julia-tf-x-iter-next
  [[_ it]]
  (julia-free-call "popfirst!" it))

(defn julia-tf-x-iter-null
  [_]
  (list ':- "Iterators.Stateful([])"))

(defn julia-tf-x-iter-has?
  [[_ obj]]
  (list 'and
        (list 'not (list 'isa obj 'Dict))
        (list 'applicable 'iterate obj)))

(defn julia-tf-x-iter-native?
  [[_ it]]
  (list 'isa it 'Iterators.Stateful))

(def +julia-iter+
  {:x-iter-from-obj    {:macro #'julia-tf-x-iter-from-obj   :emit :macro}
   :x-iter-from-arr    {:macro #'julia-tf-x-iter-from-arr   :emit :macro}
   :x-iter-from        {:macro #'julia-tf-x-iter-from       :emit :macro}
   :x-iter-eq          {:macro #'julia-tf-x-iter-eq         :emit :macro
                        :op-spec {:allow-blocks true}}
   :x-iter-null        {:macro #'julia-tf-x-iter-null       :emit :macro}
   :x-iter-next        {:macro #'julia-tf-x-iter-next       :emit :macro}
   :x-iter-has?        {:macro #'julia-tf-x-iter-has?       :emit :macro}
   :x-iter-native?     {:macro #'julia-tf-x-iter-native?    :emit :macro}})

(defn julia-tf-x-bit-and
  [[_ a b]]
  (julia-free-infix a "&" b))

(defn julia-tf-x-bit-or
  [[_ a b]]
  (julia-free-infix a "|" b))

(defn julia-tf-x-bit-lshift
  [[_ a b]]
  (julia-free-infix a "<<" b))

(defn julia-tf-x-bit-rshift
  [[_ a b]]
  (julia-free-infix a ">>" b))

(defn julia-tf-x-bit-xor
  [[_ a b]]
  (julia-free-infix a "⊻" b))

(def +julia-bit+
  {:x-bit-and    {:macro #'julia-tf-x-bit-and    :emit :macro
                  :value/template #'julia-tf-x-bit-and}
   :x-bit-or     {:macro #'julia-tf-x-bit-or     :emit :macro
                  :value/template #'julia-tf-x-bit-or}
   :x-bit-lshift {:macro #'julia-tf-x-bit-lshift :emit :macro
                  :value/template #'julia-tf-x-bit-lshift}
   :x-bit-rshift {:macro #'julia-tf-x-bit-rshift :emit :macro
                  :value/template #'julia-tf-x-bit-rshift}
   :x-bit-xor    {:macro #'julia-tf-x-bit-xor    :emit :macro
                  :value/template #'julia-tf-x-bit-xor}})

;;
;; RETURN
;;

(defn julia-tf-x-return-encode
  ([[_ out id key]]
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
                              :value ~out})))))))

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
          +julia-iter+
          +julia-bit+
          +julia-return+))
