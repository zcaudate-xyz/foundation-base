(ns code.framework.generate
  (:require [std.block.navigate :as nav]
            [std.block :as b]
            [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.lib.schema :as schema]
            [rt.postgres.grammar.form-deftype :as form-deftype]
            [std.lang.base.grammar-spec :as grammar-spec]
            [clojure.set :as set]))

(defn get-template-params
  [code]
  (->> (nav/navigator code)
       (iterate (fn [nav]
                  (nav/find-next nav
                                 (fn [block]
                                   (or (= :unquote-splice (:tag (b/info block)))
                                       (= :unquote (:tag (b/info block))))))))
       (drop 1)
       (take-while identity)
       (map nav/value)))

(defn get-template
  [code-str & [input-fn multi]]
  (let [code   (if multi
                 (b/parse-root code-str)
                 (b/parse-first code-str))
        params (get-template-params code)]
    {:code code
     :params params
     :input-fn (or input-fn identity)
     :multi multi}))

(defn fill-template
  [template input]
  (let [{:keys [code
                params
                input-fn]} template
        input (input-fn input)
        missing  (set/difference (set (map second params))
                                 (set (keys input)))
        _ (when (not-empty missing)
            (h/error "Missing params: " {:missing missing
                                         :input input}))]
    (nav/root-string
     (reduce (fn [nav param]
               
               (cond-> (nav/find-next-token nav param)
                 (= 'unquote (first param)) (nav/replace (get input (second param)))
                 (= 'unquote-splicing (first param)) (nav/replace-splice (get input (second param)))))
             (nav/navigator code)
             params))))

;;
;; Spec generation
;;

(defn resolve-schema
  [sym]
  (let [resolved (if (symbol? sym)
                   (if (and (namespace sym)
                            (= "-" (namespace sym)))
                     (symbol (str (ns-name *ns*)) (name sym))
                     sym)
                   sym)
        var (resolve resolved)]
    (if var
      @var
      (h/error "Cannot resolve schema: " {:sym sym :resolved resolved}))))

(defn pg-arg-type
  [col-type]
  (case col-type
    :uuid :uuid
    :string :text
    :long :long
    :int :int
    :float :float
    :double :double
    :boolean :boolean
    :enum :text
    :ref :uuid
    :json :jsonb
    :jsonb :jsonb
    :timestamp :long
    :instant :long
    :text))

(defn pg-col-arg
  [col-name type prefix]
  (symbol (str prefix (str/replace (str/snake-case (name col-name)) "-" "_"))))

(defn- format-docstring
  [op table-name]
  (let [human-name (str/replace (str/snake-case table-name) "_" " ")]
    (case op
      :insert (str "Creates a new " human-name " entry to record system events or changes.")
      :update (str "Updates the " human-name " entry.")
      :purge  (str "Purges the " human-name " entry.")
      "")))

(defn- extract-schema-definition
  [schema-sym]
  (let [schema (resolve-schema schema-sym)
        formatted (form-deftype/pg-deftype-format schema)
        [fmeta [_ sym spec params]] formatted
        col-spec (mapv vec (partition 2 spec))]
    {:sym sym
     :fmeta fmeta
     :col-spec col-spec
     :params params
     :table-name (name sym)}))

(defn create-insert
  "generates an insert helper for a postgres type"
  {:added "4.0"}
  [schema-sym]
  (let [{:keys [sym fmeta col-spec table-name]} (extract-schema-definition schema-sym)
        fn-name (symbol (str "create-" (str/replace (str/snake-case table-name) "_" "-")))
        docstring (format-docstring :insert table-name)

        has-tracker? (:static/tracker fmeta)

        args (reduce (fn [acc [col attrs]]
                       (let [type (:type attrs)
                             arg-type (pg-arg-type type)
                             arg-name (pg-col-arg col type "i-")]
                         (if (not (or (get-in attrs [:sql :default])
                                      (:generated attrs)
                                      (= col :id)))
                           (conj acc arg-type arg-name)
                           acc)))
                     []
                     col-spec)

        args (if has-tracker?
               (conj args :jsonb 'o-op)
               args)

        input-map (reduce (fn [acc [col attrs]]
                             (let [arg-name (pg-col-arg col (:type attrs) "i-")]
                               (if (not (or (get-in attrs [:sql :default])
                                            (:generated attrs)
                                            (= col :id)))
                                 (assoc acc col arg-name)
                                 acc)))
                           {}
                           col-spec)

        track-map (if has-tracker?
                    {:track 'o-op}
                    {})]

    `(defn.pg ^{:%% :sql} ~fn-name ~docstring {:added "0.1"}
       ~args
       (rt.postgres.script.impl/t:insert ~schema-sym ~input-map ~track-map))))

(defn create-update
  "generates an update helper for a postgres type"
  {:added "4.0"}
  [schema-sym]
  (let [{:keys [sym fmeta col-spec table-name]} (extract-schema-definition schema-sym)
        fn-name (symbol (str "create-" (str/replace (str/snake-case table-name) "_" "-") "-update"))
        docstring (format-docstring :update table-name)

        has-tracker? (:static/tracker fmeta)

        primary-col (first (keep (fn [[col attrs]] (when (:primary attrs) col)) col-spec))
        id-col (or primary-col :id)
        id-type (pg-arg-type (get-in (into {} col-spec) [id-col :type] :uuid))
        id-arg (pg-col-arg id-col id-type "")

        args (reduce (fn [acc [col attrs]]
                       (let [type (:type attrs)
                             arg-type (pg-arg-type type)
                             arg-name (pg-col-arg col type "i-")]
                         (if (not (or (get-in attrs [:sql :default])
                                      (:generated attrs)
                                      (= col id-col)
                                      (:primary attrs))) ;; Skip primary in update set
                           (conj acc arg-type arg-name)
                           acc)))
                     [id-type id-arg]
                     col-spec)

        args (if has-tracker?
               (conj args :jsonb 'o-op)
               args)

        input-map (reduce (fn [acc [col attrs]]
                             (let [arg-name (pg-col-arg col (:type attrs) "i-")]
                               (if (not (or (get-in attrs [:sql :default])
                                            (:generated attrs)
                                            (= col id-col)
                                            (:primary attrs)))
                                 (assoc acc col arg-name)
                                 acc)))
                           {}
                           col-spec)

        track-map (if has-tracker?
                    {:track 'o-op}
                    {})

        where-map {id-col id-arg}]

    `(defn.pg ^{:%% :sql} ~fn-name ~docstring {:added "0.1"}
       ~args
       (rt.postgres.script.impl/t:update ~schema-sym (merge {:set ~input-map :where ~where-map} ~track-map)))))

(defn create-purge
  "generates a purge helper for a postgres type"
  {:added "4.0"}
  [schema-sym]
  (let [{:keys [sym col-spec table-name]} (extract-schema-definition schema-sym)
        fn-name (symbol (str "create-" (str/replace (str/snake-case table-name) "_" "-") "-purge"))
        docstring (format-docstring :purge table-name)

        primary-col (first (keep (fn [[col attrs]] (when (:primary attrs) col)) col-spec))
        id-col (or primary-col :id)
        id-type (pg-arg-type (get-in (into {} col-spec) [id-col :type] :uuid))
        id-arg (pg-col-arg id-col id-type "")

        args [id-type id-arg]
        where-map {id-col id-arg}]

    `(defn.pg ^{:%% :sql} ~fn-name ~docstring {:added "0.1"}
       ~args
       (rt.postgres.script.impl/t:delete ~schema-sym {:where ~where-map}))))
