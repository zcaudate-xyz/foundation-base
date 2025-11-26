(ns std.vm.toy-interpreter
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.check :as check]
            [std.block.type :as type]
            [std.block.parse :as parse]
            [std.lib.zip :as zip]
            [std.print.ansi :as ansi]
            [std.string :as str])
  (:import (std.protocol.block IBlock IBlockContainer IBlockExpression)))

;;
;; Block Zipper
;;

(def +block-context+
  {:create-container    construct/block
   :create-element      construct/block
   :is-container?       base/container?
   :is-empty-container? (fn [b] (empty? (base/block-children b)))
   :is-element?         (constantly true)
   :list-elements       base/block-children
   :update-elements     base/replace-children
   :add-element         construct/add-child

   ;; Required by check-context in std.lib.zip
   :cursor              '|
   :at-left-most?       zip/at-left-most?
   :at-right-most?      zip/at-right-most?
   :at-inside-most?     zip/at-inside-most?
   :at-inside-most-left? zip/at-inside-most-left?
   :at-outside-most?    zip/at-outside-most?

   :update-step-inside  (fn [b c] b)
   :update-step-right   (fn [b c] b)
   :update-step-left    (fn [b c] b)
   :update-step-outside (fn [b c] b)})

(defn block-zip
  "creates a zipper for block"
  [root]
  (zip/zipper root +block-context+))

;;
;; Helper Functions
;;

(defn expression?
  "checks if a block is an expression (not whitespace/comment)"
  [b]
  (and (base/block? b)
       (not= :void (base/block-type b))
       (not= :comment (base/block-type b))))

(declare list-expression?)

(declare list-expression?)

(defn value?
  "checks if a block is a fully reduced value"
  [b]
  (let [type (base/block-type b)]
    (cond (= :token type) true
          (list-expression? b) false
          (= :collection type) true
          :else false)))

(defn symbol-token?
  [b]
  (= :symbol (base/block-tag b)))

(defn list-expression?
  [b]
  (let [tag (base/block-tag b)]
    (or (= :list tag)
        ;; (= :collection tag) ;; Do NOT rely on :collection for generic lists here, rely on :list tag.
        ;; parse-string produces :list. construct/block might produce :list.
        ;; Let's trust check/collection-tag
        )))

(defn get-expressions
  "gets all non-whitespace children"
  [b]
  (filter expression? (base/block-children b)))

;;
;; Environment & Primitives
;;

