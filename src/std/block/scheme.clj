(ns std.block.scheme
  (:require [std.block.base :as base]
            [std.block.parse :as parse]
            [std.block.construct :as construct]
            [std.print.ansi :as ansi]
            [std.string :as str]))

;; --- Instruction Set ---

(defrecord Op [code arg])

(defn op [code & [arg]]
  (->Op code arg))

(defn op-string [op]
  (if (:arg op)
    (format "%-10s %s" (name (:code op)) (:arg op))
    (name (:code op))))

;; --- Compiler Context ---

(def ^:dynamic *labels* nil)
(def ^:dynamic *next-label* 0)

(defn gen-label []
  (let [l *next-label*]
    (set! *next-label* (inc *next-label*))
    (keyword (str "L" l))))

(defn emit [& ops]
  (vec ops))

;; --- Compilation ---

(declare compile-expr)

(defn compile-sequence [exprs]
  (if (empty? exprs)
    []
    (let [n (count exprs)
          compiled (map compile-expr exprs)]
      ;; Emit POP after every expression except the last one
      (concat (mapcat (fn [ops] (concat ops [(op :POP)])) (butlast compiled))
              (last compiled)))))

(defn compile-if [exprs]
  (let [[test then else] exprs
        l-else (gen-label)
        l-end  (gen-label)]
    (concat (compile-expr test)
            [(op :JMP_FALSE l-else)]
            (compile-expr then)
            [(op :JMP l-end)
             (op :LABEL l-else)]
            (if else (compile-expr else) [(op :PUSH nil)])
            [(op :LABEL l-end)])))

(defn compile-define [exprs]
  (let [[sym val] exprs]
    (concat (compile-expr val)
            [(op :STORE_GLOBAL (base/block-value sym))
             (op :PUSH nil)]))) ;; define returns void/nil

(defn compile-app [exprs]
  (let [[func & args] exprs
        prim-map {'+ :ADD '- :SUB '* :MUL '/ :DIV '= :EQ '< :LT '> :GT 'display :PRINT}]
    (if (and (symbol? (base/block-value func))
             (contains? prim-map (base/block-value func)))
      ;; Primitive Op
      ;; Wait, primitive ops in Stack Machine (Reverse Polish) need arguments FIRST.
      ;; But the recursive mapcat compile-expr args puts them there.
      ;; Issue might be the recursion depth or missing PUSHes.
      ;; Ah, '1' is token. 'compile-expr' calls 'compile-block' which handles tokens.
      ;; Let's debug print inside compile-expr.
      (let [ops (concat (mapcat compile-expr args)
                        [(op (get prim-map (base/block-value func)))])]
        ops)
      ;; Function Call (Not fully implemented in toy)
      (concat (mapcat compile-expr args)
              (compile-expr func)
              [(op :CALL (count args))]))))

