(ns std.lang.model.spec-erlang
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lib :as h]
            [std.string :as str]))

;;
;; UTILS
;;

(defn to-erlang-var [sym]
  (if (symbol? sym)
    (let [s (name sym)]
      (if (re-find #"^[a-z]" s)
        (symbol (str (str/upper-case (subs s 0 1)) (subs s 1)))
        sym))
    sym))

(defn capitalize-locals [form locals]
  (if (empty? locals)
    form
    (h/postwalk (fn [x]
                  (if (and (symbol? x) (locals x))
                    (to-erlang-var x)
                    x))
                form)))

(defn emit-ast [form]
  (common/emit-common form
                      preprocess/*macro-grammar*
                      preprocess/*macro-opts*))

(defn wrap-raw [s]
  (list 'erl-raw s))

;;
;; LANG
;;

(defn tf-erlang-defn
  "transforms defn to erlang function definition"
  [[_ sym args & body]]
  (let [name (ut/sym-default-str sym)
        locals (set (filter symbol? args))
        erl-args (map to-erlang-var args)
        erl-body (map #(capitalize-locals % locals) body)]
    (list 'defn- sym (vec erl-args) erl-body)))

(defn emit-erlang-defn
  "emits erlang function"
  [[_ sym params body]]
  (wrap-raw
   (str (ut/sym-default-str sym)
        "("
        (str/join ", " (map emit-ast params))
        ") -> "
        (str/join ", " (map emit-ast body))
        ".")))

(defn tf-erlang-case
  "transforms case"
  [[_ expr & clauses]]
  (let [pairs (partition 2 clauses)]
    (list 'case* expr pairs)))

(defn emit-erlang-case
  "emits erlang case"
  [[_ expr clauses]]
  (wrap-raw
   (str "case " (emit-ast expr) " of "
        (str/join "; "
                  (map (fn [[pat body]]
                         (str (emit-ast pat) " -> " (emit-ast body)))
                       clauses))
        " end")))

(defn tf-erlang-tuple
  "transforms tuple"
  [[_ & elements]]
  (cons 'tuple* elements))

(defn emit-erlang-tuple
  "emits erlang tuple"
  [[_ & elements]]
  (wrap-raw
   (str "{" (str/join ", " (map emit-ast elements)) "}")))

(defn emit-erlang-var
  "emits var assignment"
  [[_ sym _ val]]
  (wrap-raw
   (str (emit-ast (to-erlang-var sym)) " = " (emit-ast val))))

(def +features+
  (-> (grammar/build :include [:builtin :math :compare :logic :control-base])
      (grammar/build:extend
       {:erl-raw {:op :erl-raw :symbol #{'erl-raw} :type :token}
        :defn   {:macro #'tf-erlang-defn :emit :macro :type :macro :symbol #{'defn}}
        :case   {:macro #'tf-erlang-case :emit :macro :type :macro :symbol #{'case}}
        :tuple  {:macro #'tf-erlang-tuple :emit :macro :type :macro :symbol #{'tuple}}
        :var    {:symbol #{'var} :emit :macro :macro #'emit-erlang-var :type :macro}
        :eq-exact {:raw "=:="}
        :defn- {:op :defn- :symbol #{'defn-} :emit :macro :macro #'emit-erlang-defn :type :macro}
        :case* {:op :case* :symbol #{'case*} :emit :macro :macro #'emit-erlang-case :type :macro}
        :tuple* {:op :tuple* :symbol #{'tuple*} :emit :macro :macro #'emit-erlang-tuple :type :macro}
        :send  {:op :send :symbol #{'send '!} :raw "!" :emit :infix}})
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
        :else (common/emit-common key grammar mopts)))

(def +template+
  (->> {:default {:comment   {:prefix "%"}
                  :common    {:statement "" :apply "(" :sep ", "}
                  :block     {:body {:start "" :end "" :sep ", "}}}
        :token   {:nil       {:as "undefined"}
                  :boolean   {:as identity}
                  :string    {:quote :double}
                  :symbol    {}
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
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
