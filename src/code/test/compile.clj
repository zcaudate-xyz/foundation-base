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

(def ^:dynamic *compile-meta* nil)

(def ^:dynamic *compile-desc* nil)

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

(defn strip
  "removes all checks in the function
 
   (strip '[(def a 1)
            (+ a 3)
            => 5])
   => '[(def a 1) (+ a 3)]"
  {:added "3.0"}
  ([body]
   (strip body []))
  ([[x y z & more :as arr] out]
   (cond (empty? arr)
         out

         (fact-skip? x)
         (recur (rest arr) out)

         (get +arrows+ y)
         (recur more (conj out x))

         (string? x)
         (recur (rest arr) out)

         :else
         (recur (rest arr)
                (conj out x)))))

(declare rewrite-nested-checks)

(defn group-and-rewrite [body parent-meta]
  (loop [forms body
         out []]
    (cond (empty? forms)
          out

          (and (> (count forms) 2)
               (= (nth forms 1) '=>))
          (let [input (nth forms 0)
                arrow (nth forms 1)
                output (nth forms 2)
                check `(code.test.base.process/check
                        (std.lib.result/result {:status :success :data ~input :form (quote ~input) :from :evaluate})
                        (std.lib.result/result {:status :success :data ~output :form (quote ~output) :from :evaluate})
                        {:line ~(or (-> arrow meta :line)
                                    (:line (meta input))
                                    (:line parent-meta))
                         :column ~(or (-> arrow meta :column)
                                      (:column (meta input))
                                      (:column parent-meta))
                         :path ~(str (or (:path parent-meta) *file*))})]
            (recur (drop 3 forms) (conj out check)))

          :else
          (recur (rest forms) (conj out (rewrite-nested-checks (first forms)))))))

(defn rewrite-nested-checks [form]
  (let [parent-meta (meta form)]
    (cond (and (list? form) (= 'let (first form)))
          (let [[_ bindings & body] form
                nbody (group-and-rewrite body parent-meta)]
            (with-meta `(let ~bindings ~@nbody) parent-meta))

          (and (list? form) (= 'do (first form)))
          (let [[_ & body] form
                nbody (group-and-rewrite body parent-meta)]
            (with-meta `(do ~@nbody) parent-meta))

          :else form)))

(defn split
  "creates a sequence of pairs from a loose sequence
   (split '[(def a 1)
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
   (split body []))
  ([[x y z & more :as arr] out]
   (let [meta-fn (fn []
                   (cond-> *compile-meta*
                     *compile-desc* (assoc :desc *compile-desc*)))]
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
           (binding [*compile-desc* x]
             (split (rest arr) out))

           :else
           (recur (rest arr)
                  (conj out {:type :form
                             :meta (merge (meta-fn) (meta x))
                             :form (rewrite-nested-checks x)}))))))

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
   (let [body  (walk/postwalk-replace (:replace meta) body)
         ns    (h/ns-sym)
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

(defn fact-prepare-derived
  "prepares fact for a derived form"
  {:added "3.0"}
  ([fsource title {:keys [id] :as meta}]
   (let [{:keys [code]} fsource
         {:keys [original]} code
         id (or id
                (symbol (str (:id fsource)
                             (str/truncate (rt/no-dots title) 30))))]
     (fact-prepare-meta id meta (:desc meta) original))))

(defn fact-prepare-link
  "prepares fact for a linked form"
  {:added "3.0"}
  ([fsource title {:keys [id line column type]}]
   (let [id  (or id
                 (symbol (str (:id fsource)
                              (str/truncate (rt/no-dots title) 30))))
         fpkg (-> fsource
                  (assoc :type type
                         :id id
                         :line line
                         :column column))]
     fpkg)))

(defn fact-thunk
  "creates a thunk form"
  {:added "3.0"}
  ([{:keys [full] :as fpkg}]
   (let [meta (into {} (dissoc fpkg :setup :teardown :let :use))]
     `(fn []
        (process/run-single (quote ~meta)
                            (quote ~full))))))

(defn create-fact
  "creates a fact given meta and body"
  {:added "3.0"}
  ([meta body]
   (let [{:keys [ns id global]} meta
         body  (binding [rewrite/*path* (:path meta)]
                 (mapv rewrite/rewrite-nested-checks body))
         bare  (strip body)
         full  (binding [*compile-meta* meta] (split body))
         code  {:declare  (snippet/fact-declare meta)
                :setup    (snippet/fact-setup meta)
                :teardown (snippet/fact-teardown meta)
                :check    (snippet/fact-wrap-check meta)
                :ceremony (snippet/fact-wrap-ceremony meta)
                :bindings (snippet/fact-wrap-bindings meta)
                :replace  (snippet/fact-wrap-replace meta)
                :bare bare
                :original body}
         wrap  {:check        (eval (:check code))
                :replace      (eval (:replace code))
                :ceremony     (eval (:ceremony code))
                :bindings     (eval (:bindings code))}
         function  {:slim     (eval (snippet/fact-slim (:bare code)))
                    :thunk    (eval (fact-thunk (assoc meta :full full)))
                    :declare  (eval (:declare code))
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
   (let [_ ((-> fpkg :code :declare eval))]
     (binding [rt/*eval-global* global]
       (let [meta (select-keys fpkg [:use :let :replace :setup :teardown])
             code {:setup    (snippet/fact-setup meta)
                   :teardown (snippet/fact-teardown meta)
                   :ceremony (snippet/fact-wrap-ceremony meta)
                   :bindings (snippet/fact-wrap-bindings meta)
                   :replace  (snippet/fact-wrap-replace meta)
                   :check    (snippet/fact-wrap-check meta)}
             wrap {:replace      (eval (:replace code))
                   :ceremony     (eval (:ceremony code))
                   :bindings     (eval (:bindings code))
                   :check        (eval (:check code))}
             function {:setup    (eval (:setup code))
                       :teardown (eval (:teardown code))}]
         (-> fpkg
             (update :code merge code)
             (update :function merge function)
             (assoc  :wrap wrap)))))))

(defn fact-eval
  "creates the forms in eval mode"
  {:added "3.0"}
  ([{:keys [ns id] :as fpkg}]
   `(binding [rt/*eval-fact* true]
      ~@(snippet/fact-let-defs fpkg)
      ((rt/get-fact (quote ~ns) (quote ~id))))))

(defmacro fact
  "top level macro for test definitions"
  {:added "3.0" :style/indent 1}
  ([desc & body]
   (let [{:keys [id eval] :as meta} (clojure.core/meta &form)
         [meta body] (fact-prepare-core desc body meta)
         _    (clojure.core/eval  (snippet/fact-let-declare meta))
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

(defn fact-eval-current
  "helper function for eval"
  {:added "3.0"}
  ([id meta]
   (if (and rt/*eval-mode*
            (not (false? (:eval meta))))
     `((rt/get-fact (quote ~id))))))

(defn fact:let-install
  "installer for `fact:let` macro"
  {:added "3.0"}
  ([args meta]
   (let [title (str "_let_" (or (:title meta)
                                (str/join "_" args)))
         fsource (rt/find-fact meta)
         {:keys [id] :as fpkg}  (fact-prepare-link fsource title (assoc meta :type :derived))
         bindings (snippet/replace-bindings (:let meta) args)
         fpkg (-> fpkg
                  (assoc :let args)
                  (assoc-in [:wrap :bindings]
                            (clojure.core/eval (snippet/fact-wrap-bindings {:use (:use fsource)
                                                                            :let bindings}))))
         _ (rt/set-fact id fpkg)]
     fpkg)))

(defmacro fact:let
  "runs a form that has binding substitutions"
  {:added "3.0" :style/indent 1}
  ([args]
   (let [meta (clojure.core/meta &form)
         {:keys [id]} (fact:let-install args meta)]
     (fact-eval-current id meta))))

(defn fact:derive-install
  "installer for `fact:derive` macro"
  {:added "3.0"}
  ([desc meta]
   (let [title (str "_derive_" (or (:title meta)
                                   (str/join "_" (concat (keys (:replace meta))
                                                         (:let meta)))))
         fsource (rt/find-fact meta)
         nlet (snippet/replace-bindings (:let fsource) (:let meta))
         nreplace (merge (:replace fsource) (:replace meta))
         [meta body]  (fact-prepare-derived fsource title
                                            (assoc meta
                                                   :type :derived
                                                   :desc desc
                                                   :use (:use fsource)
                                                   :let nlet
                                                   :replace nreplace))]
     (install-fact meta body))))

(defmacro fact:derive
  "runs a form derived from a previous test"
  {:added "3.0" :style/indent 1}
  ([desc]
   (let [meta (clojure.core/meta &form)
         {:keys [id]} (fact:derive-install desc meta)]
     (fact-eval-current id meta))))

(defn fact:table-install
  "installer for `fact:table` macro"
  {:added "3.0"}
  ([header inputs meta]
   (let [title (str "_table_" (or (:title meta)
                                  (str/join "_" header)))
         fsource (rt/find-fact meta)
         {:keys [id] :as fpkg}  (fact-prepare-link fsource title (assoc meta :type :table))
         fpkg (assoc fpkg :header header :inputs inputs)
         _  (rt/set-fact id fpkg)]
     fpkg)))

(defmacro fact:table
  "runs a form with tabular value substitutions"
  {:added "3.0" :style/indent 1}
  ([header & inputs]
   (let [meta (clojure.core/meta &form)
         {:keys [id]} (fact:table-install header inputs meta)]
     (fact-eval-current id meta))))

(defn fact:bench-install
  "installer for `fact:bench` macro"
  {:added "3.0"}
  ([params inputs meta]
   (let [title (str "_bench_" (:title meta))
         fsource (rt/find-fact meta)
         {:keys [id] :as fpkg}  (fact-prepare-link fsource title (assoc meta :type :bench))
         inputs (if (empty? inputs)
                  [[:default []]]
                  inputs)
         fpkg (assoc fpkg :params params :inputs inputs)
         _  (rt/set-fact id fpkg)]
     fpkg)))

(defmacro fact:bench
  "runs a small micro bench for the current fact"
  {:added "3.0" :style/indent 1}
  ([]
   (with-meta `(fact:bench {})
     (clojure.core/meta &form)))
  ([params & inputs]
   (let [meta (clojure.core/meta &form)
         {:keys [id]} (fact:bench-install params inputs meta)]
     (fact-eval-current id meta))))

(defn fact:check-install
  "installer for `fact:check` macro"
  {:added "3.0"}
  ([inputs meta]
   (let [title (str "_check_" (:title meta))
         fsource (rt/find-fact meta)
         {:keys [id] :as fpkg}  (fact-prepare-link fsource title (assoc meta :type :check))
         fpkg (assoc fpkg :inputs inputs)
         _  (rt/set-fact id fpkg)]
     fpkg)))

(defmacro fact:check
  "runs a check over a range of values"
  {:added "3.0" :style/indent 1}
  ([inputs]
   (let [meta (clojure.core/meta &form)
         {:keys [id]} (fact:check-install inputs meta)]
     (fact-eval-current id meta))))
