(ns std.lang.model.spec-jq
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lib :as h]
            [std.string :as str]
            [std.lang.model.spec-xtalk]))

;;
;; JQ
;;

(defn jq-args
  "custom args for jq"
  {:added "4.0"}
  [args grammar mopts]
  (let [arg-strings (map #(common/*emit-fn* % grammar mopts) args)]
    (if (empty? arg-strings)
      ""
      (str "(" (str/join "; " arg-strings) ")"))))

(defn jq-invoke
  "outputs an invocation (same as vector)"
  {:added "4.0"}
  [[f & args] grammar mopts]
  (let [fstr (common/*emit-fn* f grammar mopts)]
    (if (= fstr ".")
      (if (seq args)
        (let [k (first args)
              kstr (common/*emit-fn* k grammar mopts)]
           (if (or (keyword? k)
                   (re-find #"^[a-zA-Z_][a-zA-Z0-9_]*$" kstr))
             (str "." (if (keyword? k) (subs kstr 1) kstr))
             (str ".[" kstr "]")))
        ".")
      (str fstr (jq-args args grammar mopts)))))

(defn jq-args-ast
  "ast for args"
  {:added "4.0"}
  [args]
  (if (empty? args)
    ""
    (apply list :% (list 'k:lparen) (concat (interpose (list 'k:semi) args) [(list 'k:rparen)]))))

(defn jq-defn
  "transforms a function to allow for inputs"
  {:added "4.0"}
  [[_ sym args & body]]
  (list :% (list 'k:def) (list 'k:space) sym (jq-args-ast args) (list 'k:colon) (list 'k:space) (apply list 'do body) (list 'k:semi)))

(defn jq-as
  "jq variable binding"
  {:added "4.0"}
  [[_ sym]]
  (list :% (list 'k:as) (list 'k:space) (list :$ sym)))

(defn jq-label
  "jq label"
  {:added "4.0"}
  [[_ sym]]
  (list :% (list 'k:label) (list 'k:space) (list :$ sym)))

(defn jq-break
  "jq break"
  {:added "4.0"}
  [[_ sym]]
  (if sym
    (list :% (list 'k:break) (list 'k:space) (list :$ sym))
    (list 'k:break)))

(defn jq-dot
  "jq dot access"
  {:added "4.0"}
  ([[_ & [k]] _ _]
   (cond (nil? k)
         (list 'identity)

         (symbol? k)
         (symbol (str "." (name k)))

         (keyword? k)
         (symbol (str "." (name k)))

         :else
         (list :% "." "[" k "]"))))

(defn jq-try
  "jq try/catch"
  {:added "4.0"}
  [[_ body handler]]
  (list :% (list 'k:try) (list 'k:space) body (list 'k:space) (list 'k:catch) (list 'k:space) handler))

(defn jq-if
  "jq if/then/else"
  {:added "4.0"}
  [[_ test then else]]
  (list :% (list 'k:if) (list 'k:space) test (list 'k:space) (list 'k:then) (list 'k:space) then (list 'k:space) (list 'k:else) (list 'k:space) else (list 'k:space) (list 'k:end)))

(defn jq-reduce
  "jq reduce"
  {:added "4.0"}
  [[_ inputs as init update]]
  (list :% (list 'k:reduce) (list 'k:space) inputs (list 'k:space) (list 'k:as) (list 'k:space) (list :$ as) (list 'k:space)
        (list :% (list 'k:lparen) (list '% init) (list 'k:semi) (list 'k:space) (list '% update) (list 'k:rparen))))

(defn jq-foreach
  "jq foreach"
  {:added "4.0"}
  [[_ inputs as init update & [extract]]]
  (if extract
    (list :% (list 'k:foreach) (list 'k:space) inputs (list 'k:space) (list 'k:as) (list 'k:space) (list :$ as) (list 'k:space)
          (list :% (list 'k:lparen) (list '% init) (list 'k:semi) (list 'k:space) (list '% update) (list 'k:semi) (list 'k:space) (list '% extract) (list 'k:rparen)))
    (list :% (list 'k:foreach) (list 'k:space) inputs (list 'k:space) (list 'k:as) (list 'k:space) (list :$ as) (list 'k:space)
          (list :% (list 'k:lparen) (list '% init) (list 'k:semi) (list 'k:space) (list '% update) (list 'k:rparen)))))

(def +features+
  (-> (grammar/build :include [:builtin
                               :builtin-helper
                               :free-control
                               :free-literal
                               :math
                               :compare
                               :logic
                               :return
                               :vars
                               :fn
                               :control-base
                               :control-general
                               :top-base
                               :block])
      (grammar/build:override
       {:defn      {:macro #'jq-defn :emit :macro}
        :def       {:macro #'jq-defn :emit :macro}
        :quote     {:emit :json}
        :break     {:op :break     :symbol #{'break} :macro #'jq-break :emit :macro}
        :index     {:symbol #{}}})
      (grammar/build:extend
       {:pipe      {:op :pipe      :symbol '#{>> |}   :raw "|"  :emit :infix}
        :comma     {:op :comma     :symbol '#{,,}   :raw ","  :emit :infix}
        :assign    {:op :assign    :symbol '#{:=}   :raw "="  :emit :infix}
        :update    {:op :update    :symbol '#{|=}   :raw "|=" :emit :infix}
        :plus-up   {:op :plus-up   :symbol '#{+=}   :raw "+=" :emit :infix}
        :sub-up    {:op :sub-up    :symbol '#{-=}   :raw "-=" :emit :infix}
        :mul-up    {:op :mul-up    :symbol '#{*=}   :raw "*=" :emit :infix}
        :div-up    {:op :div-up    :symbol '#{div=}   :raw "/=" :emit :infix}
        :mod-up    {:op :mod-up    :symbol '#{%=}   :raw "%=" :emit :infix}
        :alt       {:op :alt       :symbol '#{alt}   :raw "//" :emit :infix}
        :try-opt   {:op :try-opt   :symbol '#{?}    :raw "?"  :emit :post}
        :rec       {:op :rec       :symbol '#{..}   :raw ".." :emit :token}
        :dot       {:op :dot       :symbol #{'get}  :macro #'jq-dot :emit :macro}
        :as        {:op :as        :symbol #{'as}   :macro #'jq-as :emit :macro}
        :label     {:op :label     :symbol #{'label} :macro #'jq-label :emit :macro}
        :try       {:op :try       :symbol #{'try}  :macro #'jq-try :emit :macro}
        :if        {:op :if        :symbol #{'if}   :macro #'jq-if :emit :macro}
        :reduce    {:op :reduce    :symbol #{'reduce} :macro #'jq-reduce :emit :macro}
        :foreach   {:op :foreach   :symbol #{'foreach} :macro #'jq-foreach :emit :macro}
        :identity  {:op :identity  :symbol #{'identity '.} :raw "." :emit :token :value true}

        :k:space   {:op :k:space   :symbol #{'k:space} :raw " " :emit :token :value true}
        :k:def     {:op :k:def     :symbol #{'k:def} :raw "def" :emit :token :value true}
        :k:as      {:op :k:as      :symbol #{'k:as} :raw "as" :emit :token :value true}
        :k:label   {:op :k:label   :symbol #{'k:label} :raw "label" :emit :token :value true}
        :k:break   {:op :k:break   :symbol #{'k:break} :raw "break" :emit :token :value true}
        :k:try     {:op :k:try     :symbol #{'k:try} :raw "try" :emit :token :value true}
        :k:catch   {:op :k:catch   :symbol #{'k:catch} :raw "catch" :emit :token :value true}
        :k:if      {:op :k:if      :symbol #{'k:if} :raw "if" :emit :token :value true}
        :k:then    {:op :k:then    :symbol #{'k:then} :raw "then" :emit :token :value true}
        :k:else    {:op :k:else    :symbol #{'k:else} :raw "else" :emit :token :value true}
        :k:end     {:op :k:end     :symbol #{'k:end} :raw "end" :emit :token :value true}
        :k:reduce  {:op :k:reduce  :symbol #{'k:reduce} :raw "reduce" :emit :token :value true}
        :k:foreach {:op :k:foreach :symbol #{'k:foreach} :raw "foreach" :emit :token :value true}
        :k:lparen  {:op :k:lparen  :symbol #{'k:lparen} :raw "(" :emit :token :value true}
        :k:rparen  {:op :k:rparen  :symbol #{'k:rparen} :raw ")" :emit :token :value true}
        :k:colon   {:op :k:colon   :symbol #{'k:colon} :raw ":" :emit :token :value true}
        :k:semi    {:op :k:semi    :symbol #{'k:semi} :raw ";" :emit :token :value true}
        :var-ref   {:op :var-ref   :symbol #{:$}       :raw "$" :emit :pre}})))

(def +template+
  (->> {:banned #{:regex :set}
        :highlight '#{def if then else end reduce foreach as try catch label break}
        :default {:common    {:statement ""
                              :start  "" :end ""
                              :sep ""
                              :namespace-full "_"
                              :space " "}
                  :invoke    {:custom #'jq-invoke}
                  :define    {:space " "}
                  :symbol    {:full {:replace {\. "_"
                                               \- "_"}
                                     :sep "_"}
                              :global identity}
                  :block     {:statement ""
                              :parameter {:start "(" :end ")"}
                              :body      {:start ":" :end ";"}}}
        :token   {:nil       {:as "null"}
                  :string    {:quote :double}}
        :data    {:map       {:start "{" :end "}" :sep "," :assign ":"}
                  :vector    {:start "[" :end "]" :sep ","}
                  :free      {:start "(" :end ")" :sep "; "}}
        :block   {:function  {:defn      {:raw "def" :sep "; "}}}}
       (h/merge-nested (update-in (emit/default-grammar)
                                  [:token :symbol]
                                  dissoc :replace))))

(def +grammar+
  (grammar/grammar :jq
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :jq
              :parent :xtalk
              :meta (book/book-meta {})
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
