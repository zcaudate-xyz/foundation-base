(ns std.lang.model.spec-perl
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
            [std.lang.model.spec-xtalk.fn-perl :as fn]
            [std.string :as str]
            [std.lib :as h]))

;;
;; LANG
;;

(defn perl-var
  "emit perl variable declaration"
  [[_ sym & args]]
  (let [val (last args)
        sym-str (if (keyword? sym)
                  (h/strn sym)
                  (str "$" (h/strn sym)))]
    (list :- (str "my " sym-str " = " (common/*emit-fn* val preprocess/*macro-grammar* preprocess/*macro-opts*)))))

(defn perl-symbol
  "emit perl symbol with $ prefix if it's a variable"
  [sym grammar mopts]
  (let [s (str sym)]
    (cond (:perl/func mopts)
          s

          (or (str/starts-with? s "$")
              (str/starts-with? s "@")
              (str/starts-with? s "%"))
          s

          :else
          (str "$" s))))

(defn perl-invoke-args
  [args grammar mopts]
  (str/join ", " (common/emit-invoke-args args grammar mopts)))

(defn perl-invoke
  "emit perl function call"
  [[f & args] grammar mopts]
  (let [f-str (common/*emit-fn* f grammar (assoc mopts :perl/func true))
        args-str (perl-invoke-args args grammar (dissoc mopts :perl/func))]
    (str f-str "(" args-str ")")))

(defn perl-defn
  "emit perl subroutine definition"
  [[_ sym args & body]]
  (let [grammar preprocess/*macro-grammar*
        mopts   preprocess/*macro-opts*
        sym-str (common/emit-symbol sym grammar (assoc mopts :perl/func true))
        args-emit (map (fn [arg]
                         (str "my " (perl-symbol arg grammar mopts) " = shift;"))
                       args)
        body-str (common/*emit-fn* (cons 'do body) grammar mopts)]
    (list :- (str "sub " sym-str " {\n"
                  (if (seq args-emit)
                    (str (str/join "\n" args-emit) "\n")
                    "")
                  body-str "\n}"))))

(defn perl-array
  "emit perl array reference"
  [arr grammar mopts]
  (str "[" (str/join ", " (common/emit-array arr grammar mopts)) "]"))

(defn perl-map
  "emit perl hash reference"
  [m grammar mopts]
  (let [entries (map (fn [[k v]]
                       (str (common/*emit-fn* k grammar mopts)
                            " => "
                            (common/*emit-fn* v grammar mopts)))
                     m)]
    (str "{" (str/join ", " entries) "}")))

(def +features+
  (let [base (grammar/build :exclude [:pointer :block :data-range])
        base-keys (set (keys base))
        fn-override (select-keys fn/+perl+ base-keys)
        fn-extend   (apply dissoc fn/+perl+ (keys fn-override))]
    (-> base
        (grammar/build:override
         {:var        {:macro #'perl-var :emit :macro}
          :defn       {:macro #'perl-defn :emit :macro}
          :and        {:raw "&&"}
          :or         {:raw "||"}
          :not        {:raw "!"}
          :pow        {:raw "**" :emit :infix :symbol #{'**}}
          :eq         {:raw "=="}
          :neq        {:raw "!="}
          :gt         {:raw ">"}
          :lt         {:raw "<"}
          :gte        {:raw ">="}
          :lte        {:raw "<="}})
        (grammar/build:override fn-override)
        (grammar/build:extend fn-extend)
        (grammar/build:extend
         {:concat     {:op :concat :symbol #{'concat} :raw "." :emit :infix}
          :die        {:op :die   :symbol #{'die}   :raw "die"   :emit :prefix}}))))

(def +template+
  (->> {:banned #{:keyword}
        :allow   {:assign  #{:symbol}}
        :default {:common    {:statement ";"
                              :start  ""
                              :end    ""}
                  :block     {:parameter {:start "(" :end ")"}
                              :body      {:start "{" :end "}"}}
                  :invoke    {:custom #'perl-invoke}}
        :token   {:nil       {:as "undef"}
                  :boolean   {:as (fn [b] (if b "1" "0"))}
                  :string    {:quote :double}
                  :symbol    {:custom #'perl-symbol}}
        :data    {:vector    {:custom #'perl-array}
                  :map       {:custom #'perl-map}}
        :define  {:def       {:raw ""}
                  :defglobal {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :pl
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :perl
              :parent :xtalk
              :meta (book/book-meta {})
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
