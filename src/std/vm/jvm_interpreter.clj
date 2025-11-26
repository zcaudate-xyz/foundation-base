(ns std.vm.jvm-interpreter
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.check :as check]
            [std.block.parse :as parse]
            [std.lib.zip :as zip]
            [std.print.ansi :as ansi]
            [std.string :as str])
  (:import (std.protocol.block IBlock)))

;; --- Context & Zip ---

(def +jvm-context+
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
  (zip/zipper root +jvm-context+))

;; --- Visuals ---

(defn clear-screen []
  (print "\u001b[2J\u001b[H")
  (flush))

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
  (_verify [_] (base/block-verify inner)))

(defmethod print-method HighlightBlock [v w]
  (.write w (base/block-string v)))

(defn highlight [z]
  (zip/replace-right z (HighlightBlock. (zip/right-element z))))

;; --- JVM Structure ---

(defn block-val [b]
  (if (base/block? b) (base/block-value b) b))

(defn filter-valid [blocks]
  (let [items (if (base/container? blocks)
                (base/block-children blocks)
                (if (seq? blocks) blocks [blocks]))]
    (filter (fn [b] (and (base/block? b)
                         (not= :void (base/block-type b))
                         (not= :comment (base/block-type b))))
            items)))

;; --- VM State ---

(defrecord Frame [method-name code-zip stack locals ip ret-addr])
(defrecord JVM [classes frames output])

