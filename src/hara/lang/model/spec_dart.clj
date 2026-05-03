(ns hara.lang.model.spec-dart
  (:require [hara.lang.base.book :as book]
             [hara.lang.base.emit :as emit]
             [hara.lang.base.emit-common :as common]
             [hara.lang.base.emit-data :as data]
            [hara.lang.base.grammar :as grammar]
             [hara.lang.base.script :as script]
             [hara.lang.base.util :as ut]
             [hara.lang.model.spec-xtalk]
             [hara.lang.model.spec-dart.rewrite :as rewrite]
             [hara.lang.model.spec-xtalk.fn-dart :as fn-dart]
             [std.lib.collection :as collection]
             [std.lib.template :as template]))

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

(defn- dart-symbol-global
  [key _grammar _mopts]
  (list '. '__globals__ [(ut/sym-default-str key)]))

(defn dart-fn
  [[_ & args]]
  (if (symbol? (first args))
    (apply list 'fn.inner (rest args))
    (apply list 'fn.inner args)))

(defn dart-var
  "Normalizes Dart `var` declarations so values always lower through `var* :=`.
   This keeps collection literals and complex expressions from being misread as
   modifiers by the generic def-assign emitter."
  {:added "4.1"}
  [[_ decl & args]]
  (if (empty? args)
    (list 'var* decl)
    (let [bound (last args)]
      (cond
        (vector? decl)
        (let [tmp (gensym "value_")]
          (apply list 'do*
                 (cons (list 'var* tmp := bound)
                       (map-indexed (fn [i sym]
                                      (list 'var* sym := (list '. tmp [i])))
                                    decl))))

        (set? decl)
        (apply list 'do*
               (map (fn [sym]
                      (list 'var* sym := (list '. bound [(ut/sym-default-str sym)])))
                    (sort-by ut/sym-default-str decl)))

         :else
         (list 'var* decl := bound)))))

(defn dart-tf-let-bind
  "Expands let bindings into a Dart statement block so trailing returns keep
   their statement terminators inside emitted function bodies."
  {:added "4.1"}
  [[_ bindings & body]]
  `(~'do ~@(map (fn [[sym val]]
                  (if (= '_ sym)
                    val
                    `(~'var ~sym := ~val)))
                (partition 2 bindings))
    ~@body))

(defn tf-for-object
  "for object transform"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (cond (= k '_)
        (apply list 'for [(list 'var* v) :in (list '. m 'values)]
               body)

        (= v '_)
        (apply list 'for [(list 'var* k) :in (list '. m 'keys)]
               body)

        :else
        (let [entry (gensym "entry_")]
          (apply list 'for [(list 'var* entry) :in (list '. m 'entries)]
                 (concat [(list 'var* k := (list '. entry 'key))
                          (list 'var* v := (list '. entry 'value))]
                          body)))))

(defn tf-for-array
  "for array transform"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (let [arr-sym (gensym "arr_")]
    (if (vector? e)
      (let [[i v] e]
        (template/$ (do (var* ~arr-sym := ~arr)
                        (for [(var* ~i := 0) (< ~i (. ~arr-sym length)) (:++ ~i)]
                          (var* ~v := (. ~arr-sym [~i]))
                          ~@body))))
      (let [i (gensym "i")]
        (template/$ (do (var* ~arr-sym := ~arr)
                        (for [(var* ~i := 0) (< ~i (. ~arr-sym length)) (:++ ~i)]
                          (var* ~e := (. ~arr-sym [~i]))
                          ~@body)))))))

(defn tf-for-iter
  "for iter transform"
  {:added "4.0"}
  [[_ [e it] & body]]
  (let [it-sym (gensym "iter_")]
    (template/$ (do (var* ~it-sym := ~it)
                    (while (. ~it-sym (moveNext))
                      (var* ~e := (. ~it-sym current))
                      ~@body)))))

(defn dart-tf-ternary
  "nil-safe ternary transform for dart-specific rewrites"
  {:added "4.1"}
  [[_ test then else]]
  (list :? (list 'and
                 (list 'x:not-nil? test)
                 (list 'not= false test))
        then
        else))

(def +features+
  (-> (grammar/build :exclude [:pointer
                               :block
                               :data-set])
        (grammar/build:override
          {:fn          {:macro #'dart-fn :emit :macro}
           :var         {:symbol '#{var*} :raw "var"}
           :let-bind    {:macro #'dart-tf-let-bind :emit :macro}
           :pow         {:emit :alias :raw 'math.pow :value true}
            :defn        {:symbol '#{defn}}
           :new         {:symbol '#{new} :raw "new" :emit :new}
           :for-object  {:macro #'tf-for-object :emit :macro}
         :for-array   {:macro #'tf-for-array  :emit :macro}
         :for-iter    {:macro #'tf-for-iter   :emit :macro}
         :with-global {:value true :raw "__globals__"}})
       (grammar/build:override fn-dart/+dart+)
        (grammar/build:extend
           {:dart-or      {:op :dart-or      :symbol #{'dart:or}      :emit :infix             :raw "??"}
            :dart-ternary {:op :dart-ternary :symbol #{'dart:ternary} :macro #'dart-tf-ternary :emit :macro}
            :dart-var     {:op :dart-var     :symbol #{'var}          :macro #'dart-var         :emit :macro}})))

(def +template+
  (-> (emit/default-grammar)
      (collection/merge-nested
       {:banned #{:set :regex}
         :highlight '#{return break continue}
           :default {:common    {:statement ";"}
                     :function  {:prefix ""
                                 :raw ""
                                 :args {:sep ", "}}
                     :invoke    {:reversed true :hint ""}
                     :block     {:start " {" :end "}"}}
          :block   {:for {:parameter {:sep ";"}}}
          :function {:defgen {:body {:start " sync* {" :end "}"}}}
          :define  {:def {:raw "var"}}
          :rewrite {:staging [#'rewrite/dart-rewrite-stage]}
          :token   {:symbol {:replace {\- "_"}
                             :global #'dart-symbol-global}
                    :nil {:as "null"}}
           :data    {:vector {:start "[" :end "]" :space ""}
                     :map    {:start "<dynamic, dynamic>{" :end "}" :space ""}
                    :map-entry {:key-fn #'dart-map-key}}})))

(def +grammar+
  (grammar/grammar :dt
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name {:keys [as]} _]
                        (list :-
                              (str "import '" name "'"
                                   (when as
                                     (str " as " as))
                                   ";")))}))

(def +book+
  (book/book {:lang :dart
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
