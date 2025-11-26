(ns std.vm.scheme-interpreter
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.check :as check]
            [std.block.parse :as parse]
            [std.lib.zip :as zip]
            [std.print.ansi :as ansi]
            [std.string :as str])
  (:import (std.protocol.block IBlock IBlockContainer IBlockExpression)))

;; --- Context & Zip ---

(def +scheme-context+
  {:create-container    construct/block
   :create-element      construct/block
   :is-container?       base/container?
   :is-empty-container? (fn [b] (empty? (base/block-children b)))
   :is-element?         (constantly true)
   :list-elements       base/block-children
   :update-elements     base/replace-children
   :add-element         construct/add-child

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

(defn block-zip [root]
  (zip/zipper root +scheme-context+))

;; --- Predicates ---

(declare lambda?)

(defn expression? [b]
  (and (base/block? b)
       (not= :void (base/block-type b))
       (not= :comment (base/block-type b))))

(defn list-expression? [b]
  (let [tag (base/block-tag b)]
    (or (= :list tag)
        (= :collection tag)))) ;; For generic block constructions

(defn symbol-token? [b]
  (or (= :symbol (base/block-tag b))
      (symbol? (base/block-value b))))

(defn value? [b]
  (cond
    (lambda? b) true ;; Lambda forms are values
    (list-expression? b) false ;; Standard lists are calls/forms
    (base/container? b) (every? value? (filter expression? (base/block-children b))) ;; Vectors/Literals
    :else true)) ;; Tokens are values

;; --- Helpers ---

(defn get-exprs [node]
  (filter expression? (base/block-children node)))

(defn block-val [b]
  (if (base/block? b) (base/block-value b) b))

(defn lambda? [b]
  (and (list-expression? b)
       (let [exprs (get-exprs b)]
         (and (seq exprs)
              (= 'lambda (block-val (first exprs)))))))

;; --- Globals ---

(defonce +env+ (atom {})) ;; Global Environment for defines

(def +primitives+
  {'+ + '- - '* * '/ /
   '= = '< < '> >
   'display println})

(defn lookup [sym]
  (or (get @+env+ sym)
      (get +primitives+ sym)))

;; --- Substitution ---

(defn substitute [body param arg]
  ;; Replace occurrences of param (symbol) with arg (block) in body
  (cond
    (symbol-token? body)
    (if (= (block-val body) param) arg body)

    (base/container? body)
    (let [children (base/block-children body)
          new-children (mapv #(substitute % param arg) children)]
      (base/replace-children body new-children))

    :else body))

(defn apply-lambda [lambda args]
  ;; lambda: (lambda (p1 p2) body...)
  ;; args: [v1 v2] (blocks)
  ;; Returns: (begin body...) with substitutions
  (let [exprs (get-exprs lambda)
        params-node (second exprs)
        params (map block-val (get-exprs params-node))
        body (drop 2 exprs)]

    (if (not= (count params) (count args))
      (throw (ex-info "Arity mismatch" {:params params :args args})))

    (let [subbed-body (reduce (fn [b-exprs [p a]]
                                (map #(substitute % p a) b-exprs))
                              body
                              (map vector params args))]
      (if (= 1 (count subbed-body))
        (first subbed-body)
        (construct/block (cons 'begin (map base/block-value subbed-body))))))) ;; Re-wrap in begin if multiple

;; --- Redex Finding ---

(defn find-redex [z]
  (let [node (zip/right-element z)]
    (cond
      ;; Symbol -> Redex if defined
      (symbol-token? node)
      (if (lookup (block-val node)) z nil)

      ;; List
      (list-expression? node)
      (let [exprs (get-exprs node)]
        (if (empty? exprs)
          nil ;; () is nil/empty
          (let [op (first exprs)
                op-sym (if (symbol-token? op) (block-val op))]
            (cond
              ;; Special Forms
              (= 'lambda op-sym) nil ;; Lambda is a value
              (= 'quote op-sym) nil ;; Quote is a value

              (= 'if op-sym)
              (let [test (second exprs)]
                (if (value? test)
                  z ;; Ready to reduce
                  (let [idx (.indexOf (vec (base/block-children node)) test)]
                    (recur (-> z zip/step-inside (zip/step-right idx))))))

              (= 'define op-sym)
              (let [val (nth exprs 2 nil)]
                (if (value? val)
                  z ;; Ready to define
                  ;; Use index in children directly (exprs is filtered)
                  ;; Need to find the child block that corresponds to `val`
                  (let [idx (.indexOf (vec (base/block-children node)) val)]
                    (recur (-> z zip/step-inside (zip/step-right idx))))))

              (= 'set! op-sym)
              (let [val (nth exprs 2 nil)]
                (if (value? val)
                  z
                  (let [idx (.indexOf (vec (base/block-children node)) val)]
                    (recur (-> z zip/step-inside (zip/step-right idx))))))

              (= 'begin op-sym)
              ;; Find first non-value
              (let [children (base/block-children node)
                    ;; skip 'begin symbol
                    expr-children (rest exprs)
                    first-non-val (first (filter (complement value?) expr-children))]
                (if first-non-val
                  ;; Use .indexOf on `exprs` (which are blocks) to find index in `exprs` list?
                  ;; No, we need index in `children` list.
                  ;; `exprs` is derived from `children` filtering for expressions.
                  ;; `first-non-val` is one of the blocks in `children`.
                  (recur (-> z zip/step-inside (zip/step-right (.indexOf (vec children) first-non-val))))
                  ;; All values? Then begin is redex (to reduce to last val)
                  z))

              ;; Application
              :else
              (loop [children (base/block-children node)
                     i 0]
                (if (empty? children)
                  z ;; (proc) - ready
                  (let [child (first children)]
                    (if (and (expression? child) (not (value? child)))
                      ;; Found non-value arg (or op)
                      (let [res (find-redex (-> z zip/step-inside (zip/step-right i)))]
                        (or res z)) ;; If inner not found, but this is non-value list, maybe it's a redex itself? No, find-redex handles recursive search.
                      (recur (rest children) (inc i))))))))))

      :else nil)))

;; --- Reduction ---

(defn reduce-expr [z]
  (let [node (zip/right-element z)]
    (cond
      ;; Symbol Lookup
      (symbol-token? node)
      (let [val (lookup (block-val node))]
        (if (or (fn? val) (lambda? (construct/block val))) ;; Prims or stored lambdas
          (zip/replace-right z (construct/block val)) ;; Replace symbol with value
          z)) ;; Undefined?

      ;; Lists
      (list-expression? node)
      (let [exprs (get-exprs node)
            op (first exprs)
            op-val (block-val op)]
        (cond
          (= 'if op-val)
          (let [[_ test then else] exprs
                test-val (block-val test)]
            (zip/replace-right z (if test-val then (or else (construct/block nil)))))

          (= 'define op-val)
          (let [[_ sym val] exprs]
            (swap! +env+ assoc (block-val sym) (if (base/block? val) (base/block-value val) val)) ;; Store raw value/lambda-list in env
            ;; Replace define with void/nil
            (zip/replace-right z (construct/block nil)))

          (= 'begin op-val)
          (let [[_ & rest-exprs] exprs]
            (if (empty? rest-exprs)
              (zip/replace-right z (construct/block nil))
              (if (= 1 (count rest-exprs))
                (zip/replace-right z (first rest-exprs))
                ;; (begin a b c) -> (begin b c) if a is value?
                ;; Actually in functional substitution, we assume 'a' was evaluated if we are here?
                ;; No, find-redex for 'begin' selects the whole block.
                ;; We need to eval items sequentially.
                ;; This substitution model for side-effects is tricky.
                ;; We'll assume 'begin' simply unwraps if head is value.
                ;; But wait, find-redex stops at 'begin'.
                ;; We need to find the first non-value in begin.
                ;; Let's adjust find-redex for 'begin' later if needed.
                ;; For now, standard substitution: replace (begin v1 v2) with (begin v2).
                (let [first-expr (first rest-exprs)]
                  (if (value? first-expr)
                    (let [new-children (cons op (rest rest-exprs))]
                      (zip/replace-right z (base/replace-children node new-children)))
                    ;; If first not value, we shouldn't be reducing 'begin' yet?
                    ;; find-redex should have pointed to first-expr.
                    ;; Correct. `find-redex` for begin should recurse.
                    z))))

          ;; Application
          :else
          (let [func (if (symbol-token? op) (lookup (block-val op)) op)
                args (rest exprs)]
            (cond
              ;; Primitive
              (fn? func)
              (let [arg-vals (map block-val args)]
                (zip/replace-right z (construct/block (apply func arg-vals))))

              ;; Lambda (Closure)
              ;; func might be a Block (if literal lambda) or list (from env lookup)
              ;; If from env, it might be raw list data. Construct block first.
              (or (lambda? func) (and (list? func) (= 'lambda (first func))))
              (let [func-block (if (base/block? func) func (construct/block func))]
                (zip/replace-right z (apply-lambda func-block args)))

              :else
              (throw (ex-info "Not a function" {:op op}))))))
      :else z))))

;; Re-fix find-redex for BEGIN
(defn find-redex [z]
  (let [node (zip/right-element z)]
    (cond
      (symbol-token? node)
      (if (lookup (block-val node)) z nil)

      (list-expression? node)
      (let [exprs (get-exprs node)]
        (if (empty? exprs) nil
          (let [op (first exprs)
                op-sym (if (symbol-token? op) (block-val op))]
            (cond
              (= 'lambda op-sym) nil
              (= 'quote op-sym) nil
              (= 'if op-sym) (let [t (second exprs)] (if (value? t) z (recur (-> z zip/step-inside (zip/step-right (.indexOf (vec (base/block-children node)) t))))))
              (= 'define op-sym) (let [v (nth exprs 2 nil)] (if (value? v) z (recur (-> z zip/step-inside (zip/step-right (.indexOf (vec (base/block-children node)) v))))))

              (= 'begin op-sym)
              ;; Find first non-value
              (let [children (base/block-children node)
                    ;; skip 'begin symbol
                    expr-children (rest exprs)
                    first-non-val (first (filter (complement value?) expr-children))]
                (if first-non-val
                  ;; Use .indexOf on `exprs` (which are blocks) to find index in `exprs` list?
                  ;; No, we need index in `children` list.
                  ;; `exprs` is derived from `children` filtering for expressions.
                  ;; `first-non-val` is one of the blocks in `children`.
                  (recur (-> z zip/step-inside (zip/step-right (.indexOf (vec children) first-non-val))))
                  ;; All values? Then begin is redex (to reduce to last val)
                  z))

              :else
              ;; App
              (loop [children (base/block-children node) i 0]
                (if (empty? children) z
                  (let [child (first children)]
                    (if (and (expression? child) (not (value? child)))
                      (recur (-> z zip/step-inside (zip/step-right i)) (dec i)) ;; Recurse down
                      (recur (rest children) (inc i))))))))))
      :else nil)))

;; --- Visuals ---

(deftype HighlightBlock [inner]
  IBlock
  (_type [_] (base/block-type inner))
  (_tag [_] (base/block-tag inner))
  (_string [_] (ansi/style (base/block-string inner) [:bold :cyan :underline]))
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

(defn highlight [z]
  (zip/replace-right z (HighlightBlock. (zip/right-element z))))

(defn clear-screen []
  (print "\u001b[2J\u001b[H")
  (flush))

(defn visualize [z]
  (let [root (zip/root-element z)]
    (println "---------------------------------------------------")
    (println (base/block-string root))
    (println "---------------------------------------------------")))

(defn step [z]
  (if-let [rz (find-redex z)]
    (do
      (visualize (highlight rz))
      (let [nz (reduce-expr rz)]
        (zip/step-outside-most nz)))
    nil))

(defn run-step [input]
  (let [root (if (string? input) (parse/parse-string input) input)
        z (block-zip root)]
    (step z)))

(defn animate [input delay]
  (reset! +env+ {})
  (let [root (if (string? input) (parse/parse-string input) input)
        z (block-zip root)]
    (clear-screen)
    (println "Source:" (base/block-string root))
    (loop [curr z i 0]
      (if (> i 100) (println "Max steps reached")
          (if-let [rz (find-redex curr)]
            (do
              (clear-screen)
              (visualize (highlight rz))
              (Thread/sleep delay)
              (let [nz (reduce-expr rz)]
                (recur (zip/step-outside-most nz) (inc i))))
            (do
              (clear-screen)
              (println "Result:" (base/block-string (zip/right-element curr)))))))))

(defn run [input]
  (reset! +env+ {})
  (let [root (if (string? input) (parse/parse-string input) input)
        z (block-zip root)]
    (println "Source:" (base/block-string root))
    (loop [curr z i 0]
      (if (> i 100) (println "Max steps reached")
          (if-let [next-z (step curr)]
            (recur next-z (inc i))
            (println "Result:" (base/block-string (zip/right-element curr))))))))