(defn make-jvm [root-code]
  ;; root-code: (class Name (method main ...) (method foo ...))
  (let [root-block (if (base/container? root-code) root-code (construct/block root-code))
        children (vec (filter-valid root-block)) ;; Uses fixed filter-valid
        class-name (if (> (count children) 1) (block-val (second children)) "Unknown")
        ;; DEBUG: print children tags
        ;; _ (println "Children:" (map base/block-tag children))
        methods (filter (fn [b]
                          (if (and (base/block? b) (base/container? b))
                            (let [parts (vec (filter-valid b))]
                              (and (seq parts) (= 'method (block-val (first parts)))))
                            ;; Also check list? if construct/block wasn't used for deeper children properly?
                            (if (list? b)
                               (= 'method (first b))
                               false)))
                        children) ;; Iterate over VALID children (skips voids)
        method-map (reduce (fn [acc m]
                             (let [parts (vec (filter-valid m))
                                   ;; (method name [...])
                                   ;; parts[0] = method
                                   ;; parts[1] = name
                                   ;; parts[2] = body (vector)
                                   name (block-val (nth parts 1))
                                   code-block (nth parts 2)]
                               ;; If 'main' is passed as 'main, we might have a symbol/string mismatch.
                               ;; Ensure keys are consistent (symbols).
                               (assoc acc (if (string? name) (symbol name) name) code-block)))
                           {}
                           methods)]
    (JVM. method-map [] [])))

(defn push-frame [jvm method-name args]
  (let [method-name (if (string? method-name) (symbol method-name) method-name)
        method-block (get (:classes jvm) method-name)] ;; Actually stored in :classes for this toy
    (if method-block
      (let [block-to-zip (if (base/container? method-block) method-block (construct/block method-block))
            z (block-zip block-to-zip)
            ;; We need to step INSIDE the vector body.
            ;; block-zip creates a zipper AT the vector.
            ;; step-inside moves to the first instruction.
            z (if (zip/can-step-inside? z) (zip/step-inside z) z)
            frame (Frame. method-name z [] (vec args) 0 nil)]
        (update jvm :frames conj frame))
      (throw (ex-info "Method not found" {:method method-name :available (keys (:classes jvm))})))))

(defn pop-frame [jvm]
  (update jvm :frames pop))

(defn current-frame [jvm]
  (peek (:frames jvm)))

(defn update-current-frame [jvm f]
  (let [frames (:frames jvm)
        curr (peek frames)
        new-curr (f curr)]
    (assoc jvm :frames (conj (pop frames) new-curr))))

;; --- Instructions ---

(defn exec-inst [jvm inst-block]
  (let [parts (map block-val (filter-valid (base/block-children (if (base/container? inst-block) inst-block (construct/block inst-block)))))
        op (first parts)
        args (rest parts)]

    (update-current-frame jvm
      (fn [frame]
        (let [{:keys [stack locals ip]} frame
              next-ip (zip/can-step-right? (:code-zip frame))
              frame (if next-ip
                      (assoc frame :code-zip (zip/step-right (:code-zip frame)))
                      (assoc frame :code-zip nil)) ;; Done
              ]

          (case op
            iconst_0 (update frame :stack conj 0)
            iconst_1 (update frame :stack conj 1)
            iconst_2 (update frame :stack conj 2)
            iconst_3 (update frame :stack conj 3)
            iconst_4 (update frame :stack conj 4)
            iconst_5 (update frame :stack conj 5)

            bipush (update frame :stack conj (first args))

            istore_0 (assoc frame :stack (pop stack) :locals (assoc locals 0 (peek stack)))
            istore_1 (assoc frame :stack (pop stack) :locals (assoc locals 1 (peek stack)))
            istore_2 (assoc frame :stack (pop stack) :locals (assoc locals 2 (peek stack)))
            istore_3 (assoc frame :stack (pop stack) :locals (assoc locals 3 (peek stack)))

            iload_0 (update frame :stack conj (get locals 0))
            iload_1 (update frame :stack conj (get locals 1))
            iload_2 (update frame :stack conj (get locals 2))
            iload_3 (update frame :stack conj (get locals 3))

            iadd (let [v2 (peek stack) s1 (pop stack)
                       v1 (peek s1) s2 (pop s1)]
                   (assoc frame :stack (conj s2 (+ v1 v2))))

            isub (let [v2 (peek stack) s1 (pop stack)
                       v1 (peek s1) s2 (pop s1)]
                   (assoc frame :stack (conj s2 (- v1 v2))))

            imul (let [v2 (peek stack) s1 (pop stack)
                       v1 (peek s1) s2 (pop s1)]
                   (assoc frame :stack (conj s2 (* v1 v2))))

            ;; Control Flow (Toy - scanning vector)
            ;; Real JVM uses offsets. Here we use label scanning?
            ;; Or just execute linearly for this demo.
            ;; Let's implement goto by rewinding/forwarding zip? Hard without labels.
            ;; Let's assume linear execution for now or simple loops.

            return (assoc frame :code-zip nil) ;; Halt frame

            ireturn (assoc frame :code-zip nil) ;; Halt frame, push val to parent?

            (throw (ex-info "Unknown opcode" {:op op}))))))))

;; --- Visualization ---

(defn visualize [jvm]
  (let [frame (current-frame jvm)]
    (when frame
      (let [z (:code-zip frame)
            z-high (if (zip/right-element z) (highlight z) z)
            ;; We need to show the whole method body
            root (zip/root-element z-high)]
        (println "---------------------------------------------------")
        (println (ansi/style (str "Method: " (:method-name frame)) [:bold :white]))
        (println "---------------------------------------------------")
        (println (base/block-string root))
        (println "---------------------------------------------------")
        (println (ansi/style "Operand Stack:   " [:bold]) (str/join " " (:stack frame)))
        (println (ansi/style "Local Variables: " [:bold]) (str/join " " (:locals frame)))))))

(defn animate [code-str delay]
  (let [root (parse/parse-string code-str)
        jvm (make-jvm root)
        jvm (push-frame jvm 'main [])]
    (clear-screen)
    (loop [jvm jvm i 0]
      (if (> i 100) (println "Max steps")
          (let [frame (current-frame jvm)]
            (if (and frame (zip/right-element (:code-zip frame)))
              (do
                (clear-screen)
                (visualize jvm)
                (Thread/sleep delay)
                (recur (exec-inst jvm (zip/right-element (:code-zip frame))) (inc i)))
              (do
                (clear-screen)
                (println "Execution Finished.")
                (visualize jvm))))))))
