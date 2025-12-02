(ns std.vm.llvm-interpreter
  (:require [std.block.base :as base]
            [std.block.type :as type]
            [std.block.construct :as construct]
            [std.block.check :as check]
            [std.block.parse :as parse]
            [std.lib.zip :as zip]
            [std.print.ansi :as ansi]
            [std.string :as str])
  (:import (std.protocol.block IBlock)))

;; --- Context & Zip ---

(def +llvm-context+
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
  (zip/zipper root +llvm-context+))

;; --- Visuals ---

(defn clear-screen []
  (print "\u001b[2J\u001b[H")
  (flush))

(deftype HighlightBlock [inner]
  IBlock
  (_type [_] (base/block-type inner))
  (_tag [_] (base/block-tag inner))
  (_string [_] (ansi/style (base/block-string inner) [:bold :magenta :underline]))
  (_length [_] (base/block-length inner))
  (_width [_] (base/block-width inner))
  (_height [_] (base/block-height inner))
  (_prefixed [_] (base/block-prefixed inner))
  (_suffixed [_] (base/block-suffixed inner))
  (_verify [_] (base/block-verify inner)))

(defmethod print-method HighlightBlock [v w]
  (.write w (base/block-string v)))

(defn highlight [z]
  (zip/replace-right z (HighlightBlock. (zip/right-element z))))

;; --- BlockIR Structure ---

(defn block-val [b]
  (if (and (base/block? b) (base/expression? b))
    (base/block-value b)
    b))

(defn filter-valid [blocks]
  (let [items (if (base/container? blocks)
                (base/block-children blocks)
                (if (seq? blocks) blocks [blocks]))]
    (filter (fn [b] (and (base/block? b)
                         (not= :void (base/block-type b))
                         (not= :comment (base/block-type b))
                         (not= :linespace (base/block-type b))
                         (not= :linebreak (base/block-type b))))
            items)))

;; ... (rest of file)

(defn highlight-ip [vm]
  (let [{:keys [block idx]} (:ip vm)
        z (:code-zip vm)
        ;; Find function body
        func-z (zip/step-inside z) ;; define
        ;; Search for block with label == block
        found-block-z (loop [curr (zip/step-right (zip/step-right (zip/step-right func-z)))] ;; skip define @main (args)
                        (let [node (zip/right-element curr)]
                          (cond
                            (nil? node) nil
                            
                            (and (base/container? node)
                                 (= block (block-val (first (filter-valid node)))))
                            curr

                            :else (if (zip/can-step-right? curr)
                                    (recur (zip/step-right curr))
                                    nil))))]
    (if found-block-z
      ;; Step into block and find instruction at idx + 1 (label is 0)
      ;; We need to skip the label (first valid) and then skip 'idx' more valid instructions.
      (let [start-z (zip/step-inside found-block-z)
            ;; Helper to find next valid sibling
            next-valid (fn [z]
                         (loop [curr (zip/step-right z)]
                           (cond
                             (nil? curr) nil
                             (let [b (zip/right-element curr)]
                               (and (base/block? b)
                                    (not (type/void-block? b))
                                    (not (type/comment-block? b))))
                             curr
                             :else (recur (zip/step-right curr)))))
            ;; Find label (first valid)
            first-valid (if (let [b (zip/right-element start-z)]
                              (and (base/block? b)
                                   (not (type/void-block? b))
                                   (not (type/comment-block? b))))
                          start-z
                          (next-valid start-z))
            
            ;; Now skip 'idx' instructions
            inst-z (loop [curr first-valid i 0]
                     (if (>= i (inc idx)) ;; +1 for label
                       curr
                       (recur (next-valid curr) (inc i))))]
        (highlight inst-z))

      z))) ;; Not found?

(defn instruction? [b]
  ;; Instructions are vectors [ ... ]
  (or (= :vector (base/block-tag b))
      (= :list (base/block-tag b))))

(defn get-instructions [block-node]
  (filter instruction? (base/block-children block-node)))

;; --- VM State ---

(defrecord LLVMState [registers memory ip prev-block func-map block-map return-val code-zip])

