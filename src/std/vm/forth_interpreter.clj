(ns std.vm.forth-interpreter
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.check :as check]
            [std.block.parse :as parse]
            [std.lib.zip :as zip]
            [std.print.ansi :as ansi]
            [std.string :as str])
  (:import (std.protocol.block IBlock)))

;; --- Context & Zip ---

(def +forth-context+
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
  (zip/zipper root +forth-context+))

;; --- VM State ---

(defrecord ForthVM [stack dictionary ip code-zip output])

(defn make-vm [root-block]
  ;; Move zip inside the vector to the first element
  (let [z (block-zip root-block)
        z (if (zip/can-step-inside? z) (zip/step-inside z) z)]
    (ForthVM. [] {} 0 z [])))

;; --- Visuals ---

(defn clear-screen []
  (print "\u001b[2J\u001b[H")
  (flush))

(deftype HighlightBlock [inner]
  IBlock
  (_type [_] (base/block-type inner))
  (_tag [_] (base/block-tag inner))
  (_string [_] (ansi/style (base/block-string inner) [:bold :yellow :underline]))
  (_length [_] (base/block-length inner))
  (_width [_] (base/block-width inner))
  (_height [_] (base/block-height inner))
  (_prefixed [_] (base/block-prefixed inner))
  (_suffixed [_] (base/block-suffixed inner))
  (_verify [_] (base/block-verify inner)))

(defmethod print-method HighlightBlock [v w]
  (.write w (base/block-string v)))

(defn visualize [vm]
  (let [z (:code-zip vm)
        ;; Highlight current IP
        curr (zip/right-element z)
        z-high (if curr (zip/replace-right z (HighlightBlock. curr)) z)
        root (zip/root-element z-high)]

    (println "---------------------------------------------------")
    (println (base/block-string root))
    (println "---------------------------------------------------")
    (println (ansi/style "Stack: " [:bold]) (str/join " " (:stack vm)))
    (println (ansi/style "Output: " [:bold]) (str/join "" (:output vm)))))

;; --- Operations ---

(defn push [vm val]
  (update vm :stack conj val))

(defn pop-stack [vm]
  (let [s (:stack vm)]
    (if (empty? s)
      (throw (ex-info "Stack underflow" {}))
      [(peek s) (assoc vm :stack (pop s))])))

(defn binary-op [vm f]
  (let [[b vm] (pop-stack vm)
        [a vm] (pop-stack vm)]
    (push vm (f a b))))

(defn unary-op [vm f]
  (let [[a vm] (pop-stack vm)]
    (push vm (f a))))

(declare step)

(defn run-block [vm block]
  ;; This is for recursive execution (like `if` branches)
  ;; We need to suspend the current zip, run the new block, then resume?
  ;; Or just flatten the structure?
  ;; For simplicity: recursive call to run-loop, but we lose top-level visualization of the parent.
  ;; Ideally, we'd push a stack frame.
  ;; But for this toy, let's just recurse `run-loop` with a new VM sharing the stack.
  (let [sub-vm (make-vm block)
        sub-vm (assoc sub-vm :stack (:stack vm) :dictionary (:dictionary vm) :output (:output vm))
        res-vm (loop [v sub-vm]
                 (if (zip/right-element (:code-zip v))
                   (recur (step v))
                   v))]
    (assoc vm :stack (:stack res-vm) :output (:output res-vm))))

(defn exec-word [vm word]
  (case word
    + (binary-op vm +)
    - (binary-op vm -)
    * (binary-op vm *)
    / (binary-op vm /)
    > (binary-op vm >)
    < (binary-op vm <)
    = (binary-op vm =)

    dup (let [s (:stack vm)] (push vm (peek s)))
    drop (let [[_ v] (pop-stack vm)] v)
    swap (let [[a v1] (pop-stack vm) [b v2] (pop-stack v1)] (-> v2 (push a) (push b)))

    . (let [[a v] (pop-stack vm)] (update v :output conj (str a " ")))

    if (let [[else-blk v1] (pop-stack vm)
             [then-blk v2] (pop-stack v1)
             [test v3] (pop-stack v2)]
         ;; Blocks are usually vectors in our syntax
         (if test
           (run-block v3 then-blk)
           (run-block v3 else-blk)))

    ;; Default: check dictionary
    (throw (ex-info (str "Unknown word: " word) {}))))

(defn step [vm]
  (let [z (:code-zip vm)
        elem (zip/right-element z)]
    (if (nil? elem)
      vm ;; Halt
      (let [next-z (if (zip/can-step-right? z) (zip/step-right z) nil) ;; If no right, we are done after this
            next-vm (assoc vm :code-zip next-z)

            ;; Execute logic
            tag (base/block-tag elem)]

        (cond
          (= :linespace tag) next-vm ;; Skip
          (= :comment tag) next-vm   ;; Skip
          (= :void (base/block-type elem)) next-vm

          :else
          (let [val (base/block-value elem)]
            (cond
              ;; Words/Ops
              (symbol? val)
              (exec-word next-vm val)

              ;; Literals (Numbers, Strings, Vectors/Blocks)
              :else
              (push next-vm (if (base/container? elem) elem val))))))))) ;; Push block as is, or value


;; --- Runner ---

(defn animate [code-str delay]
  (let [root (parse/parse-string code-str)
        vm (make-vm root)]
    (clear-screen)
    (println "Source: " code-str)
    (loop [vm vm i 0]
      (if (> i 200) (println "Max steps")
          (if (zip/right-element (:code-zip vm))
            (do
              (clear-screen)
              (visualize vm)
              (Thread/sleep delay)
              (recur (step vm) (inc i)))
            (do
              (clear-screen)
              (visualize vm)
              (println "Done.")))))))
