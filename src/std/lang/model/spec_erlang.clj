(ns std.lang.model.spec-erlang
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-erlang :as fn]
            [std.lib :as h]
            [std.string :as str]))

;;
;; UTILS
;;

(defn erlang-symbol
  "custom symbol emitter for erlang"
  {:added "4.0"}
  [sym grammar mopts]
  (let [s (name sym)]
    (if (re-find #"^[a-z]" s)
      (str (str/upper-case (subs s 0 1)) (subs s 1))
      s)))

(defn emit-ast [form]
  (common/emit-common form
                      preprocess/*macro-grammar*
                      preprocess/*macro-opts*))

(defn wrap-raw [s]
  (list 'erl-raw s))

;;
;; LANG
;;

(defn emit-erlang-defn
  "emits erlang function"
  [[_ sym args & body] grammar mopts]
  (str (ut/sym-default-str sym)
       "("
       (str/join ", " (map #(common/*emit-fn* % grammar mopts) args))
       ") -> "
       (str/join ", " (map #(common/*emit-fn* % grammar mopts) body))
       "."))

(defn emit-erlang-case
  "emits erlang case"
  [[_ expr & clauses] grammar mopts]
  (let [pairs (partition 2 clauses)]
    (str "case " (common/*emit-fn* expr grammar mopts) " of "
         (str/join "; "
                   (map (fn [[pat body]]
                          (str (common/*emit-fn* pat grammar mopts) " -> "
                               (common/*emit-fn* body grammar mopts)))
                        pairs))
         " end")))

(defn emit-erlang-tuple
  "emits erlang tuple"
  [[_ & elements] grammar mopts]
  (str "{" (str/join ", " (map #(common/*emit-fn* % grammar mopts) elements)) "}"))

(defn emit-erlang-var
  "emits var assignment"
  [[_ sym val] grammar mopts]
  (str (common/*emit-fn* sym grammar mopts)
       " = "
       (common/*emit-fn* val grammar mopts)))

(def +features+
  (-> (grammar/build :include [:builtin :math :compare :logic :control-base])
      (merge (grammar/build-xtalk))
      (grammar/build:extend
       {:erl-raw {:op :erl-raw :symbol #{'erl-raw} :type :token}
        :defn   {:emit #'emit-erlang-defn :symbol #{'defn}}
        :case   {:emit #'emit-erlang-case :symbol #{'case}}
        :tuple  {:emit #'emit-erlang-tuple :symbol #{'tuple}}
        :var    {:emit #'emit-erlang-var :symbol #{'var}}
        :eq-exact {:raw "=:="}
        :send  {:op :send :symbol #{'send '!} :raw "!" :emit :infix}})
      (grammar/build:override fn/+erlang+)
      (grammar/build:override
       {:and    {:raw "and"}
        :or     {:raw "or"}
        :not    {:raw "not"}
        :eq     {:raw "=="}
        :neq    {:raw "/="}
        :mod    {:raw "rem"}})))

(defn erlang-map-key
  "custom erlang map key"
  [key grammar mopts]
  (cond (keyword? key) (name key)
        (string? key) (str "\"" key "\"")
        :else (common/*emit-fn* key grammar mopts)))

(def +template+
  (->> {:default {:comment   {:prefix "%"}
                  :common    {:statement "" :apply "(" :sep ", "}
                  :block     {:body {:start "" :end "" :sep ", "}}}
        :token   {:nil       {:as "undefined"}
                  :boolean   {:as identity}
                  :string    {:quote :double}
                  :symbol    {:custom #'erlang-symbol}
                  :erl-raw   {:as second}}
        :data    {:vector    {:start "[" :end "]" :space ""}
                  :map       {:start "#{" :end "}" :space "" :assign " => " :key-fn #'erlang-map-key}
                  :map-entry {:start "" :end "" :space "" :assign " => " :key-fn #'erlang-map-key}
                  :set       {:start "#{" :end "}" :space "" :assign " => "}}
        :function {:defn     {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :erlang
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta {}))

(def +book+
  (book/book {:lang :erlang
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
