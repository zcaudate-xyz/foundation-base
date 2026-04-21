(ns code.manage.unit.import
  (:require [code.framework :as base]
            [code.framework.common :as common]
            [code.framework.docstring :as docstring]
            [code.framework.test.fact :as fact]
            [code.manage.unit.walk :as walk]
            [code.project :as project]
            [code.query :as query]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.lib.collection :as collection]
            [std.lib.result :as res]
            [std.task :as task])
  (:refer-clojure :exclude [import]))

(defn examples-config
  "Parses `:examples` metadata into a selection map."
  {:added "4.1"}
  ([mta]
   (let [examples (:examples mta)]
     (when (vector? examples)
       (loop [[x & more] examples
              current nil
              out {}]
         (cond (nil? x)
               out

               (integer? x)
               (recur more x (assoc out x {:check true}))

               (= :no-check x)
               (recur more current (if current
                                     (assoc-in out [current :check] false)
                                     out))

               :else
               (recur more current out)))))))

(defn significant-block?
  {:added "4.1"}
  ([blk]
   (and (not (block/void? blk))
        (not (block/comment? blk)))))

(defn arrow-block?
  {:added "4.1"}
  ([blk]
   (and (= :symbol (block/tag blk))
        (= '=> (block/value blk)))))

(defn next-significant-index
  {:added "4.1"}
  ([blocks start]
   (first (keep-indexed (fn [i blk]
                          (when (and (<= start i)
                                     (significant-block? blk))
                            i))
                        blocks))))

(defn trim-right-void
  {:added "4.1"}
  ([blocks]
   (loop [out blocks]
     (if (and (seq out)
              (block/void? (peek out)))
       (recur (pop out))
       out))))

(defn strip-check
  {:added "4.1"}
  ([blocks]
   (if-let [arrow-idx (first (keep-indexed (fn [i blk]
                                             (when (arrow-block? blk)
                                               i))
                                           blocks))]
     (-> (subvec blocks 0 arrow-idx)
         trim-right-void)
     blocks)))

(defn find-example-end
  {:added "4.1"}
  ([blocks expr-idx]
   (let [sig-idx (next-significant-index blocks (inc expr-idx))]
     (cond (nil? sig-idx)
           (count blocks)

           (arrow-block? (nth blocks sig-idx))
           (if-let [result-idx (next-significant-index blocks (inc sig-idx))]
             (or (next-significant-index blocks (inc result-idx))
                 (count blocks))
             (count blocks))

           :else
           sig-idx))))

(defn gather-selected-fact-body
  "Builds a filtered fact body using `:examples` metadata."
  {:added "4.1"}
  ([nav mta]
   (let [config (examples-config mta)
         blocks (->> (iterate nav/right* nav)
                     (map nav/block)
                     (take-while some?)
                     vec)
         ranges (loop [expr-idx (next-significant-index blocks 0)
                       idx 0
                       out []]
                  (if (nil? expr-idx)
                    out
                    (let [end-idx (find-example-end blocks expr-idx)]
                      (recur (when (< end-idx (count blocks))
                               end-idx)
                             (inc idx)
                             (conj out [idx expr-idx end-idx])))))]
     (->> ranges
          (keep (fn [[idx start end]]
                  (when-let [{:keys [check]} (get config idx)]
                    (let [example (-> (subvec blocks start end)
                                      trim-right-void)
                          example (if check example (strip-check example))]
                      (when (seq example)
                        [idx example])))))
          (sort-by first)
          (map second)
          (reduce (fn [out example]
                    (into out
                          (concat (if (empty? out)
                                    [(block/newline) (block/space) (block/space)]
                                    [(block/newline) (block/newline) (block/space) (block/space)])
                                  example)))
                  [])))))

(defn gather-fact-body
  {:added "4.1"}
  ([nav mta]
   (cond common/*test-full*
         (fact/gather-fact-body nav)

         common/*test-examples*
         (if (contains? mta :examples)
           (gather-selected-fact-body nav mta)
           (fact/gather-fact-body nav))

         :else
         [])))

(defn gather-fact
  {:added "4.1"}
  ([nav]
   (let [nav (if (nil? (nav/tag nav))
               (nav/left nav)
               nav)
         nav (if (= :list (nav/tag nav))
               (nav/down nav)
               nav)]
     (if-let [mta (common/gather-meta nav)]
       (let [nav (if (symbol? (nav/value nav))
                   (nav/right nav)
                   nav)
             exp (and nav (nav/value nav))
             [intro nnav] (if (string? exp)
                            [exp (if (nav/right nav)
                                   (nav/right* nav))]
                            ["" nav])]
         (assoc mta
                :form  (-> nav nav/left nav/value)
                :sexp  (-> nav nav/up nav/value)
                :line  (nav/line-info (nav/up nav))
                :test  (if nnav
                         (gather-fact-body nnav mta)
                         [])
                :intro intro))))))

(defn analyse-fact-tests
  {:added "4.1"}
  ([nav]
   (let [root (if (= :root (nav/tag nav))
                (nav/down nav)
                nav)
         fns  (if root
                (query/$* root ['(#{fact comment} | & _)] {:return :zipper :walk :top})
                [])]
     (->> (keep gather-fact fns)
          (reduce (fn [m {:keys [ns var class sexp test intro line form] :as meta}]
                    (-> m
                        (update-in [ns var]
                                   assoc
                                   :ns ns
                                   :var var
                                   :class class
                                   :test {:path common/*path*
                                          :sexp sexp
                                          :form form
                                          :code test
                                          :line line}
                                   :meta (dissoc (apply dissoc meta common/+test-vars+)
                                                 :examples)
                                   :intro intro)))
                  {})))))

(defn analyse-test-code
  {:added "4.1"}
  ([s]
   (let [nav        (-> (nav/parse-root s)
                        (nav/down))
         nsloc      (query/$ nav [(ns | _ & _)] {:walk :top
                                                 :return :zipper
                                                 :first true})
         ns-form    (-> nsloc nav/up nav/value)
         frameworks (base/find-test-frameworks ns-form)]
     (->> frameworks
          (map (fn [framework]
                 (case framework
                   :fact (analyse-fact-tests nav)
                   (common/analyse-test framework nav))))
          (apply collection/merge-nested)
          (common/entry)))))

(defn analyse
  {:added "4.1"}
  ([ns params lookup project]
   (let [path (lookup ns)]
      (binding [common/*path* path
                common/*test-full* (:full params)
                common/*test-examples* (:examples params)]
        (analyse-test-code (slurp path))))))

(defn import
  "imports unit test docstrings, with optional example body selection"
  {:added "4.1"}
  ([ns params lookup project]
   (let [source-ns   (project/source-ns ns)
         test-ns     (project/test-ns ns)
         source-file (lookup source-ns)
         test-file   (lookup test-ns)
         params      (task/single-function-print params)]
     (cond (nil? source-file)
           (res/result {:status :error
                        :data :no-source-file})

           (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           (let [import-fn    (fn [nsp refers]
                                (fn [zloc]
                                  (docstring/insert-docstring zloc nsp refers)))
                 refers       (analyse test-ns params lookup project)
                 transform-fn (fn [text] (walk/walk-string text '_ refers import-fn))]
             (base/transform-code source-ns (assoc params :transform transform-fn) lookup project))))))
