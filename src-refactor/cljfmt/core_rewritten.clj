(ns cljfmt.core-rewritten
  (:require [clojure.string :as str]
            [std.block.construct :as construct]
            [std.block.navigate :as e]
            [std.lib.zip :as zl]
            [std.block.base :as base]
            [clojure.java.io :as io]))

(def read-resource* (comp read-string slurp io/resource))
(defmacro read-resource [path] `'~(read-resource* path))

;; --- Stage 1.2: Adapted transform function ---
(defn transform [form zf & args]
  (let [initial-nav (e/navigator form) ; Convert Clojure form to std.block.navigate navigator
        transformed-nav (apply zf initial-nav args)]
    (read-string (e/root-string transformed-nav))))

;; --- Stage 1.4: Replacement for edit-all ---
(defn- edit-all
  ([nav p? f]
   (edit-all nav p? f e/next))
  ([nav p? f nextf]
   (loop [current-nav (if (p? nav) (f nav) nav)]
     (if-let [next-match-nav (e/find-next current-nav nextf p?)]
       (recur (f next-match-nav))
       current-nav))))

;; --- Stage 1.3: Basic helper predicates for std.lib.zip/std.block.navigate ---
(defn- clojure-whitespace? [nav]
  (e/space? (e/block nav)))

(defn- comment? [nav]
  (e/comment? (e/block nav)))

(defn- token? [nav]
  (e/token? (e/block nav)))

(defn- element? [nav]
  (not (or (clojure-whitespace? nav)
           (comment? nav)
           (e/void? (e/block nav)))))

(defn- linebreak? [nav]
  (e/linebreak? (e/block nav)))

(defn- n-tag [nav]
  (e/tag (e/block nav)))

(defn- n-string [nav]
  (e/string (e/block nav)))

(defn- n-sexpr [nav]
  (e/value (e/block nav)))

(defn- n-comma? [nav]
  (and (e/block nav)
       (= (e/tag (e/block nav)) :token)
       (= (e/value (e/block nav)) \,)))

(defn- root? [nav]
  (e/left-most? (e/top nav))) ; top nav should have no parents

(defn- top? [nav]
  (zero? (count (zl/hierarchy nav)))) ; check if parent chain is empty, assuming (zl/hierarchy (e/navigator 1)) => (nil)

(defn- unquote? [nav]
  (and nav (= (n-tag nav) :unquote)))

(defn- deref? [nav]
  (and nav (= (n-tag nav) :deref)))

(defn- unquote-deref? [nav]
  (and (deref? nav)
       (unquote? (e/up nav))))

(defn- reader-macro? [nav]
  (and nav (= (n-tag nav) :reader-macro)))

(defn- namespaced-map? [nav]
  (and nav (= (n-tag nav) :namespaced-map)))

(defn- n-newlines [amount]
  (construct/block (apply str (repeat amount "\n")) :type :linebreak))

(defn- n-spaces [amount]
  (construct/block (apply str (repeat amount " ")) :type :whitespace))

;; --- Stage 2.1: Port remove-surrounding-whitespace ---

(defn- surrounding-whitespace? [nav]
  (and (not (top? nav))
       (clojure-whitespace? nav)
       (or (and (nil? (e/left* nav))
                ;; don't convert ~ @ to ~ @
                (not (unquote-deref? (e/right* nav)))
                ;; ignore space before comments
                (not (comment? (e/right* nav))))
           #_(nil? (e/skip e/right* clojure-whitespace? nav)))))

(defn remove-surrounding-whitespace [form]
  (transform form edit-all surrounding-whitespace? e/delete))

;; --- Stage 2.2: Port insert-missing-whitespace ---

(defn- missing-whitespace? [nav]
  (and (element? nav)
       (not (reader-macro? (e/up nav)))
       (not (namespaced-map? (e/up nav)))
       (element? (e/right* nav))))

(defn- insert-space-right [nav]
  (e/insert-token-to-right nav (n-spaces 1)))

(defn insert-missing-whitespace [form]
  (transform form edit-all missing-whitespace? insert-space-right))

;; --- Stage 2.3: Port remove-consecutive-blank-lines ---

(defn- space? [nav]
  (clojure-whitespace? nav))

(defn- comma? [nav]
  (n-comma? nav))

(defn- skip-whitespace-and-commas [nav]
  (e/skip e/next #(or (space? %) (comma? %)) nav))

(defn- skip-clojure-whitespace
  ([nav] (skip-clojure-whitespace nav e/next))
  ([nav f] (e/skip f clojure-whitespace? nav)))

(defn- count-newlines [nav]
  (loop [current-nav nav, newlines 0]
    (if (linebreak? current-nav)
      (recur (-> current-nav e/right* skip-whitespace-and-commas)
             (-> current-nav n-string count (+ newlines)))
      (if (comment? (skip-clojure-whitespace current-nav e/left*))
        (inc newlines)
        newlines))))

(defn- final-transform-element? [nav]
  (nil? (skip-clojure-whitespace (e/next nav))))

(defn- consecutive-blank-line? [nav]
  (and (> (count-newlines nav) 2)
       (not (final-transform-element? nav))))

(defn- remove-clojure-whitespace [nav]
  (if (clojure-whitespace? nav)
    (recur (e/remove nav))
    nav))

(defn- replace-consecutive-blank-lines [nav]
  (let [nav-elem-before (-> nav
                            skip-clojure-whitespace
                            e/prev
                            remove-clojure-whitespace)]
    (-> nav-elem-before
        e/next
        (e/insert-token-to-left (n-newlines (if (comment? nav-elem-before) 1 2))))))

(defn remove-consecutive-blank-lines [form]
  (transform form edit-all consecutive-blank-line? replace-consecutive-blank-lines))

;; --- Stage 2.4: Port remove-trailing-whitespace and remove-multiple-non-indenting-spaces ---

(defn- final? [nav]
  (and (nil? (e/right* nav)) (root? (e/up nav))))

(defn- trailing-whitespace? [nav]
  (and (space? nav)
       (or (linebreak? (e/right* nav)) (final? nav))))

(defn remove-trailing-whitespace [form]
  (transform form edit-all trailing-whitespace? e/remove))

(defn- replace-with-one-space [nav]
  (e/replace nav (n-spaces 1)))

(defn- indentation? [nav]
  (and (linebreak? (e/left* nav)) (space? nav)))

(defn- non-indenting-whitespace? [nav]
  (and (space? nav) (not (indentation? nav))))

(defn remove-multiple-non-indenting-spaces [form]
  (transform form edit-all non-indenting-whitespace? replace-with-one-space))

;; --- Stage 3.1: Port indent and its related helper functions (Part 8) ---

(defn- line-comment? [nav]
  (and (comment? nav) (re-matches #"(?s);;([^;].*)" (n-string nav))))

(defn- comment-next? [nav]
  (-> nav e/next skip-whitespace comment?))

(defn- comment-next-other-than-line-comment? [nav]
  (when-let [next-nav (-> nav e/next skip-whitespace)]
    (and (comment? next-nav) (not (line-comment? next-nav)))))

(defn- should-indent? [nav opts]
  (and (linebreak? nav)
       (if (:indent-line-comments? opts)
         (not (comment-next-other-than-line-comment? nav))
         (not (comment-next? nav)))))

(defn- should-unindent? [nav opts]
  (and (indentation? nav)
       (if (:indent-line-comments? opts)
         (not (comment-next-other-than-line-comment? nav))
         (not (comment-next? nav)))))

(defn unindent
  ([form]
   (unindent form {}))
  ([form opts]
   (transform form edit-all #(should-unindent? % opts) e/remove)))

(def ^:private start-element
  {:meta "^", :meta* "#^", :vector "[",       :map "{"
   :list "(", :eval "#=",  :uneval "#_",      :fn "#("
   :set "#{", :deref " @",  :reader-macro "#", :unquote "~"
   :var "#'", :quote "'",  :syntax-quote "`", :unquote-splicing "~ @"
   :namespaced-map "#"})

