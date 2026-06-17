^{:no-test true}
(ns hara.model.annex.spec-xtalk.fn-r
  (:require [hara.common.grammar-xtalk :as default]
            [std.lib.template :as template]))

;;
;; CORE
;;

(defn r-tf-x-del
  [[_ obj]]
  (list := obj nil))

(defn r-tf-x-cat
  [[_ & args]]
  (apply list 'paste (concat args [:sep ""])))

(defn r-tf-x-len
  [[_ arr]]
  (list 'length arr))

(defn r-tf-x-err
  [[_ msg]]
  (list 'stop msg))

(defn r-tf-x-eval
  [[_ s]]
  (list 'eval (list 'parse :text s)))

(defn r-tf-x-apply
  [[_ f args]]
  (list 'do.call f args))

(defn r-tf-x-print
  ([[_ & args]]
   (let [out (cond (empty? args) nil
                   (= 1 (count args)) (first args)
                   :else (cons 'x:cat args))]
     (template/$
      (local
       (block
        (print ~out)
        nil))))))

(defn r-tf-x-shell
  ([[_ s root cb]]
   (template/$
    (tryCatch
     (block
       (var command := (:? (x:nil? ~root)
                           ~s
                           (paste0 "cd " (shQuote ~root) " && " ~s)))
       (var output := (system2 "sh" (c "-lc" command)
                               :stdout true
                               :stderr true))
       (var status := (attr output "status"))
       (var out := (paste output :collapse "\n"))
       (if (is.null status)
         (~cb nil out)
         (~cb {:code status
               :err out
               :out out}
              nil)))
     :error (fn [err]
              (~cb err nil))))))

(defn r-tf-x-random
  [_]
  '(runif 1))

(defn r-tf-x-type-native
  [[_ obj]]
  (template/$
   (do (var t := (typeof ~obj))
       (cond (is.null ~obj)
             (return "nil")

             (== t "list")
             (if (not (is.null (names ~obj)))
               (return "object")
               (return "array"))

             (== t "closure")
             (return "function")

             (and (is.atomic ~obj)
                  (not (is.null ~obj))
                  (not (== 1 (length ~obj))))
             (return "array")

             (== t "double")
             (return "number")

             (== t "character")
             (return "string")

             (== t "logical")
             (return "boolean")

             :else (return t)))))

(def +r-core+
  {:x-del            {:macro #'r-tf-x-del   :emit :macro}
   :x-cat            {:macro #'r-tf-x-cat   :emit :macro}
   :x-len            {:macro #'r-tf-x-len   :emit :macro}
   :x-err            {:macro #'r-tf-x-err     :emit :macro}
   :x-eval           {:macro #'r-tf-x-eval    :emit :macro}
   :x-random         {:macro #'r-tf-x-random  :emit :macro}
   :x-apply          {:macro #'r-tf-x-apply   :emit :macro}
   :x-print          {:macro #'r-tf-x-print         :emit :macro}
   :x-now-ms         {:default '(floor (* 1000 (as.numeric (Sys.time)))) :emit :unit}
   :x-type-native    {:macro #'r-tf-x-type-native    :emit :macro}
   :x-unpack         {:emit :throw}})

;;
;; GLOBAL
;;

(defn r-tf-x-global-has?
  [[_ sym]]
  (list 'not (list 'x:nil? (list 'tryCatch (list 'get (str sym) :envir '.GlobalEnv)
                                 :error '(fn:> [e] nil)))))

(defn r-tf-x-global-set
  [[_ sym val]]
  (list 'assign (str sym) val :envir '.GlobalEnv))

(defn r-tf-x-global-del
  [[_ sym]]
  (list 'assign (str sym) nil :envir '.GlobalEnv))

(def +r-global+
  {:x-global-has?   {:macro #'r-tf-x-global-has?  :emit :macro}
   :x-global-set    {:macro #'r-tf-x-global-set   :emit :macro}
   :x-global-del    {:macro #'r-tf-x-global-del   :emit :macro}})

;;
;; CUSTOM
;;

(defn r-tf-x-not-nil?
  [[_ obj]]
  (list 'not (list 'is.null obj)))

(defn r-tf-x-nil?
  [[_ obj]]
  (list 'is.null obj))

(defn r-propagate-symbol
  [sym]
  (when (symbol? sym)
    (let [name (str sym)]
      (template/$
       (when (exists ~name
                         :envir (parent.env (environment))
                         :inherits true)
         (assign ~name
                  ~sym
                  :envir (parent.env (environment))))))))

(defn r-local-block
  [& body]
  (list 'local (apply list 'block body)))

(defn r-as-list
  [value]
  (list 'as.list value))

(defn r-unname
  [value]
  (list 'unname value))

(defn r-update-self
  [sym expr]
  (let [sync (r-propagate-symbol sym)]
    (if sync
      (template/$
       (do (:= ~sym ~expr)
           ~sync
           ~sym))
      (template/$
       (do (:= ~sym ~expr)
           ~sym)))))

(defn r-tf-x-has-key?
  [[_ obj key check]]
  (let [present (template/$
                 (:? (x:is-object? ~obj)
                     (not (is.na (match (as.character ~key)
                                        (names ~obj))))
                     (and (is.numeric ~key)
                          (>= ~key 1)
                          (<= ~key (length ~obj)))))]
    (if check
      (template/$
       (and ~present
            (== ~check (x:get-key ~obj ~key nil))))
      present)))

(defn r-tf-x-set-key
  [[_ obj key value]]
  (let [sync (r-propagate-symbol obj)]
    (if sync
      (template/$
       (do (:= (. ~obj [(:? (x:is-object? ~obj)
                            (as.character ~key)
                            ~key)])
               ~value)
           ~sync
           ~obj))
      (template/$
       (do (:= (. ~obj [(:? (x:is-object? ~obj)
                            (as.character ~key)
                            ~key)])
               ~value)
           ~obj)))))

(defn r-tf-x-del-key
  [[_ obj key]]
  (let [sync (r-propagate-symbol obj)]
    (if sync
      (template/$
       (do (:= (. ~obj [(:? (x:is-object? ~obj)
                            (as.character ~key)
                            ~key)])
               nil)
           ~sync
           ~obj))
      (template/$
       (do (:= (. ~obj [(:? (x:is-object? ~obj)
                            (as.character ~key)
                            ~key)])
               nil)
           ~obj)))))

(defn r-tf-x-get-key
  [[_ obj key default]]
  (let [val (template/$
             (:? (x:is-object? ~obj)
                 (. ~obj [(as.character ~key)])
                 (:? (and (is.numeric ~key)
                          (>= ~key 1)
                          (<= ~key (length ~obj)))
                     (. ~obj [~key])
                     nil)))]
    (if default
      (list :?
            (list 'x:has-key? obj key)
            val
            default)
      val)))

(def +r-custom+
  {:x-not-nil?         {:macro #'r-tf-x-not-nil? :emit :macro}
   :x-nil?             {:macro #'r-tf-x-nil?  :emit :macro}
   :x-has-key?         {:macro #'r-tf-x-has-key? :emit :macro}
   :x-set-key          {:macro #'r-tf-x-set-key :emit :macro}
   :x-del-key          {:macro #'r-tf-x-del-key :emit :macro}
   :x-get-key          {:macro #'r-tf-x-get-key :emit :macro}})

;;
;; MATH
;;

(defn r-tf-x-m-abs   [[_ num]] (list 'abs num))
(defn r-tf-x-m-acos  [[_ num]] (list 'acos num))
(defn r-tf-x-m-asin  [[_ num]] (list 'asin num))
(defn r-tf-x-m-atan  [[_ num]] (list 'atan num))
(defn r-tf-x-m-ceil  [[_ num]] (list 'ceiling num))
(defn r-tf-x-m-cos   [[_ num]] (list 'cos num))
(defn r-tf-x-m-cosh  [[_ num]] (list 'cosh num))
(defn r-tf-x-m-exp   [[_ num]] (list 'exp num))
(defn r-tf-x-m-floor [[_ num]] (list 'floor num))
(defn r-tf-x-m-loge  [[_ num]] (list 'log num))
(defn r-tf-x-m-log10 [[_ num]] (list 'log10 num))
(defn r-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn r-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn r-tf-x-m-mod   [[_ num denom]] (list 'mod num denom))
(defn r-tf-x-m-quot  [[_ num denom]] (list 'floor
                                             (list '/ num denom)))
(defn r-tf-x-m-pow   [[_ base n]] (list 'pow base n))
(defn r-tf-x-m-sin   [[_ num]] (list 'sin num))
(defn r-tf-x-m-sinh  [[_ num]] (list 'sinh num))
(defn r-tf-x-m-sqrt  [[_ num]] (list 'sqrt num))
(defn r-tf-x-m-tan   [[_ num]] (list 'tan num))
(defn r-tf-x-m-tanh  [[_ num]] (list 'tanh num))

(def +r-math+
  {:x-m-abs           {:macro #'r-tf-x-m-abs,                 :emit :macro}
   :x-m-acos          {:macro #'r-tf-x-m-acos,                :emit :macro}
   :x-m-asin          {:macro #'r-tf-x-m-asin,                :emit :macro}
   :x-m-atan          {:macro #'r-tf-x-m-atan,                :emit :macro}
   :x-m-ceil          {:macro #'r-tf-x-m-ceil,                :emit :macro}
   :x-m-cos           {:macro #'r-tf-x-m-cos,                 :emit :macro}
   :x-m-cosh          {:macro #'r-tf-x-m-cosh,                :emit :macro}
   :x-m-exp           {:macro #'r-tf-x-m-exp,                 :emit :macro}
   :x-m-floor         {:macro #'r-tf-x-m-floor,               :emit :macro}
   :x-m-loge          {:macro #'r-tf-x-m-loge,                :emit :macro}
   :x-m-log10         {:macro #'r-tf-x-m-log10,               :emit :macro}
   :x-m-max           {:macro #'r-tf-x-m-max,                 :emit :macro}
   :x-m-min           {:macro #'r-tf-x-m-min,                 :emit :macro}
   :x-m-mod           {:macro #'r-tf-x-m-mod,                 :emit :macro}
   :x-m-pow           {:macro #'r-tf-x-m-pow,                 :emit :macro}
   :x-m-quot          {:macro #'r-tf-x-m-quot,                :emit :macro}
   :x-m-sin           {:macro #'r-tf-x-m-sin,                 :emit :macro}
   :x-m-sinh          {:macro #'r-tf-x-m-sinh,                :emit :macro}
   :x-m-sqrt          {:macro #'r-tf-x-m-sqrt,                :emit :macro}
   :x-m-tan           {:macro #'r-tf-x-m-tan,                 :emit :macro}
   :x-m-tanh          {:macro #'r-tf-x-m-tanh,                :emit :macro}})

;;
;; TYPE
;;

(defn r-tf-x-to-string
  [[_ e]]
  (list 'toString e))

(defn r-tf-x-to-number
  [[_ e]]
  (list 'as.numeric e))

(defn r-tf-x-is-string?
  [[_ e]]
  (list '== "character" (list 'typeof e)))

(defn r-tf-x-is-number?
  [[_ e]]
  (list 'is.numeric e))

(defn r-tf-x-is-integer?
  [[_ e]]
  (template/$ (and (is.numeric ~e)
                   (== 1 (length ~e))
                   (== ~e (floor ~e)))))

(defn r-tf-x-is-boolean?
  [[_ e]]
  (list '== "logical" (list 'typeof e)))

(defn r-tf-x-is-function?
  [[_ e]]
  (list '== "closure" (list 'typeof e)))

(defn r-tf-x-is-object?
  [[_ e]]
  (template/$ (and (== "list" (typeof ~e))
             (not (is.null (names ~e))))))

(defn r-tf-x-is-array?
  [[_ e]]
  (template/$
   (or (and (== "list" (typeof ~e))
            (is.null (names ~e)))
       (and (is.atomic ~e)
            (not (is.null ~e))
            (not (== 1 (length ~e)))))))

(def +r-type+
  {:x-to-string      {:macro #'r-tf-x-to-string :emit :macro}
   :x-to-number      {:macro #'r-tf-x-to-number :emit :macro}
   :x-is-string?     {:macro #'r-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'r-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'r-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'r-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'r-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'r-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'r-tf-x-is-array? :emit :macro}})

;;
;; LU
;;

(defn r-tf-x-lu-key
  [obj]
  (let [scalar? (template/$ (and (== 1 (length ~obj))
                                 (or (is.character ~obj)
                                     (is.numeric ~obj)
                                     (is.logical ~obj))))]
    (list ':?
          (list 'is.null obj)
          "__xt_nil__"
          (list ':?
                (list '== "environment" (list 'typeof obj))
                (list 'paste "env" (list 'format obj) :sep ":")
                (list ':?
                      scalar?
                      (list 'paste (list 'typeof obj)
                            (list 'as.character obj)
                            :sep ":")
                      (list 'tracemem obj))))))

(defn r-tf-x-lu-create
  "creates an environment-backed lookup table"
  {:added "4.1"}
  ([[_]]
   (list 'new.env :hash true :parent (list 'emptyenv))))

(defn r-tf-x-lu-eq
  "converts map to array"
  {:added "4.0"}
  ([[_ o1 o2]]
   (list '== (r-tf-x-lu-key o1) (r-tf-x-lu-key o2))))

(defn r-tf-x-lu-get
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj default]]
   (list 'get0 (r-tf-x-lu-key obj)
         :envir lu
         :inherits false
         :ifnotfound default)))

(defn r-tf-x-lu-set
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj gid]]
   (list 'assign (r-tf-x-lu-key obj)
         gid
         :envir lu)))

(defn r-tf-x-lu-del
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj]]
   (let [key (r-tf-x-lu-key obj)]
     (list ':?
           (list 'exists key :envir lu :inherits false)
           (list 'rm :list key :envir lu :inherits false)
           nil))))

(def +r-lu+
  {:x-lu-create      {:macro #'r-tf-x-lu-create :emit :macro}
   :x-lu-eq          {:macro #'r-tf-x-lu-eq :emit :macro}
   :x-lu-get         {:macro #'r-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'r-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'r-tf-x-lu-del :emit :macro}})

;;
;; ARR
;;   

(defn r-tf-x-arr-push
  [[_ arr item]]
  (r-update-self arr
                 (list 'append (r-as-list arr)
                       (list 'list item))))

(defn r-tf-x-arr-pop
  [[_ arr]]
  (let [items (gensym "arr_items__")
        len   (gensym "arr_len__")
        item  (gensym "arr_item__")
        sync  (r-propagate-symbol arr)]
    (apply r-local-block
           (concat [(list 'var items := (r-as-list arr))
                    (list 'var len := (list 'length items))
                    (list 'var item := (list ':? (list '== len 0)
                                                 nil
                                                 (list '. items [len])))
                    (list ':= arr (list ':? (list '<= len 1)
                                            []
                                            (list 'head items -1)))]
                   (when sync [sync])
                   [item]))))

(defn r-tf-x-arr-push-first
  [[_ arr item]]
  (r-update-self arr
                 (list 'append (list 'list item)
                       (r-as-list arr))))

(defn r-tf-x-arr-pop-first
  [[_ arr]]
  (let [items (gensym "arr_items__")
        len   (gensym "arr_len__")
        item  (gensym "arr_item__")
        sync  (r-propagate-symbol arr)]
    (apply r-local-block
           (concat [(list 'var items := (r-as-list arr))
                    (list 'var len := (list 'length items))
                    (list 'var item := (list ':? (list '== len 0)
                                                 nil
                                                 (list '. items [1])))
                    (list ':= arr (list ':? (list '<= len 1)
                                            []
                                            (list 'tail items -1)))]
                   (when sync [sync])
                   [item]))))

(defn r-tf-x-arr-remove
  [[_ arr idx]]
  (let [items       (gensym "arr_items__")
        len         (gensym "arr_len__")
        pos         (gensym "arr_pos__")
        invalid     (gensym "arr_invalid__")
        item        (gensym "arr_item__")
        before      (gensym "arr_before__")
        after-count (gensym "arr_after_count__")
        after       (gensym "arr_after__")
        sync        (r-propagate-symbol arr)]
    (apply r-local-block
           (concat [(list 'var items := (r-as-list arr))
                    (list 'var len := (list 'length items))
                    (list 'var pos := (list '+ idx 1))
                    (list 'var invalid := (list 'or
                                                (list '< pos 1)
                                                (list '> pos len)))
                    (list 'var item := (list ':? invalid
                                             nil
                                             (list '. items [pos])))
                    (list 'var before := (list ':? (list '<= pos 1)
                                               []
                                               (list 'head items (list '- pos 1))))
                    (list 'var after-count := (list '- len pos))
                    (list 'var after := (list ':? (list '<= after-count 0)
                                              []
                                              (list 'tail items after-count)))
                    (list ':= arr (list ':? invalid
                                        items
                                        (list 'append before after)))]
                   (when sync [sync])
                   [item]))))

(defn r-tf-x-arr-insert
  [[_ arr idx item]]
  (r-update-self arr
                 (list 'append (r-as-list arr)
                       (list 'list item)
                       :after (list '- idx 1))))

(defn r-tf-x-arr-slice
  [[_ arr start finish]]
  (let [items      (gensym "arr_items__")
        start-idx  (gensym "arr_start__")
        finish-idx (gensym "arr_finish__")]
    (r-local-block
     (list 'var items := (r-as-list arr))
     (list 'var start-idx := (list '+ start 1))
     (list 'var finish-idx := (list ':? (list 'x:nil? finish)
                                        (list 'length items)
                                        (list 'min finish
                                              (list 'length items))))
     (list 'if (list '< finish-idx start-idx)
           []
           (r-unname
            (list 'lapply
                  (list 'seq.int start-idx finish-idx)
                  (list 'fn ['i]
                        (list 'return (list '. items ['i])))))))))

(defn r-tf-x-arr-clone
  [[_ arr]]
  (r-unname (r-as-list arr)))

(defn r-tf-x-arr-reverse
  [[_ arr]]
  (r-unname (list 'rev (r-as-list arr))))

(defn r-tf-x-obj-keys
  [[_ obj]]
  (list ':? (list 'x:nil? obj)
        []
        (r-as-list (list 'names obj))))

(defn r-tf-x-obj-vals
  [[_ obj]]
  (list ':? (list 'x:nil? obj)
        []
        (r-unname (r-as-list obj))))

(defn r-tf-x-obj-pairs
  [[_ obj]]
  (list ':? (list 'x:nil? obj)
        []
        (r-unname
         (list 'Map 'list
               (r-as-list (list 'names obj))
               (r-unname (r-as-list obj))))))

(defn r-tf-x-obj-clone
  [[_ obj]]
  (list ':? (list 'x:nil? obj)
        {}
        (r-as-list obj)))

(defn r-tf-x-obj-assign
  [[_ obj other]]
  (let [out  (gensym "obj_out__")
        vals (gensym "obj_vals__")
        sync (r-propagate-symbol obj)
        tail (if sync
               [(list ':= obj out)
                sync
                obj]
               [out])]
    (apply r-local-block
           (concat [(list 'var out := (list ':? (list 'x:nil? obj)
                                           {}
                                           (r-as-list obj)))
                    (list 'var vals := (list ':? (list 'x:nil? other)
                                            {}
                                            (r-as-list other)))
                    (list 'if (list 'and
                                    (list 'not (list 'x:nil? other))
                                    (list 'not (list 'is.null (list 'names vals))))
                          (list 'block
                                (list 'for ['k :in (list 'names vals)]
                                      (list ':= (list '. out [(list 'as.character 'k)])
                                            (list '. vals ['k]))))
                          nil)]
                   tail))))

(defn r-tf-x-arr-assign
  [[_ arr other]]
  (r-update-self arr
                 (list 'append (r-as-list arr)
                       (r-as-list other))))

(defn r-tf-x-arr-concat
  [[_ arr other]]
  (r-unname
   (list 'append (r-as-list arr)
         (r-as-list other))))

(defn r-tf-x-arr-sort
  [[_ arr key-fn comp-fn]]
  (let [items (gensym "arr_items__")
        len   (gensym "arr_len__")
        left  (gensym "left_key__")
        right (gensym "right_key__")
        tmp   (gensym "arr_tmp__")
        sync  (r-propagate-symbol arr)
        swap  (list 'block
                    (list 'var tmp := (list '. items ['i]))
                    (list ':= (list '. items ['i])
                          (list '. items ['j]))
                    (list ':= (list '. items ['j]) tmp))
        inner (list 'block
                    (list 'var left := (list key-fn (list '. items ['i])))
                    (list 'var right := (list key-fn (list '. items ['j])))
                    (list 'if (list 'not (list comp-fn left right))
                          swap
                          nil))
        body  (list 'block
                    (list 'for ['i :in (list 'seq_len (list '- len 1))]
                          (list 'for ['j :in (list 'seq (list '+ 'i 1) len 1)]
                                inner)))]
    (apply r-local-block
           (concat [(list 'var items := (r-as-list arr))
                    (list 'var len := (list 'length items))
                    (list 'if (list '> len 1)
                          body
                          nil)
                    (list ':= arr items)]
                   (when sync [sync])
                   [arr]))))

(defn r-tf-x-arr-each
  [[_ arr f]]
  (template/$
   (do (lapply (as.list ~arr) ~f)
       true)))

(defn r-tf-x-arr-every
  [[_ arr pred]]
  (list 'all
        (list 'vapply (r-as-list arr)
              pred
              (list 'logical 1))))

(defn r-tf-x-arr-some
  [[_ arr pred]]
  (list 'any
        (list 'vapply (r-as-list arr)
              pred
              (list 'logical 1))))

(defn r-tf-x-arr-find
  [[_ arr pred]]
  (let [matches (gensym "arr_matches__")
        idx     (gensym "arr_idx__")]
    (r-local-block
     (list 'var matches := (list 'vapply (r-as-list arr)
                                 pred
                                 (list 'logical 1)))
     (list 'var idx := (list 'match true matches))
     (list ':? (list 'is.na idx)
           -1
           (list '- idx 1)))))

(defn r-tf-x-arr-map
  [[_ arr f]]
  (r-unname
   (list 'lapply (r-as-list arr) f)))

(defn r-tf-x-arr-filter
  [[_ arr pred]]
  (r-unname
   (list 'Filter pred (r-as-list arr))))

(defn r-tf-x-arr-foldl
  [[_ arr f init]]
  (list 'Reduce f (r-as-list arr)
        :init init))

(defn r-tf-x-arr-foldr
  [[_ arr f init]]
  (template/$
   (Reduce (fn [e out]
             (return (~f out e)))
           (as.list ~arr)
           :init ~init
           :right true)))

(def +r-arr+
  {:x-arr-clone       {:macro #'r-tf-x-arr-clone       :emit :macro}
   :x-arr-reverse     {:macro #'r-tf-x-arr-reverse     :emit :macro}
   :x-arr-push        {:macro #'r-tf-x-arr-push        :emit :macro}
   :x-arr-pop         {:macro #'r-tf-x-arr-pop         :emit :macro}
   :x-arr-push-first  {:macro #'r-tf-x-arr-push-first  :emit :macro}
   :x-arr-pop-first   {:macro #'r-tf-x-arr-pop-first   :emit :macro}
   :x-arr-remove      {:macro #'r-tf-x-arr-remove      :emit :macro}
   :x-arr-insert      {:macro #'r-tf-x-arr-insert      :emit :macro}
   :x-arr-slice       {:macro #'r-tf-x-arr-slice       :emit :macro}
   :x-arr-assign      {:macro #'r-tf-x-arr-assign      :emit :macro}
   :x-arr-concat      {:macro #'r-tf-x-arr-concat      :emit :macro}
   :x-arr-sort        {:macro #'r-tf-x-arr-sort        :emit :macro}
   :x-arr-each        {:macro #'r-tf-x-arr-each        :emit :macro}
   :x-arr-every       {:macro #'r-tf-x-arr-every       :emit :macro}
   :x-arr-some        {:macro #'r-tf-x-arr-some        :emit :macro}
   :x-arr-find        {:macro #'r-tf-x-arr-find        :emit :macro}
   :x-arr-map         {:macro #'r-tf-x-arr-map         :emit :macro}
   :x-arr-filter      {:macro #'r-tf-x-arr-filter      :emit :macro}
   :x-arr-foldl       {:macro #'r-tf-x-arr-foldl       :emit :macro}
   :x-arr-foldr       {:macro #'r-tf-x-arr-foldr       :emit :macro}
   :x-obj-keys        {:macro #'r-tf-x-obj-keys        :emit :macro}
   :x-obj-vals        {:macro #'r-tf-x-obj-vals        :emit :macro}
   :x-obj-pairs       {:macro #'r-tf-x-obj-pairs       :emit :macro}
   :x-obj-clone       {:macro #'r-tf-x-obj-clone       :emit :macro}
   :x-obj-assign      {:macro #'r-tf-x-obj-assign      :emit :macro}})

;;
;; STRING
;;

(defn r-tf-x-str-len
  ([[_ s]]
   (list 'nchar s)))

(defn r-tf-x-str-split
  ([[_ s tok]]
   (list 'unlist (list 'strsplit s tok :fixed true))))

(defn r-tf-x-str-join
  ([[_ s arr]]
   (list 'paste arr :collapse s)))

(defn r-tf-x-str-index-of
  ([[_ s tok & [start]]]
   (let [start-expr (list ':? (list 'or
                                    (list 'is.null start)
                                    (list '<= start 1))
                          1
                          start)
         segment    (list ':? (list '<= start-expr 1)
                          s
                          (list 'substr s start-expr (list 'nchar s)))
         idx        (list 'regexpr tok segment :fixed true)]
     (list ':?
           (list '< idx 0)
           idx
           (list '+
                 (list '- start-expr 1)
                 (list '- idx 1))))))

(defn r-tf-x-str-substring
  ([[_ s start & [end]]]
   (list 'substr s
         start
         (if end
           (list ':? (list 'x:nil? end)
                 (list 'nchar s)
                 end)
           (list 'nchar s)))))

(defn r-tf-x-str-char
  ([[_ s i]]
   (list '. (list 'utf8ToInt (list 'substr s i i)) [1])))

(defn r-tf-x-str-to-upper
  ([[_ s]]
   (list 'toupper s)))

(defn r-tf-x-str-to-lower
  ([[_ s]]
   (list 'tolower s)))

(defn r-tf-x-str-to-fixed
  ([[_ n digits]]
   (list 'sprintf (list 'paste0 "%." digits "f") n)))

(defn r-tf-x-str-replace
  ([[_ s tok replacement]]
   (list 'gsub tok replacement s)))

(defn r-tf-x-str-trim
  ([[_ s]]
   (list 'trimws s)))

(defn r-tf-x-str-trim-left
  ([[_ s]]
   (list 'trimws s :which "left")))

(defn r-tf-x-str-trim-right
  ([[_ s]]
   (list 'trimws s :which "right")))

(defn r-tf-x-str-comp
  [[_ a b]]
  (list '< a b))

(def +r-str+
  {:x-str-len        {:macro #'r-tf-x-str-len        :emit :macro}
   :x-str-split      {:macro #'r-tf-x-str-split      :emit :macro}
   :x-str-join       {:macro #'r-tf-x-str-join       :emit :macro}
   :x-str-index-of   {:macro #'r-tf-x-str-index-of   :emit :macro}
   :x-str-substring  {:macro #'r-tf-x-str-substring  :emit :macro}
   :x-str-char       {:macro #'r-tf-x-str-char       :emit :macro}
   :x-str-to-upper   {:macro #'r-tf-x-str-to-upper   :emit :macro}
   :x-str-to-lower   {:macro #'r-tf-x-str-to-lower   :emit :macro}
   :x-str-to-fixed   {:macro #'r-tf-x-str-to-fixed   :emit :macro}
   :x-str-replace    {:macro #'r-tf-x-str-replace    :emit :macro}
   :x-str-trim       {:macro #'r-tf-x-str-trim       :emit :macro}
   :x-str-trim-left  {:macro #'r-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right {:macro #'r-tf-x-str-trim-right :emit :macro}
   :x-str-comp       {:macro #'r-tf-x-str-comp       :emit :macro}})


;;
;; JSON
;;

(defn r-tf-x-json-encode
  ([[_ obj]]
   (list 'toJSON obj :auto-unbox true)))

(defn r-tf-x-json-decode
  ([[_ s]]
   (list 'fromJSON s)))

(def +r-js+
  {:x-json-encode      {:macro #'r-tf-x-json-encode      :emit :macro}
   :x-json-decode      {:macro #'r-tf-x-json-decode      :emit :macro}})



;;
;; COM
;;

(defn r-tf-x-return-encode
  ([[_ out id key]]
   (template/$
     (do* (library "jsonlite")
          (var outtype := (x:type-native ~out))
          (var payload := {:type "data"
                           :return outtype
                           :value ~out})
          (when (not (is.null ~id))
            (:= (. payload ["id"]) ~id))
          (when (not (is.null ~key))
            (:= (. payload ["key"]) ~key))
         (when (== "function" outtype)
           (:= (. payload ["type"]) "raw")
           (:= (. payload ["value"]) (toString ~out)))
         (tryCatch
          (block
            (return (toJSON payload :auto-unbox true :null "null")))
          :error (fn [err]
                   (do (:= (. payload ["type"]) "raw")
                       (:= (. payload ["value"]) (toString ~out))
                       (return (toJSON payload :auto-unbox true :null "null")))))))))

(defn r-tf-x-return-wrap
  ([[_ f encode-fn]]
   (template/$
      (do* (library "jsonlite")
           (tryCatch
            (block
              (var out := (~f))
              (tryCatch
               (block
                 (~encode-fn out))
               :error (fn [encode-err]
                        (~encode-fn out nil nil))))
            :error (fn [err]
                     (toJSON {:type "error"
                              :return "error"
                              :message (conditionMessage err)
                              :call (toString (conditionCall err))
                              :value (toString err)}
                            :auto-unbox true :null "null")))))))

(defn r-tf-x-return-eval
  ([[_ s wrap-fn]]
   (template/$ (return (~wrap-fn
                 (fn []
                   (eval (parse :text ~s))))))))

(def +r-return+
  {:x-return-encode  {:macro #'r-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'r-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'r-tf-x-return-eval     :emit :macro}})

(defn r-tf-x-bit-and
  [[_ i1 i2]]
  (list 'bitwAnd i1 i2))

(defn r-tf-x-bit-or
  [[_ i1 i2]]
  (list 'bitwOr i1 i2))

(defn r-tf-x-bit-lshift
  [[_ x n]]
  (list 'bitwShiftL x n))

(defn r-tf-x-bit-rshift
  [[_ x n]]
  (list 'bitwShiftR x n))

(defn r-tf-x-bit-xor
  [[_ x n]]
  (list 'bitwXor x n))

(def +r-bit+
  {:x-bit-and        {:macro #'r-tf-x-bit-and    :emit :macro}
   :x-bit-or         {:macro #'r-tf-x-bit-or     :emit :macro}
   :x-bit-lshift     {:macro #'r-tf-x-bit-lshift :emit :macro}
   :x-bit-rshift     {:macro #'r-tf-x-bit-rshift :emit :macro}
   :x-bit-xor        {:macro #'r-tf-x-bit-xor    :emit :macro}})

;;
;; SHELL
;;

(defn r-tf-x-pwd
  [[_]]
  '(getwd))

(def +r-shell+
  {:x-pwd            {:macro #'r-tf-x-pwd    :emit :macro}
   :x-shell          {:macro #'r-tf-x-shell  :emit :macro
                      :op-spec {:allow-blocks true}}})

;;
;; FILE
;;

(defn r-tf-x-file-resolve
  [[_ root relpath]]
  (template/$
   (normalizePath
    (:? (or (is.null ~root)
            (grepl "^/" ~relpath))
        ~relpath
        (file.path ~root ~relpath)))))

(defn r-tf-x-file-slurp
  [[_ filename cb]]
  (template/$
   (tryCatch
    (block
      (var lines := (readLines ~filename :warn false))
      (~cb nil (paste lines :collapse "\n")))
    :error (fn [err]
             (~cb err nil)))))

(defn r-tf-x-file-spit
  [[_ filename content cb]]
  (template/$
   (tryCatch
    (block
      (cat ~content :file ~filename :sep "")
      (~cb nil ~filename))
    :error (fn [err]
             (~cb err nil)))))

(def +r-file+
  {:x-file-resolve   {:macro #'r-tf-x-file-resolve  :emit :macro}
   :x-file-slurp     {:macro #'r-tf-x-file-slurp    :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-file-spit      {:macro #'r-tf-x-file-spit     :emit :macro
                      :op-spec {:allow-blocks true}}})

(defn r-tf-x-socket-connect
  ([[_ host port opts cb]]
   (template/$
    (tryCatch
     (block
       (var conn := (socketConnection :host ~host
                                      :port ~port
                                      :blocking true))
       (~cb nil conn))
     :error (fn [err]
              (~cb err nil))))))

(defn r-tf-x-socket-send
  ([[_ conn s]]
   (template/$
    (do (writeLines ~s ~conn :sep "")
        (flush ~conn)))))

(defn r-tf-x-socket-close
  ([[_ conn]]
   (template/$ (close ~conn))))

(def +r-socket+
  {:x-socket-connect      {:macro #'r-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'r-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'r-tf-x-socket-close        :emit :macro}})

;;
;; ITER
;;

(defn r-tf-x-iter-mark
  [value]
  (list 'structure value :class "xt_iterator"))

(defn r-tf-x-iter-from-obj
  [[_ obj]]
  (r-tf-x-iter-mark
   (r-unname
    (list 'Map 'list
          (r-as-list (list 'names obj))
          (r-unname (r-as-list obj))))))

(defn r-tf-x-iter-from-arr
  [[_ arr]]
  (r-tf-x-iter-mark
   (r-unname (r-as-list arr))))

(defn r-tf-x-iter-from
  [[_ obj]]
  (template/$
   (:? (x:iter-native? ~obj)
       ~obj
       (:? (x:is-array? ~obj)
           (x:iter-from-arr ~obj)
           (:? (and (x:is-object? ~obj)
                    (x:has-key? ~obj "::" "iterator")
                    (x:has-key? ~obj "iterator"))
               (x:get-key ~obj "iterator")
               nil)))))

(defn r-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (template/$
   (if (not (== (length ~it0) (length ~it1)))
     (return false)
     (do (for [i :in (seq_len (length ~it0))]
           (if (not (~eq-fn (. ~it0 [i])
                            (. ~it1 [i])))
             (return false)))
         (return true)))))

(defn r-tf-x-iter-next
  [[_ it]]
  (template/$
   (:? (== 0 (length ~it))
       nil
       (. ~it [1]))))

(defn r-tf-x-iter-has?
  [[_ obj]]
  (template/$
   (isTRUE
    (or (x:iter-native? ~obj)
        (x:is-array? ~obj)
        (and (x:is-object? ~obj)
             (x:has-key? ~obj "::" "iterator")
             (x:has-key? ~obj "iterator"))))))

(defn r-tf-x-iter-native?
  [[_ it]]
  (list 'isTRUE (list 'inherits it "xt_iterator")))

(def +r-iter+
  {:x-iter-from-obj       {:macro #'r-tf-x-iter-from-obj       :emit :macro}
   :x-iter-from-arr       {:macro #'r-tf-x-iter-from-arr       :emit :macro}
   :x-iter-from           {:macro #'r-tf-x-iter-from           :emit :macro}
   :x-iter-eq             {:macro #'r-tf-x-iter-eq             :emit :macro}
   :x-iter-null           {:default '(structure (list) :class "xt_iterator")
                           :emit :unit}
   :x-iter-next           {:macro #'r-tf-x-iter-next           :emit :macro}
   :x-iter-has?           {:macro #'r-tf-x-iter-has?           :emit :macro}
   :x-iter-native?        {:macro #'r-tf-x-iter-native?        :emit :macro}})

;;
;; THREAD
;;

(def +r-thread+
  {})

;;
;; PROMISE
;;

(def +r-promise+
  {})

(def +r+
  (merge +r-core+
          +r-global+
          +r-custom+
          +r-math+
          +r-bit+
          +r-type+
          +r-lu+
          +r-arr+
          +r-str+
          +r-js+
          +r-return+
          +r-shell+
          +r-file+
          +r-socket+
          +r-iter+
          +r-thread+
          +r-promise+))


(comment

  
;;
;; OBJ
;;


(defn r-tf-x-obj-keys
  [[_ m]]
  (list 'names m))

(defn r-tf-x-obj-vals
  [[_ m]]
  (list 'c (list 'unlist  m)))

(def +r-obj+
  {:x-obj-keys       {:macro #'r-tf-x-obj-keys       :emit :macro}
   :x-obj-vals       {:macro #'r-tf-x-obj-vals       :emit :macro}})

;;
;; FN
;;

(def +r-fn+
  {})

(defn r-tf-x-arr-clone
  [[_ arr]]
  (list 'append [] arr))

(defn r-tf-x-arr-slice
  [[_ arr start end]]
  (list :% arr \[ (list :to (+ start 1) end) \]))

:x-arr-clone       {:macro #'r-tf-x-arr-clone   :emit :macro}
:x-arr-slice       {:macro #'r-tf-x-arr-slice   :emit :macro}

  
         
  )
