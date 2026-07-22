(ns clj-kondo.hooks.hara-xtalk
  "Canonical XTalk checks exposed through clj-kondo.

   The hook analyzes the original XTalk form, then returns a small Clojure
   stub so clj-kondo does not mistake XTalk's runtime syntax for Clojure." 
  (:require [clj-kondo.hooks-api :as api]
            [clojure.string :as str]))

(def ^:private +block-heads+
  '#{if cond when while for forange for:array for:object for:index for:iter
     for:async do do* doto try switch case br* let})

(defn- canonical-head [head]
  (if (symbol? head)
    (symbol (name head))
    head))

(defn- nested-dot-access? [form]
  (and (seq? form)
       (= '. (canonical-head (first form)))
       (seq? (second form))
       (= '. (canonical-head (first (second form))))))

(defn- flatten-dot-access [form]
  (if (nested-dot-access? form)
    (let [[_ object & tail] form
          [_ base & segments] (flatten-dot-access object)]
      (list* '. base (concat segments tail)))
    form))

(defn- report! [node rule level message]
  (api/reg-finding!
   (merge (meta node)
          {:type rule
           :level level
           :message message})))

(defn- block-head? [head]
  (contains? +block-heads+ (canonical-head head)))

(defn- fn-arrow-suggestion [form]
  (let [[_ args body] form]
    (when (and (vector? args)
               (= 3 (count form))
               (nil? body))
      (list 'fn args (list 'return body)))))

(defn- lint-fn-arrows!
  ([node]
   (lint-fn-arrows! node false))
  ([node dot-object?]
   (when node
     (let [form (api/sexpr node)
           head (when (seq? form) (canonical-head (first form)))]
       (when (and (not dot-object?)
                  (nested-dot-access? form))
         (report! node :hara.xtalk/nested-dot-access :warning
                  (str "nested dot access can be flattened into a single dot form: "
                       (flatten-dot-access form))))
       (when (and (api/list-node? node)
                  (= 'fn:> head))
         (when-let [suggestion (fn-arrow-suggestion form)]
           (report! node :hara.xtalk/redundant-fn-arrow :warning
                    (str "fn:> with an explicit argument vector and nil body can use canonical fn with an explicit return: "
                         suggestion))))
       (doseq [[idx child-node] (map-indexed vector (:children node))]
         (lint-fn-arrows! child-node
                          (and (= head '.)
                               (= 1 idx))))))))

(defn- binding-value-nodes [bindings-node]
  (when (api/vector-node? bindings-node)
    (->> (:children bindings-node)
         (partition-all 2)
         (keep (fn [[_ value]] value)))))

(defn- node-pairs [head node]
  (let [children (vec (rest (:children node)))
        child (fn [i] (nth children i nil))
        statement-children (fn [xs] (map vector xs (repeat :statement)))]
    (cond
      (= head 'if)
      [[(child 0) :value] [(child 1) :statement] [(child 2) :statement]]

      (= head 'cond)
      (mapcat (fn [[test result]] [[test :value] [result :statement]])
              (partition-all 2 children))

      (#{'when 'while} head)
      (concat [[(child 0) :value]]
              (statement-children (drop 1 children)))

      (#{'for 'forange 'for:array 'for:object 'for:index 'for:iter 'for:async}
       head)
      (concat [[(child 0) :value]]
              (statement-children (drop 1 children)))

      (#{'do 'do* 'doto 'try 'switch 'case 'br*} head)
      (statement-children children)

      (= head 'let)
      (concat (map vector (binding-value-nodes (child 0)) (repeat :value))
              (statement-children (drop 1 children)))

      (= head 'var)
      (when-let [value (last children)]
        [[value :value]])

      (= head ':=)
      (when-let [value (last children)]
        [[value :value]])

      (= head 'return)
      [[(child 0) :value]]

      (#{'fn 'fn.inner} head)
      (let [body (if (api/vector-node? (child 0))
                   (drop 1 children)
                   (drop 2 children))]
        (statement-children body))

      (block-head? head)
      (statement-children children)

      :else
      (map vector children (repeat :value)))))

(declare lint-node!)

(defn- lint-var-target! [node]
  (let [target-node (nth (vec (rest (:children node))) 0 nil)
        target (some-> target-node api/sexpr)]
    (cond
      (set? target)
      (let [invalid (remove symbol? target)
            fields (->> target
                        (filter symbol?)
                        (group-by #(symbol (str/replace (name %) "-" "_"))))]
        (when (seq invalid)
          (report! target-node :hara.xtalk/invalid-destructuring :error
                   "set destructuring targets must contain only symbols"))
        (doseq [[field bindings] fields]
          (when (> (count bindings) 1)
            (report! target-node :hara.xtalk/field-collision :error
                     (str "destructuring fields collide after snake_case emission: " field
                          "; prefer spear-case binding "
                          (symbol (str/replace (name field) "_" "-")))))))

      (vector? target)
      (when-not (every? symbol? target)
        (report! target-node :hara.xtalk/invalid-destructuring :error
                 "vector destructuring targets must contain only symbols")))))

(defn- lint-node! [node context]
  (when node
    (let [form (api/sexpr node)]
      (cond
        (api/list-node? node)
        (let [head (canonical-head (first form))]
          (when (and (= :value context) (block-head? head))
            (report! node :hara.xtalk/block-in-value :error
                     (str "block form " head
                          " is not valid in value position; use :? for value conditionals")))
          (when (and (= head '.)
                     (nested-dot-access? form))
            (report! node :hara.xtalk/nested-dot-access :warning
                     (str "nested dot access can be flattened into a single dot form: "
                          (flatten-dot-access form))))
          (when (and (= head 'var)
                     (>= (count form) 2))
            (lint-var-target! node))
          (when (and (= head 'x:get-key)
                     (or (= 3 (count form))
                         (and (= 4 (count form))
                              (nil? (nth form 3)))))
            (report! node :hara.xtalk/redundant-get-key :warning
                     "simple x:get-key can use canonical dot access (. obj [key])"))
          (when-let [suggestion (when (= head 'fn:>)
                                  (fn-arrow-suggestion form))]
            (report! node :hara.xtalk/redundant-fn-arrow :warning
                     (str "fn:> with an explicit argument vector and nil body can use canonical fn with an explicit return: "
                          suggestion)))
          (doseq [[child-node child-context] (node-pairs head node)]
            (lint-node! child-node child-context)))

        (or (api/vector-node? node)
            (api/set-node? node))
        (doseq [child-node (:children node)]
          (lint-node! child-node :value))

        (api/map-node? node)
        (doseq [child-node (:children node)]
          (lint-node! child-node :value))))))

(defn- function-args-node [node]
  (or (some #(when (api/vector-node? %) %)
            (rest (:children node)))
      (some #(when (and (api/list-node? %)
                        (api/vector-node? (first (:children %))))
               (first (:children %)))
            (rest (:children node)))))

(defn- function-body-nodes [node]
  (let [tail (vec (rest (:children node)))
        args-index (first (keep-indexed
                           (fn [i child]
                             (when (api/vector-node? child) i))
                           tail))
        after-args (if args-index
                     (subvec tail (inc args-index))
                     [])
        arities (filter #(and (api/list-node? %)
                              (api/vector-node? (first (:children %))))
                        tail)]
    (if args-index
      after-args
      (mapcat #(rest (:children %)) arities))))

(defn- lint-definition! [node]
  (let [form (api/sexpr node)
        head (canonical-head (first form))]
    (cond
      (#{'defn.xt 'defgen.xt} head)
      (doseq [body-node (function-body-nodes node)]
        (lint-node! body-node :statement))

      (= head 'def.xt)
      (when-let [value-node (last (:children node))]
        (lint-node! value-node :value)))))

(defn- definition-name-node [node]
  (let [tail (rest (:children node))
        args-node (some #(when (api/vector-node? %) %) tail)]
    (some #(when (and (api/token-node? %)
                      (symbol? (api/sexpr %)))
             %)
          (take-while #(not (identical? % args-node)) tail))))

(defn- definition-stub [node _op]
  (let [name-node (definition-name-node node)
        _args-node (function-args-node node)]
    (cond
      (= '. (api/sexpr name-node))
      (api/list-node [(api/token-node 'do)])

      name-node
      (api/list-node [(api/token-node 'clojure.core/def)
                      name-node (api/coerce nil)])

      :else
      (api/list-node [(api/token-node 'do)]))))

(defn defn-xt [{:keys [node]}]
  (lint-definition! node)
  {:node (definition-stub node 'defn)})

(defn def-xt [{:keys [node]}]
  (lint-definition! node)
  {:node (definition-stub node 'def)})

(defn defmacro-xt [{:keys [node]}]
  {:node (definition-stub node 'defmacro)})

(defn defgen-xt [{:keys [node]}]
  (lint-definition! node)
  {:node (definition-stub node 'defn)})

(defn defvar-xt [{:keys [node]}]
  {:node (definition-stub node 'def)})

(defn defspec-xt [{:keys [node]}]
  (if-let [name-node (definition-name-node node)]
    {:node (api/list-node [(api/token-node 'clojure.core/declare)
                           name-node])}
    {:node (api/list-node [(api/token-node 'do)])}))

(defn defglobal-xt [{:keys [node]}]
  {:node (definition-stub node 'def)})

(defn fact-xt
  "Keep code.test facts opaque to Clojure analysis.

   Their assertion bodies contain target-language forms such as !.js, fn:>,
   return, and target-specific symbols. The XTalk hooks lint source definitions
   directly; a fact hook prevents the custom test macro body from being
   mistaken for ordinary Clojure."
  [{:keys [node]}]
  (lint-fn-arrows! node)
  {:node (api/list-node [(api/token-node 'do)])})

(defn fact-global-xt
  "Keep fact:global setup/teardown forms opaque to Clojure analysis."
  [{:keys [node]}]
  (lint-fn-arrows! node)
  {:node (api/list-node [(api/token-node 'do)])})
