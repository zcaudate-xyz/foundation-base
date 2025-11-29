(ns code.test.compile
  (:require [std.lib.walk :as walk]
            [code.project :as project]
            [std.string :as str]
            [code.test.base.process :as process]
            [code.test.base.runtime :as rt]
            [code.test.compile.snippet :as snippet]
            [code.test.compile.types :as types]
            [code.test.compile.rewrite :as rewrite]
            [std.math :as math]
            [std.lib :as h]))

(def => '=>)

(def +arrows+ '{=> :test-equal})

(defn arrow?
  "checks if form is an arrow"
  {:added "3.0"}
  ([obj]
   (= obj '=>)))

(def fact-allowed?
  '#{fact:get
     fact:list
     fact:all
     fact:missing
     fact:compile})

(defn fact-skip?
  "checks if form should be skipped
 
   (fact-skip? '(fact:component))
   => true"
  {:added "3.0"}
  ([x]
   (and (list? x)
        (let [head (first x)]
          (and (symbol? head)
               (not (fact-allowed? head))
               (or (= 'fact head)
                   (.startsWith (name head)
                                "fact:")))))))

(defn rewrite-top-level
  "creates a sequence of pairs from a loose sequence
   (rewrite-top-level '[(def a 1)
            (+ a 3)
            => 5])
   (contains-in '[{:type :form,
                   :meta {:line 8, :column 12},
                   :form '(def a 1)}
                 {:type :test-equal,
                   :meta {:line 9, :column 12},
                   :input  {:form '(+ a 3)},
                   :output {:form 5}}])"
  {:added "3.0"}
  ([body]
   (rewrite-top-level body []))
  ([[x y z & more :as arr] out]
   (let [meta-fn (fn []
                   (cond-> types/*compile-meta*
                     types/*compile-desc* (assoc :desc types/*compile-desc*)))]
     (cond (empty? arr)
           out

           (fact-skip? x)
           (recur (rest arr) out)

           (get +arrows+ y)
           (recur more
                  (conj out {:type (get +arrows+ y)
                             :meta (merge (meta-fn)
                                          (or (meta x) (meta y) (meta z)))
                             :input  {:form x}
                             :output {:form z}}))

           (string? x)
           (binding [types/*compile-desc* x]
             (rewrite-top-level (rest arr) out))

           :else
           (recur (rest arr)
                  (conj out {:type :form
                             :meta (merge (meta-fn) (meta x))
                             :original x
                             :form (rewrite/rewrite-nested-checks x)}))))))

(defn fact-id
  "creates an id from fact data
 
   (fact-id {} \"hello there\")
   => 'test-hello-there"
  {:added "3.0"}
  ([{:keys [id refer] :as m} desc]
   (let [desc-fn (fn [s] (str/spear-case
                          (munge
                           (rt/no-dots (str/truncate s 100)))))
         id    (or (rt/fact-id m)
                   (if desc  (symbol (str "test-" (desc-fn desc))))
                   (throw (ex-info "Description required" {:require [:refer :id :desc]})))]
     id)))

(defn fact-prepare-meta
  "parses and converts fact to symbols
 
   (fact-prepare-meta 'test-hello
                      {}
                      \"hello\"
                     '(1 => 1))"
  {:added "3.0"}
  ([id meta desc body]
   (let [ns    (h/ns-sym)
         path  (h/suppress (project/code-path ns true))
         meta  (-> (dissoc meta :eval)
                   (assoc :path (str path) :desc desc :ns ns :id id))]
     [meta body])))

(defn fact-prepare-core
  "prepares fact for a core form"
  {:added "3.0"}
  ([desc? body meta]
   (let [{:keys [replace]} meta
         [desc body] (if (string? desc?)
                       [desc? body]
                       [nil (cons desc? body)])
         id    (fact-id meta desc)]
     (fact-prepare-meta id meta desc body))))

(defn fact-thunk
  "creates a thunk form"
  {:added "3.0"}
  ([{:keys [full] :as fpkg}]
   (let [meta (into {} (dissoc fpkg :setup :teardown :let :use))]
     `(fn []
        (process/run-check (quote ~meta)
                           (quote ~full))))))

(defn create-fact
  "creates a fact given meta and body"
  {:added "3.0"}
  ([meta body]
   (let [{:keys [ns id global]} meta
         full  (binding [types/*compile-meta* meta
                         types/*file-path*  (:path meta)]
                 (rewrite-top-level body))
         code  {:setup    (snippet/fact-setup meta)
                :teardown (snippet/fact-teardown meta)
                :check    (snippet/fact-wrap-check meta)
                :ceremony (snippet/fact-wrap-ceremony meta)}
         wrap  {:check        (eval (:check code))
                :ceremony     (eval (:ceremony code))}
         function  {:thunk    (eval (fact-thunk (assoc meta :full full)))
                    :setup    (eval (:setup code))
                    :teardown (eval (:teardown code))}
         fpkg  (-> (merge {:type :core} meta)
                   (assoc :code code
                          :full full
                          :wrap wrap
                          :function function)
                   (types/map->Fact))]
     fpkg)))

(defn install-fact
  "installs the current fact"
  {:added "3.0"}
  ([meta body]
   (let [{:keys [ns id] :as fpkg} (create-fact meta body)
         _ (rt/set-fact ns id fpkg)]
     fpkg)))

(defn fact:compile
  "recompiles fact with a different global"
  {:added "3.0"}
  ([fpkg]
   (fact:compile fpkg nil))
  ([fpkg global]
   (binding [rt/*eval-global* global]
     (let [meta (select-keys fpkg [:use :setup :teardown])
           code {:setup    (snippet/fact-setup meta)
                 :teardown (snippet/fact-teardown meta)
                 :ceremony (snippet/fact-wrap-ceremony meta)
                 :check    (snippet/fact-wrap-check meta)}
           wrap {:ceremony     (eval (:ceremony code))
                 :bindings     (eval (:bindings code))
                 :check        (eval (:check code))}
           function {:setup    (eval (:setup code))
                     :teardown (eval (:teardown code))}]
       (-> fpkg
           (update :code merge code)
           (update :function merge function)
           (assoc  :wrap wrap))))))

(defn fact-eval
  "creates the forms in eval mode"
  {:added "3.0"}
  ([{:keys [ns id] :as fpkg}]
   `(binding [rt/*eval-fact* true]
      (let [fact# (or (rt/get-fact (quote ~ns) (quote ~id))
                      (get-in @(.getRawRoot #'rt/*registry*) [(quote ~ns) :facts (quote ~id)]))]
        (when (and fact# (nil? (rt/get-fact (quote ~ns) (quote ~id))))
          (rt/set-fact (quote ~ns) (quote ~id) fact#))
        ((rt/get-fact (quote ~ns) (quote ~id)))))))

(defmacro fact
  "top level macro for test definitions"
  {:added "3.0" :style/indent 1}
  ([desc & body]
   (let [{:keys [id eval] :as meta} (clojure.core/meta &form)
         [meta body] (fact-prepare-core desc body meta)
         fpkg (install-fact meta body)]
     (if id
       (intern (:ns fpkg) id fpkg))
     (if (and rt/*eval-mode*
              (not (false? eval)))
       (fact-eval fpkg)))))

(defmacro fact:template
  "adds a template to the file"
  {:added "3.0" :style/indent 1}
  ([desc & body]
   `(binding [rt/*eval-mode* false]
      ~(with-meta
         `(fact ~desc ~@body)
         (assoc (meta &form) :eval false)))))

(defn fact:purge
  "purges all facts in namespace"
  {:added "3.0" :style/indent 1}
  ([]
   (rt/purge-facts)))

(defn fact:list
  "lists all facts in namespace"
  {:added "3.0" :style/indent 1}
  ([]
   (rt/list-facts)))

(defmacro fact:all
  "returns all facts in namespace"
  {:added "3.0" :style/indent 1}
  ([]
   `(rt/all-facts))
  ([ns]
   `(rt/all-facts (quote ~ns))))

(defn fact:rerun
  "reruns all facts along with filter and compile options"
  {:added "3.0"}
  ([facts]
   (fact:rerun facts {}))
  ([facts filters]
   (fact:rerun facts filters nil))
  ([facts filters global]
   (let [results (->> facts
                      (vals)
                      (sort-by :line)
                      (filter :refer)
                      (filter (fn [m]
                                (every? (fn [[k v]]
                                          (let [f (if (fn? v) v (partial = v))]
                                            (f (get m k))))
                                        filters)))
                      (mapv #(fact:compile % (merge (rt/get-global) global)))
                      (mapv #(%)))]
     [(every? true? results) (count results)])))

(defn fact:missing
  "returns all missing facts for a given namespace"
  {:added "3.0" :style/indent 1}
  ([]
   (let [ns (-> (str (h/ns-sym))
                (str/replace "-test$" "")
                (symbol))]
     (fact:missing ns)))
  ([ns]
   (->> (h/difference
         (set (map #(symbol (name ns) (name %))
                   (keys (ns-interns ns))))
         (set (map :refer (vals (rt/all-facts)))))
        (map (comp symbol name))
        sort)))

(defmacro fact:get
  "gets elements of the current fact"
  {:added "3.0" :style/indent 1}
  ([]
   (let [m    (meta &form)
         {:keys [id]} (rt/find-fact m)]
     (rt/get-fact (quote ~id))))
  ([id]
   `(fact:get ~(h/ns-sym) ~id))
  ([ns id]
   (if (symbol? id)
     `(rt/get-fact (quote ~ns) (quote ~id))
     `(let [fpkg# (rt/find-fact (quote ~ns) {:source (quote ~id)})]
        (rt/get-fact (quote ~ns) (:id fpkg#))))))

(defmacro fact:exec
  "runs main hook for fact form"
  {:added "3.0" :style/indent 1}
  ([]
   `((fact:get)))
  ([id]
   `((fact:get ~id)))
  ([ns id]
   `((fact:get ~ns ~id))))

(defmacro fact:setup
  "runs setup hook for current fact"
  {:added "3.0" :style/indent 1}
  ([]
   `(rt/run-op ~(meta &form) :setup)))

(defmacro fact:setup?
  "checks if setup hook has been ran"
  {:added "3.0" :style/indent 1}
  ([]
   `(rt/run-op ~(meta &form) :setup?)))

(defmacro fact:teardown
  "runs teardown hook for current fact"
  {:added "3.0" :style/indent 1}
  ([]
   `(rt/run-op (meta &form) :teardown)))

(defmacro fact:remove
  "removes the current fact"
  {:added "3.0" :style/indent 1}
  ([]
   `(rt/run-op (meta &form) :remove)))

(defmacro fact:symbol
  "gets the current fact symbol"
  {:added "3.0" :style/indent 1}
  ([]
   `(rt/run-op (meta &form) :symbol)))
