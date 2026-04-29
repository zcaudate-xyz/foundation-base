(ns std.lang.model.spec-elisp
  (:require [clojure.string :as str]
             [std.lang.base.book :as book]
             [std.lang.base.emit-common :as emit-common]
             [std.lang.base.grammar :as grammar]
             [std.lang.base.script :as script]
             [std.lang.model.spec-lisp-common :as common]
             [std.lang.model.spec-xtalk]
             [std.lang.model.spec-xtalk.fn-elisp :as fn]
            [std.lib.collection :as collection]))

(defn elisp-tf-break
  [_]
  (list '__xt_break_throw__))

(defn elisp-tf-bsl
  [[_ x n]]
  (list 'lsh x n))

(defn elisp-tf-bsr
  [[_ x n]]
  (list 'lsh x (list '- n)))

(defn elisp-tf-bxor
  [[_ x n]]
  (list 'logxor x n))

(defn elisp-tf-band
  [[_ x n]]
  (list 'logand x n))

(defn elisp-tf-bor
  [[_ x n]]
  (list 'logior x n))

(defn elisp-tf-mod
  [[_ x n]]
  (list 'mod x n))

(defn elisp-tf-pow
  [[_ x n]]
  (list 'expt x n))

(defn elisp-tf-xor
  [[_ a b]]
  (list '__xt_xor__ a b))

(defn elisp-tf-throw
  [[_ x]]
  (if (and (collection/form? x)
           (or (contains? #{'x:ex 'x:ex-new} (first x))
               (and (= 'vector (first x))
                    (= "__xt_error__" (second x)))))
    (list 'do
          (list 'var 'err x)
          (list 'signal
                (list 'intern "error")
                (list 'list
                      (list 'x:ex-message 'err)
                      (list 'x:ex-data 'err))))
    (list 'error "%s" x)))

(defn elisp-tf-for-array
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i v] e]
      (list 'do
            (list 'var i 0)
            (list 'catch
                  :__xt_break__
                  (apply list 'while
                         (list '< i (list 'x:len arr))
                         (concat [(list 'var v (list '. arr [i]))]
                                  body
                                  [(list ':= i (list '+ i 1))])))))
    (let [idx (gensym "idx__")]
      (list 'do
            (list 'var idx 0)
            (list 'catch
                  :__xt_break__
                  (apply list 'while
                         (list '< idx (list 'x:len arr))
                         (concat [(list 'var e (list '. arr [idx]))]
                                  body
                                  [(list ':= idx (list '+ idx 1))])))))))

(defn elisp-tf-for-object
  [[_ [[k v] m] & body]]
  (let [keys (gensym "keys__")
        idx  (gensym "idx__")
        key  (if (= k '_) (gensym "key__") k)]
    (list 'do
          (list 'var keys (list 'x:obj-keys m))
          (list 'var idx 0)
          (list 'catch
                :__xt_break__
                (apply list 'while
                       (list '< idx (list 'x:len keys))
                       (concat [(list 'var key (list '. keys [idx]))]
                                (if (not= v '_)
                                  [(list 'var v (list '. m [key]))]
                                 [])
                               body
                               [(list ':= idx (list '+ idx 1))]))))))

(defn elisp-tf-for-iter
  [[_ [e it] & body]]
  (let [iter (gensym "iter__")]
    (list 'do
          (list 'var iter it)
          (list 'if
                (list 'x:iter-native? iter)
                (list 'catch
                      :__xt_break__
                      (apply list 'while 't
                             (concat [(list 'var e (list 'x:iter-next iter))
                                      (list 'if
                                            (list 'equal e "__xt_iter_end__")
                                            (list '__xt_break_throw__)
                                            nil)]
                                     body)))
                (elisp-tf-for-array (list* 'for:array [e iter] body))))))

(defn elisp-tf-for-index
  [[_ [i [start stop step]] & body]]
  (let [step (or step 1)
        sign (if (and (number? step)
                      (neg? step))
               '>
               '<)]
    (list 'do
          (list 'var i start)
          (list 'catch
                :__xt_break__
                (apply list 'while
                       (list sign i stop)
                       (concat body
                               [(list ':= i (list '+ i step))]))))))

(def +elisp-local-override+
  {:break {:macro #'elisp-tf-break :emit :macro}
   :mod   {:macro #'elisp-tf-mod   :emit :macro}
   :pow   {:macro #'elisp-tf-pow   :emit :macro}})

(def +elisp-local-extend+
  {:with-global {:op :with-global :symbol #{'!:G}        :emit :with-global}
   :xor        {:op :xor         :symbol #{'xor}         :macro #'elisp-tf-xor        :emit :macro}
   :band       {:op :band        :symbol #{'b:&}         :macro #'elisp-tf-band       :emit :macro}
   :bor        {:op :bor         :symbol #{'b:|}         :macro #'elisp-tf-bor        :emit :macro}
   :bxor       {:op :bxor        :symbol #{'b:xor}       :macro #'elisp-tf-bxor       :emit :macro}
   :bsl        {:op :bsl         :symbol #{'b:<<}        :macro #'elisp-tf-bsl        :emit :macro}
   :bsr        {:op :bsr         :symbol #{'b:>>}        :macro #'elisp-tf-bsr        :emit :macro}
   :throw      {:op :throw       :symbol #{'throw}       :macro #'elisp-tf-throw      :emit :macro}
   :for-array  {:op :for-array   :symbol #{'for:array}   :macro #'elisp-tf-for-array  :emit :macro}
   :for-object {:op :for-object  :symbol #{'for:object}  :macro #'elisp-tf-for-object :emit :macro}
   :for-iter   {:op :for-iter    :symbol #{'for:iter}    :macro #'elisp-tf-for-iter   :emit :macro}
   :for-index  {:op :for-index   :symbol #{'for:index}   :macro #'elisp-tf-for-index  :emit :macro}})

(def +features+
  (-> (grammar/build-min [:coroutine
                          :xtalk])
      (merge (grammar/build-xtalk))
      (grammar/build:override +elisp-local-override+)
      (grammar/build:override fn/+elisp+)
      (grammar/build:extend +elisp-local-extend+)))

(def +reserved+
  (grammar/to-reserved +features+))

(defn elisp-expand
  [form]
  (common/expand-form +reserved+ form))

(defn elisp-invoke
  [[sym & args :as form] grammar mopts]
  (let [{:keys [keyword-fn]} (get-in grammar [:default :invoke])]
    (cond (keyword? sym)
          (cond keyword-fn
                (keyword-fn form grammar mopts)

                (namespace sym)
                (emit-common/emit-invoke-static form grammar mopts)

                :else
                (emit-common/emit-invoke-typecast form grammar mopts))

          (symbol? sym)
          (emit-common/emit-invoke-raw
           (emit-common/emit-wrapping sym grammar mopts)
           args
           grammar
           mopts)

          :else
          (emit-common/emit-invoke-raw "funcall" (cons sym args) grammar mopts))))

(defn elisp-normalize-funcalls
  [form]
  (cond (collection/form? form)
        (let [head (first form)]
          (if (= 'let head)
            (let [bindings (second form)
                  body     (drop 2 form)]
              (list* 'let
                     (apply list
                            (map (fn [binding]
                                   (if (and (collection/form? binding)
                                            (= 2 (count binding)))
                                     (list (first binding)
                                           (elisp-normalize-funcalls (second binding)))
                                     (elisp-normalize-funcalls binding)))
                                 bindings))
                     (map elisp-normalize-funcalls body)))
            (let [items (apply list (map elisp-normalize-funcalls form))
                  head  (first items)]
              (if (collection/form? head)
                (apply list 'funcall items)
                items))))

        (vector? form)
        (mapv elisp-normalize-funcalls form)

        (map? form)
        (into (empty form)
              (map (fn [[k v]]
                     [(elisp-normalize-funcalls k)
                      (elisp-normalize-funcalls v)]))
              form)

        (set? form)
        (set (map elisp-normalize-funcalls form))

        :else
        form))

(defn- elisp-function-prologue
  [args]
  [])

(def +elisp-transform-config+
  {:begin 'progn
    :reserved +reserved+
   :lambda-form (fn [args body]
                   (list* 'lambda
                          (apply list args)
                          (concat (elisp-function-prologue args)
                                  body)))
    :defn-form (fn [sym args body]
                 (list* 'defun
                        sym
                        (apply list args)
                        (concat (elisp-function-prologue args)
                                body)))
   :let-form (fn [bindings body]
               (list* 'let (apply list bindings) body))
   :while-form (fn [test body]
                 (list* 'while test body))
   :try-form (fn [body catch finally]
               (let [body-form (if (= 1 (count body))
                                 (first body)
                                 (cons 'progn body))
                      caught    (if catch
                                  (let [raw-sym (gensym "err__")
                                        bind-sym (or (:sym catch) 'err)
                                        catch-form (if (= 1 (count (:body catch)))
                                                     (first (:body catch))
                                                     (cons 'progn (:body catch)))]
                                     (list 'condition-case raw-sym
                                           body-form
                                           (list 'error
                                                 (list 'let
                                                       (list (list bind-sym
                                                                   (list 'error-message-string raw-sym)))
                                                       catch-form))))
                                  body-form)]
                 (if (seq finally)
                   (list* 'unwind-protect caught finally)
                   caught)))
    :not-equal-form (fn [[x y]]
                      (list 'not (list 'equal x y)))
    :equal-form (fn [[x y]]
                  (list 'equal x y))
    :nil-form (fn [x]
                (list 'null x))
    :assign-symbol-form (fn [sym value]
                          (list 'progn
                                (list 'setq sym value)
                                (list 'if
                                      (list 'functionp sym)
                                      (list 'fset (list 'intern (name sym)) sym)
                                      nil)
                                sym))
   :index-read-form (fn [obj key kind]
                      (case kind
                        :key (list 'gethash key obj)
                        :idx (list 'aref obj key)
                        :auto (list 'if
                                    (list 'hash-table-p obj)
                                    (list 'gethash key obj)
                                    (list 'aref obj key))))
    :index-write-form (fn [obj key value kind]
                        (case kind
                          :key (list 'progn
                                     (list 'puthash key value obj)
                                     obj)
                          :idx (list 'progn
                                     (list 'aset obj key value)
                                     obj)
                          :auto (list 'if
                                      (list 'hash-table-p obj)
                                      (list 'progn
                                            (list 'puthash key value obj)
                                            obj)
                                      (list 'progn
                                            (list 'aset obj key value)
                                            obj))))
   :global-symbol '__xt_globals__
   :global-read-form (fn [global key]
                       (list 'gethash key global))
   :global-write-form (fn [global key value]
                        (list 'progn
                              (list 'puthash key value global)
                              global))})

(defn elisp-transform
  [form]
  (common/transform-form +elisp-transform-config+ form))

(declare emit-elisp-form)

(defn emit-elisp-coll
  [start end coll]
  (str start
       (str/join " " (map emit-elisp-form coll))
       end))

(defn emit-elisp-map
  [m]
  (let [table '__xt_tbl]
    (emit-elisp-form
     (list 'let
           (list (list table (list 'make-hash-table :test (list 'quote 'equal))))
           (cons 'progn
                 (concat
                  (map (fn [[k v]]
                         (list 'puthash
                               (if (keyword? k) (name k) k)
                               v
                               table))
                       m)
                  [table]))))))

(defn emit-elisp-form
  [form]
  (cond (nil? form)      "nil"
        (true? form)     "t"
        (false? form)    "nil"
        (string? form)   (pr-str form)
        (keyword? form)  (str form)
        (number? form)   (str form)
        (symbol? form)   (if (namespace form)
                           (name form)
                           (str form))
        (map? form)      (emit-elisp-map form)
        (vector? form)   (emit-elisp-coll "(vector " ")" form)
        (set? form)      (emit-elisp-coll "(list " ")" form)
        (collection/form? form)
        (emit-elisp-coll "(" ")" form)
        :else
        (pr-str form)))

(defn emit-elisp
  "emits code into emacs lisp schema"
  {:added "4.1"}
  [form mopts]
  (-> (common/prepare-top-level 'progn form)
      (elisp-expand)
      (elisp-transform)
      (elisp-normalize-funcalls)
      (emit-elisp-form)))

(def +grammar+
  (grammar/grammar :elisp
    +reserved+
    {:default {:invoke {:custom #'elisp-invoke}}
     :emit #'emit-elisp}))

(def +meta+ (book/book-meta {}))

(def +book+
  (book/book {:lang :elisp
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
