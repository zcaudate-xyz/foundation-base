(ns jvm.chisel.db.pg
  "The Postgres seam: translate a Postgres plan tree — the map produced by
   `EXPLAIN (FORMAT JSON)` — into a chip plan consumable by `schedule/schedule`,
   `cluster/admit` and `schedule/run-plan`. Pure data in, pure data out; a live
   database is needed only to *produce* the JSON.

   This is the host-side half of the essay's split: Postgres parses, rewrites
   and optimises SQL into a plan tree; this namespace lowers the offloadable
   fragment of that tree to the chip's operator graph. What cannot be lowered
   refuses the whole plan (`{:ok? false :reason ...}`) — per-subtree partial
   offload is a later slice.

     Postgres                         chip
     Seq Scan + Filter          ─▶   :scan :preds
     Hash Join (Inner)          ─▶   :join-build (inner) + :join-probe
     Aggregate (Plain)          ─▶   :reduce (fn parsed from Output)
     anything else              ─▶   refusal naming the offending node

   Filters are Postgres's own expression strings (\"((amount >= 100) AND
   (amount <= 500))\"); a small recursive-descent parser lowers `col OP int`
   comparisons, AND-chains, redundant parens and integer casts, and refuses
   OR/NOT, function calls, string literals (dictionary encoding is a later
   slice) and column-to-column comparisons.

   Modeling approximation, stated plainly: chip lanes carry a single column
   (the join/group key). Filters on other columns are accepted, but the
   reference treats the lane value as the key — real integration pre-projects
   the key column on the host."
  (:require [clojure.data.json :as json]
            [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; filter lowering: Postgres expression string -> chip predicates
;; ---------------------------------------------------------------------------

(def ^:private token-re
  #"(?:::|<=|>=|<>|!=|=|<|>|\(|\)|,|[A-Za-z_][A-Za-z0-9_$.]*|-?[0-9]+|'[^']*')")

(defn- classify [t]
  (cond
    (re-matches #"-?[0-9]+" t)            [:int (Long/parseLong t)]
    (str/starts-with? t "'")              [:str (subs t 1 (dec (count t)))]
    (#{"(" ")"} t)                        t
    (= "," t)                             :comma
    (= "::" t)                            :cast
    (#{"=" "<" "<=" ">" ">=" "<>" "!="} t) [:op t]
    (re-matches #"[A-Za-z_][A-Za-z0-9_$.]*" t)
    (let [u (str/upper-case t)]
      (if (#{"AND" "OR" "NOT"} u) (keyword u) [:ident t]))
    :else (throw (ex-info (str "unexpected token: " t) {:token t}))))

(defn- drop-parens
  "toks starts with \"(\" — returns the tokens after the matching \")\"."
  [toks]
  (loop [depth 0 toks toks]
    (cond
      (empty? toks)        (throw (ex-info "unbalanced parentheses" {}))
      (= "(" (first toks)) (recur (inc depth) (rest toks))
      (= ")" (first toks)) (if (= 1 depth) (rest toks) (recur (dec depth) (rest toks)))
      :else                (recur depth (rest toks)))))

(defn- strip-casts
  "Remove `::type` (and optional `::type(args)`) sequences from a token list."
  [toks]
  (loop [acc [] toks toks]
    (cond
      (empty? toks) acc
      (= :cast (first toks))
      (let [t2 (drop 2 toks)                       ; :: and the type name
            t3 (if (= "(" (first t2)) (drop-parens t2) t2)]
        (recur acc t3))
      :else (recur (conj acc (first toks)) (rest toks)))))

;; AST: [:cmp op lhs rhs] | [:and [nodes]] | [:or [nodes]] | [:not node]
;;      lhs/rhs: [:col name] | [:int n] | [:str s]

(declare parse-expr)

(defn- parse-operand [toks]
  (let [t (first toks)]
    (cond
      (and (vector? t) (= :int (first t))) [[:int (second t)] (rest toks)]
      (and (vector? t) (= :str (first t))) [[:str (second t)] (rest toks)]
      (and (vector? t) (= :ident (first t)))
      (if (= "(" (second toks))
        (throw (ex-info (str "function call not supported: " (second t)) {}))
        [[:col (second t)] (rest toks)])
      :else (throw (ex-info (str "expected operand near " (pr-str t)) {})))))

(defn- parse-cmp [toks]
  (let [[l r1] (parse-operand toks)
        t (first r1)]
    (if (and (vector? t) (= :op (first t)))
      (let [[r r2] (parse-operand (rest r1))]
        [[:cmp (second t) l r] r2])
      (throw (ex-info (str "expected comparison operator near " (pr-str t)) {})))))

(defn- parse-atom [toks]
  (cond
    (= :NOT (first toks)) (let [[x r] (parse-atom (rest toks))] [[:not x] r])
    (= "(" (first toks))  (let [[x r] (parse-expr (rest toks))]
                            (if (= ")" (first r))
                              [x (rest r)]
                              (throw (ex-info "unbalanced parentheses" {}))))
    :else (parse-cmp toks)))

(defn- parse-and [toks]
  (let [[l r] (parse-atom toks)]
    (loop [acc [l] toks r]
      (if (= :AND (first toks))
        (let [[x r2] (parse-atom (rest toks))] (recur (conj acc x) r2))
        [(if (= 1 (count acc)) (first acc) [:and acc]) toks]))))

(defn- parse-expr [toks]
  (let [[l r] (parse-and toks)]
    (loop [acc [l] toks r]
      (if (= :OR (first toks))
        (let [[x r2] (parse-and (rest toks))] (recur (conj acc x) r2))
        [(if (= 1 (count acc)) (first acc) [:or acc]) toks]))))

(defn- parse-sql [s]
  (let [raw (re-seq token-re s)]
    (when (or (nil? raw) (not (str/blank? (str/replace s token-re " "))))
      (throw (ex-info (str "cannot tokenize: " s) {})))
    (let [toks (strip-casts (mapv classify raw))
          [ast rest-toks] (parse-expr toks)]
      (when (seq rest-toks)
        (throw (ex-info (str "trailing tokens: " (pr-str rest-toks)) {})))
      ast)))

(def ^:private op->kw
  {"=" :eq "<>" :neq "!=" :neq "<" :lt "<=" :lte ">" :gt ">=" :gte})

(def ^:private flip-op
  {"<" ">" "<=" ">=" ">" "<" ">=" "<=" "=" "=" "<>" "<>" "!=" "!="})

(defn- lower-cmp [[_ op l r]]
  (cond
    (and (= :col (first l)) (= :int (first r))) [[(op->kw op) (second r)]]
    (and (= :int (first l)) (= :col (first r))) [[(op->kw (flip-op op)) (second l)]]
    (and (= :col (first l)) (= :col (first r)))
    (throw (ex-info "column-to-column comparison not supported" {}))
    :else (throw (ex-info "comparison needs a column and an integer constant" {}))))

(defn- lower [ast]
  (cond
    (= :cmp (first ast)) (lower-cmp ast)
    (= :and (first ast)) (mapcat lower (second ast))
    (= :or  (first ast)) (throw (ex-info "OR not supported in filters" {}))
    (= :not (first ast)) (throw (ex-info "NOT not supported in filters" {}))))

(defn sql-preds
  "Lower a Postgres Filter/cond string to chip predicates:
   {:ok? true :preds [[:gte 100] ...]} or {:ok? false :reason ...}."
  [s]
  (try
    {:ok? true :preds (vec (lower (parse-sql s)))}
    (catch Exception e
      {:ok? false :reason (.getMessage e)})))

(defn- equi-join-cond?
  "True iff `s` parses to a single `col = col` comparison (one equi-key)."
  [s]
  (try
    (let [ast (parse-sql s)]
      (and (= :cmp (first ast))
           (= "=" (nth ast 1))
           (= :col (first (nth ast 2)))
           (= :col (first (nth ast 3)))))
    (catch Exception _ false)))

;; ---------------------------------------------------------------------------
;; plan tree -> chip plan
;; ---------------------------------------------------------------------------

(defn- fail [reason] {:ok? false :reason reason})

(defn- agg-op
  "The reduce op for an Aggregate node, parsed from its Output list."
  [plan]
  (some (fn [out]
          (when-let [m (re-find #"(?i)\b(count|sum|min|max|avg)\s*\(" out)]
            (keyword (str/lower-case (second m)))))
        (get plan "Output" [])))

(declare translate*)

(defn- translate-scan [plan state]
  (let [f  (get plan "Filter")
        fp (if f (sql-preds f) {:ok? true :preds []})]
    (if-not (:ok? fp)
      (fail (str "filter not lowerable: " f " (" (:reason fp) ")"))
      (let [id (keyword (str "n" (:id state)))]
        {:ok? true
         :nodes [{:id id :op :scan :inputs [[:src (:src state)]] :preds (:preds fp)}]
         :actuals [(get plan "Actual Rows")]
         :output id
         :state {:id (inc (:id state)) :src (inc (:src state))}}))))

(defn- translate-aggregate [plan state opts]
  (cond
    (seq (get plan "Group Key"))
    (fail "grouped aggregation not supported (no hash-aggregate block)")

    (not= "Plain" (get plan "Strategy"))
    (fail (str "aggregate strategy not supported: " (get plan "Strategy")))

    :else
    (let [op (agg-op plan)]
      (cond
        (nil? op)     (fail "cannot determine aggregate function from Output")
        (= :avg op)   (fail "aggregate function not supported: avg")
        :else
        (let [kids (get plan "Plans")]
          (if-not (= 1 (count kids))
            (fail "aggregate must have exactly one child plan")
            (let [r (translate* (first kids) state opts)]
              (if-not (:ok? r)
                r
                (let [id (keyword (str "n" (:id (:state r))))]
                  {:ok? true
                   :nodes (conj (:nodes r)
                                {:id id :op :reduce :inputs [(:output r)] :reduce-op op})
                   :actuals (conj (:actuals r) (get plan "Actual Rows"))
                   :output id
                   :state (update (:state r) :id inc)})))))))))

(defn- translate-hash-join [plan state opts]
  (cond
    (not= "Inner" (get plan "Join Type"))
    (fail (str "join type not supported: " (get plan "Join Type")))

    (get plan "Join Filter")
    (fail "join filters not supported (probe is key equality only)")

    (not (equi-join-cond? (get plan "Hash Cond" "")))
    (fail (str "hash cond must be a single col = col: " (get plan "Hash Cond")))

    :else
    (let [[outer hash-node] (get plan "Plans")
          inner (and (= "Hash" (get hash-node "Node Type"))
                     (first (get hash-node "Plans")))]
      (if-not inner
        (fail "hash join without a Hash inner child")
        (let [ro (translate* outer state opts)]
          (if-not (:ok? ro)
            ro
            (let [ri (translate* inner (:state ro) opts)]
              (if-not (:ok? ri)
                ri
                (let [sid (:id (:state ri))
                      bid (keyword (str "n" sid))
                      pid (keyword (str "n" (inc sid)))]
                  {:ok? true
                   :nodes (vec (concat (:nodes ro) (:nodes ri)
                                       [{:id bid :op :join-build :inputs [(:output ri)]
                                         :buckets (:join-buckets opts) :k 0x9E}
                                        {:id pid :op :join-probe
                                         :inputs [(:output ro) bid]}]))
                   :actuals (vec (concat (:actuals ro) (:actuals ri)
                                         [(get hash-node "Actual Rows")
                                          (get plan "Actual Rows")]))
                   :output pid
                   :state {:id (+ sid 2) :src (:src (:state ri))}})))))))))

(defn- translate* [plan state opts]
  (case (get plan "Node Type")
    "Seq Scan"  (translate-scan plan state)
    "Aggregate" (translate-aggregate plan state opts)
    "Hash Join" (translate-hash-join plan state opts)
    (fail (str "unsupported node type: " (get plan "Node Type")))))

(defn- root-plan [x]
  (let [x (if (sequential? x) (first x) x)]
    (or (get x "Plan") x)))

(defn plan->chip-plan
  "Translate a Postgres plan tree (the full `EXPLAIN (FORMAT JSON)` array, or
   the \"Plan\" map, or a bare node map) into a chip plan. `opts`:
   {:width 32 :lanes 8 :join-buckets 16}. Returns
   {:ok? true :plan {...} :actuals [per-node \"Actual Rows\" or nil]} or
   {:ok? false :reason ...}. Sources are assigned left-to-right in Postgres
   child order (probe/outer first); node ids are :n0.. in emission order."
  ([pg] (plan->chip-plan pg {}))
  ([pg opts]
   (let [opts (merge {:width 32 :lanes 8 :join-buckets 16} opts)
         r    (translate* (root-plan pg) {:id 0 :src 0} opts)]
     (if-not (:ok? r)
       r
       {:ok? true
        :plan {:width (:width opts) :lanes (:lanes opts)
               :sources (:src (:state r))
               :nodes (:nodes r)}
        :actuals (:actuals r)}))))

(defn explain-file->chip-plan
  "Slurp + JSON-parse + translate an EXPLAIN (FORMAT JSON) file."
  ([path] (explain-file->chip-plan path {}))
  ([path opts] (plan->chip-plan (json/read-str (slurp path)) opts)))