(defn- prior-line-string [nav]
  (loop [current-nav nav
         worklist '()]
    (if-let [p (e/left* current-nav)]
      (let [s            (n-string p)
            new-worklist (cons s worklist)]
        (if-not (str/includes? s "\n")
          (recur p new-worklist)
          (apply str new-worklist)))
      (if-let [p (e/up current-nav)]
        ;; newline cannot be introduced by start-element
        (recur p (cons (start-element (n-tag p)) worklist))
        (apply str worklist)))))

(defn- last-line-in-string ^String [^String s]
  (subs s (inc (.lastIndexOf s "\n"))))

(defn- margin [nav]
  (-> nav prior-line-string last-line-in-string count))

(defn- coll-indent [nav]
  (-> nav e/leftmost margin))

(defn- remove-namespace [x]
  (if (symbol? x) (symbol (name x)) x))

(defn pattern? [v]
  (instance? #?(:clj java.util.regex.Pattern :cljs js/RegExp) v))

(defn- uneval? [nav]
  (= (n-tag nav) :uneval))

(defn- index-of [nav]
  (->> (iterate e/left nav)
       (remove uneval?)
       (take-while identity)
       (count)
       (dec)))

(defn- skip-meta [nav]
  (if (#{:meta :meta*} (n-tag nav))
    (-> nav e/down e/right)
    nav))

(defn- cursive-two-space-list-indent? [nav]
  (-> nav e/leftmost skip-meta n-tag #{:vector :map :list :set} not))

(defn- zprint-two-space-list-indent? [nav]
  (-> nav e/leftmost n-tag #{:token :list}))

(defn two-space-list-indent? [nav context]
  (case (:function-arguments-indentation context)
    :community false
    :cursive (cursive-two-space-list-indent? nav)
    :zprint (zprint-two-space-list-indent? nav)))

(def indent-size 2)

(defn- indent-width [nav]
  (case (n-tag nav)
    :list indent-size
    :fn   (inc indent-size)))

(defn- list-indent [nav context]
  (if (> (index-of nav) 1)
    (-> nav e/leftmost e/right margin)
    (cond-> (coll-indent nav)
      (two-space-list-indent? nav context) inc)))

(defn- top-level-form [nav]
  (->> nav
       (iterate e/up)
       (take-while (complement root?))
       last))

(defn- keyword-node? [nav]
  (and (token? nav) (keyword? (n-sexpr nav))))

(defn- ns-token? [nav]
  (and (token? nav)
       (= 'ns (n-sexpr nav))))

(defn- ns-form? [nav]
  (and (top? nav)
       (= (n-tag nav) :list)
       (some-> nav e/down ns-token?)))

(defn- token-value [nav]
  (let [nav (skip-meta nav)]
    (when (token? nav) (n-sexpr nav))))

(defn- reader-conditional? [nav]
  (and (reader-macro? nav) (#{"?" "? @"} (-> nav e/down token-value str))))

(defn- find-next-keyword [nav]
  (e/find-next nav keyword-node?))

(defn- first-symbol-in-reader-conditional [nav]
  (when (reader-conditional? nav)
    (when-let [key-nav (-> nav e/down e/right e/down find-next-keyword)]
      (when-let [value-nav (-> key-nav e/next skip-meta)]
        (when (token? value-nav)
          (n-sexpr value-nav))))))

(defn- form-symbol [nav]
  (let [nav (e/leftmost nav)]
    (or (token-value nav)
        (first-symbol-in-reader-conditional nav))))

(defn- index-matches-top-argument? [nav depth idx]
  (and (> depth 0)
       (= (inc idx) (index-of (nth (iterate e/up nav) depth)))))

(defn- qualify-symbol-by-alias-map [possible-sym alias-map]
  (when-let [ns-str (namespace possible-sym)]
    (symbol (get alias-map ns-str ns-str) (name possible-sym))))

(defn- qualify-symbol-by-ns-name [possible-sym ns-name]
  (when ns-name
    (symbol (name ns-name) (name possible-sym))))

(defn- fully-qualified-symbol [possible-sym context]
  (if (symbol? possible-sym)
    (or (qualify-symbol-by-alias-map possible-sym (:alias-map context))
        (qualify-symbol-by-ns-name possible-sym (:ns-name context)))
    possible-sym))

(defn form-matches-key? [nav key context]
  (when-some [possible-sym (form-symbol nav)]
    (let [bare-sym (remove-namespace possible-sym)]
      (if (pattern? key)
        (re-find key (str bare-sym))
        (or (= key (fully-qualified-symbol possible-sym context))
            (= key bare-sym))))))

(defn- inner-indent [nav key depth idx context]
  (let [top (nth (iterate e/up nav) depth)]
    (when (and (e/left nav)
               (form-matches-key? top key context)
               (or (nil? idx) (index-matches-top-argument? nav depth idx)))
      (let [nav-up (e/up nav)]
        (+ (margin nav-up) (indent-width nav-up))))))

(defn- nth-form [nav n]
  (reduce (fn [current-nav f] (when current-nav (f current-nav)))
          (e/leftmost nav)
          (repeat n e/right)))

(defn- first-form-in-line? [nav]
  (and (some? nav)
       (if-let [nav-left (e/left* nav)]
         (if (space? nav-left)
           (recur nav-left)
           (or (linebreak? nav-left) (comment? nav-left)))
         true)))

(defn- block-indent [nav key idx context]
  (when (form-matches-key? nav key context)
    (let [nav-after-idx (some-> nav (nth-form (inc idx)))]
      (if (and (or (nil? nav-after-idx) (first-form-in-line? nav-after-idx))
               (> (index-of nav) idx))
        (inner-indent nav key 0 nil context)
        (list-indent nav context)))))

;; --- Placeholders for resources ---
(def default-indents
  (merge (read-resource "cljfmt/indents/clojure.clj")
         (read-resource "cljfmt/indents/compojure.clj")
         (read-resource "cljfmt/indents/fuzzy.clj")))

(def default-aligned-forms
  #?(:clj (read-resource "cljfmt/aligned_forms/clojure.clj")
     :cljs {})

(def default-options
  {:alias-map                             {},
   :align-binding-columns?                false,
   :align-map-columns?                    false,
   :aligned-forms                         default-aligned-forms,
   :extra-aligned-forms                   {},
   :extra-indents                         {},
   :function-arguments-indentation        :community,
   :indent-line-comments?                 false,
   :indentation?                          true,
   :indents                               default-indents,
   :insert-missing-whitespace?            true,
   :remove-consecutive-blank-lines?       true,
   :remove-multiple-non-indenting-spaces? false,
   :remove-surrounding-whitespace?        true,
   :remove-trailing-whitespace?           true,
   :sort-ns-references?                   false,
   :split-keypairs-over-multiple-lines?   false}))

;; --- Stage 3.1: Port indent and its related helper functions (Part 8) ---

(defmulti ^:private indenter-fn
  (fn [_sym _context [type & _args]] type))

(defmethod indenter-fn :inner [sym context [_ depth idx]]
  (fn [nav] (inner-indent nav sym depth idx context)))

(defmethod indenter-fn :block [sym context [_ idx]]
  (fn [nav] (block-indent nav sym idx context)))

(defmethod indenter-fn :default [sym context [_]]
  (fn [nav]
    (when (form-matches-key? nav sym context)
      (list-indent nav context))))

(defn- make-indenter [[key opts] context]
  (apply some-fn (map (partial indenter-fn key context) opts)))

(defn- indent-order [[key specs]]
  (let [get-depth (fn [[type depth]] (if (= type :inner) depth 0))
        max-depth (transduce (map get-depth) max 0 specs)
        key-order  (cond
                     (qualified-symbol? key) 0
                     (simple-symbol? key)    1
                     (pattern? key)          2)]
    [(- max-depth) key-order (str key)]))

(defn- custom-indent [nav indents context]
  (if (empty? indents)
    (list-indent nav context)
    (let [indenter (->> indents
                        (map #(make-indenter % context))
                        (apply some-fn))]
      (or (indenter nav)
          (list-indent nav context)))))

(defn- indent-amount [nav indents context]
  (let [tag (n-tag (e/up nav))
        gp  (e/up (e/up nav))]
    (cond
      (reader-conditional? gp) (coll-indent nav)
      (#{:list :fn} tag)       (custom-indent nav indents context)
      (= :meta tag)            (indent-amount (e/up nav) indents context)
      :else                    (coll-indent nav))))

(defn- indent-line [nav indents context]
  (let [width (indent-amount nav indents context)]
    (if (> width 0)
      (e/insert-token-to-right nav (n-spaces width))
      nav)))

(defn- find-namespace [nav]
  (some-> nav e/top e/down (e/find e/right ns-form?) e/down e/next n-sexpr))

(defn indent
  ([form]
   (indent form default-indents {}))
  ([form indents]
   (indent form indents {}))
  ([form indents alias-map]
   (indent form indents alias-map default-options))
  ([form indents alias-map opts]
   (let [ns-name (find-namespace (e/navigator form)) ; Use e/navigator here
         sorted-indents (sort-by indent-order indents)
         context (merge (select-keys opts [:function-arguments-indentation])
                        {:alias-map alias-map
                         :ns-name ns-name})]
     (transform form edit-all #(should-indent? % opts)
                #(indent-line % sorted-indents context)))))

(defn reindent
  ([form]
   (indent (unindent form)))
  ([form indents]
   (indent (unindent form) indents))
  ([form indents alias-map]
   (indent (unindent form) indents alias-map))
  ([form indents alias-map opts]
   (indent (unindent form opts) indents alias-map opts)))

;; --- Stage 3.2: Port align-map-columns and align-form-columns (Part 3) ---

(defn- list? [nav]
  (base/list? (e/block nav)))

(defn- map? [nav]
  (base/map? (e/block nav)))

(defn- map-key? [nav]
  (and (map? (e/up nav))
       (even? (index-of nav))
       (not (uneval? nav))
       (not (clojure-whitespace? nav))))

(defn- preceded-by-line-break? [nav]
  (loop [previous (e/left* nav)]
    (cond
      (linebreak? previous)
      true
      (clojure-whitespace? previous)
      (recur (e/left* previous))
      :else
      false)))

(defn- map-key-without-line-break? [nav]
  (and (map-key? nav) (not (preceded-by-line-break? nav))))

(defn- insert-newline-left [nav]
  (e/insert-token-to-left nav (n-newlines 1)))

(defn split-keypairs-over-multiple-lines [form]
  (transform form edit-all map-key-without-line-break? insert-newline-left))

(defn- skip-to-linebreak-or-element [nav]
  (e/skip e/right* (some-fn space? comma?) nav))

(defn- reduce-columns [nav f init]
  (loop [current-nav nav, col 0, acc init]
    (if-some [next-nav (skip-to-linebreak-or-element current-nav)]
      (if (linebreak? next-nav)
        (recur (e/right* next-nav) 0 acc)
        (recur (e/right* next-nav) (inc col) (f next-nav col acc)))
      acc)))

(defn- count-columns [nav]
  (inc (reduce-columns nav #(max %2 %3) 0)))

(defn- trailing-commas [nav]
  (let [right (e/right* nav)]
    (if (and right (comma? right))
      (n-string right)
      "")))

(defn- node-end-position [nav]
  (let [lines (str (prior-line-string nav)
                   (n-string nav)
                   (trailing-commas nav))]
    (transduce (comp (remove #(str/starts-with? % ";"))
                     (map count))
               max 0 (str/split lines #"\r?\n"))))

(defn- max-column-end-position [nav col]
  (reduce-columns nav
                  (fn [current-nav c max-pos]
                    (if (= c col)
                      (max max-pos (node-end-position current-nav))
                      max-pos))
                  0))

(defn- node-str-length [nav]
  (-> nav e/block e/length))

(defn- update-space-left [nav delta]
  (let [left (e/left* nav)]
    (cond
      (space? left) (let [n (max 0 (+ delta (node-str-length left)))]
                      (e/right* (e/replace left (n-spaces n))))
      (pos? delta)  (e/insert-token-to-left nav (n-spaces delta))
      :else         nav)))

(defn- nil-if-end [nav]
  (when (and nav (not (zl/at-outside-most? nav))) nav))

(defn- skip-to-next-line [nav]
  (->> nav (e/skip e/next (complement linebreak?)) e/next nil-if-end))

(defn- pad-inside-node [nav padding]
  (if-some [nav (e/down nav)]
    (loop [current-nav nav]
      (if-some [next-nav (skip-to-next-line current-nav)]
        (recur (update-space-left next-nav padding))
        current-nav))
    nav))

(defn- pad-node [nav padding]
  (-> (update-space-left nav padding)
      (zl/subedit (partial pad-inside-node padding))))

(defn- pad-to-position [nav start-position]
  (pad-node nav (- start-position (margin nav))))

(defn- edit-column [nav column f]
  (loop [current-nav nav, col 0]
    (if-some [next-nav (skip-to-linebreak-or-element current-nav)]
      (let [current-nav (if (and (= col column) (not (linebreak? current-nav))) (f current-nav) current-nav)
            col  (if (linebreak? current-nav) 0 (inc col))]
        (if-some [next-nav (e/right* current-nav)]
          (recur next-nav col)
          current-nav))
      current-nav)))

(defn- align-one-column [nav col]
  (if-some [nav (e/down nav)]
    (let [start-position (inc (max-column-end-position nav (dec col)))]
      (e/up (edit-column nav col #(pad-to-position % start-position))))
    nav))

(defn- align-columns [nav]
  (reduce align-one-column nav (-> nav e/down count-columns range rest)))

(defn align-map-columns [form]
  (transform form edit-all map? align-columns))

(defn- aligned-form? [nav aligned-forms context]
  (and (list? (e/up nav))
       (some (fn [[k indexes]]
               (and (form-matches-key? nav k context)
                    (contains? (set indexes) (dec (index-of nav)))))
             aligned-forms)))

(defn align-form-columns [form aligned-forms alias-map]
  (let [ns-name  (find-namespace (e/navigator form))
        context  {:alias-map alias-map, :ns-name ns-name}
        aligned? #(aligned-form? % aligned-forms context)]
    (transform form edit-all aligned? align-columns)))

(defn realign-form
  "Realign a rewrite-clj form such that the columns line up into columns."
  [form]
  (-> form e/navigator align-columns e/root-string read-string))

(defn- unalign-from-space [nav]
  (pad-node (e/right* nav) (- 1 (node-str-length nav))))

(defn unalign-form
  "Remove any consecutive non-indenting whitespace within the form."
  [form]
  (-> form e/navigator e/down
      (edit-all non-indenting-whitespace? unalign-from-space e/right*)
      e/root-string read-string))

;; --- Stage 4.1: Port sort-ns-references and its helper functions (Part 2) ---

(def ^:private ns-reference-symbols
  #{:import :require :require-macros :use})

(defn- ns-reference? [nav]
  (and (list? nav)
       (some-> nav e/up ns-form?)
       (some-> nav n-sexpr first ns-reference-symbols)))

(defn- re-indexes [re s]
  (let [matcher    #?(:clj  (re-matcher re s)
                      :cljs (js/RegExp. (.-source re) "g"))
        next-match #?(:clj  #(when (.find matcher) [(.start matcher) (.end matcher)])
                      :cljs #(when-let [result (.exec matcher s)] [(.-index result) (.-lastIndex matcher)]))]
    (take-while some? (repeatedly next-match))))

(defn- re-seq-matcher [re charmap coll]
  {:pre (every? charmap coll)}
  (let [s (apply str (map charmap coll))
        v (vec coll)]
    (for [[start end] (re-indexes re s)]
      {:value (subvec v start end)
       :start start
       :end   end})))

(defn- find-elements-with-comments [blocks]
  (re-seq-matcher #"(?CNS*)*E(S*C)?"
                  #(case (e/tag %) 
                     (:whitespace :comma) \S
                     :comment \C
                     :linebreak \N
                     \E)
                  blocks))

(defn- splice-into [coll splices]
  (letfn [(splice [v i splices] 
            (when-let [[{:keys [value start end]} & splices] (seq splices)]
              (lazy-cat (subvec v i start) value (splice v end splices))))]
    (splice (vec coll) 0 splices)))

(defn- add-newlines-after-comments [blocks]
  (mapcat #(if (comment? %) [% (n-newlines 1)] [%]) blocks))

(defn- remove-newlines-after-comments [blocks]
  (mapcat #(when-not (and %1 (comment? %1) (linebreak? %2)) [%2])
          (cons nil blocks)
          blocks))

;; --- Stage 4.1: Port sort-ns-references and its helper functions (Part 2) ---

(defn- sort-node-arguments-by [f blocks]
  (let [blocks-with-newlines  (add-newlines-after-comments blocks)
        args   (rest (find-elements-with-comments blocks-with-newlines))
        sorted (sort-by f (map :value args))]
    (->> sorted
         (map #(assoc %1 :value %2) args)
         (splice-into blocks-with-newlines)
         (remove-newlines-after-comments))))

(defn- update-children [nav f]
  (let [block (e/block nav)
        new-block (base/replace-children block (f (base/block-children block)))]
    (e/replace nav new-block)))

(defn- nodes-string [blocks]
  (apply str (map e/string blocks)))

(defn- remove-node-metadata [blocks]
  (mapcat #(if (= (e/tag %) :meta)
             (rest (base/block-children %))
             [%])
          blocks))

(defn- node-sort-string [blocks]
  (-> (remove (some-fn comment? clojure-whitespace?) blocks)
      (remove-node-metadata)
      (nodes-string)
      (str/replace #"[\[\]\(\)\{\}]" "")
      (str/trim)))

(defn sort-arguments [nav]
  (update-children nav #(sort-node-arguments-by node-sort-string %)))

(defn sort-ns-references [form]
  (transform form edit-all ns-reference? sort-arguments))

;; --- Stage 4.2 & 4.3: Implement reformat-form and reformat-string ---

(def includes?
  #?(:clj  (fn [^String a ^String b] (.contains a b))
     :cljs str/includes?))

(defn- e-child-sexprs [nav]
  (map n-sexpr (base/block-children (e/block nav))))

(defn- e-vector? [nav]
  (base/vector? (e/block nav)))

(defn- e-find-all [nav p?]
  (loop [matches []
         current-nav nav]
    (if-let [next-match-nav (e/find-next current-nav e/next p?)]
      (recur (conj matches next-match-nav)
             (e/next next-match-nav))
      matches)))

(defn- ns-require-form? [nav]
  (and (some-> nav top-level-form ns-form?)
       (some-> nav e-child-sexprs first (= :require))))

(defn- as-keyword? [nav]
  (and (= :token (n-tag nav))
       (= :as (n-sexpr nav))))

(defn- symbol-node? [nav]
  (some-> nav e/block base/symbol?))

(defn- leftmost-symbol [nav]
  (some-> nav e/leftmost (e/find (comp symbol-node? skip-meta))))

(defn- as-zloc->alias-mapping [as-nav]
  (let [alias             (some-> as-nav e/right n-sexpr)
        current-namespace (some-> as-nav leftmost-symbol n-sexpr)
        grandparent-nav  (some-> as-nav e/up e/up)
        parent-namespace  (when-not (ns-require-form? grandparent-nav)
                            (when (or (e-vector? grandparent-nav)
                                      (list? grandparent-nav))
                              (first (e-child-sexprs grandparent-nav))))]
    (when (and (symbol? alias) (symbol? current-namespace))
      {(str alias) (if parent-namespace
                      (format "%s.%s" parent-namespace current-namespace)
                      (str current-namespace))}))) 

(defn- alias-map-for-form [form]
  (when-let [req-nav (-> form e/navigator (e/find e/next ns-require-form?))]
    (->> (e-find-all req-nav as-keyword?)
         (map as-zloc->alias-mapping)
         (apply merge))))

(defn- stringify-map [m]
  (into {} (map (fn [[k v]] [(str k) (str v)])) m))

(def default-line-separator
  #?(:clj (System/lineSeparator) :cljs "\n"))

(defn normalize-newlines [s]
  (str/replace s #"\r\n" "\n"))

(defn replace-newlines [s sep]
  (str/replace s #"\n" sep))

(defn find-line-separator [s]
  (or (re-find #"\r?\n" s) default-line-separator))

(defn wrap-normalize-newlines [f]
  (fn [s]
    (let [sep (find-line-separator s)]
      (-> s normalize-newlines f (replace-newlines sep)))))

(defn reformat-form
  "Reformats a rewrite-clj form data structure. Accepts a map of
  [formatting options][1]. See also: [[reformat-string]].
  
  [1]: https://github.com/weavejester/cljfmt#formatting-options"
  ([form]
   (reformat-form form {}))
  ([form options]
   (let [opts      (merge default-options options)
         indents   (merge (:indents opts) (:extra-indents opts))
         aligned   (merge (:aligned-forms opts) (:extra-aligned-forms opts))
         alias-map #?(:clj  (merge (alias-map-for-form form)
                                   (stringify-map (:alias-map opts)))
                      :cljs (stringify-map (:alias-map opts)))]
     (-> form
         (cond-> (:sort-ns-references? opts)
           sort-ns-references)
         (cond-> (:split-keypairs-over-multiple-lines? opts)
           split-keypairs-over-multiple-lines)
         (cond-> (:remove-consecutive-blank-lines? opts)
           remove-consecutive-blank-lines)
         (cond-> (:remove-surrounding-whitespace? opts)
           remove-surrounding-whitespace)
         (cond-> (:insert-missing-whitespace? opts)
           insert-missing-whitespace)
         (cond-> (:remove-multiple-non-indenting-spaces? opts)
           remove-multiple-non-indenting-spaces)
         (cond-> (:indentation? opts)
           (reindent indents alias-map opts))
         (cond-> (:align-map-columns? opts)
           align-map-columns)
         (cond-> (:align-form-columns? opts)
           (align-form-columns aligned alias-map))
         (cond-> (:remove-trailing-whitespace? opts)
           remove-trailing-whitespace)))))

(defn reformat-string
  "Reformat a string. Accepts a map of [formatting options][1].

  [1]: https://github.com/weavejester/cljfmt#formatting-options"
  ([form-string]
   (reformat-string form-string {}))
  ([form-string options]
   (-> (read-string form-string) ; Using read-string for parsing
       (reformat-form options)
       (str)))) ; Using str to convert back to string