(defn compile-block [b]
  (let [tag (base/block-tag b)]
    (cond
      (or (= :token tag) (= :long tag) (= :symbol tag)) ;; Include :symbol tag
      (let [val (base/block-value b)]
        (if (symbol? val)
          [(op :LOAD val)]
          [(op :PUSH val)]))

      (or (= :list tag) (= :collection tag) (= :vector tag)) ;; Handle vectors as lists too
      (let [children (filter (fn [c] (let [t (base/block-tag c)] (and (not= :linespace t) (not= :void t)))) (base/block-children b))]
        (if (empty? children)
           []
           (let [[op-node & args] children
                 op-val (if (base/expression? op-node) (base/block-value op-node))]
             (cond
               (= 'if op-val)     (compile-if args)
               (= 'define op-val) (compile-define args)
               (= 'begin op-val)  (compile-sequence args)
               :else              (compile-app children)))))

      :else [])))

(defn compile-expr [b]
  (compile-block b))

(defn resolve-labels [ops]
  (let [label-map (into {} (keep-indexed (fn [i o] (if (= :LABEL (:code o)) [(:arg o) i])) ops))
        ;; We need to remove labels from the stream and adjust indices?
        ;; Simpler: Keep labels as NO-OPs or strip them and adjust jumps.
        ;; Let's strip them.

        ;; 1. Filter out labels and build map of *real* indices
        real-ops (vec (remove #(= :LABEL (:code %)) ops))

        ;; Re-scan to map labels to real indices
        real-label-map (loop [i 0
                              orig-ops ops
                              m {}]
                         (if (empty? orig-ops)
                           m
                           (let [o (first orig-ops)]
                             (if (= :LABEL (:code o))
                               (recur i (rest orig-ops) (assoc m (:arg o) i))
                               (recur (inc i) (rest orig-ops) m)))))

        ;; 2. Patch Jumps
        patched-ops (mapv (fn [o]
                            (if (#{:JMP :JMP_FALSE :JMP_TRUE} (:code o))
                              (assoc o :arg (get real-label-map (:arg o)))
                              o))
                          real-ops)]
    patched-ops))

(defn compile-op-stream [root]
  (binding [*next-label* 0]
    (let [ops (compile-expr root)]
      (resolve-labels ops))))

(defn compile-input [input]
  (if (string? input)
    (compile-op-stream (parse/parse-string input))
    (compile-op-stream input)))

(defn compile! [input]
  (compile-input input))

;; --- Virtual Machine ---

(defrecord VM [stack env ip output])

(defn run-vm [ops]
  (loop [vm (VM. [] {} 0 [])]
    (let [{:keys [stack env ip output]} vm]
      (if (>= ip (count ops))
        vm
        (let [op (nth ops ip)
              code (:code op)
              arg (:arg op)]
          (case code
            :PUSH (recur (assoc vm :ip (inc ip) :stack (conj stack arg)))
            :LOAD (recur (assoc vm :ip (inc ip) :stack (conj stack (get env arg))))
            :STORE_GLOBAL (recur (assoc vm :ip (inc ip)
                                       :stack (pop stack)
                                       :env (assoc env arg (peek stack))))

            :ADD (let [b (peek stack) s1 (pop stack)
                       a (peek s1) s2 (pop s1)]
                   (recur (assoc vm :ip (inc ip) :stack (conj s2 (+ a b)))))
            :SUB (let [b (peek stack) s1 (pop stack)
                       a (peek s1) s2 (pop s1)]
                   (recur (assoc vm :ip (inc ip) :stack (conj s2 (- a b)))))
            :MUL (let [b (peek stack) s1 (pop stack)
                       a (peek s1) s2 (pop s1)]
                   (recur (assoc vm :ip (inc ip) :stack (conj s2 (* a b)))))
            :EQ  (let [b (peek stack) s1 (pop stack)
                       a (peek s1) s2 (pop s1)]
                   (recur (assoc vm :ip (inc ip) :stack (conj s2 (= a b)))))

            :JMP (recur (assoc vm :ip arg))
            :JMP_FALSE (let [val (peek stack) s1 (pop stack)]
                         (if (not val)
                           (recur (assoc vm :ip arg :stack s1))
                           (recur (assoc vm :ip (inc ip) :stack s1))))

            :PRINT (let [val (peek stack) s1 (pop stack)]
                     (recur (assoc vm :ip (inc ip) :stack s1 :output (conj output val))))

            :POP (recur (assoc vm :ip (inc ip) :stack (pop stack)))

            (throw (ex-info "Unknown Op" {:op op}))))))))

;; --- Visualization ---

(defn print-assembly [ops]
  (println (ansi/style "=== Scheme Bytecode ===" [:bold :cyan]))
  (doseq [[i op] (map-indexed vector ops)]
    (println (format "%3d: %s" i (op-string op)))))

(defn visualize-run [input]
  (println (ansi/style (str "Source: " (if (string? input) input (base/block-string input))) [:bold :white]))
  (let [ops (compile! input)]
    (print-assembly ops)
    (println (ansi/style "--- Execution ---" [:bold :yellow]))
    (let [final-vm (run-vm ops)]
      (println "Output:" (:output final-vm))
      (println "Stack: " (:stack final-vm))
      (println "Env:   " (:env final-vm)))))
