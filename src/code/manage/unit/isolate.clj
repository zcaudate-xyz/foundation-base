(ns code.manage.unit.isolate
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [code.framework :as base]
            [code.manage.unit.scaffold :as scaffold]
            [code.project :as project]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.fs :as fs]
            [std.lib.result :as res]
            [std.task :as task]))

(def ^:dynamic *test-forms*
  '#{fact})

(def ^:dynamic *meta-tags*
  #{:meta :hash-meta})

(defn unwrap-fact-block
  "returns the exact metadata prefix and inner fact block"
  {:added "4.1"}
  ([blk]
   (loop [prefix ""
          blk    blk]
     (if (*meta-tags* (block/tag blk))
       (let [children   (block/children blk)
             expr       (last (filter block/expression? children))
             block-str  (block/string blk)
             expr-str   (block/string expr)
             prefix-str (subs block-str 0 (- (count block-str)
                                             (count expr-str)))]
         (recur (str prefix prefix-str) expr))
       {:prefix prefix
        :block  blk}))))

(defn unwrap-meta-block
  "returns the inner expression for metadata-wrapped blocks"
  {:added "4.1"}
  ([blk]
   (loop [blk blk]
     (if (*meta-tags* (block/tag blk))
       (let [children (block/children blk)
             expr     (last (filter block/expression? children))]
         (recur expr))
       blk))))

(defn ns-block?
  "checks if a block is an ns form"
  {:added "4.1"}
  ([blk]
   (let [blk      (unwrap-meta-block blk)
         children (remove block/void? (block/children blk))
         op       (first children)]
     (and (= :list (block/tag blk))
          (= 'ns (block/value op))))))

(defn fact-block?
  "checks if a block is a fact form"
  {:added "4.1"}
  ([blk]
   (let [blk      (unwrap-meta-block blk)
         children (remove block/void? (block/children blk))
         op       (first children)]
     (and (= :list (block/tag blk))
           op
           (*test-forms* (block/value op))))))

(defn top-level-entries
  "returns top level blocks with their line ranges"
  {:added "4.1"}
  ([s]
   (->> (nav/parse-root s)
        (nav/down)
        (iterate nav/right)
        (take-while identity)
        (mapv (fn [z]
                {:block (nav/block z)
                 :line  (nav/line-info z)})))))

(defn rewrite-ns-form
  "rewrites the namespace symbol for an ns form"
  {:added "4.1"}
  ([blk target-ns]
   (let [form  (block/value (unwrap-meta-block blk))
         nform (cons 'ns (cons target-ns (nnext form)))]
     (block/string (block/block nform)))))

(defn latest-run-file
  "returns the most recent `.hara/runs/run-*.edn` file"
  {:added "4.1"}
  ([root]
   (let [dir (fs/path root ".hara/runs")]
      (when (fs/exists? dir)
       (some->> (fs/select dir {:include [#"run-.*\.edn$"]})
                (filter fs/file?)
                (sort-by fs/last-modified)
                 last
                 str)))))

(def ^:private +legacy-unreadable-pattern+
  #"#(?:function|object)\[[^\]]*\]|#<[^>\n]*>")

(defn sanitize-run-string
  "quotes legacy unreadable printed objects so the report can be parsed as EDN"
  {:added "4.1"}
  [s]
  (str/replace s +legacy-unreadable-pattern+ pr-str))

(defn parse-run-string
  "parses a run report, preserving legacy `#error` payloads as plain maps"
  {:added "4.1"}
  [s]
  (edn/read-string {:readers {'error #(assoc % :tag :error)}}
                   s))

(defn read-run-file
  "reads a saved run report"
  {:added "4.1"}
  ([path]
   (let [report (slurp path)]
     (try
       (parse-run-string report)
       (catch Exception _
         (parse-run-string (sanitize-run-string report)))))))

(defn failure-line
  "normalises a failure line entry into a row number"
  {:added "4.1"}
  ([entry]
   (let [line (:line entry)]
     (cond (integer? line)
           line

           (map? line)
           (:row line)

           :else
           nil))))

(defn significant-entry?
  {:added "4.1"}
  ([entry]
   (let [blk (:block entry)]
     (and (not (block/void? blk))
          (not (block/comment? blk))))))

(defn arrow-entry?
  {:added "4.1"}
  ([entry]
   (let [blk (:block entry)]
     (and (= :symbol (block/tag blk))
          (= '=> (block/value blk))))))

(defn next-significant-index
  {:added "4.1"}
  ([entries start]
   (first (keep-indexed (fn [i entry]
                          (when (and (<= start i)
                                     (significant-entry? entry))
                            i))
                        entries))))

(defn trim-right-void
  {:added "4.1"}
  ([entries]
   (loop [out entries]
     (if (and (seq out)
              (block/void? (:block (peek out))))
       (recur (pop out))
       out))))

(defn find-example-end
  {:added "4.1"}
  ([entries expr-idx]
   (let [sig-idx (next-significant-index entries (inc expr-idx))]
     (cond (nil? sig-idx)
           (count entries)

           (arrow-entry? (nth entries sig-idx))
           (if-let [result-idx (next-significant-index entries (inc sig-idx))]
             (or (next-significant-index entries (inc result-idx))
                 (count entries))
             (count entries))

           :else
           sig-idx))))

(defn entry-matches-line?
  "checks if an entry spans any of the target lines"
  {:added "4.1"}
  ([{:keys [line]} target-lines]
   (some (fn [row]
           (<= (:row line) row (:end-row line)))
         target-lines)))

(defn body-entries
  "returns all child entries of a fact block, including whitespace"
  {:added "4.1"}
  ([blk row-base]
   (->> (-> blk nav/navigator nav/down)
        (iterate nav/right*)
        (take-while (comp some? nav/block))
        (mapv (fn [z]
                (let [line (nav/line-info z)]
                  {:block (nav/block z)
                   :line  (-> line
                              (update :row #(+ row-base %))
                              (update :end-row #(+ row-base %)))}))))))

(defn fact-body-entries
  "returns the operator, optional intro, and body entries for a fact"
  {:added "4.1"}
  ([entry]
   (let [{:keys [prefix block]} (unwrap-fact-block (:block entry))
         row-base        (+ (-> entry :line :row)
                            (count (filter #{\newline} prefix))
                            -1)
         entries         (body-entries block row-base)
         op-idx          (next-significant-index entries 0)
         intro-idx       (when-let [idx (next-significant-index entries (inc op-idx))]
                            (when (= :string (some-> (nth entries idx nil) :block block/tag))
                              idx))
         body-start      (if intro-idx
                           (inc intro-idx)
                           (inc op-idx))]
     {:op     (nth entries op-idx)
      :intro  (when intro-idx
                (nth entries intro-idx))
      :body   (subvec entries body-start)})))

(defn example-ranges
  "returns body ranges for individual fact examples"
  {:added "4.1"}
  ([entries]
   (loop [expr-idx (next-significant-index entries 0)
          out      []]
     (if (nil? expr-idx)
       out
       (let [end-idx (find-example-end entries expr-idx)]
         (recur (when (< end-idx (count entries))
                  end-idx)
                (conj out [expr-idx end-idx])))))))

(defn selected-fact-body
  "returns a body string for the selected failing examples within a fact"
  {:added "4.1"}
  ([entries target-lines intro?]
   (->> (example-ranges entries)
        (keep (fn [[start end]]
                (let [example (-> (subvec entries start end)
                                  trim-right-void)]
                  (when (some #(entry-matches-line? % target-lines)
                              (filter significant-entry? example))
                    (apply str (map (comp block/string :block) example))))))
        (map-indexed (fn [i example]
                       (str (if (zero? i)
                              (if intro? "\n\n  " "\n  ")
                              "\n\n  ")
                            example)))
        (apply str))))

(defn isolate-fact-string
  "returns a fact string containing only the selected examples"
  {:added "4.1"}
  ([entry target-lines]
   (let [{:keys [prefix]}        (unwrap-fact-block (:block entry))
         {:keys [op intro body]} (fact-body-entries entry)
         selected                (selected-fact-body body target-lines (boolean intro))]
     (when (seq selected)
       (str prefix
            "("
            (block/string (:block op))
            (when intro
              (str " " (block/string (:block intro))))
            selected
            ")")))))

(defn failing-entries
  "returns failing run entries for a test namespace"
  {:added "4.1"}
  ([run-data ns]
   (let [test-ns (project/test-ns ns)]
     (->> [:failed :throw :timeout]
          (mapcat #(get run-data %))
          (filter (fn [entry]
                    (= test-ns (:ns entry))))
          vec))))

(defn failure-function
  "returns the preferred function identifier for a failing entry"
  {:added "4.1"}
  ([entry]
   (or (:function entry)
       (some-> (:name entry) symbol)
       (some-> (failure-line entry) (str "line-") symbol))))

(defn failure-functions
  "returns unique failure identifiers in encounter order"
  {:added "4.1"}
  ([entries]
   (reduce (fn [out entry]
             (let [f (failure-function entry)]
               (if (or (nil? f)
                       (some #{f} out))
                 out
                 (conj out f))))
           []
            entries)))

(defn isolate-target-ns
  "returns the target namespace for isolated tests"
  {:added "4.1"}
  ([ns]
   (isolate-target-ns ns "-isolated"))
  ([ns suffix]
   (symbol (str (project/source-ns ns)
                suffix
                (project/test-suffix)))))

(defn isolate-string
  "returns a test file containing only the selected failing checks"
  {:added "4.1"}
  ([original target-ns target-lines]
   (->> (top-level-entries original)
        (keep (fn [{:keys [block] :as entry}]
                (cond (ns-block? block)
                      (rewrite-ns-form block target-ns)

                      (not (fact-block? block))
                      (block/string block)

                      :else
                      (isolate-fact-string entry target-lines))))
        (str/join "\n\n"))))

(defn isolate
  "copies failing facts from a run report into a new test namespace

   (code.manage.unit.isolate/isolate
     'xt.lang.spec-base-test
     {:run \".hara/runs/run-1776913569354.edn\"
      :suffix \"-fix\"
      :write false}
     lookup
     project)"
  {:added "4.1"}
  ([ns {:keys [run suffix write] :as params} lookup project]
   (let [params    (task/single-function-print params)
         test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         run-file  (or run (latest-run-file (:root project)))]
     (cond
       (nil? test-file)
       (res/result {:status :error
                    :data :no-test-file})

       (nil? run-file)
       (res/result {:status :error
                    :data :no-run-file})

       :else
       (let [run-data     (read-run-file run-file)
             failures     (failing-entries run-data test-ns)
             target-lines (set (keep failure-line failures))]
         (cond
           (empty? failures)
           (res/result {:status :info
                        :data :no-failures})

           :else
           (let [target-ns   (isolate-target-ns test-ns (or suffix "-isolated"))
                 target-file (scaffold/new-filename target-ns project write)
                 revised     (isolate-string (slurp test-file)
                                            target-ns
                                            target-lines)
                 result      (base/transform-code target-ns
                                                  (assoc params
                                                         :no-analysis true
                                                         :transform (constantly revised)
                                                         :verify {:parse block/parse-root})
                                                  (assoc lookup target-ns target-file)
                                                  project)]
             (assoc result
                    :run run-file
                    :target target-ns
                    :functions (failure-functions failures)))))))))