(defn make-vm [root-code]
  ;; Root code: (define @main ... (entry ...) (label ...))
  ;; We need to index the basic blocks.
  (let [func-def (filter-valid root-code)
        ;; Skip 'define' '@name' 'args'
        body (drop 3 func-def)
        ;; Body is a list of Basic Blocks (lists)
        ;; (label inst inst ...)
        blocks (filter base/container? body)
        block-map (reduce (fn [acc b]
                            (let [children (filter-valid b)
                                  label (block-val (first children))
                                  insts (vec (rest children))]
                              (assoc acc label insts)))
                          {}
                          blocks)
        entry-label (block-val (first (filter-valid (first blocks))))
        _ (println "Make VM - Entry:" entry-label "Class:" (class entry-label) "Nil?:" (nil? entry-label))
        _ (if (nil? entry-label) (throw (ex-info "Entry label not found" {:blocks (count blocks) :first-block (first blocks)})))
        z (block-zip root-code)]
    (LLVMState. {} {} {:block entry-label :idx 0} nil {} block-map nil z)))

;; --- Operations ---

(defn resolve-val [vm val]
  (cond
    (and (symbol? val) (str/starts-with? (name val) "%"))
    (get (:registers vm) val)

    (number? val) val

    :else val))

(defn update-reg [vm reg val]
  (assoc-in vm [:registers reg] val))

(defn jump [vm label]
  (assoc vm :prev-block (-> vm :ip :block)
            :ip {:block label :idx 0}))

(defn next-inst [vm]
  (update-in vm [:ip :idx] inc))

