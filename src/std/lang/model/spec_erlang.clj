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
  (common/*emit-fn* form
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
    (list 'erl-defn-internal sym (vec erl-args) erl-body)))

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
    (list 'erl-case-internal expr pairs)))

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
  (cons 'erl-tuple-internal elements))

(defn emit-erlang-tuple
  "emits erlang tuple"
  [[_ & elements]]
  (wrap-raw
   (str "{" (str/join ", " (map emit-ast elements)) "}")))

(defn emit-erlang-var
  "emits var assignment"
  [[_ sym val]]
  (wrap-raw
   (str (emit-ast (to-erlang-var sym)) " = " (emit-ast val))))

(defn emit-erlang-fun
  "emits fun reference"
  [[_ sym arity]]
  (wrap-raw (str "fun " (emit-ast sym) "/" (emit-ast arity))))

(def +features+
  (-> (grammar/build :include [:builtin :math :compare :logic :control-base])
      (merge (grammar/build-xtalk))
      (grammar/build:extend
       {:erl-raw {:op :erl-raw :symbol #{'erl-raw} :type :token :emit :internal}

        :erl-tuple {:op :erl-tuple :symbol #{'tuple 'erl-tuple} :emit :macro :macro #'tf-erlang-tuple :type :macro}
        :erl-tuple-internal {:op :erl-tuple-internal :symbol #{'erl-tuple-internal} :emit :macro :macro #'emit-erlang-tuple :type :macro}

        :erl-def {:op :erl-def :symbol #{'def-erl 'erl-def} :emit :macro :macro #'tf-erlang-defn :type :macro}
        :erl-defn-internal {:op :erl-defn-internal :symbol #{'erl-defn-internal} :emit :macro :macro #'emit-erlang-defn :type :macro}

        :erl-case {:op :erl-case :symbol #{'case 'erl-case} :emit :macro :macro #'tf-erlang-case :type :macro}
        :erl-case-internal {:op :erl-case-internal :symbol #{'erl-case-internal} :emit :macro :macro #'emit-erlang-case :type :macro}

        :erl-assign {:op :erl-assign :symbol #{'assign 'erl-assign} :emit :macro :macro #'emit-erlang-var :type :macro}

        :erl-fun {:op :erl-fun :symbol #{'fun 'erl-fun} :emit :macro :macro #'emit-erlang-fun :type :macro}

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
                  :common    {:statement "" :apply "(" :sep ", "
                              :namespace-full ":" :namespace-sep ":"}
                  :block     {:body {:start "" :end "" :sep ", "}}}
        :token   {:nil       {:as "undefined"}
                  :boolean   {:as identity}
                  :string    {:quote :double}
                  :symbol    {}
                  :erl-raw   {:as second}}
        :data    {:vector    {:start "[" :end "]" :space "" :type :data}
                  :map       {:start "#{" :end "}" :space "" :assign " => " :key-fn #'erlang-map-key :type :data}
                  :map-entry {:start "" :end "" :space "" :assign " => " :key-fn #'erlang-map-key :type :data}
                  :set       {:start "#{" :end "}" :space "" :assign " => " :type :data}}
        :function {:defn     {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :erlang
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta {}))

(def +modules+
  {'gen_tcp   {:code {'connect {:op :invoke} 'recv {:op :invoke} 'send {:op :invoke}}}
   'json      {:code {'decode {:op :invoke} 'encode {:op :invoke}}}
   'string    {:code {'trim {:op :invoke}}}
   'maps      {:code {'get {:op :invoke}}}
   'io_lib    {:code {'format {:op :invoke}}}
   'erl_scan  {:code {'string {:op :invoke}}}
   'erl_parse {:code {'parse_exprs {:op :invoke}}}
   'erl_eval  {:code {'exprs {:op :invoke}}}})

(def +book+
  (book/book {:lang :erlang
              :parent :xtalk
              :meta +meta+
              :modules +modules+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
