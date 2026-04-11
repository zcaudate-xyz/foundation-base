(ns std.lang.typed.xtalk-parse
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [std.lang.typed.xtalk-common :as types]))

(defn read-forms
  [file-path]
  (with-open [r (java.io.PushbackReader. (io/reader file-path))]
    (let [eof (Object.)]
      (loop [forms []]
        (let [form (read {:eof eof :read-cond :allow} r)]
          (if (identical? form eof)
            forms
            (recur (conj forms form))))))))

(defn ns-form?
  [form]
  (and (seq? form)
       (= 'ns (first form))))

(defn defspec?
  [form]
  (and (seq? form)
       (= "defspec.xt" (name (first form)))))

(defn defn?
  [form]
  (and (seq? form)
        (#{"defn" "defn-" "defn.xt" "defgen.xt"} (name (first form)))))

(defn defmacro?
  [form]
  (and (seq? form)
       (= "defmacro.xt" (name (first form)))))

(defn defvalue?
  [form]
  (and (seq? form)
       (= "def.xt" (name (first form)))))

(defn parse-ns-name
  [forms]
  (some (fn [form]
          (when (ns-form? form)
            (second form)))
        forms))

(defn extract-aliases
  [require-forms]
  (->> require-forms
       (reduce (fn [acc form]
                 (if (vector? form)
                   (let [ns-sym (first form)
                         pairs (partition 2 (rest form))
                         alias (some (fn [[k v]]
                                       (when (= :as k)
                                         v))
                                     pairs)]
                     (cond-> acc
                       alias (assoc alias ns-sym)))
                   acc))
               {})))

(defn extract-ns-aliases
  [ns-form]
  (->> (rest ns-form)
       (filter seq?)
       (filter #(= :require (first %)))
       (mapcat rest)
       extract-aliases))

(defn script-form?
  [form]
  (and (seq? form)
       (= "script" (name (first form)))))

(defn extract-script-aliases
  [forms]
  (->> forms
       (filter script-form?)
       (map #(nth % 2 nil))
       (filter map?)
       (mapcat :require)
       extract-aliases))

(defn arg-from-inline-form
  [form ctx]
  (when (and (seq? form)
             (= 2 (count form))
             (symbol? (second form)))
    (let [[type-form sym] form]
      (when (or (keyword? type-form)
                (vector? type-form)
                (and (symbol? type-form)
                     (types/likely-type-symbol? type-form)))
        (types/make-arg sym
                        (types/normalize-type type-form ctx)
                        [])))))

(defn arg-declared-type
  [sym ctx]
  (when-let [ret (get (meta sym) :-)]
    (types/normalize-return-meta ret ctx)))

(defn binding-symbols
  [form]
  (cond
    (symbol? form)
    (if (#{'& 'as} form)
      []
      [form])

    (vector? form)
    (mapcat binding-symbols form)

    (map? form)
    (mapcat binding-symbols (concat (keys form) (vals form)))

    (seq? form)
    (mapcat binding-symbols form)

    :else
    []))

(defn parse-fn-inputs
  [args-form ctx]
  (loop [out []
         pending nil
         [item & more] args-form]
    (cond
      (nil? item)
      (if pending
        (throw (ex-info "Dangling type token in function args"
                        {:pending pending
                         :args args-form}))
        out)

      pending
      (if (symbol? item)
        (recur (conj out (types/make-arg item pending []))
               nil
               more)
        (throw (ex-info "Expected symbol after typed arg token"
                         {:pending pending
                          :item item
                          :args args-form})))

       (= '& item)
       (let [rest-target (first more)
             syms (binding-symbols rest-target)]
         (recur (into out
                      (map #(types/make-arg % types/+unknown-type+ []))
                      syms)
                nil
                nil))

       (arg-from-inline-form item ctx)
       (recur (conj out (arg-from-inline-form item ctx))
              nil
              more)

      (symbol? item)
      (let [declared (arg-declared-type item ctx)]
        (if (and more
                 (types/likely-type-symbol? item)
                 (symbol? (first more)))
          (recur out
                 (types/normalize-type item ctx)
                 more)
          (recur (conj out (types/make-arg item
                                           (or declared types/+unknown-type+)
                                           []))
                 nil
                 more)))

       (or (keyword? item)
           (and (vector? item)
                (symbol? (first more))))
       (recur out
              (types/normalize-type item ctx)
              more)

       (or (vector? item)
           (map? item))
       (recur (into out
                    (map #(types/make-arg % types/+unknown-type+ []))
                    (binding-symbols item))
              nil
              more)

       :else
       (throw (ex-info "Unsupported function arg form"
                       {:item item
                       :args args-form})))))

(defn parse-spec-decl
  [ns-sym spec-sym type-form spec-meta aliases]
  (let [ctx {:ns ns-sym
             :aliases aliases}]
    (types/make-spec-def ns-sym spec-sym
                         (types/normalize-type type-form ctx)
                         spec-meta)))

(defn parse-decl-preamble
  [items form-sym]
  (let [[docstring items] (if (string? (first items))
                            [(first items) (rest items)]
                            [nil items])
        [attr-map items] (if (and (map? (first items))
                                  (next items))
                            [(first items) (rest items)]
                            [nil items])]
    {:docstring docstring
     :attr-map attr-map
     :items items
     :meta (cond-> (merge attr-map (meta form-sym))
             docstring (assoc :docstring docstring))}))

(defn parse-defspec
  [form ns-sym aliases]
  (let [[_ spec-sym & more] form
        {:keys [meta items]} (parse-decl-preamble more spec-sym)
        type-form (first items)]
    (parse-spec-decl ns-sym spec-sym type-form meta aliases)))

(defn parse-callable-items
  [items]
  (cond
    (vector? (first items))
    [(first items) (rest items)]

    (and (seq? (first items))
         (vector? (ffirst items)))
    [(ffirst items) (rest (first items))]

     :else
     [(first items) (rest items)]))

(defn multi-callable-items?
  [items]
  (and (next items)
       (every? (fn [item]
                 (and (seq? item)
                      (vector? (first item))))
               items)))

(defn parse-defn
  [form ns-sym aliases]
  (let [[def-op fn-sym & more] form
        {:keys [meta items]} (parse-decl-preamble more fn-sym)
        [args-form body] (parse-callable-items items)
        ctx {:ns ns-sym
              :aliases aliases}
        output (if-let [ret (get meta :-)]
                  (types/normalize-return-meta ret ctx)
                  types/+unknown-type+)]
    (types/make-fn-def ns-sym fn-sym
                        (parse-fn-inputs args-form ctx)
                        output
                        (assoc meta
                               :aliases aliases
                               :generator (= "defgen.xt" (name def-op)))
                        body
                        nil)))

(defn parse-defmacro
  [form ns-sym aliases]
  (let [[_ macro-sym & more] form
         {:keys [meta items]} (parse-decl-preamble more macro-sym)
         [args-form body] (parse-callable-items items)
         raw-body (if (multi-callable-items? items)
                    items
                    body)
         ctx {:ns ns-sym
              :aliases aliases}]
     (types/make-fn-def ns-sym macro-sym
                        (parse-fn-inputs args-form ctx)
                        types/+unknown-type+
                        (assoc meta :aliases aliases
                                    :macro true)
                        raw-body
                        nil)))

(defn parse-defvalue
  [form ns-sym aliases]
  (let [[_ value-sym & more] form
        {:keys [meta items]} (parse-decl-preamble more value-sym)
        ctx {:ns ns-sym
              :aliases aliases}]
    (types/make-value-def ns-sym value-sym
                          (if-let [ret (get meta :-)]
                            (types/normalize-return-meta ret ctx)
                            types/+unknown-type+)
                          (assoc meta :aliases aliases
                                     :def true)
                          (first items)
                          nil)))

(defn merge-spec-inputs
  [inputs spec-inputs]
  (if (= (count inputs) (count spec-inputs))
    (mapv (fn [arg spec-type]
            (if (= (:type arg) types/+unknown-type+)
              (assoc arg :type spec-type)
              arg))
          inputs spec-inputs)
    inputs))

(defn attach-function-spec
  [fn-def spec]
  (if (and spec
           (= :fn (get-in spec [:type :kind])))
    (let [spec-type (:type spec)]
      (assoc fn-def
             :inputs (merge-spec-inputs (:inputs fn-def)
                                        (:inputs spec-type))
             :output (if (= (:output fn-def) types/+unknown-type+)
                       (:output spec-type)
                       (:output fn-def))
              :spec spec))
    fn-def))

(defn attach-value-spec
  [value-def spec]
  (if (and spec
           (not= :fn (get-in spec [:type :kind])))
    (assoc value-def
           :type (if (= (:type value-def) types/+unknown-type+)
                   (:type spec)
                   (:type value-def))
           :spec spec)
    value-def))

(defn spec-map-by-kind
  [specs pred]
  (into {}
        (keep (fn [spec]
                (when (pred spec)
                  [(types/type-key (some-> spec :ns symbol) (:name spec))
                   spec])))
        specs))

(defn attach-specs
  [{:keys [specs functions macros values] :as analysis}]
  (let [fn-specs (spec-map-by-kind specs #(= :fn (get-in % [:type :kind])))
        value-specs (spec-map-by-kind specs #(not= :fn (get-in % [:type :kind])))]
    (assoc analysis
           :functions
           (mapv (fn [fn-def]
                   (attach-function-spec fn-def
                                         (get fn-specs
                                              (types/type-key (some-> fn-def :ns symbol)
                                                              (:name fn-def)))))
                 functions)
           :macros
           (mapv (fn [macro-def]
                   (attach-function-spec macro-def
                                         (get fn-specs
                                              (types/type-key (some-> macro-def :ns symbol)
                                                              (:name macro-def)))))
                 macros)
           :values
           (mapv (fn [value-def]
                   (attach-value-spec value-def
                                      (get value-specs
                                           (types/type-key (some-> value-def :ns symbol)
                                                           (:name value-def)))))
                 values))))

(defn analyze-file-raw
  [file-path]
  (let [forms (read-forms file-path)
        ns-sym (parse-ns-name forms)
        aliases (merge (or (some->> forms
                                    (some #(when (ns-form? %) %))
                                    extract-ns-aliases)
                           {})
                       (extract-script-aliases forms))]
    (-> (reduce (fn [acc form]
                   (cond
                     (defspec? form) (update acc :specs conj (parse-defspec form ns-sym aliases))
                     (defn? form) (update acc :functions conj (parse-defn form ns-sym aliases))
                     (defmacro? form) (update acc :macros conj (parse-defmacro form ns-sym aliases))
                     (defvalue? form) (update acc :values conj (parse-defvalue form ns-sym aliases))
                     :else acc))
                  {:ns ns-sym
                   :aliases aliases
                   :specs []
                   :functions []
                   :macros []
                   :values []}
                 forms))))

(defn analyze-file
  [file-path]
  (-> file-path
      analyze-file-raw
      attach-specs))

(defn register-types!
  [{:keys [specs functions macros values] :as analysis}]
  (doseq [spec specs]
    (types/register-spec! (types/type-key (some-> spec :ns symbol)
                                          (:name spec))
                          spec))
  (doseq [fn-def functions]
    (types/register-function! (types/type-key (some-> fn-def :ns symbol)
                                              (:name fn-def))
                              fn-def))
  (doseq [macro-def macros]
    (types/register-macro! (types/type-key (some-> macro-def :ns symbol)
                                           (:name macro-def))
                           macro-def))
  (doseq [value-def values]
    (types/register-value! (types/type-key (some-> value-def :ns symbol)
                                           (:name value-def))
                           value-def))
  analysis)

(defn analyze-namespace-raw
  [ns-sym]
  (let [rel-path (-> (str ns-sym)
                     (str/replace #"\." "/")
                     (str/replace #"-" "_")
                     (str ".clj"))
        source-roots ["src/"
                      "test/"
                      "clojure/src/"
                      "backend/src/"
                      "backend/clojure/src/"
                      "main/src/"
                      "main/clojure/src/"]
        target-file (some (fn [root]
                            (let [path (str root rel-path)]
                               (when (.exists (io/file path))
                                 path)))
                           source-roots)]
     (if target-file
       (analyze-file-raw target-file)
       (throw (ex-info "Namespace source file not found"
                       {:ns ns-sym
                        :searched source-roots})))))

(defn analyze-namespace
  [ns-sym]
  (-> ns-sym
      analyze-namespace-raw
      attach-specs))
