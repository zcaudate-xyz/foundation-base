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

(defn ns->relative-path [ns-sym]
  (str "src/"
       (-> (name ns-sym)
           (str/replace "." "/")
           (str/replace "-" "_"))
       ".clj"))

(defn public-def-form? [form]
  (and (seq? form)
       (symbol? (first form))
       (str/starts-with? (name (first form)) "def")))

(defn def-symbol [form]
  (let [[_ maybe-name & _] form]
    (when (symbol? maybe-name)
      maybe-name)))

(defn private-symbol? [sym]
  (boolean (:private (meta sym))))

(defn public-symbols-from-file [ns-sym]
  (let [path (path-in-project (ns->relative-path ns-sym))]
    (if-not (.exists (io/file path))
      []
      (->> (read-all-forms path)
           (keep (fn [form]
                   (when (public-def-form? form)
                     (let [sym (def-symbol form)]
                       (when (and sym (not (private-symbol? sym)))
                         sym)))))
           distinct
           sort))))

(defn read-aggregate-map [aggregate-ns]
  (let [path (path-in-project (get aggregate-sources aggregate-ns))
        forms (read-all-forms path)
        ns-form (first forms)
        alias-map (ns-alias-map ns-form)]
    (reduce
     (fn [acc form]
       (if-not (seq? form)
         acc
         (let [op-name (some-> form first name)]
           (cond
             (= "intern-all" op-name)
             (reduce (fn [m ns-sym]
                       (let [real-ns (resolve-source-ns alias-map ns-sym)]
                         (reduce (fn [m2 public-sym]
                                   (assoc m2 (str public-sym)
                                          {:target (str public-sym)
                                           :source-ns real-ns
                                           :source-name public-sym}))
                                 m
                                 (public-symbols-from-file real-ns))))
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

(def std-string-preferred
  {"blank?"       {:source-ns 'clojure.string :source-name 'blank?}
   "split"        {:source-ns 'clojure.string :source-name 'split}
   "split-lines"  {:source-ns 'clojure.string :source-name 'split-lines}
   "join"         {:source-ns 'clojure.string :source-name 'join}
   "upper-case"   {:source-ns 'clojure.string :source-name 'upper-case}
   "lower-case"   {:source-ns 'clojure.string :source-name 'lower-case}
   "reverse"      {:source-ns 'clojure.string :source-name 'reverse}
   "starts-with?" {:source-ns 'clojure.string :source-name 'starts-with?}
   "ends-with?"   {:source-ns 'clojure.string :source-name 'ends-with?}
   "includes?"    {:source-ns 'clojure.string :source-name 'includes?}
   "trim"         {:source-ns 'clojure.string :source-name 'trim}
   "trim-left"    {:source-ns 'clojure.string :source-name 'triml}
   "trim-right"   {:source-ns 'clojure.string :source-name 'trimr}
   "trim-newlines" {:source-ns 'clojure.string :source-name 'trim-newline}
   "escape"       {:source-ns 'clojure.string :source-name 'escape}
   "replace"      {:source-ns 'clojure.string :source-name 'replace}
   "replace-first" {:source-ns 'clojure.string :source-name 'replace-first}
   "capitalize"   {:source-ns 'clojure.string :source-name 'capitalize}})

(def std-lib-legacy-overrides
  {"add-trace"   {:source-ns 'std.lib.trace :source-name 'add-base-trace}
   "has-trace?"  {:source-ns 'std.lib.trace :source-name 'has-trace?}
   "json-load"   {:source-ns 'std.json :source-name 'read}
   "make:config" {:source-ns 'std.make :source-name 'make-config}
   "req"         {:source-ns 'std.concurrent :source-name 'req}
   "tracked"     {:source-ns 'std.lib.component.track :source-name 'tracked}
   "tracked:all" {:source-ns 'std.lib.component.track :source-name 'tracked:all}
   "watch:add"   {:source-ns 'std.lib.watch :source-name 'watch:add}
   "watch:list"  {:source-ns 'std.lib.watch :source-name 'watch:list}
   "watch:remove" {:source-ns 'std.lib.watch :source-name 'watch:remove}
   "yield"       {:source-ns 'std.lib.generate :source-name 'yield}
   "yield-all"   {:source-ns 'std.lib.generate :source-name 'yield-all}})

(def file-target-overrides
  {"src/std/fs/watch.clj"
   {"watch:add"   {:source-ns 'std.fs.watch :source-name 'add-io-watch}
    "watch:list"  {:source-ns 'std.fs.watch :source-name 'list-io-watch}
    "watch:remove" {:source-ns 'std.fs.watch :source-name 'remove-io-watch}}

   "test/std/fs/watch_test.clj"
   {"watch:add"   {:source-ns 'std.fs.watch :source-name 'add-io-watch}
    "watch:list"  {:source-ns 'std.fs.watch :source-name 'list-io-watch}
    "watch:remove" {:source-ns 'std.fs.watch :source-name 'remove-io-watch}}

   "test/std/concurrent/pool_test.clj"
   {"info" {:source-ns 'std.concurrent.pool :source-name 'pool:info}}})

(def legacy-aggregate-aliases
  {'h 'std.lib
   'str 'std.string})

(defn lookup-target
  ([aggregate-ns token]
   (lookup-target nil aggregate-ns token))
  ([rel-path aggregate-ns token]
   (or (get-in file-target-overrides [rel-path token])
       (when (= aggregate-ns 'std.string)
         (get std-string-preferred token))
       (when (= aggregate-ns 'std.lib)
         (get std-lib-legacy-overrides token))
       (get-in aggregate-maps [aggregate-ns token]))))

(defn lookup-legacy-prefixed-token [rel-path token]
  (when-let [[lhs rhs] (and (str/includes? token "/")
                            (let [[a b] (str/split token #"/" 2)]
                              [a b]))]
    (when-let [aggregate-ns (case lhs
                              "h" 'std.lib
                              "str" 'std.string
                              nil)]
      (lookup-target rel-path aggregate-ns rhs))))

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

(defn token-aggregate-ns [lhs-sym full-alias-map aggregate-alias-map]
  (cond
    (contains? aggregate-alias-map lhs-sym)
    (get aggregate-alias-map lhs-sym)

    (contains? aggregate-maps lhs-sym)
    lhs-sym

    (contains? full-alias-map lhs-sym)
    nil

    :else
    (get legacy-aggregate-aliases lhs-sym)))

(defn qualify-token [token rel-path full-alias-map aggregate-alias-map refer-rewrites]
  (or
   (when-let [{:keys [source-ns source-name]} (get refer-rewrites token)]
     {:token (str source-ns "/" source-name)
      :source-ns source-ns})
   (when-let [{:keys [source-ns source-name]} (lookup-legacy-prefixed-token rel-path token)]
     {:token (str source-ns "/" source-name)
      :source-ns source-ns})
   (when-let [[lhs rhs] (and (str/includes? token "/")
                             (let [[a b] (str/split token #"/" 2)]
                               [a b]))]
     (let [lhs-sym (symbol lhs)
           aggregate-ns (token-aggregate-ns lhs-sym full-alias-map aggregate-alias-map)]
       (when aggregate-ns
         (when-let [{:keys [source-ns source-name]} (lookup-target rel-path aggregate-ns rhs)]
           {:token (str source-ns "/" source-name)
            :source-ns source-ns}))))))

(defn remaining-aggregate-token [token full-alias-map aggregate-alias-map]
  (cond
    (contains? aggregate-alias-map (symbol token))
    nil

    (lookup-legacy-prefixed-token nil token)
    true

    (and (str/includes? token "/")
         (let [[lhs rhs] (str/split token #"/" 2)
               lhs-sym (symbol lhs)
               aggregate-ns (token-aggregate-ns lhs-sym full-alias-map aggregate-alias-map)]
           (when aggregate-ns
             [aggregate-ns rhs])))
    true

    :else
    false))

(defn rewrite-body [text rel-path full-alias-map aggregate-alias-map refer-rewrites]
  (rewrite-code-tokens
   text
   (fn [token data]
     (if-let [{:keys [token source-ns]} (qualify-token token rel-path full-alias-map aggregate-alias-map refer-rewrites)]
       [token (update data :used-ns conj source-ns)]
       [token (cond-> data
                (remaining-aggregate-token token full-alias-map aggregate-alias-map)
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

(defn refer-rewrite-plan [rel-path aggregate-spec]
  (let [refer-spec (:refer aggregate-spec)]
    (if (or (nil? refer-spec) (= :all refer-spec))
      {:refer {}
       :rewrite {}
       :keep (if (= :all refer-spec) [:all] [])}
      (reduce
       (fn [acc sym]
         (if-let [{:keys [source-ns source-name]} (lookup-target rel-path (:ns aggregate-spec) (str sym))]
           (if (= (name sym) (name source-name))
             (update-in acc [:refer source-ns] (fnil conj #{}) sym)
             (assoc-in acc [:rewrite (str sym)]
                       {:source-ns source-ns
                        :source-name source-name}))
           (update acc :keep conj sym)))
       {:refer {}
        :rewrite {}
        :keep []}
       refer-spec))))

(defn aggregate-alias-map [aggregate-specs]
  (->> aggregate-specs
       (keep (fn [{:keys [ns as]}]
               (when as [as ns])))
       (into {})))

(defn keep-aggregate-spec? [spec remaining-tokens kept-refers]
  (or (= :all kept-refers)
      (seq kept-refers)
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

(defn hidden-file? [path]
  (let [name (.getName (io/file path))]
    (or (str/starts-with? name ".")
        (str/starts-with? name "#")
        (str/ends-with? name "#")
        (str/includes? name "~undo-tree~"))))

(defn process-file [file-path write? verbose?]
  (let [text (slurp file-path)]
    (if-not (str/includes? text "(ns ")
      (let [result {:path (rel-path file-path)
                    :changed? false
                    :unresolved-count 0
                    :unresolved []
                    :skipped? true}]
        (when verbose?
          (println (:path result) " skipped=no-ns"))
        result)
      (let [[ns-start ns-end] (ns-form-span text)
        rel-path (rel-path file-path)
        ns-text (.substring text ns-start ns-end)
        ns-form (tr/read-string {:read-cond :allow} ns-text)
        original-specs (normalized-require-specs ns-form)
        full-alias-map (ns-alias-map ns-form)
        aggregate-specs (vec (aggregate-specs original-specs))
        alias-map (aggregate-alias-map aggregate-specs)
        refer-plan (reduce (fn [acc spec]
                             (let [{:keys [refer rewrite]} (refer-rewrite-plan rel-path spec)]
                               {:refer (merge-with set/union (:refer acc) refer)
                                :rewrite (merge (:rewrite acc) rewrite)
                                :keep (concat (:keep acc) (:keep (refer-rewrite-plan rel-path spec)))}))
                           {:refer {}
                            :rewrite {}
                            :keep []}
                           aggregate-specs)
        body-text (.substring text ns-end)
        [new-body {:keys [used-ns remaining]}] (rewrite-body body-text rel-path full-alias-map alias-map (:rewrite refer-plan))
        concrete-specs (reduce add-spec
                               {}
                               (concat
                                (non-aggregate-specs original-specs)
                                (map (fn [ns-sym] {:ns ns-sym}) used-ns)
                                (for [[ns-sym syms] (:refer refer-plan)]
                                  {:ns ns-sym
                                   :refer (vec (sort syms))})))
        kept-aggregate (keep (fn [spec]
                               (let [kept-refers (if (= :all (:refer spec))
                                                   :all
                                                   (filter (set (:keep refer-plan))
                                                           (or (:refer spec) [])))]
                                 (when (keep-aggregate-spec? spec remaining kept-refers)
                                   (cond
                                     (= :all kept-refers)
                                     spec

                                     (empty? kept-refers)
                                     (dissoc spec :refer)

                                     :else
                                     (assoc spec :refer (vec kept-refers))))))
                             aggregate-specs)
        new-specs (->> kept-aggregate
                       (reduce add-spec concrete-specs)
                      vals
                      vec)
        new-ns (render-ns-form ns-form new-specs)
        new-text (str (.substring text 0 ns-start) new-ns new-body)
        changed? (not= text new-text)
        unresolved-count (count remaining)
        result {:path rel-path
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
        result))))

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
                          (filter #(str/ends-with? (.getName %) ".clj"))
                          (remove #(hidden-file? (.getPath %))))

                     (.isFile f)
                     (if (hidden-file? (.getPath f)) [] [f])

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
