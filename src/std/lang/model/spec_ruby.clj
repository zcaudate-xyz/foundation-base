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
  "emit ruby symbol
   (spec-ruby/ruby-symbol :a spec-ruby/+grammar+ {})
   => \":a\"
   (spec-ruby/ruby-symbol 'a spec-ruby/+grammar+ {})
   => \"a\""
  {:added "4.1"}
  [sym grammar mopts]
  (cond (keyword? sym)
        (str ":" (name sym))

        :else
        (common/emit-symbol sym grammar mopts)))

(defn ruby-var
  "emit ruby variable
   (spec-ruby/ruby-var '(var a 1))
   => '(:= a 1)"
  {:added "4.1"}
  [[_ sym & args]]
  (list ':= sym (last args)))

(defn ruby-map
  "emit ruby hash
   (l/emit-as :ruby '[{:a 1 :b 2}])
   => \"{:a => 1, :b => 2}\""
  {:added "4.1"}
  [m grammar mopts]
  (let [entries (map (fn [[k v]]
                       (str (common/*emit-fn* k grammar mopts)
                            " => "
                            (common/*emit-fn* v grammar mopts)))
                     m)]
    (str "{" (str/join ", " entries) "}")))

(defn ruby-fn
  "basic transform for ruby blocks
   (spec-ruby/ruby-fn '(fn [a] (+ a 1)))
   => '(fn.inner [a] (+ a 1))"
  {:added "4.1"}
  ([[_ & args]]
   (let [[args & body] args]
     (apply list :- :lambda
            (concat (if (not-empty args)
                      [(list 'quote args) "{"]
                      ["{"])
                    body
                    ["}"])))))

(def +features+
  (-> (grammar/build :exclude [:pointer :block :data-range])
      (grammar/build:override
       {:var        {:macro #'ruby-var :emit :macro}
        :and        {:raw "&&"}
        :or         {:raw "||"}
        :not        {:raw "!" :emit :prefix}
        :eq         {:raw "=="}
        :fn         {:macro  #'ruby-fn   :type :macro}
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
        :default {:common    {:statement ""}
                  :block     {:parameter {:start " " :end ""}
                              :body      {:start "" :end "end" :append false}}
                  :invoke    {:start "("}
                  :function  {:raw "def"
                              :body      {:start "" :end "end"}}}
        :block   {:try      {:raw  "begin"
                             :wrap {:start "" :end "end"}
                             :body {:start "" :end ""}
                             :control {:catch  {:raw  "rescue Exception =>"
                                                :body {:start "" :end ""}}}}}
        :token   {:nil       {:as "nil"}
                  :boolean   {:as (fn [b] (if b "true" "false"))}
                  :string    {:quote :double}
                  :symbol    {:custom #'ruby-symbol}}
        :data    {:vector    {:start "[" :end "]" :space ""}
                  :map       {:custom #'ruby-map}}
        :function {:defn      {:raw "def"
                               :body      {:start "" :end "end"}}}
        :define   {:def       {:raw "def"}
                   :defglobal {:raw "def"}}}
       (h/merge-nested (emit/default-grammar))))


(comment
  :try     {:raw "BEGIN"
            :wrap    {:start "" :end "END;"}
            :body    {:start "" :end "EXCEPTION"}
            :control {:default {:parameter  {:start "" :end ""}
                                :body {:append true
                                       :start "" :end ""}}
                      :catch   {:raw "WHEN"
                                :parameter  {:start " " :end " THEN"}}}}

  :default {:comment   {:prefix "--"}
            :common    {:apply ":" :statement ""
                        :namespace-full "___"
                        :namespace-sep  "_"}
            :index     {:offset 1  :end-inclusive true}
            :return    {:multi true}
            :block     {:parameter {:start " " :end " "}
                        :body      {:start "" :end ""}}
            :function  {:raw "function"
                        :body      {:start "" :end "end"}}
            :infix     {:if  {:check "and" :then "or"}}
            :global    {:reference nil}})

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
