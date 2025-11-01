(ns std.block.format
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.grid :as grid] ; Will eventually be replaced or integrated
            [code.query.block :as nav]
            [code.query.match :as match]
            [std.lib :as h]
            [std.lib.zip :as zip])) ; Added zip for more direct zipper manipulation

;; --- Rule Definition ---

(defrecord FormattingRule [match directives])

(defn rule
  "Creates a new formatting rule."
  [match directives]
  (->FormattingRule match directives))

;; --- Whitespace Manipulation Helpers ---

(defn- remove-left-whitespace
  "Removes all whitespace blocks to the left of the current position."
  [nav]
  (loop [current-nav nav]
    (let [left-elem (nav/left* current-nav)]
      (if (and left-elem (base/void? (nav/block left-elem)))
        (recur (nav/delete-left current-nav))
        current-nav))))

(defn- remove-right-whitespace
  "Removes all whitespace blocks to the right of the current position."
  [nav]
  (loop [current-nav nav]
    (let [right-elem (nav/right* current-nav)]
      (if (and right-elem (base/void? (nav/block right-elem)))
        (recur (nav/delete-right current-nav))
        current-nav))))

(defn- ensure-single-space-left
  "Ensures there is exactly one space to the left of the current position."
  [nav]
  (let [nav (remove-left-whitespace nav)]
    (nav/insert-left nav (construct/space))))

(defn- ensure-single-space-right
  "Ensures there is exactly one space to the right of the current position."
  [nav]
  (let [nav (remove-right-whitespace nav)]
    (nav/insert-right nav (construct/space))))

;; --- Line Wrapping Helpers ---

(def ^:dynamic *max-line-length* 80)

(defn- current-line-length
  "Calculates the length of the current line up to the current navigator position."
  [nav]
  (let [line-start-nav (nav/prev-anchor nav)
        elements-on-line (zip/right-elements line-start-nav)]
    (reduce + 0 (map base/block-width elements-on-line))))

(defn- should-line-wrap?
  "Determines if the current line should be wrapped."
  [nav line-wrap-strategy]
  (case line-wrap-strategy
    :always true
    :if-long (> (current-line-length nav) *max-line-length*)
    false))

(defn- apply-line-wrapping
  "Applies line wrapping based on directives and max-line-length."
  [nav line-wrap-strategy]
  (if (should-line-wrap? nav line-wrap-strategy)
    (nav/insert-left nav (construct/newline)) ; Simple strategy: always break before if too long
    nav))

(defn- apply-metadata-spacing
  "Ensures correct spacing around metadata blocks."
  [nav]
  (if (base/modifier? (nav/block nav)) ; Check if it's a modifier (like metadata)
    (-> nav
        (ensure-single-space-left)
        (ensure-single-space-right))
    nav))

;; --- Directive Application ---

(defn- apply-indentation
  "Applies indentation based on directives."
  [nav indent-level]
  (if (and indent-level (pos? indent-level))
    (nav/insert-left nav (construct/spaces indent-level))
    nav))

(defn- apply-line-break
  "Applies a line break based on directives."
  [nav line-break-after]
  (if line-break-after
    (nav/insert-right nav (construct/newline))
    nav))

(defn- apply-space-before
  "Applies space before the current element."
  [nav space-before]
  (if space-before
    (ensure-single-space-left nav)
    nav))

(defn- apply-space-after
  "Applies space after the current element."
  [nav space-after]
  (if space-after
    (ensure-single-space-right nav)
    nav))

(defn- apply-directives
  "Applies formatting directives to a navigator at its current position."
  [nav directives]
  (let [{:keys [indent-level line-break-after space-before space-after line-wrap-strategy]} directives]
    (-> nav
        (apply-indentation indent-level)
        (apply-space-before space-before)
        (apply-space-after space-after)
        (apply-line-break line-break-after)
        (apply-line-wrapping line-wrap-strategy)
        (apply-comment-indentation indent-level)
        (apply-metadata-spacing)))) ; Apply metadata spacing

;; --- Rule Application ---

(defn find-matching-rule
  "Finds the most specific matching rule for the current navigator position."
  [nav rules]
  ;; For now, a simple linear search. More advanced would prioritize specificity.
  (first (filter #((match/compile-matcher (:match %)) nav) rules)))

(defn format-block
  "Formats a block (or sub-tree) using a set of rules.
   This function performs a full depth-first traversal."
  [initial-nav rules]
  (loop [nav initial-nav]
    (if (zip/end? nav) ; Check if we've reached the end of the traversal
      (zip/root nav)   ; Return the root of the modified tree
      (let [current-nav (if-let [matching-rule (find-matching-rule nav rules)]
                           (apply-directives nav (:directives matching-rule)) ; Apply directives to current node
                           nav)]
        (recur (zip/next current-nav))))))

;; --- Default Rules ---

(def ^:private +default-rules+
  [(rule '(defn _ & _)
         {:indent-level 1 :line-break-after true :line-wrap-strategy :always}) ; Basic defn formatting
   (rule '(list _ & _)
         {:space-before true :line-wrap-strategy :if-long}) ; Default space before list elements
   (rule '_
         {:space-before true}) ; Default space before any element
   (rule (match/p-fn base/comment?)
         {:indent-level 0}) ; Comments should be indented based on context, not their own rule
   (rule (match/p-fn base/modifier?) ; Rule for metadata
         {:space-before true :space-after true})
   ])

;; --- Main Formatting Function ---

(defn format-code
  "Formats a code string using a set of formatting rules."
  [code-str rules]
  (let [initial-nav (nav/parse-string code-str)
        formatted-nav (format-block initial-nav rules)]
    (nav/root-string formatted-nav)))

(defn format
  "Formats a code string using the default Clojure Style Guide rules."
  [code-str]
  (format-code code-str +default-rules+))