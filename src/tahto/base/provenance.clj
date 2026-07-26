(ns tahto.common.provenance)

(def +field-keys+
  [:tahto/phase
   :tahto/subsystem
   :tahto/lang
   :tahto/module
   :tahto/namespace
   :tahto/entry
   :tahto/symbol
   :tahto/line
   :tahto/form])

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
  (let [inner (or (:tahto/provenance data) {})
        base  (merge (select-keys inner +field-keys+)
                     (select-keys data +field-keys+))]
    (if (empty? base)
      {}
      (let [line (or (:tahto/line base)
                     (line-of (:tahto/entry base))
                     (line-of (:tahto/form base))
                     (line-of data))]
        (compact
         (assoc base
                :tahto/module (module-id (:tahto/module base))
                :tahto/namespace (namespace-id (:tahto/namespace base))
                :tahto/line line))))))

(defn provenance
  [& inputs]
  (->> inputs
       (map #(if (map? %) (frame %) {}))
       (remove empty?)
       (apply merge {})))

(defn provenance-stack
  [data]
  (let [stack (:tahto/provenance-stack data)]
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
  (= (select-keys left [:tahto/phase
                        :tahto/subsystem
                        :tahto/module
                        :tahto/namespace
                        :tahto/line])
     (select-keys right [:tahto/phase
                         :tahto/subsystem
                         :tahto/module
                         :tahto/namespace
                         :tahto/line])))

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
  (let [merged (apply provenance (:tahto/provenance mopts) inputs)]
    (if (empty? merged)
      mopts
      (assoc mopts :tahto/provenance merged))))

(defn error-with-provenance
  [message data ^Throwable t]
  (let [cause-data  (ex-data t)
        current     (frame data)
        inner-stack (provenance-stack cause-data)
        stack       (append-frame (vec inner-stack) current)
        merged      (if (seq stack)
                      (reduce merge {} (reverse stack))
                      {})
        wrapped?    (:tahto/wrapped cause-data)
        plain-data  (dissoc data
                            :tahto/provenance
                            :tahto/provenance-stack)
        payload     (cond-> (merge cause-data plain-data merged)
                      (seq merged)
                      (assoc :tahto/provenance merged)

                      (seq stack)
                      (assoc :tahto/provenance-stack stack)

                      true
                      (assoc :tahto/wrapped true
                             :tahto/cause-class (.getName (class t))
                             :tahto/cause-message (.getMessage t))

                      (and cause-data
                           (not wrapped?))
                      (assoc :tahto/cause-data cause-data))]
    (ex-info (if-let [cause-message (.getMessage t)]
               (str message ": " cause-message)
               message)
             payload
             t)))

(defn throw-with-provenance
  [message data ^Throwable t]
  (throw (error-with-provenance message data t)))
