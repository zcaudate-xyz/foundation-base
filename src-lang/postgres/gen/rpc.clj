(ns postgres.gen.rpc
  "Generic typed PostgreSQL RPC generation helpers.

   This namespace owns target-independent mutation classification, aggregate
   topic inference and canonical xt.db notification forms. Applications remain
   responsible only for selecting source namespaces and supplying wrapper
   authentication/configuration."
  (:require [clojure.string :as str]))

(def +entity-aggregates+
  ["User" "Brand" "Campaign" "Topic"])

(defn snake-name
  [x]
  (some-> x name (str/replace "_" "-") str/lower-case))

(defn- aggregate-token
  [aggregate]
  (str/lower-case aggregate))

(defn infer-aggregate
  "Infers the owning entity aggregate from a command name, falling back to
   returned/source tables. Explicit metadata is used only as an override for
   ambiguous commands."
  [{:keys [function-name source-tables output-shape notify]}]
  (or (some-> (:entity notify) name)
      (some (fn [aggregate]
              (when (re-find
                     (re-pattern
                      (str "(^|-)" (aggregate-token aggregate) "(-|$)"))
                     (snake-name function-name))
                aggregate))
            +entity-aggregates+)
      (some (fn [table]
              (some (fn [aggregate]
                      (when (str/starts-with? (name table) aggregate)
                        aggregate))
                    +entity-aggregates+))
            (remove nil?
                    (concat [(some-> output-shape :source-table)]
                            source-tables)))))

(defn shape-rows
  "Returns [table path cardinality] entries for every table row represented
   by a typed output shape. A blank path denotes the complete RPC result."
  ([shape]
   (shape-rows shape []))
  ([shape path]
   (let [here (when-let [table (:source-table shape)]
                [[(name table) path :one]])
         nested (mapcat
                 (fn [[field child]]
                   (let [field-path (conj path (name field))
                         item-shape (get-in child [:items :shape])
                         child (or (:shape child) child)]
                     (cond
                     item-shape
                     (map (fn [[table item-path _]]
                            [table (into field-path item-path) :many])
                          (shape-rows item-shape []))

                     (map? child)
                     (shape-rows child field-path)

                     (sequential? child)
                     (mapcat #(when (map? %)
                                (shape-rows % field-path))
                             child)

                     :else nil)))
                 (:fields shape))]
     (vec (distinct (concat here nested))))))

