(ns std.lang.model.spec-xtalk.typed-parse
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [std.lang.model.spec-xtalk.typed-common :as types]))

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
       (#{"defn" "defn-" "defn.xt"} (name (first form)))))

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
          (vector? item))
      (recur out
             (types/normalize-type item ctx)
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

(defn parse-defspec
  [form ns-sym aliases]
  (let [[_ spec-sym & more] form
        [docstring more] (if (string? (first more))
                           [(first more) (rest more)]
                           [nil more])
        [attr-map more] (if (map? (first more))
                          [(first more) (rest more)]
                          [nil more])
        spec-meta (cond-> (merge (meta spec-sym) attr-map)
                    docstring (assoc :docstring docstring))
        type-form (first more)]
    (parse-spec-decl ns-sym spec-sym type-form spec-meta aliases)))

(defn parse-defn
  [form ns-sym aliases]
  (let [[_ fn-sym & more] form
        [docstring more] (if (string? (first more))
                           [(first more) (rest more)]
                           [nil more])
        [attr-map more] (if (map? (first more))
                          [(first more) (rest more)]
                          [nil more])
        args-form (first more)
        body (rest more)
        ctx {:ns ns-sym
             :aliases aliases}
        output (if-let [ret (get (merge (meta fn-sym) attr-map) :-)]
                 (types/normalize-return-meta ret ctx)
                 types/+unknown-type+)]
    (types/make-fn-def ns-sym fn-sym
                       (parse-fn-inputs args-form ctx)
                       output
                       (cond-> (merge attr-map (meta fn-sym))
                         docstring (assoc :docstring docstring)
                         true (assoc :aliases aliases))
                       body
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

(defn attach-function-specs
  [{:keys [specs functions] :as analysis}]
  (let [spec-map (into {}
                       (keep (fn [spec]
                               (when (= :fn (get-in spec [:type :kind]))
                                 [(types/type-key (some-> spec :ns symbol) (:name spec))
                                  spec])))
                       specs)]
    (assoc analysis :functions
           (mapv (fn [fn-def]
                   (attach-function-spec fn-def
                                         (get spec-map
                                              (types/type-key (some-> fn-def :ns symbol)
                                                              (:name fn-def)))))
                 functions))))

(defn analyze-file
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
                    :else acc))
                {:ns ns-sym
                 :aliases aliases
                 :specs []
                 :functions []}
                forms)
        attach-function-specs)))

(defn register-types!
  [{:keys [specs functions] :as analysis}]
  (doseq [spec specs]
    (types/register-spec! (types/type-key (some-> spec :ns symbol)
                                          (:name spec))
                          spec))
  (doseq [fn-def functions]
    (types/register-function! (types/type-key (some-> fn-def :ns symbol)
                                              (:name fn-def))
                              fn-def))
  analysis)

(defn analyze-namespace
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
      (analyze-file target-file)
      (throw (ex-info "Namespace source file not found"
                      {:ns ns-sym
                       :searched source-roots})))))
