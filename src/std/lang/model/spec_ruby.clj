(ns std.lang.model.spec-ruby
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-top-level :as top]
            [std.lang.base.emit-data :as data]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-ruby :as fn]
            [std.string :as str]
            [std.lib :as h]))

(defn ruby-symbol
  "emit ruby symbol"
  [sym grammar mopts]
  (cond (keyword? sym)
        (str ":" (name sym))

        :else
        (common/emit-symbol sym grammar mopts)))

(defn ruby-fn
  "basic transform for ruby blocks"
  ([[_ & args]]
   (cond (symbol? (first args))
         (apply list 'fn.inner (with-meta (first args)
                                 {:inner true})
                (rest args))

         :else
         (let [[args & body] args]
           (apply list 'fn.inner args body)))))

(defn ruby-defn
  "emit ruby function definition"
  [[_ sym args & body]]
  (let [grammar preprocess/*macro-grammar*
        mopts   preprocess/*macro-opts*
        sym-str (common/emit-symbol sym grammar mopts)
        args-str (str/join ", " (common/emit-invoke-args args grammar mopts))
        body-str (common/*emit-fn* (cons 'do body) grammar mopts)]
    (list :- (str "def " sym-str (if (not-empty args-str) (str "(" args-str ")") "") "\n"
                  body-str "\n"
                  "end"))))

(defn ruby-var
  "emit ruby variable"
  [[_ sym & args]]
  (list ':= sym (last args)))

(defn ruby-map
  "emit ruby hash"
  [m grammar mopts]
  (let [entries (map (fn [[k v]]
                       (str (common/*emit-fn* k grammar mopts)
                            " => "
                            (common/*emit-fn* v grammar mopts)))
                     m)]
    (str "{" (str/join ", " entries) "}")))

(def +features+
  (-> (grammar/build :exclude [:pointer :block :data-range])
      (grammar/build:override
       {:var        {:macro #'ruby-var :emit :macro}
        :defn       {:macro #'ruby-defn :emit :macro}
        :fn         {:macro #'ruby-fn   :emit :macro}
        :and        {:raw "&&"}
        :or         {:raw "||"}
        :not        {:raw "!" :emit :prefix}
        :eq         {:raw "=="}
        :neq        {:raw "!="}
        :gt         {:raw ">"}
        :lt         {:raw "<"}
        :gte        {:raw ">="}
        :lte        {:raw "<="}})
       (grammar/build:override fn/+ruby+)
       (grammar/build:extend
        {:assign     {:op :assign :symbol #{':=} :raw "=" :emit :infix}
         :puts       {:op :puts :symbol #{'puts} :raw "puts" :emit :prefix}
         :nil?       {:op :nil? :symbol #{'nil?} :raw "nil?" :emit :postfix}
         :attr       {:op :attr :symbol #{'attr_accessor} :raw "attr_accessor" :emit :prefix}
         :end        {:op :end  :symbol #{'end}  :raw "end"  :emit :token}})))

(def +template+
  (->> {:banned #{}
        :allow   {:assign  #{:symbol}}
        :default {:common    {:statement "\n"}
                  :block     {:parameter {:start " " :end ""}
                              :body      {:start "" :end "end" :append false}}
                  :function  {:raw "lambda"
                              :args      {:start " { |" :end "| " :space ""}
                              :body      {:start "" :end "}"}}
                  :invoke    {:reversed true}}
        :token   {:nil       {:as "nil"}
                  :boolean   {:as (fn [b] (if b "true" "false"))}
                  :string    {:quote :double}
                  :symbol    {:custom #'ruby-symbol}}
        :data    {:vector    {:start "[" :end "]" :space ""}
                  :map       {:custom #'ruby-map}}
        :define  {:def       {:raw ""}
                  :defglobal {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :rb
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :ruby
              :parent :xtalk
              :meta (book/book-meta {})
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