(defn- normal-token
  [x]
  (-> x name str/lower-case (str/replace #"[^a-z0-9]" "")))

(defn inferred-shape-rows
  "Recovers table paths for symbolic composite fields when the typed analyser
   knows the mutation's source tables but cannot carry a called function's row
   shape through the enclosing JSON object."
  [shape source-tables]
  (let [tables (map name source-tables)]
    (->> (:fields shape)
         (keep (fn [[field value]]
                 (let [value-shape (:shape value)]
                   (when (or (= :symbol (:kind value))
                             (= :unknown (:kind value))
                             (and value-shape
                                  (nil? (:source-table value-shape))
                                  (seq (:fields value-shape))))
                   (let [field-token (normal-token field)
                         matches (filter #(str/ends-with? (normal-token %)
                                                         field-token)
                                         tables)]
                     (when (= 1 (count matches))
                       [(first matches) [(name field)] :one]))))))
         vec)))

(defn- input-names
  [inputs]
  (set (map (comp snake-name :name) inputs)))

(defn infer-topic-id
  "Infers the aggregate id expression strategy. Existing aggregate ids win;
   create-style commands may derive the id from the returned aggregate row.
   The User session id and explicit ambiguity overrides are represented
   distinctly so wrapper generators can substitute their auth expression."
  [{:keys [aggregate function-name inputs output-shape notify]}]
  (let [explicit (some-> (:arg notify) snake-name)
        explicit-path (:path notify)
        expected (str "i-" (aggregate-token aggregate) "-id")
        inputs* (input-names inputs)
        output-table (some-> output-shape :source-table name)]
    (cond
      explicit-path
      {:kind :result :path (mapv name explicit-path)}

      explicit
      {:kind :arg :name explicit}

      (and (= aggregate "User")
           (str/starts-with? (snake-name function-name) "super-user-")
           (contains? inputs* "i-target-id"))
      {:kind :arg :name "i-target-id"}

      (contains? inputs* expected)
      {:kind (if (and (= aggregate "User")
                      (= expected (some-> inputs first :name snake-name)))
               :session
               :arg)
       :name expected}

      (and (= aggregate "User") (contains? inputs* "i-target-id"))
      {:kind :arg :name "i-target-id"}

      (contains? (:fields output-shape) :topic-id)
      {:kind :result :path ["topic_id"]}

      (= aggregate output-table)
      {:kind :result :path ["id"]}

      :else nil)))

(defn notification-plan
  "Builds a canonical private entity-topic notification plan for a mutating
   typed function, or nil for non-entity/non-mutating functions. Entity
   mutations with an unusable typed result or topic id fail generation with a
   diagnostic instead of silently omitting synchronisation."
  [{:keys [function-name inputs report output-shape notify] :as function}]
  (when (get-in report [:analysis :mutating])
    (when-let [aggregate (infer-aggregate
                          {:function-name function-name
                           :source-tables (get-in report [:analysis :source-tables])
                           :output-shape output-shape
                           :notify notify})]
      (let [source-tables (get-in report [:analysis :source-tables])
            rows (vec (distinct (concat (shape-rows output-shape)
                                        (inferred-shape-rows output-shape
                                                             source-tables))))
            topic-id (infer-topic-id
                      {:aggregate aggregate
                       :function-name function-name
                       :inputs inputs
                       :output-shape output-shape
                       :notify notify})]
        (when-not (seq rows)
          (throw (ex-info "Entity mutation has no typed notification result"
                          {:function function-name
                           :aggregate aggregate
                           :output-shape output-shape})))
        (when-not topic-id
          (throw (ex-info "Cannot infer entity topic id"
                          {:function function-name
                           :aggregate aggregate
                           :inputs (mapv :name inputs)
                           :output-shape output-shape})))
        {:aggregate aggregate
         :topic-id topic-id
         :event (if (re-find #"(^|-)(purge|delete)(-|$)"
                             (snake-name function-name))
                  "db/remove"
                  "db/sync")
         :rows rows
         :private true}))))

(defn result-path-form
  "Builds the supported PostgreSQL DSL form for reading JSON result paths."
  [root path text?]
  (reduce (fn [form [idx key]]
            (list (if (and text? (= idx (dec (count path))))
                    :->>
                    :->)
                  form
                  key))
          root
          (map-indexed vector path)))

(defn notification-payload
  "Builds a canonical db/sync or db/remove request from a notification plan."
  [{:keys [event rows]} result-sym]
  (let [table-rows (reduce (fn [out [table path cardinality]]
                             (update out table (fnil conj [])
                                     [path (or cardinality :one)]))
                            (sorted-map)
                            rows)]
    {event
     (into (sorted-map)
           (map (fn [[table entries]]
                  (let [expressions
                        (mapv (fn [[path cardinality]]
                                (let [row-form (if (seq path)
                                                 (result-path-form result-sym path false)
                                                 result-sym)]
                                  (cond
                                    (and (= event "db/remove")
                                         (= cardinality :many))
                                    (list 'pg/jsonb-path-query-array row-form "$[*].id")

                                    (= event "db/remove")
                                    [(result-path-form result-sym
                                                       (conj (vec path) "id")
                                                       true)]

                                    (= cardinality :many)
                                    row-form

                                    :else [row-form])))
                              entries)
                        all-single? (every? #(= :one (second %)) entries)
                        table-value (cond
                                      all-single?
                                      (mapv first expressions)

                                      (= 1 (count expressions))
                                      (first expressions)

                                      :else
                                      (reduce (fn [left right]
                                                (list '|| left right))
                                              expressions))]
                    [table table-value])))
           table-rows)}))

(defn topic-id-form
  [{:keys [kind name path]} {:keys [auth-form result-sym arg-form]}]
  (case kind
    :session auth-form
    :result (result-path-form result-sym path true)
    :arg (arg-form name)))

(defn realtime-send-form
  "Builds the canonical private realtime-send-request wrapper form."
  [plan {:keys [auth-form result-sym arg-form] :as forms}]
  (let [id-form (topic-id-form (:topic-id plan) forms)]
    (list 's/realtime-send-request
          (list '|| (str (:aggregate plan) ":") id-form)
          (notification-payload plan result-sym)
          true)))
