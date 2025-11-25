(ns code.test.manage
  (:require [code.test.base.runtime :as rt]
            [code.test.base.executive :as executive]
            [code.test.compile.snippet :as snippet]
            [code.project :as project]
            [std.lib :as h]))

(defn fact:global-map
  "sets and gets the global map
 
   (fact:global-map *ns* {})"
  {:added "3.0"}
  ([ns {:keys [import unimport remove component] :as m}]
   (let [unimported (-> unimport rt/get-global :component keys)
         component (or (h/->> (mapv (fn [x]
                                      (cond (vector? x)
                                            (-> (first x) rt/get-global :component
                                                (select-keys (second x)))
                                            :else
                                            (-> x rt/get-global :component)))
                                    import)
                              (apply merge)
                              (apply dissoc % (concat unimported remove))
                              (merge % component))
                       {})
         m (-> (dissoc m :unimport :import :remove)
               (assoc :component component))]
     (rt/update-global ns #(h/merge-nested % m)))))

(defn fact:global-fn
  "global getter and setter
 
   (fact:global-fn :get [])"
  {:added "3.0"}
  ([]
   (fact:global-fn :get))
  ([cmd & args]
   (let [ns (h/ns-sym)]
     (cond (map? cmd)
           (fact:global-map ns cmd)

           (symbol? cmd)
           (let [[sym cmd] [cmd (or (first args)
                                    :init)]
                 data (rt/get-global ns :component sym)]
             (case cmd
               :purge    (fact:global-fn :remove [:component sym])
               :create   (intern ns (with-meta sym {:dynamic true})
                                 (eval (:create data)))
               :prelim   (if (:prelim data)
                           (eval ((:prelim data) sym))
                           (eval sym))
               :setup    (if (:setup data)
                           (eval ((:setup data) sym))
                           (eval sym))
               :teardown (if (:teardown data)
                           (eval ((:teardown data) sym))
                           (eval sym))
               :init     (do (fact:global-fn sym :create)
                             (fact:global-fn sym :setup))))

           :else
           (case cmd
             :get      (apply rt/get-global ns args)
             :set      (rt/set-global ns (first args))
             :purge    (rt/set-global ns nil)
             :update   (rt/update-global ns (first args))
             :remove   (rt/update-global ns
                                         (fn [m]
                                           (h/dissoc-nested m (first args))))
             :prelim   (if rt/*eval-mode*
                         (eval (snippet/vecify (rt/get-global ns :prelim))))
             :setup    (if rt/*eval-mode*
                         (eval (snippet/vecify (rt/get-global ns :setup))))
             :teardown (if rt/*eval-mode*
                         (eval (snippet/vecify (rt/get-global ns :teardown))))
             :list     (keys (rt/get-global ns :component))
             (eval (snippet/vecify (rt/get-global ns cmd))))))))

(defmacro fact:global
  "fact global getter and setter
 
   (fact:global)"
  {:added "3.0"}
  ([]
   `(fact:global-fn))
  ([cmd & args]
   `(fact:global-fn (quote ~cmd)
                    ~@(map #(list `quote %) args))))

(defn fact:ns-load
  "loads a test namespace"
  {:added "3.0"}
  ([ns]
   (project/in-context
    (executive/load-namespace ns))))

(defn fact:ns-unload
  "unloads a test namespace"
  {:added "3.0"}
  ([ns & more]
   (project/in-context
    (executive/unload-namespace ns))))

(defn fact:ns-alias
  "imports all aliases into current namespace"
  {:added "3.0"}
  ([ns]
   (let [aliases (->> (ns-aliases ns)
                      (mapv (fn [[sym ^clojure.lang.Namespace ns]]
                              [(.getName ns) :as sym])))]
     (do (apply require aliases)
         aliases))))

(defn fact:ns-unalias
  "removes all aliases from current namespace"
  {:added "3.0"}
  ([ns]
   (let [aliases (->> (ns-aliases ns)
                      (mapv (fn [[sym ^clojure.lang.Namespace ns]]
                              [(.getName ns) :as sym])))]
     (doseq [alias aliases]
       (ns-unalias (h/ns-sym) (last alias)))
     aliases)))

(defn fact:ns-intern
  "imports all interns into current namespace"
  {:added "3.0"}
  ([ns]
   (mapv (partial apply h/intern-var (h/ns-sym))
         (ns-interns ns))))

(defn fact:ns-unintern
  "removes all interns into current namespace"
  {:added "3.0"}
  ([ns]
   (let [isyms (sort (keys (ns-interns ns)))]
     (doseq [sym isyms]
       (ns-unmap (h/ns-sym) sym))
     isyms)))

(defn fact:ns-import
  "loads, imports and aliases current namespace"
  {:added "3.0"}
  ([ns]
   (let [sym (if (vector? ns)
               (first ns)
               ns)]
     (rt/add-link sym)
     (fact:ns-load sym)
     (fact:ns-alias sym)
     (fact:global-fn {:import [ns]}))))

(defn fact:ns-unimport
  "unload, unimports and unalias current namespace"
  {:added "3.0"}
  ([ns]
   (fact:global-fn {:unimport [ns]})
   (fact:ns-unalias ns)
   (fact:ns-unload ns)
   (rt/remove-link ns)))

(defn fact:ns-fn
  "fact ns getter and setter"
  {:added "3.0"}
  ([forms]
   (mapv (fn [[tag & inputs]]
           (let [fact-fn (case tag
                           :link     rt/add-link
                           :unlink   rt/remove-link
                           :load     (fn [ns]
                                       (rt/add-link ns)
                                       (fact:ns-load ns))
                           :unload   (fn [ns]
                                       (fact:ns-unload)
                                       (rt/remove-link ns))
                           :alias    fact:ns-alias
                           :unalias  fact:ns-unalias
                           :intern   fact:ns-intern
                           :unintern fact:ns-unintern
                           :import   fact:ns-import
                           :unimport fact:ns-unimport
                           :clone    (fn [ns]
                                       (fact:ns-import ns)
                                       (fact:ns-intern ns))
                           :unclone  (fn [ns]
                                       (fact:ns-unintern ns)
                                       (fact:ns-unimport ns))
                           :global   fact:global-fn)]
             (mapv fact-fn inputs)))
         forms)))

(defmacro fact:ns
  "fact ns macro"
  {:added "3.0"}
  ([& forms]
   `(fact:ns-fn (quote ~forms))))
