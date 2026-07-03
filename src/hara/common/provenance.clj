(ns hara.common.provenance)

(def +field-keys+
  [:hara/phase
   :hara/subsystem
   :hara/lang
   :hara/module
   :hara/namespace
   :hara/entry
   :hara/symbol
   :hara/line
   :hara/form])

(defn module-id
  [module]
  (cond (map? module)
        (:id module)

        :else
        module))

(defn namespace-id
  [namespace]
  (cond (instance? clojure.lang.Namespace namespace)
        (ns-name namespace)

        :else
        namespace))

(defn line-of
  [value]
  (cond (nil? value)
        nil

        (map? value)
        (or (:line value)
            (line-of (:entry value))
            (line-of (:form value))
            (some-> value meta :line))

        (instance? clojure.lang.IObj value)
        (-> value meta :line)

        :else
        nil))

(defn compact
  [m]
  (reduce-kv (fn [acc k v]
               (if (nil? v)
                 acc
                 (assoc acc k v)))
             {}
             m))

(defn frame
  [data]
  (let [inner (or (:hara/provenance data) {})
        base  (merge (select-keys inner +field-keys+)
                     (select-keys data +field-keys+))]
    (if (empty? base)
      {}
      (let [line (or (:hara/line base)
                     (line-of (:hara/entry base))
                     (line-of (:hara/form base))
                     (line-of data))]
        (compact
         (assoc base
                :hara/module (module-id (:hara/module base))
                :hara/namespace (namespace-id (:hara/namespace base))
                :hara/line line))))))

(defn provenance
  [& inputs]
  (->> inputs
       (map #(if (map? %) (frame %) {}))
       (remove empty?)
       (apply merge {})))

(defn provenance-stack
  [data]
  (let [stack (:hara/provenance-stack data)]
    (cond (seq stack)
          (->> stack
               (mapv frame)
               (remove empty?)
               vec)

          (map? data)
          (let [single (frame data)]
            (if (empty? single)
              []
              [single]))

          :else
          [])))

(defn same-site?
  [left right]
  (= (select-keys left [:hara/phase
                        :hara/subsystem
                        :hara/module
                        :hara/namespace
                        :hara/line])
     (select-keys right [:hara/phase
                         :hara/subsystem
                         :hara/module
                         :hara/namespace
                         :hara/line])))

(defn append-frame
  [stack current]
  (cond (empty? current)
        stack

        (and (seq stack)
             (same-site? (peek stack) current))
        stack

        :else
        (conj stack current)))

(defn with-provenance
  [mopts & inputs]
  (let [merged (apply provenance (:hara/provenance mopts) inputs)]
    (if (empty? merged)
      mopts
      (assoc mopts :hara/provenance merged))))

(defn error-with-provenance
  [message data ^Throwable t]
  (let [cause-data  (ex-data t)
        current     (frame data)
        inner-stack (provenance-stack cause-data)
        stack       (append-frame (vec inner-stack) current)
        merged      (if (seq stack)
                      (reduce merge {} (reverse stack))
                      {})
        wrapped?    (:hara/wrapped cause-data)
        plain-data  (dissoc data
                            :hara/provenance
                            :hara/provenance-stack)
        payload     (cond-> (merge cause-data plain-data merged)
                      (seq merged)
                      (assoc :hara/provenance merged)

                      (seq stack)
                      (assoc :hara/provenance-stack stack)

                      true
                      (assoc :hara/wrapped true
                             :hara/cause-class (.getName (class t))
                             :hara/cause-message (.getMessage t))

                      (and cause-data
                           (not wrapped?))
                      (assoc :hara/cause-data cause-data))]
    (ex-info (if-let [cause-message (.getMessage t)]
               (str message ": " cause-message)
               message)
             payload
             t)))

(defn throw-with-provenance
  [message data ^Throwable t]
  (throw (error-with-provenance message data t)))
