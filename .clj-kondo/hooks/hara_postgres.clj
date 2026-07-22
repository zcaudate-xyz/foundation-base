(ns clj-kondo.hooks.hara-postgres
    "clj-kondo hooks for Hara's generated language macros and PostgreSQL DSL.

   l/script- interns grammar macros at runtime. clj-kondo cannot execute that
   setup, so this hook emits placeholders for generated symbols. defsel.pg and
   defret.pg are intentionally absent: postgres.core owns those forms."
    (:require [clj-kondo.hooks-api :as api]))

(def ^:private +postgres-ops+
     '[defrun defn defn- defglobal defgen deftemp defclass defabstract def
       defconst deftype defenum defindex defpolicy defpublication
       defsubscription deftrigger defpartition])

(def ^:private +common-ops+
     '[defrun defn defn- defglobal defgen deftemp defclass defabstract def])

(def ^:private +language-highlights+
     {:postgres '[return break do:assert]})

(def ^:private +language-suffix+
     {:postgres "pg"
      :javascript "js"
      :typescript "ts"
      :xtalk "xt"
      :dart "dt"
      :julia "jl"
      :python "py"
      :ruby "rb"
      :rust "rs"
      :golang "go"
      :c "c"
      :cpp "cpp"
      :lua "lua"
      :sql "sql"
      :oracle "oracle"})

(defn- language-suffix [lang]
       (or (get +language-suffix+ lang)
           (some-> lang name)))

(defn- generated-symbols [lang]
       (let [suffix (language-suffix lang)
             ops (if (= :postgres lang) +postgres-ops+ +common-ops+)]
            (->> (concat
                  (for [prefix '(def$. defmacro. !. defptr.)]
                       (symbol (str prefix suffix)))
                  (for [op ops]
                       (symbol (str op "." suffix))))
                 distinct)))

(defn- placeholder-node [sym]
       (api/list-node
        [(api/token-node 'clojure.core/def)
         (api/token-node sym)
         (api/token-node 'clojure.core/identity)]))

(defn- require-node [libspec]
       (let [ns-sym (if (vector? libspec) (first libspec) libspec)
             spec-node (if (vector? libspec)
                           (api/coerce libspec)
                           (api/token-node ns-sym))]
            (api/list-node
             [(api/token-node 'clojure.core/require)
              (api/list-node [(api/token-node 'quote) spec-node])])))

(defn- script-lang [node]
       (let [lang-form (some-> (nth (:children node) 1 nil) api/sexpr)]
            (if (vector? lang-form)
                (second lang-form)
                lang-form)))

(defn script
      "Model hara.lang/script and hara.lang/script- for clj-kondo."
      [{:keys [node]}]
      (let [children (:children node)
            lang (script-lang node)
            config-map (some-> (nth children 2 nil) api/sexpr)
            requires (if (map? config-map) (:require config-map) [])]
           {:node
            (api/list-node
             (cons (api/token-node 'do)
                   (concat (map require-node requires)
                           (map placeholder-node (generated-symbols lang))
                           (map placeholder-node (get +language-highlights+ lang)))))}))

(defn form-head [form]
      (when (seq? form) (str (first form))))

(defn all-nodes [form]
      (tree-seq coll? seq form))

(defn forms-headed [form head]
      (filter #(= head (form-head %)) (all-nodes form)))

(defn symbol-nodes [form]
      (filter symbol? (all-nodes form)))

(defn binding-name [form]
      (some-> (last (symbol-nodes form)) name))

(defn allowed-input-name? [name]
      (or (= name "m")
          (= name "_")
          (.startsWith name "i-")
          (.startsWith name "o-")))

(defn allowed-local-name? [name]
      (or (= name "m")
          (= name "_")
          (.startsWith name "v-")
          (.startsWith name "o-")))

(defn let-bindings [let-form]
      (let [bindings (second let-form)]
           (when (vector? bindings)
                 (map first (partition 2 bindings)))))

(defn report! [node rule message]
      (api/reg-finding!
       (merge (meta node)
              {:type rule
               :level :warning
               :message message})))

(defn function-shape [node]
      (let [children (rest (:children node))
            metadata (some-> (first children) api/sexpr)
            children (if (or (api/map-node? (first children))
                             (api/keyword-node? (first children)))
                         (rest children)
                         children)
            name-node (first children)
            remainder (rest children)
            args-index (first (keep-indexed (fn [i child]
                                                (when (api/vector-node? child) i))
                                            remainder))]
           (when (and name-node args-index)
                 {:name-node name-node
                  :args-node (nth (vec remainder) args-index)
                  :body-nodes (drop (inc args-index) remainder)
                  :sql? (or (= :sql (:%% metadata))
                            (= :sql (:%% (meta name-node))))})))

(defn- normalized-args-node [args-node]
       (api/vector-node
        (->> (:children args-node)
             (keep (fn [child]
                       (let [value (api/sexpr child)]
                            (when (symbol? value)
                                  (api/token-node value))))))))

(defn lint-function! [{:keys [name-node args-node body-nodes sql?]}]
      (let [name (api/sexpr name-node)
            args (api/sexpr args-node)
            body (map api/sexpr body-nodes)
            lets (vec (mapcat #(forms-headed % "let") body))
            top-level-lets (filter #(some #{%} body) lets)
            top-let-count (count top-level-lets)
            input-names (keep #(when (symbol? %) (name %)) (symbol-nodes args))
            local-bindings (mapcat let-bindings top-level-lets)
            local-names (keep binding-name local-bindings)
            direct-returns (filter (fn [form]
                                       (and (= "return" (form-head form))
                                            (seq? (second form))))
                                   (all-nodes (cons 'do body)))]
           (when (and (not (or sql?
                               (and (zero? top-let-count)
                                    (some #(and (seq? %)
                                                (.startsWith (str (first %)) "pg/t:"))
                                          body))))
                      (not= 1 top-let-count))
                 (report! name-node :hara.postgres/one-let
                          (str name " should contain exactly one top-level let")))
           (when (> (count lets) 1)
                 (report! name-node :hara.postgres/nested-let
                          (str name " contains nested or repeated let forms")))
           (doseq [input input-names]
                  (when-not (allowed-input-name? input)
                            (report! name-node :hara.postgres/input-prefix
                                     (str "input binding " input " should use i-* or m"))))
           (doseq [local local-names]
                  (when-not (allowed-local-name? local)
                            (report! name-node :hara.postgres/local-prefix
                                     (str "local binding " local " should use v-* or o-*"))))
           (when (seq direct-returns)
                 (report! name-node :hara.postgres/return-bound
                          (str name " returns an expression directly; bind it before return")))))

(defn defn-pg
      "Expand and lint defn.pg/defrun.pg calls."
      [{:keys [node]}]
      (if-let [{:keys [name-node args-node] :as shape} (function-shape node)]
              (do
               (lint-function! shape)
               {:node (api/list-node
                       [(api/token-node 'clojure.core/defn)
                        name-node
                        (normalized-args-node args-node)
                        (api/list-node [(api/token-node 'clojure.core/identity)
                                        (normalized-args-node args-node)])])})
              {:node (api/list-node [(api/token-node 'do)])}))
