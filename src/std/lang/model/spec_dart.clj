(ns std.lang.model.spec-dart
  (:require [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-dart :as fn-dart]
            [std.lib.collection :as collection]
            [std.lib.template :as template]
            [std.lib.walk :as walk]))

(defn dart-map-key
  [key grammar mopts]
  (cond
    (or (keyword? key)
        (string? key)
        (symbol? key)
        (number? key)
        (boolean? key)
        (nil? key))
    (data/default-map-key key grammar mopts)

    :else
    (emit/emit-main key grammar mopts)))

(defn tf-for-object
  "for object transform"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (cond (= k '_)
        (apply list 'for [(list 'var v) :in (list '. m 'values)]
               body)

        (= v '_)
        (apply list 'for [(list 'var k) :in (list '. m 'keys)]
               body)

        :else
        (let [entry 'entry]
          (apply list 'for [(list 'var entry) :in (list '. m 'entries)]
                 (concat [(list 'var k (list '. entry 'key))
                          (list 'var v (list '. entry 'value))]
                         body)))))

(defn tf-for-array
  "for array transform"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i v] e]
      (template/$ (for [(var ~i := 0) (< ~i (. ~arr length)) (:++ ~i)]
                   (var ~v (. ~arr [~i]))
                   ~@body)))
    (template/$ (for [(var ~e) :in ~arr]
                 ~@body))))

(defn tf-for-iter
  "for iter transform"
  {:added "4.0"}
  [[_ [e it] & body]]
  (template/$ (for [(var ~e) :in ~it]
               ~@body)))

(defn tf-for-return
  "for return transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error final]}]]
  (let [cb (list 'fn [err res]
                 (list 'if err
                       error
                       success))
        out (walk/prewalk (fn [x]
                            (if (= x '(x:callback))
                              cb
                              x))
                          statement)]
    (cond->> out
      final (list 'return))))

(defn tf-for-try
  "for try transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error]}]]
  (template/$ (try
                (var ~res := ~statement)
                ~success
                (catch ~err ~error))))

(defn tf-for-async
  "for async transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error finally]}]]
  (template/$ (. (Future (fn []
                           (return ~statement)))
                 ~@(if success
                     [(list 'then
                            (list 'fn [res]
                                  success))])
                 ~@(if error
                     [(list 'catchError
                            (list 'fn [err]
                                  error))])
                 ~@(if finally
                     [(list 'whenComplete
                            (list 'fn '[]
                                  finally))]))))

(def +features+
  (let [base (-> (grammar/build :exclude [:pointer
                                          :block
                                          :data-set])
                 (grammar/build:override
                  {:var         {:symbol '#{var} :raw "var"}
                   :defn        {:symbol '#{defn}}
                   :new         {:symbol '#{new} :raw "new" :emit :new}
                   :for-object  {:macro #'tf-for-object :emit :macro}
                   :for-array   {:macro #'tf-for-array  :emit :macro}
                   :for-iter    {:macro #'tf-for-iter   :emit :macro}
                   :for-return  {:macro #'tf-for-return :emit :macro}
                   :for-try     {:macro #'tf-for-try    :emit :macro}
                   :for-async   {:macro #'tf-for-async  :emit :macro}
                   :with-global {:value true :raw "globalThis"}}))
        base-keys (set (keys base))
        overrides (select-keys fn-dart/+dart+ base-keys)
        extensions (apply dissoc fn-dart/+dart+ base-keys)]
    (cond-> base
      (seq overrides) (grammar/build:override overrides)
      (seq extensions) (grammar/build:extend extensions))))

(def +template+
  (-> (emit/default-grammar)
      (collection/merge-nested
       {:banned #{:set :regex}
        :highlight '#{return break continue}
         :default {:common    {:statement ""}
                   :function  {:prefix ""
                               :raw ""
                               :args {:sep ", "}}
                   :invoke    {:reversed true :hint ""}
                   :block     {:start " {" :end "}"}}
         :block   {:for {:parameter {:sep ";"}}}
         :token   {:symbol {:replace {\- "_"}}
                   :nil {:as "null"}}
         :data    {:vector {:start "[" :end "]" :space ""}
                   :map    {:space ""}
                  :map-entry {:key-fn #'dart-map-key}}})))

(def +grammar+
  (grammar/grammar :dt
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ _]
                        (list :- "import" (str "'" name "';")))}))

(def +book+
  (book/book {:lang :dart
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
