(ns replace-std-aggregate-refs
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.reader :as tr]))

(def project-root
  (.getCanonicalPath (io/file ".")))

(def aggregate-sources
  {'std.lib    "src/std/lib.clj"
   'std.string "src/std/string.clj"})

(def delimiter-chars
  #{\space \newline \tab \return \,
    \( \) \[ \] \{ \}
    \" \; \' \` \~ \^ \@})

(defn path-in-project [path]
  (.getPath (io/file project-root path)))

(defn read-all-forms [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (loop [forms []]
      (let [form (tr/read {:eof ::eof :read-cond :allow} reader)]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(defn require-clause [ns-form]
  (->> (drop 2 ns-form)
       (filter seq?)
       (filter #(= :require (first %)))
       first))

(defn parse-require-spec [spec]
  (cond
    (symbol? spec)
    {:ns spec}

    (vector? spec)
    (let [[ns-sym & opts] spec]
      (loop [m {:ns ns-sym}
             xs opts]
        (if (empty? xs)
          m
          (let [[k v & more] xs]
            (recur (assoc m k v) more)))))

    :else
    (throw (ex-info "Unsupported require spec" {:spec spec}))))

(defn render-require-spec [{:keys [ns] :as spec}]
  (let [attrs (dissoc spec :ns)
        ordered-keys (concat [:as :refer :exclude :rename]
                             (sort (remove #{:as :refer :exclude :rename}
                                           (keys attrs))))
        parts (mapcat (fn [k]
                        (when-let [v (get attrs k)]
                          [k v]))
                      ordered-keys)]
    (pr-str (vec (cons ns parts)))))

(defn render-clause [clause]
  (pr-str clause))

(defn render-require-clause [specs]
  (let [specs (sort-by (comp str :ns) specs)
        rendered (map render-require-spec specs)]
    (if (seq rendered)
      (str "(:require " (first rendered)
           (apply str
                  (for [entry (rest rendered)]
                    (str "\n            " entry)))
           ")")
      "(:require)")))

(defn render-ns-form [ns-form specs]
  (let [[_ ns-name & clauses] ns-form
        clauses (remove #(and (seq? %)
                              (= :require (first %)))
                        clauses)
        require-entry (when (seq specs)
                        (render-require-clause specs))
        rendered-clauses (concat (when require-entry [require-entry])
                                 (map render-clause clauses))]
    (str "(ns " ns-name
         (when (seq rendered-clauses)
           (str "\n  "
                (str/join "\n  " rendered-clauses)))
         ")")))

(defn ns-alias-map [ns-form]
  (->> (require-clause ns-form)
       rest
       (keep (fn [spec]
               (let [{:keys [ns as]} (parse-require-spec spec)]
                 (when as [as ns]))))
       (into {})))

(defn resolve-source-ns [alias-map sym]
  (let [ns-part (some-> sym namespace symbol)]
    (or (get alias-map ns-part)
        ns-part
        sym)))

(defn read-aggregate-map [aggregate-ns]
  (let [path (path-in-project (get aggregate-sources aggregate-ns))
        forms (read-all-forms path)
        ns-form (first forms)
        alias-map (ns-alias-map ns-form)]
    (doseq [ns-sym (vals alias-map)]
      (when (and (symbol? ns-sym)
                 (not (contains? #{'clojure.set} ns-sym)))
        (require ns-sym)))
    (reduce
     (fn [acc form]
       (if-not (seq? form)
         acc
         (let [op-name (some-> form first name)]
           (cond
             (= "intern-all" op-name)
             (reduce (fn [m ns-sym]
                       (let [real-ns (resolve-source-ns alias-map ns-sym)]
                         (require real-ns)
                         (reduce (fn [m2 public-sym]
                                   (assoc m2 (str public-sym)
                                          {:target (str public-sym)
                                           :source-ns real-ns
                                           :source-name public-sym}))
                                 m
                                 (sort (keys (ns-publics real-ns))))))
                     acc
                     (rest form))

             (= "intern-in" op-name)
             (reduce (fn [m entry]
                       (let [[target source] (if (vector? entry)
                                               entry
                                               [(symbol (name entry)) entry])
                             source-ns (resolve-source-ns alias-map source)
                             source-name (symbol (name source))]
                         (assoc m (str target)
                                {:target (str target)
                                 :source-ns source-ns
                                 :source-name source-name})))
                     acc
                     (rest form))

             :else
             acc))))
     {}
     (rest forms))))

(def aggregate-maps
  (into {}
        (for [agg (keys aggregate-sources)]
          [agg (read-aggregate-map agg)])))

(defn token-delimiter? [ch]
  (or (nil? ch)
      (contains? delimiter-chars ch)))

(defn read-token [text start]
  (loop [i start]
    (if (or (>= i (count text))
            (token-delimiter? (.charAt text i)))
      [(.substring text start i) i]
      (recur (inc i)))))

(defn rewrite-code-tokens [text token-fn]
  (let [sb (StringBuilder.)]
    (loop [i 0
           state :code
           data {:used-ns #{}
                 :remaining #{}}]
      (if (>= i (count text))
        [(str sb) data]
        (let [ch (.charAt text i)]
          (case state
            :comment
            (do (.append sb ch)
                (recur (inc i)
                       (if (= ch \newline) :code :comment)
                       data))

            :string
            (cond
              (= ch \\)
              (do (.append sb ch)
                  (when (< (inc i) (count text))
                    (.append sb (.charAt text (inc i))))
                  (recur (+ i 2) :string data))

              (= ch \")
              (do (.append sb ch)
                  (recur (inc i) :code data))

              :else
              (do (.append sb ch)
                  (recur (inc i) :string data)))

            :code
            (cond
              (= ch \;)
              (do (.append sb ch)
                  (recur (inc i) :comment data))

              (= ch \")
              (do (.append sb ch)
                  (recur (inc i) :string data))

              (token-delimiter? ch)
              (do (.append sb ch)
                  (recur (inc i) :code data))

              :else
              (let [[token next-i] (read-token text i)
                    [new-token new-data] (token-fn token data)]
                (.append sb new-token)
                (recur next-i :code new-data)))))))))

(defn qualify-token [token alias-map refer-rewrites]
  (or
   (when-let [{:keys [source-ns source-name]} (get refer-rewrites token)]
     {:token (str source-ns "/" source-name)
      :source-ns source-ns})
   (when-let [[lhs rhs] (and (str/includes? token "/")
                             (let [[a b] (str/split token #"/" 2)]
                               [a b]))]
     (let [lhs-sym (symbol lhs)
           aggregate-ns (cond
                          (contains? alias-map lhs-sym) (get alias-map lhs-sym)
                          (contains? aggregate-maps lhs-sym) lhs-sym
                          :else nil)]
       (when aggregate-ns
         (when-let [{:keys [source-ns source-name]} (get-in aggregate-maps [aggregate-ns rhs])]
           {:token (str source-ns "/" source-name)
            :source-ns source-ns}))))))

(defn remaining-aggregate-token [token alias-map]
  (cond
    (contains? alias-map (symbol token))
    nil

    (and (str/includes? token "/")
         (let [[lhs rhs] (str/split token #"/" 2)
               lhs-sym (symbol lhs)
               aggregate-ns (cond
                              (contains? alias-map lhs-sym) (get alias-map lhs-sym)
                              (contains? aggregate-maps lhs-sym) lhs-sym
                              :else nil)]
           (when aggregate-ns
             [aggregate-ns rhs])))
    true

    :else
    false))

(defn rewrite-body [text alias-map refer-rewrites]
  (rewrite-code-tokens
   text
   (fn [token data]
     (if-let [{:keys [token source-ns]} (qualify-token token alias-map refer-rewrites)]
       [token (update data :used-ns conj source-ns)]
       [token (cond-> data
                (remaining-aggregate-token token alias-map)
                (update :remaining conj token))]))))

(defn merge-refers [a b]
  (cond
    (= :all a) :all
    (= :all b) :all
    (and a b) (vec (sort (set/union (set a) (set b))))
    :else (or a b)))

(defn merge-specs [a b]
  (-> a
      (merge (dissoc b :refer))
      (assoc :refer (merge-refers (:refer a) (:refer b)))))

(defn add-spec [specs spec]
  (let [ns-key (:ns spec)]
    (if-let [existing (get specs ns-key)]
      (assoc specs ns-key (merge-specs existing spec))
      (assoc specs ns-key spec))))

(defn normalized-require-specs [ns-form]
  (->> (or (some-> (require-clause ns-form) rest) [])
       (map parse-require-spec)))

(defn aggregate-specs [specs]
  (filter #(contains? aggregate-maps (:ns %)) specs))

(defn non-aggregate-specs [specs]
  (remove #(contains? aggregate-maps (:ns %)) specs))

(defn refer-rewrite-plan [aggregate-spec]
  (reduce
   (fn [acc sym]
     (if-let [{:keys [source-ns source-name]} (get-in aggregate-maps [(:ns aggregate-spec) (str sym)])]
       (if (= (name sym) (name source-name))
         (update-in acc [:refer source-ns] (fnil conj #{}) sym)
         (assoc-in acc [:rewrite (str sym)]
                   {:source-ns source-ns
                    :source-name source-name}))
       (update acc :keep conj sym)))
   {:refer {}
    :rewrite {}
    :keep []}
   (or (:refer aggregate-spec) [])))

(defn aggregate-alias-map [aggregate-specs]
  (->> aggregate-specs
       (keep (fn [{:keys [ns as]}]
               (when as [as ns])))
       (into {})))

(defn keep-aggregate-spec? [spec remaining-tokens kept-refers]
  (or (seq kept-refers)
      (some #(let [[lhs _] (str/split % #"/" 2)]
               (or (= lhs (str (:ns spec)))
                   (= lhs (str (:as spec)))))
            remaining-tokens)))

(defn format-summary [result]
  (str (:path result)
       "  changed=" (:changed? result)
       "  unresolved=" (:unresolved-count result)))

(defn balanced-form-end [text start]
  (loop [i start
         depth 0
         state :code]
    (when (>= i (count text))
      (throw (ex-info "Unbalanced form" {:start start})))
    (let [ch (.charAt text i)]
      (case state
        :comment
        (recur (inc i) depth (if (= ch \newline) :code :comment))

        :string
        (cond
          (= ch \\) (recur (+ i 2) depth :string)
          (= ch \") (recur (inc i) depth :code)
          :else (recur (inc i) depth :string))

        :code
        (cond
          (= ch \;) (recur (inc i) depth :comment)
          (= ch \") (recur (inc i) depth :string)
          (= ch \() (recur (inc i) (inc depth) :code)
          (= ch \)) (let [new-depth (dec depth)]
                      (if (zero? new-depth)
                        (inc i)
                        (recur (inc i) new-depth :code)))
          :else (recur (inc i) depth :code))))))

(defn ns-form-span [text]
  (let [start (.indexOf text "(ns ")]
    (when (neg? start)
      (throw (ex-info "No ns form found" {})))
    [start (balanced-form-end text start)]))

(defn rel-path [file]
  (let [canonical (.getCanonicalPath (io/file file))
        prefix (str project-root java.io.File/separator)]
    (if (str/starts-with? canonical prefix)
      (subs canonical (count prefix))
      canonical)))

(defn process-file [file-path write? verbose?]
  (let [text (slurp file-path)
        [ns-start ns-end] (ns-form-span text)
        ns-text (.substring text ns-start ns-end)
        ns-form (tr/read-string {:read-cond :allow} ns-text)
        original-specs (normalized-require-specs ns-form)
        aggregate-specs (vec (aggregate-specs original-specs))
        alias-map (aggregate-alias-map aggregate-specs)
        refer-plan (reduce (fn [acc spec]
                             (let [{:keys [refer rewrite]} (refer-rewrite-plan spec)]
                               {:refer (merge-with set/union (:refer acc) refer)
                                :rewrite (merge (:rewrite acc) rewrite)
                                :keep (concat (:keep acc) (:keep (refer-rewrite-plan spec)))}))
                           {:refer {}
                            :rewrite {}
                            :keep []}
                           aggregate-specs)
        body-text (.substring text ns-end)
        [new-body {:keys [used-ns remaining]}] (rewrite-body body-text alias-map (:rewrite refer-plan))
        concrete-specs (reduce add-spec
                               {}
                               (concat
                                (non-aggregate-specs original-specs)
                                (map (fn [ns-sym] {:ns ns-sym}) used-ns)
                                (for [[ns-sym syms] (:refer refer-plan)]
                                  {:ns ns-sym
                                   :refer (vec (sort syms))})))
        kept-aggregate (keep (fn [spec]
                               (let [kept-refers (filter (set (:keep refer-plan))
                                                         (or (:refer spec) []))]
                                 (when (keep-aggregate-spec? spec remaining kept-refers)
                                   (cond-> (assoc spec :refer (vec kept-refers))
                                     (empty? kept-refers) (dissoc :refer)))))
                             aggregate-specs)
        new-specs (->> kept-aggregate
                       (reduce add-spec concrete-specs)
                      vals
                      vec)
        new-ns (render-ns-form ns-form new-specs)
        new-text (str (.substring text 0 ns-start) new-ns new-body)
        changed? (not= text new-text)
        unresolved-count (count remaining)
        result {:path (rel-path file-path)
                :changed? changed?
                :unresolved-count unresolved-count
                :unresolved (sort remaining)}]
    (when (and changed? write?)
      (spit file-path new-text))
    (when (and verbose? (or changed? (pos? unresolved-count)))
      (println (format-summary result))
      (when (pos? unresolved-count)
        (doseq [token (:unresolved result)]
          (println "  unresolved token:" token))))
    result))

(defn clj-files [paths]
  (let [targets (if (seq paths)
                  paths
                  ["src" "test" "src-doc" "src-extra"])]
    (->> targets
         (map io/file)
         (mapcat (fn [f]
                   (cond
                     (.isDirectory f)
                     (->> (file-seq f)
                          (filter #(.isFile %))
                          (filter #(str/ends-with? (.getName %) ".clj")))

                     (.isFile f)
                     [f]

                     :else
                     [])))
         (map #(.getPath %))
         distinct
         sort)))

(defn parse-args [args]
  (loop [opts {:write? false
               :verbose? false
               :paths []}
         xs args]
    (if (empty? xs)
      opts
      (let [[x & more] xs]
        (cond
          (= x "--write")
          (recur (assoc opts :write? true) more)

          (= x "--verbose")
          (recur (assoc opts :verbose? true) more)

          :else
          (recur (update opts :paths conj x) more))))))

(defn -main [& args]
  (let [{:keys [write? verbose? paths]} (parse-args args)
        files (clj-files paths)
        results (mapv #(process-file % write? verbose?) files)
        changed (count (filter :changed? results))
        unresolved (reduce + (map :unresolved-count results))]
    (println (if write? "write" "dry-run")
             "files=" (count files)
             "changed=" changed
             "unresolved=" unresolved)
    (when (and (not write?) (zero? changed))
      (println "No changes needed."))
    (System/exit 0)))

(apply -main *command-line-args*)