(defn exec-inst [vm inst-block]
  (let [parts (map block-val (filter-valid (if (base/container? inst-block) inst-block (construct/block inst-block))))
        ;; Formats:
        ;; [%res = op type op1 op2]
        ;; [op type op1 op2]  (void)
        ;; [br label]
        ;; [br cond label1 label2]
        has-assign? (= '= (second parts))

        [res-reg op args] (if has-assign?
                            [(first parts) (nth parts 2) (drop 3 parts)]
                            [nil (first parts) (rest parts)])]

    ;; Skip type (usually first arg after op) for toy
    ;; Actually let's include type in args for consistency or strip it?
    ;; Example: (add i32 %a %b). args: (i32 %a %b).
    ;; We'll skip the type index 0.

    (case op
      add (let [[_ a b] args
                va (resolve-val vm a)
                vb (resolve-val vm b)
                res (+ va vb)]
            (-> (update-reg vm res-reg res)
                (next-inst)))

      sub (let [[_ a b] args
                va (resolve-val vm a)
                vb (resolve-val vm b)
                res (- va vb)]
            (-> (update-reg vm res-reg res)
                (next-inst)))

      mul (let [[_ a b] args
                va (resolve-val vm a)
                vb (resolve-val vm b)
                res (* va vb)]
            (-> (update-reg vm res-reg res)
                (next-inst)))

      icmp (let [[pred type a b] args
                 va (resolve-val vm a)
                 vb (resolve-val vm b)
                 res (case pred
                       eq (= va vb)
                       ne (not= va vb)
                       sgt (> va vb)
                       slt (< va vb)
                       false)]
             (-> (update-reg vm res-reg res)
                 (next-inst)))

      phi (let [[_type & sources] args
                ;; sources: [val1 label1] [val2 label2] ...
                ;; Actually in our syntax: ([val1 label1] [val2 label2])?
                ;; Or flat: val1 label1 val2 label2
                ;; Let's assume flat for simplicity in toy vector: [phi i32 %a entry %b loop]
                prev (:prev-block vm)
                pairs (partition 2 sources)
                match (first (filter #(= (second %) prev) pairs))
                val (if match (resolve-val vm (first match)) (throw (ex-info "Phi node missing path" {:prev prev})))]
            (-> (update-reg vm res-reg val)
                (next-inst)))

      br (if (= 1 (count args))
           ;; Unconditional: [br label]
           (jump vm (first args))
           ;; Conditional: [br i1 %cond label1 label2]
           (let [[_type cond true-l false-l] args
                 vcond (resolve-val vm cond)]
             (if vcond
               (jump vm true-l)
               (jump vm false-l))))

      ret (let [[_type val] args]
            (assoc vm :return-val (resolve-val vm val) :ip nil)) ;; Halt

      (throw (ex-info "Unknown op" {:op op})))))

;; --- Execution Loop ---

(defn get-current-inst-block [vm]
  (let [{:keys [block idx]} (:ip vm)
        insts (get (:block-map vm) block)]
    (if (and insts (< idx (count insts)))
      (nth insts idx)
      nil)))

;; To highlight, we need to find the block in the zip.
;; Since `block-map` just has values, we need to search the zip.
;; This is expensive O(N) but fine for toy animation.
(defn highlight-ip [vm]
  (let [{:keys [block idx]} (:ip vm)
        z (:code-zip vm)
        ;; Find function body
        func-z (zip/step-inside z) ;; define
        ;; Search for block with label == block
        found-block-z (loop [curr (zip/step-right (zip/step-right (zip/step-right func-z)))] ;; skip define @main (args)
                        (let [node (zip/right-element curr)]
                          (cond
                            (nil? node) nil
                            
                            (and (base/container? node)
                                 (= block (block-val (first (filter-valid node)))))
                            curr

                            :else (if (zip/can-step-right? curr)
                                    (recur (zip/step-right curr))
                                    nil))))]
    (if found-block-z
      ;; Step into block and find instruction at idx + 1 (label is 0)
      ;; We need to skip the label (first valid) and then skip 'idx' more valid instructions.
      (let [start-z (zip/step-inside found-block-z)
            ;; Helper to find next valid sibling
            next-valid (fn [z]
                         (loop [curr (zip/step-right z)]
                           (cond
                             (nil? curr) nil
                             (let [b (zip/right-element curr)]
                               (and (base/block? b)
                                    (not= :void (base/block-type b))
                                    (not= :comment (base/block-type b))
                                    (not= :linespace (base/block-type b))
                                    (not= :linebreak (base/block-type b))))
                             curr
                             :else (recur (zip/step-right curr)))))
            ;; Find label (first valid) - actually step-inside might land on void if block starts with newline?
            ;; No, step-inside goes to first child.
            ;; If first child is void, we need to find first valid.
            first-valid (if (let [b (zip/right-element start-z)]
                              (and (base/block? b)
                                   (not= :void (base/block-type b))
                                   (not= :comment (base/block-type b))
                                   (not= :linespace (base/block-type b))
                                   (not= :linebreak (base/block-type b))))
                          start-z
                          (next-valid start-z))
            
            ;; Now skip 'idx' instructions
            inst-z (loop [curr first-valid i 0]
                     (if (>= i (inc idx)) ;; +1 for label
                       curr
                       (recur (next-valid curr) (inc i))))]
        (highlight inst-z))

      z))) ;; Not found?

(defn visualize [vm]
  (let [z (highlight-ip vm)
        root (zip/root-element z)]
    (println "---------------------------------------------------")
    (println (base/block-string root))
    (println "---------------------------------------------------")
    (println (ansi/style "Registers:" [:bold]))
    (doseq [[k v] (sort-by str (:registers vm))]
      (println (format "  %-5s = %s" k v)))
    (println (ansi/style "Block: " [:bold]) (-> vm :ip :block))
    (if (:return-val vm)
      (println (ansi/style "RETURN: " [:bold :green]) (:return-val vm)))))

(defn animate [code-str delay]
  (let [root (parse/parse-string code-str)
        vm (make-vm root)]
    (clear-screen)
    (loop [vm vm i 0]
      (if (> i 100) (println "Max steps")
          (if (:ip vm)
            (do
              (clear-screen)
              (visualize vm)
              (Thread/sleep delay)
              (let [inst (get-current-inst-block vm)]
                (recur (exec-inst vm inst) (inc i))))
            (do
              (clear-screen)
              (visualize vm)
              (println "Finished.")))))))
