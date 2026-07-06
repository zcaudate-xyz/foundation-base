(ns hara.model.spec-scheme
  (:require [clojure.string :as str]
            [hara.lang.book :as book]
            [hara.common.grammar :as grammar]
            [hara.lang.script :as script]
            [hara.model.spec-lisp-common :as common]
            [hara.model.spec-xtalk]
            [hara.model.spec-xtalk.fn-scheme :as fn]
            [std.lib.collection :as collection]))

(defn scheme-tf-break
  [_]
  (list '__xt_break__ (list 'void)))

(defn scheme-tf-bsl
  [[_ x n]]
  (list 'arithmetic-shift x n))

(defn scheme-tf-bsr
  [[_ x n]]
  (list 'arithmetic-shift x (list '- n)))

(defn scheme-tf-bxor
  [[_ x n]]
  (list 'bitwise-xor x n))

(defn scheme-tf-band
  [[_ x n]]
  (list 'bitwise-and x n))

(defn scheme-tf-bor
  [[_ x n]]
  (list 'bitwise-ior x n))

(defn scheme-tf-mod
  [[_ x n]]
  (list 'modulo x n))

(defn scheme-tf-pow
  [[_ x n]]
  (list 'expt x n))

(defn scheme-tf-xor
  [[_ a b]]
  (list 'if a b (list 'not b)))

(defn scheme-tf-throw
  [[_ x]]
  (if (or (map? x)
          (and (collection/form? x)
               (let [op (first x)]
                 (and (symbol? op)
                      (contains? #{"x:ex" "x:ex-new"} (name op))))))
    (list 'raise x)
    (list 'error x)))

(defn scheme-tf-for-array
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i v] e]
      (list 'do
            (list 'var i 0)
            (list 'let/ec '__xt_break__
                  (apply list 'while
                         (list '< i (list 'x:len arr))
                         (concat [(list 'var v (list '. arr [i]))]
                                 body
                                 [(list ':= i (list '+ i 1))])))))
    (let [idx (gensym "idx__")]
      (list 'do
            (list 'var idx 0)
            (list 'let/ec '__xt_break__
                  (apply list 'while
                         (list '< idx (list 'x:len arr))
                         (concat [(list 'var e (list '. arr [idx]))]
                                 body
                                 [(list ':= idx (list '+ idx 1))])))))))

(defn scheme-tf-for-object
  [[_ [[k v] m] & body]]
  (let [keys (gensym "keys__")
        idx  (gensym "idx__")
        key  (if (= k '_) (gensym "key__") k)]
    (list 'do
          (list 'var keys (list 'x:obj-keys m))
          (list 'var idx 0)
          (list 'let/ec '__xt_break__
                (apply list 'while
                       (list '< idx (list 'x:len keys))
                       (concat [(list 'var key (list '. keys [idx]))]
                               (if (not= v '_)
                                 [(list 'var v (list '. m [key]))]
                                 [])
                               body
                               [(list ':= idx (list '+ idx 1))]))))))

(defn scheme-tf-for-iter
  [[_ [e it] & body]]
  (let [iter (gensym "iter__")]
    (list 'do
          (list 'var iter it)
          (list 'if
                (list 'x:iter-native? iter)
                (list 'let/ec '__xt_break__
                      (apply list 'while true
                             (concat [(list 'var e (list 'x:iter-next iter))
                                      (list 'if
                                            (list 'equal? e "__xt_iter_end__")
                                            (list '__xt_break__ (list 'void))
                                            nil)]
                                     body)))
                (scheme-tf-for-array (list* 'for:array [e iter] body))))))

(defn scheme-tf-for-index
  [[_ [i [start stop step]] & body]]
  (let [step (or step 1)
        sign (if (and (number? step)
                      (neg? step))
               '>
               '<)]
    (list 'do
          (list 'var i start)
          (list 'let/ec '__xt_break__
                (apply list 'while
                       (list sign i stop)
                       (concat body
                               [(list ':= i (list '+ i step))]))))))

(def +scheme-local-override+
  {:break {:macro #'scheme-tf-break :emit :macro}
   :mod   {:macro #'scheme-tf-mod   :emit :macro}
   :pow   {:macro #'scheme-tf-pow   :emit :macro}
   :or    {:macro #'fn/scheme-tf-or    :emit :macro}
   :and   {:macro #'fn/scheme-tf-and   :emit :macro}
   :internal-str {:macro #'fn/scheme-tf--%%- :emit :macro}})

(def +scheme-local-extend+
  {:with-global {:op :with-global :symbol #{'!:G}        :emit :with-global}
   :xor        {:op :xor         :symbol #{'xor}         :macro #'scheme-tf-xor        :emit :macro}
   :band       {:op :band        :symbol #{'b:&}         :macro #'scheme-tf-band       :emit :macro}
   :bor        {:op :bor         :symbol #{'b:|}         :macro #'scheme-tf-bor        :emit :macro}
   :bxor       {:op :bxor        :symbol #{'b:xor}       :macro #'scheme-tf-bxor       :emit :macro}
   :bsl        {:op :bsl         :symbol #{'b:<<}        :macro #'scheme-tf-bsl        :emit :macro}
   :bsr        {:op :bsr         :symbol #{'b:>>}        :macro #'scheme-tf-bsr        :emit :macro}
   :throw      {:op :throw       :symbol #{'throw}       :macro #'scheme-tf-throw      :emit :macro}
   :for-array  {:op :for-array   :symbol #{'for:array}   :macro #'scheme-tf-for-array  :emit :macro}
   :for-object {:op :for-object  :symbol #{'for:object}  :macro #'scheme-tf-for-object :emit :macro}
   :for-iter   {:op :for-iter    :symbol #{'for:iter}    :macro #'scheme-tf-for-iter   :emit :macro}
   :for-index  {:op :for-index   :symbol #{'for:index}   :macro #'scheme-tf-for-index  :emit :macro}
   :prototype-create {:op :prototype-create :symbol #{'proto:create}
                      :macro #'fn/scheme-tf-x-prototype-create :emit :macro}
   :prototype-get    {:op :prototype-get    :symbol #{'proto:get}
                      :macro #'fn/scheme-tf-x-prototype-get    :emit :macro}
   :prototype-set    {:op :prototype-set    :symbol #{'proto:set}
                      :macro #'fn/scheme-tf-x-prototype-set    :emit :macro}
   :prototype-method {:op :prototype-method :symbol #{'proto:method}
                      :macro #'fn/scheme-tf-x-prototype-method :emit :macro}})

(def +features+
  (-> (grammar/build-min [:coroutine
                          :xtalk])
      (merge (grammar/build-xtalk))
      (grammar/build:override +scheme-local-override+)
      (grammar/build:override fn/+scheme+)
      (grammar/build:extend +scheme-local-extend+)))

(def +reserved+
  (grammar/to-reserved +features+))

(defn scheme-expand
  [form]
  (common/expand-form +reserved+ form))

 (def +scheme-transform-config+
   {:begin 'begin
     :reserved +reserved+
    :def-form (fn [sym value]
                (list 'define sym value))
    :lambda-form (fn [args body]
                    (let [body (if (seq body)
                                 body
                                 ['(void)])]
                      (list* 'lambda
                             (apply list args)
                             body)))
     :defn-form (fn [sym args body]
                  (list* 'define
                         (list* sym args)
                        body))
    :let-form (fn [bindings body]
                (list* 'let* (apply list bindings) body))
   :while-form (fn [test body]
                 (list* 'while test body))
    :try-form (fn [body catch finally]
                (let [body-form (if (= 1 (count body))
                                  (first body)
                                  (cons 'begin body))
                      caught    (if catch
                                  (let [raw-sym (gensym "err__")
                                        bind-sym (or (:sym catch) 'err)
                                        catch-form (if (= 1 (count (:body catch)))
                                                     (first (:body catch))
                                                     (cons 'begin (:body catch)))]
                                    (list 'with-handlers
                                          (list (list (list 'lambda (list raw-sym) true)
                                                       (list 'lambda
                                                             (list raw-sym)
                                                             (list 'let
                                                                   (list (list bind-sym
                                                                               (list 'if
                                                                                     (list 'exn:fail? raw-sym)
                                                                                     (list 'exn-message raw-sym)
                                                                                     raw-sym)))
                                                                   catch-form))))
                                           body-form))
                                  body-form)]
                 (if (seq finally)
                   (list 'dynamic-wind
                         (list 'lambda '() (list 'void))
                         (list 'lambda '() caught)
                         (list 'lambda '() (if (= 1 (count finally))
                                             (first finally)
                                             (cons 'begin finally))))
                   caught)))
   :not-equal-form (fn [[x y]]
                     (list 'not (list 'equal? x y)))
   :equal-form (fn [[x y]]
                 (list 'equal? x y))
   :nil-form (fn [x]
               (list 'null? x))
   :assign-symbol-form (fn [sym value]
                         (list 'set! sym value))
   :index-read-form (fn [obj key kind]
                      (case kind
                        :key (list 'hash-ref obj key nil)
                        :idx (list 'vector-ref obj key)
                        :auto (list 'if
                                    (list 'hash? obj)
                                    (list 'hash-ref obj key nil)
                                    (list 'vector-ref obj key))))
    :index-write-form (fn [obj key value kind]
                        (case kind
                          :key (list 'begin
                                     (list 'hash-set! obj key value)
                                     obj)
                          :idx (list 'begin
                                     (list 'vector-set! obj key value)
                                     obj)
                          :auto (list 'if
                                      (list 'hash? obj)
                                      (list 'begin
                                            (list 'hash-set! obj key value)
                                            obj)
                                      (list 'begin
                                            (list 'vector-set! obj key value)
                                            obj))))
    :global-symbol '__xt_globals__
    :global-read-form (fn [global key]
                        (list 'hash-ref global
                              (if (symbol? key) (name key) key)
                              nil))
   :global-write-form (fn [global key value]
                        (list 'begin
                              (list 'hash-set! global key value)
                              global))})

(defn scheme-transform
  [form]
  (common/transform-form +scheme-transform-config+ form))

(declare emit-scheme-form)

(defn emit-scheme-coll
  [start end coll]
  (str start
       (str/join " " (map emit-scheme-form coll))
       end))

(defn emit-scheme-map
  [m]
  (let [table '__xt_tbl]
    (emit-scheme-form
     (list 'let
           (list (list table (list 'make-hash)))
           (cons 'begin
                 (concat
                  (map (fn [[k v]]
                         (list 'hash-set!
                               table
                               (if (keyword? k) (name k) k)
                               v))
                       m)
                  [table]))))))

(defn emit-scheme-form
  [form]
  (cond (nil? form)      "null"
        (true? form)     "#t"
        (false? form)    "#f"
        (string? form)   (pr-str form)
        (keyword? form)  (pr-str (name form))
        (number? form)   (str form)
        (symbol? form)   (let [ns (namespace form)]
                           (if (and ns
                                    (or (= ns "-")
                                        (.contains ns ".")))
                             (name form)
                             (str form)))
        (map? form)      (emit-scheme-map form)
        (vector? form)   (emit-scheme-coll "(vector " ")" form)
        (set? form)      (emit-scheme-coll "(set" ")" form)
        (collection/form? form)
        (emit-scheme-coll "(" ")" form)
        :else
        (pr-str form)))

(defn emit-scheme
  "emits code into scheme schema"
  {:added "4.0"}
  [form mopts]
  (-> (common/prepare-top-level 'begin form)
      (scheme-expand)
      (scheme-transform)
      (emit-scheme-form)))

(def +grammar+
  (grammar/grammar :scheme
    +reserved+
    {:emit #'emit-scheme}))

(def +meta+ (book/book-meta {}))

(def +book+
  (book/book {:lang :scheme
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
