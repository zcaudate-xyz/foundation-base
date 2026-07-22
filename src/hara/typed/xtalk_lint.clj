(ns hara.typed.xtalk-lint
  (:require [hara.common.grammar :as grammar]
            [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-parse :as parse]
            [std.string.case :as string-case]))

(def +extra-block-heads+
  '#{for:array for:object for:index for:iter for:async})

(def +block-heads+
  (into +extra-block-heads+
        (keep (fn [[sym entry]]
                (when (= :block (:type entry))
                  sym))
              (grammar/to-reserved (grammar/build)))))

(defn canonical-head
  [x]
  (if (symbol? x)
    (symbol (name x))
    x))

(defn block-head?
  [head]
  (contains? +block-heads+ (canonical-head head)))

(defn nested-dot-access?
  [form]
  (and (seq? form)
       (= '. (canonical-head (first form)))
       (seq? (second form))
       (= '. (canonical-head (first (second form))))))

(defn flatten-dot-access
  "Flattens nested dot object access into one canonical dot form.

   The trailing forms are retained as method arguments, so
   `(. (. a [\"name\"]) [\"hello\"] (run 1 2 3))` becomes
   `(. a [\"name\"] [\"hello\"] (run 1 2 3))`."
  [form]
  (if (nested-dot-access? form)
    (let [[_ object & tail] form
          [_ base & segments] (flatten-dot-access object)]
      (list* '. base (concat segments tail)))
    form))

(defn source-location
  [form opts]
  (let [loc (types/source-loc form)
        file (or (when (not= "UNKNOWN" (:file loc))
                   (:file loc))
                 (:file opts))]
    (cond-> loc
      file (assoc :file file))))

(defn diagnostic
  [code severity message form context opts extra]
  (merge {:code code
          :severity severity
          :layer :canonical
          :message message
          :context context
          :form form
         :loc (source-location form opts)}
         extra))

(defn simple-destructuring-source?
  "Returns true when a destructuring var source can be safely read directly.

   Direct destructuring repeats the source once per bound field, so the source
   must be a symbol or a one-level dot access on a symbol."
  [form]
  (or (symbol? form)
      (and (seq? form)
           (= '. (canonical-head (first form)))
           (= 3 (count form))
           (symbol? (second form))
           (vector? (nth form 2))
           (= 1 (count (nth form 2))))))

(defn- valid-destructuring-target?
  [target]
  (or (and (set? target)
           (seq target)
           (every? symbol? target))
      (and (vector? target)
           (seq target)
           (every? symbol? target))))

(defn var-source-diagnostics
  [form context opts]
  (let [target (second form)
        source (last form)]
    (if (and (valid-destructuring-target? target)
             (not (simple-destructuring-source? source)))
      [(diagnostic
        :XT006
        :warning
        "destructuring var sources must be a symbol or a simple dot access (. symbol [key]); bind complex expressions to a symbol first"
        form
        context
        opts
        {:target target
         :source source})]
      [])))

(defn var-target-diagnostics
  [form context opts]
  (let [target (second form)]
    (cond
      (set? target)
      (let [invalid (remove symbol? target)
            fields (map (fn [sym]
                          [(types/field-key sym) sym])
                        (filter symbol? target))
            collisions (->> fields
                            (group-by first)
                            (filter (fn [[_ pairs]] (> (count pairs) 1)))
                            (sort-by first))]
        (vec
         (concat
          (when (seq invalid)
            [(diagnostic
              :XT005
              :error
              "set destructuring targets must contain only symbols"
              form
              context
              opts
              {:target target
               :invalid (vec invalid)})])
          (map (fn [[field pairs]]
                 (let [canonical (symbol (string-case/spear-case field))]
                   (diagnostic
                    :XT004
                    :error
                    (str "destructuring fields collide after snake_case emission: " field
                         "; prefer spear-case binding " canonical)
                    form
                    context
                    opts
                    {:field field
                     :canonical canonical
                     :bindings (mapv second pairs)})))
               collisions))))

      (vector? target)
      (when-not (every? symbol? target)
        [(diagnostic
          :XT005
          :error
          "vector destructuring targets must contain only symbols"
          form
          context
          opts
          {:target target})])

      :else
      [])))

(declare lint-form)

(defn lint-pairs
  [pairs opts]
  (vec (mapcat (fn [[form context]]
                 (when (some? form)
                   (lint-form form context opts)))
               pairs)))

(defn binding-pairs
  [bindings]
  (mapcat (fn [pair]
            (when (= 2 (count pair))
              [[(second pair) :value]]))
          (partition-all 2 (or bindings []))))

(defn function-body-forms
  [form]
  (let [[_ _ & tail] form
        tail (if (string? (first tail)) (next tail) tail)
        tail (if (map? (first tail)) (next tail) tail)]
    (if (vector? (first tail))
      [(next tail)]
      (map rest tail))))

(defn form-pairs
  [h form block?]
  (cond
    (= h '.)
    (concat [[(second form) :dot-object]]
            (map (fn [arg] [arg :value]) (drop 2 form)))

    (= h 'if)
    (concat [[(second form) :value]
             [(nth form 2 nil) :statement]
             [(nth form 3 nil) :statement]])

    (= h 'cond)
    (mapcat (fn [pair]
              [[(first pair) :value]
               [(second pair) :statement]])
            (partition-all 2 (rest form)))

    (#{'when 'while} h)
    (concat [[(second form) :value]]
            (map (fn [body] [body :statement]) (drop 2 form)))

    (#{'for 'forange 'for:array 'for:object 'for:index 'for:iter 'for:async} h)
    (concat [[(second form) :value]]
            (map (fn [body] [body :statement]) (drop 2 form)))

    (#{'do 'do* 'doto 'try 'switch 'case 'br*} h)
    (map (fn [body] [body :statement]) (rest form))

    (= h 'let)
    (concat (binding-pairs (second form))
            (map (fn [body] [body :statement]) (drop 2 form)))

    (= h 'var)
    (when (seq (drop 2 form))
      [[(last form) :value]])

    (= h ':=)
    [[(last form) :value]]

    (= h 'return)
    [[(second form) :value]]

    (#{'fn 'fn.inner} h)
    (let [body (if (vector? (second form))
                 (drop 2 form)
                 (drop 3 form))]
      (map (fn [entry] [entry :statement]) body))

    block?
    (map (fn [body] [body :statement]) (rest form))

    :else
    (map (fn [arg] [arg :value]) (rest form))))

(defn- fn-arrow-suggestion
  [form]
  (let [[_ args body] form]
    (when (and (vector? args)
               (= 3 (count form))
               (nil? body))
      (list 'fn args (list 'return body)))))

(defn- fn-arrow-diagnostic
  [form context opts]
  (when-let [suggestion (and (= 'fn:> (canonical-head (first form)))
                             (fn-arrow-suggestion form))]
    (diagnostic
     :XT003
     :warning
     "fn:> with an explicit argument vector and nil body can use canonical fn with an explicit return"
     form
     context
     opts
     {:suggestion suggestion})))

(defn- value-context?
  [context]
  (contains? #{:value :dot-object} context))

(defn- lint-fn-arrows
  ([form opts]
   (lint-fn-arrows form opts false))
  ([form opts dot-object?]
   (cond
     (seq? form)
     (let [h (canonical-head (first form))
           own (cond-> []
                 (and (nested-dot-access? form)
                      (not dot-object?))
                 (conj (diagnostic
                        :XT007
                        :warning
                        "nested dot access can be flattened into a single dot form"
                        form
                        :statement
                        opts
                        {:suggestion (flatten-dot-access form)}))

                 (fn-arrow-diagnostic form :value opts)
                 (conj (fn-arrow-diagnostic form :value opts)))]
       (into own
             (mapcat (fn [[idx entry]]
                       (lint-fn-arrows entry
                                       opts
                                       (and (= h '.)
                                            (zero? idx))))
                     (map-indexed vector (rest form)))))

     (or (vector? form) (set? form))
     (vec (mapcat #(lint-fn-arrows % opts) form))

     (map? form)
     (vec (mapcat (fn [[key value]]
                    (concat (lint-fn-arrows key opts)
                            (lint-fn-arrows value opts)))
                  form))

     :else
     [])))

(defn lint-form
  [form context opts]
  (cond
    (seq? form)
    (let [h (canonical-head (first form))
          block? (block-head? h)
          own (cond
                (and (value-context? context) block?)
                [(diagnostic
                  :XT001
                  :error
                  (str "block form " h
                       " is not valid in value position; use :? for value conditionals")
                  form
                  context
                  opts
                  {:head h})]

                (and (= h 'x:get-key)
                     (or (= 3 (count form))
                         (and (= 4 (count form))
                              (nil? (nth form 3))))
                     (string? (nth form 2)))
                (let [[_ obj key] form]
                  [(diagnostic
                    :XT002
                    :warning
                    "simple x:get-key can use canonical dot access"
                    form
                    context
                    opts
                    {:suggestion (list '. obj [key])})])

                (and (= h '.)
                     (nested-dot-access? form)
                     (not= :dot-object context))
                [(diagnostic
                  :XT007
                  :warning
                  "nested dot access can be flattened into a single dot form"
                  form
                  context
                  opts
                  {:suggestion (flatten-dot-access form)})]

                (and (= h 'fn:>)
                     (fn-arrow-suggestion form))
                [(fn-arrow-diagnostic form context opts)]

                :else
                [])
          own (if (= h 'var)
                (into own (concat (var-target-diagnostics form context opts)
                                  (var-source-diagnostics form context opts)))
                own)]
      (into own (lint-pairs (form-pairs h form block?) opts)))

    (vector? form)
    (lint-pairs (map (fn [entry] [entry :value]) form) opts)

    (set? form)
    (lint-pairs (map (fn [entry] [entry :value]) form) opts)

    (map? form)
    (lint-pairs (mapcat (fn [[key value]] [[key :value] [value :value]]) form)
                opts)

    :else
    []))

(defn lint-top-form
  [form opts]
  (let [h (when (seq? form)
            (canonical-head (first form)))]
    (cond
      (#{'defn.xt 'defgen.xt} h)
      (vec (mapcat #(lint-pairs (map (fn [entry] [entry :statement]) %) opts)
                   (mapcat function-body-forms [form])))

      (= h 'def.xt)
      (lint-form (last form) :value opts)

      (#{'fact 'fact:global} h)
      (lint-fn-arrows form opts)

      :else
      [])))

(defn lint-forms
  ([forms]
   (lint-forms forms {}))
  ([forms opts]
   (vec (mapcat #(lint-top-form % opts) forms))))

(defn lint-file
  [file-path]
  (lint-forms (parse/read-forms file-path) {:file (str file-path)}))

(defn lint-files
  [file-paths]
  (mapv (fn [file-path]
          {:file (str file-path)
           :diagnostics (lint-file file-path)})
        file-paths))

(defn summarize
  [diagnostics]
  {:total (count diagnostics)
   :errors (count (filter #(= :error (:severity %)) diagnostics))
   :warnings (count (filter #(= :warning (:severity %)) diagnostics))
   :codes (frequencies (map :code diagnostics))})