(def +primitives+
  {'+ + '- - '* * '/ / 'str str 'inc inc 'dec dec '= = 'println println})

(defn lookup-symbol
  "looks up a symbol in the environment"
  [sym env]
  (or (get env sym)
      (get +primitives+ sym)
      (throw (ex-info (str "Unable to resolve symbol: " sym) {:symbol sym}))))

;;
;; Redex Finding
;;

(defn macro? [op]
  (contains? #{'if 'let 'do 'def} op))

(declare find-redex)

(defn find-redex-list
  [z]
  (let [node (zip/right-element z)
        exprs (get-expressions node)]
    (if (empty? exprs)
      nil ;; Empty list is a value ()
      (let [op (first exprs)
            op-val (if (symbol-token? op) (base/block-value op) nil)]
        (cond
          ;; If operator is a macro/special form
          (macro? op-val)
          (cond
            (= 'if op-val)
            (let [cond-node (second exprs)] ;; (if cond ...)
              (if (value? cond-node)
                z ;; Ready to reduce (if val ...)
                ;; Find redex in condition
                (let [idx (.indexOf (vec (base/block-children node)) cond-node)]
                  (-> z zip/step-inside (zip/step-right idx) find-redex))))

            (= 'do op-val)
            z ;; (do ...) is always a redex unless empty

            (= 'def op-val)
            (let [val-node (nth exprs 2 nil)]
               (if (and val-node (not (value? val-node)))
                 ;; Evaluate value
                 (let [idx (.indexOf (vec (base/block-children node)) val-node)]
                    (-> z zip/step-inside (zip/step-right idx) find-redex))
                 z))

             (= 'let op-val)
             z ;; (let ...) is handled as a substitution redex

            :else z)

          ;; Standard Function Call
          :else
          (loop [children (base/block-children node)
                 i 0]
            (if (empty? children)
              z ;; (f) - ready to call
              (let [child (first children)]
                (if (and (expression? child)
                         (not (value? child)))
                  ;; Found a non-reduced argument, recurse
                  (let [child-z (-> z zip/step-inside (zip/step-right i))]
                     (or (find-redex child-z)
                         child-z))
                  (recur (rest children) (inc i)))))))))))

(defn find-redex
  "finds the next reducible expression zipper location"
  [z]
  (let [node (zip/right-element z)]
    (cond
      (symbol-token? node) z ;; Symbol needs lookup
      (list-expression? node) (find-redex-list z)
      :else nil)))

;;
;; Evaluation Rules
;;

(defn eval-primitive [op args]
  (apply op args))

(defn substitute [body bind-sym bind-val]
  ;; Simple substitution for toy let
  ;; Replaces all occurrences of bind-sym with bind-val in body
  (if (base/container? body)
    (let [new-children (mapv #(substitute % bind-sym bind-val) (base/block-children body))]
      (base/replace-children body new-children))
    (if (and (symbol-token? body)
             (= (base/block-value body) bind-sym))
      bind-val
      body)))

(defn reduce-expression
  "reduces the expression at the zipper"
  [z env]
  (let [node (zip/right-element z)]
    (cond
      ;; Symbol Lookup
      (symbol-token? node)
      (let [val (lookup-symbol (base/block-value node) env)]
        (cond (fn? val) z ;; Functions self-evaluate in this toy
              :else (zip/replace-right z (construct/block val))))

      ;; List Reduction
      (list-expression? node)
      (let [exprs (get-expressions node)
            op (first exprs)
            op-val (if (symbol-token? op) (base/block-value op) (if (value? op) (base/block-value op)))]

        (cond
          ;; IF
          (= 'if op-val)
          (let [cond-val (base/block-value (second exprs))
                branch   (if cond-val (nth exprs 2) (nth exprs 3))]
            (zip/replace-right z branch))

          ;; DO
          (= 'do op-val)
          (let [rest-exprs (rest exprs)]
            (cond (empty? rest-exprs) (zip/replace-right z (construct/block nil))
                  (= 1 (count rest-exprs)) (zip/replace-right z (first rest-exprs))
                  :else
                  ;; Remove the first expression (it's done) and keep 'do
                  (let [done-expr (first rest-exprs)
                        new-children (remove #(= % done-expr) (base/block-children node))]
                    (zip/replace-right z (base/replace-children node new-children)))))

          ;; LET (Simple Substitution)
          (= 'let op-val)
          (let [bindings (base/block-children (second exprs)) ;; Vector of bindings
                binding-pairs (filter expression? bindings)]
             (if (empty? binding-pairs)
               ;; (let [] body) -> (do body)
               (let [body (drop 2 exprs)]
                 (zip/replace-right z (construct/block (cons 'do body))))

               ;; (let [k v ...] body) -> (let [... ] (subst body k v))
               ;; Actually, we need to eval 'v' first?
               ;; Our find-redex doesn't enter let bindings yet.
               ;; Let's assume for this toy we substitute immediately (Call-by-name ish) or we handle reduction.
               ;; To keep it simple: We reduce the FIRST binding value.
               (let [k (first binding-pairs)
                     v (second binding-pairs)]
                 (if (value? v)
                   ;; Substitute and remove binding
                   (let [new-bindings-vec (base/replace-children (second exprs) (drop 2 bindings)) ;; super hacky on spacing
                         body (drop 2 exprs)
                         new-body (map #(substitute % (base/block-value k) v) body)
                         new-node (base/replace-children node (concat [(first (base/block-children node)) ;; let
                                                                       new-bindings-vec]
                                                                      new-body))]
                     (zip/replace-right z new-node))

                   ;; Value not ready, we should have found it in find-redex?
                   ;; I need to update find-redex to enter let bindings.
                   ;; For now, let's just say we don't support complex lets.
                   z))))

          ;; Function Application
          :else
          (let [args (map base/block-value (rest exprs))
                ;; Resolve function primitive
                func (if (symbol? op-val) (lookup-symbol op-val env) op-val)]
            (if (fn? func)
              (let [res (apply func args)]
                (zip/replace-right z (construct/block res)))
              (throw (ex-info "Not a function" {:op op-val}))))))

      :else z)))

;;
;; Visualization
;;

(deftype HighlightBlock [inner]
  IBlock
  (_type [_] (base/block-type inner))
  (_tag [_] (base/block-tag inner))
  (_string [_] (ansi/style (base/block-string inner) [:bold :green :underline])) ;; Highlight!
  (_length [_] (base/block-length inner))
  (_width [_] (base/block-width inner))
  (_height [_] (base/block-height inner))
  (_prefixed [_] (base/block-prefixed inner))
  (_suffixed [_] (base/block-suffixed inner))
  (_verify [_] (base/block-verify inner))

  IBlockContainer
  (_children [_] (base/block-children inner))
  (_replace_children [_ c] (base/replace-children inner c))

  IBlockExpression
  (_value [_] (base/block-value inner))
  (_value_string [_] (base/block-value-string inner)))

(defmethod print-method HighlightBlock [v w]
  (.write w (base/block-string v)))

(defn highlight-node [z]
  (zip/replace-right z (HighlightBlock. (zip/right-element z))))

(defn visualize [z]
  (let [root-z (zip/step-outside-most z)
        root-str (base/block-string (zip/right-element root-z))]
    (println "---------------------------------------------------")
    (println root-str)
    (println "---------------------------------------------------")))

;;
;; Main Loop
;;

(defn step [z env]
  (if-let [redex-z (find-redex z)]
    (do
      ;; Visualize
      (let [h-z (highlight-node redex-z)]
        (visualize h-z))

      ;; Reduce
      (let [new-z (reduce-expression redex-z env)]
        (zip/step-outside-most new-z)))
    nil)) ;; Done

(defn run [code-str]
  (let [root (parse/parse-string code-str)
        z (block-zip root)
        env {}]
    (println "Source:" code-str)
    (loop [curr-z z
           i 0]
      (if (> i 20) (println "Stop: Max steps")
          (if-let [next-z (step curr-z env)]
            (recur next-z (inc i))
            (println "Result:" (base/block-string (zip/right-element curr-z))))))))

(defn run-form [form]
  (let [root (construct/block form)
        z (block-zip root)
        env {}]
    (loop [curr-z z
           i 0]
      (if (> i 20) (println "Stop: Max steps")
          (if-let [next-z (step curr-z env)]
            (recur next-z (inc i))
            (println "Result:" (base/block-string (zip/right-element curr-z))))))))
