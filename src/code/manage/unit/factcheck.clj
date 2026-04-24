(ns code.manage.unit.factcheck
  (:require [clojure.string]
            [code.framework :as base]
            [code.project :as project]
            [code.test.base.context :as context]
            [code.test.base.executive :as executive]
            [code.test.base.process :as process]
            [code.test.base.runtime :as rt]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.lib.result :as res]
            [std.lib.zip :as zip]
            [std.string.prose :as prose]
            [std.task :as task]))

(def ^:dynamic *test-forms*
  '#{fact})

(def ^:dynamic *meta-tags*
  #{:meta :hash-meta})

(def ^:private +missing+
  ::missing)

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

(defn fact-block?
  "checks if a block is a fact form"
  {:added "4.1"}
  ([blk]
   (let [{:keys [block]} (unwrap-fact-block blk)
         children        (remove block/void? (block/children block))
         op              (first children)]
     (and (= :list (block/tag block))
          op
          (*test-forms* (block/value op))))))

(defn top-level-entries
  "returns all top level blocks with line info"
  {:added "4.1"}
  ([s]
   (->> (nav/parse-root s)
        (nav/down)
        (iterate nav/right)
        (take-while identity)
        (mapv (fn [z]
                {:block (nav/block z)
                 :line  (nav/line-info z)})))))

(defn child-entries
  "returns non-void child blocks together with their starting column"
  {:added "4.1"}
  ([blk]
   (->> (-> blk nav/navigator nav/down)
        (iterate zip/step-right)
        (take-while zip/get)
        (map (fn [z]
               {:block (nav/block z)
                :col   (-> z nav/line-info :col)}))
        (remove (comp block/void? :block)))))

(defn entry-block
  {:added "4.1"}
  ([entry]
   (if (map? entry)
     (:block entry)
     entry)))

(defn entry-col
  {:added "4.1"}
  ([entry]
   (if (map? entry)
     (:col entry)
     1)))

(defn parse-body
  "partitions fact body blocks into plain forms and expression/check pairs"
  {:added "4.1"}
  ([blocks]
   (loop [blocks blocks
          out    []]
     (if (empty? blocks)
       out
       (let [[expr arrow expected & more] blocks]
         (if (and arrow
                  (= :symbol (block/tag (entry-block arrow)))
                  (= "=>" (block/string (entry-block arrow))))
           (recur more
                  (conj out {:type :check
                             :expr expr
                             :expected expected}))
           (recur (rest blocks)
                  (conj out {:type :form
                             :expr expr}))))))))

(defn leading-indent
  {:added "4.1"}
  ([^String line]
   (loop [i 0]
     (if (and (< i (count line))
              (#{\space \tab} (.charAt line i)))
       (recur (inc i))
       i))))

(defn trim-indent
  {:added "4.1"}
  ([^String line n]
   (let [limit (min n (leading-indent line))]
     (subs line limit))))

(defn normalise-block-string
  {:added "4.1"}
  ([^String s]
   (normalise-block-string s 0))
  ([^String s indent]
   (let [[head & rest] (clojure.string/split-lines s)]
     (if (empty? rest)
       s
       (clojure.string/join "\n"
                            (cons head
                                  (map (fn [line]
                                         (if (clojure.string/blank? line)
                                           line
                                           (trim-indent line indent)))
                                       rest)))))))

(defn render-form
  "formats an arbitrary block or form"
  {:added "4.1"}
  ([form]
   (let [blk (entry-block form)
         col (entry-col form)]
     (normalise-block-string (block/string blk)
                             (max 0 (dec col))))))

(defn fact-block-data
  "returns the logical structure of a fact form"
  {:added "4.1"}
  ([form]
   (let [blk                 (cond (string? form)
                                   (block/parse-first form)

                                   :else
                                   form)
         {:keys [prefix block]} (unwrap-fact-block blk)
         children             (child-entries block)
         [op & more]          children
         [intro more]         (if (= :string (some-> (first more) entry-block block/tag))
                                [(first more) (next more)]
                                [nil more])]
     {:prefix prefix
      :op     op
      :intro  intro
      :items  (parse-body more)})))

(defn fact-line
  "returns the row where the inner fact form starts"
  {:added "4.1"}
  ([entry]
   (let [{:keys [prefix]} (unwrap-fact-block (:block entry))]
     (+ (-> entry :line :row)
        (count (filter #{\newline} prefix))))))

(defn render-fact
  "renders a fact using already formatted body items"
  {:added "4.1"}
  ([form rendered-items]
   (let [{:keys [prefix op intro]} (fact-block-data form)
         head (str "(" (block/string (entry-block op))
                   (when intro
                     (str " " (block/string (entry-block intro)))))
         body (not-empty (clojure.string/join "\n\n" rendered-items))]
     (str prefix
          head
          (when body
            (str (if intro "\n\n" "\n")
                 body))
          ")"))))

(defn render-checkless-item
  "renders a fact item without an expectation"
  {:added "4.1"}
  ([{:keys [expr]}]
   (prose/indent (render-form expr) 2)))

(defn result-string
  "renders an evaluation result as an EDN string"
  {:added "4.1"}
  ([value]
   (-> value executive/report-edn pr-str)))

(defn render-generated-item
  "renders a fact item with a generated expectation"
  {:added "4.1"}
  ([item value]
   (let [expr-str     (render-form (:expr item))
         expected-str (result-string value)]
     (str (prose/indent expr-str 2)
          "\n  => "
          (prose/indent-rest expected-str 5)))))

(defn factcheck-remove-form-string
  "removes `=>` expectations from a single fact form"
  {:added "4.1"}
  ([form]
   (->> (-> form fact-block-data :items)
        (map render-checkless-item)
        (render-fact form))))

(defn factcheck-remove-string
  "removes `=>` expectations from all top level facts in a file"
  {:added "4.1"}
  ([original]
   (->> (top-level-entries original)
        (map (fn [{:keys [block]}]
               (if (fact-block? block)
                 (factcheck-remove-form-string block)
                 (block/string block))))
        (clojure.string/join "\n\n"))))

(defn fact-op-form
  "returns the input form for a compiled fact op"
  {:added "4.1"}
  ([op]
   (case (:type op)
     :test-equal (get-in op [:input :form])
     (:form op))))

(defn evaluate-fact-op
  "evaluates a compiled fact op and returns its value"
  {:added "4.1"}
  ([test-ns op]
   (let [form   (fact-op-form op)
         meta   (assoc (:meta op) :ns test-ns)
         result (binding [context/*timeout* (or (:timeout meta)
                                                context/*timeout-global*)]
                  (process/evaluate {:form form
                                     :meta meta}))]
     (case (:status result)
       :success (res/result-data result)
       :timeout (throw (ex-info "factcheck evaluation timed out"
                                {:ns test-ns
                                 :form form
                                 :meta meta}))
       (throw (res/result-data result))))))

(defn fact-result-values
  "evaluates every compiled op in a fact and returns the raw values"
  {:added "4.1"}
  ([fpkg]
   (let [test-ns   (:ns fpkg)
         id        (:id fpkg)
         teardown? (or (rt/get-fact test-ns id :function :teardown)
                       (rt/get-flag test-ns id :setup))]
     (rt/setup-fact test-ns id)
     (try
       (mapv (partial evaluate-fact-op test-ns) (:full fpkg))
       (finally
         (when teardown?
           (rt/teardown-fact test-ns id)))))))

(defn factcheck-generate-form-string
  "generates `=>` expectations for a single fact form"
  {:added "4.1"}
  ([form values]
   (let [items (-> form fact-block-data :items)]
     (->> items
          (map-indexed (fn [i item]
                         (let [value (get values i +missing+)]
                           (if (= +missing+ value)
                             (render-checkless-item item)
                             (render-generated-item item value)))))
          (render-fact form)))))

(defn factcheck-generate-string
  "generates `=>` expectations for all top level facts in a file"
  {:added "4.1"}
  ([original line->values]
   (->> (top-level-entries original)
        (map (fn [entry]
               (let [blk (:block entry)]
                 (if (fact-block? blk)
                   (if-let [values (get line->values (fact-line entry))]
                     (factcheck-generate-form-string blk values)
                     (block/string blk))
                   (block/string blk)))))
        (clojure.string/join "\n\n"))))

(defn fact-results-map
  "evaluates all loaded facts in a namespace and indexes them by line"
  {:added "4.1"}
  ([test-ns]
   (try
     (rt/eval-in-ns test-ns (rt/get-global test-ns :prelim))
     (rt/eval-in-ns test-ns (rt/get-global test-ns :setup))
     (->> (rt/all-facts test-ns)
          vals
          (sort-by :line)
          (map (fn [fpkg]
                 [(:line fpkg) (fact-result-values fpkg)]))
          (into {}))
     (finally
       (rt/eval-in-ns test-ns (rt/get-global test-ns :teardown))))))

(defn factcheck-remove
  "removes `=>` expectations from fact tests"
  {:added "4.1"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [test-ns   (project/test-ns ns)
           test-file (lookup test-ns)
           params    (task/single-function-print params)]
       (cond
         (nil? test-file)
         (res/result {:status :error
                      :data :no-test-file})

         :else
         (base/transform-code test-ns
                              (assoc params :transform factcheck-remove-string)
                              lookup
                              project))))))

(defn factcheck-generate
  "loads a test namespace, evaluates fact forms and regenerates `=>` expectations"
  {:added "4.1"}
  ([ns params lookup {:keys [root] :as project}]
   (if (not (base/no-test ns params lookup project))
     (let [test-ns   (project/test-ns ns)
           test-file (lookup test-ns)
           params    (task/single-function-print params)]
       (cond
         (nil? test-file)
         (res/result {:status :error
                      :data :no-test-file})

         :else
         (binding [context/*root* root
                   context/*errors* (atom {})
                   context/*settings* (merge context/*settings* params)
                   context/*print* (conj context/*print* :no-beep)]
           (executive/load-namespace test-ns params lookup project)
           (try
             (let [line->values (fact-results-map test-ns)
                   transform-fn (fn [text]
                                  (factcheck-generate-string text line->values))]
               (base/transform-code test-ns
                                    (assoc params :transform transform-fn)
                                    lookup
                                    project))
             (finally
               (executive/unload-namespace test-ns params lookup project)))))))))
