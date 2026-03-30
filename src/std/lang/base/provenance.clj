(ns std.lang.base.provenance)

(def +field-keys+
  [:std.lang/phase
   :std.lang/subsystem
   :std.lang/lang
   :std.lang/module
   :std.lang/namespace
   :std.lang/entry
   :std.lang/symbol
   :std.lang/line
   :std.lang/form])

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
  (let [inner (or (:std.lang/provenance data) {})
        base  (merge inner
                     (select-keys data +field-keys+))]
    (if (empty? base)
      {}
      (let [line (or (:std.lang/line base)
                     (line-of (:std.lang/entry base))
                     (line-of (:std.lang/form base))
                     (line-of data))]
        (compact
         (assoc base
                :std.lang/module (module-id (:std.lang/module base))
                :std.lang/namespace (namespace-id (:std.lang/namespace base))
                :std.lang/line line))))))

(defn provenance
  [& inputs]
  (->> inputs
       (map #(if (map? %) (frame %) {}))
       (remove empty?)
       (apply merge {})))

(defn provenance-stack
  [data]
  (let [stack (:std.lang/provenance-stack data)]
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
  (= (select-keys left [:std.lang/phase
                        :std.lang/subsystem
                        :std.lang/module
                        :std.lang/namespace
                        :std.lang/line])
     (select-keys right [:std.lang/phase
                         :std.lang/subsystem
                         :std.lang/module
                         :std.lang/namespace
                         :std.lang/line])))

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
  (let [merged (apply provenance (:std.lang/provenance mopts) inputs)]
    (if (empty? merged)
      mopts
      (assoc mopts :std.lang/provenance merged))))

(defn error-with-provenance
  [message data ^Throwable t]
  (let [cause-data  (ex-data t)
        current     (frame data)
        inner-stack (provenance-stack cause-data)
        stack       (append-frame (vec inner-stack) current)
        merged      (if (seq stack)
                      (reduce merge {} (reverse stack))
                      {})
        wrapped?    (:std.lang/wrapped cause-data)
        plain-data  (dissoc data
                            :std.lang/provenance
                            :std.lang/provenance-stack)
        payload     (cond-> (merge cause-data plain-data merged)
                      (seq merged)
                      (assoc :std.lang/provenance merged)

                      (seq stack)
                      (assoc :std.lang/provenance-stack stack)

                      true
                      (assoc :std.lang/wrapped true
                             :std.lang/cause-class (.getName (class t))
                             :std.lang/cause-message (.getMessage t))

                      (and cause-data
                           (not wrapped?))
                      (assoc :std.lang/cause-data cause-data))]
    (ex-info (if-let [cause-message (.getMessage t)]
               (str message ": " cause-message)
               message)
             payload
             t)))

(defn throw-with-provenance
  [message data ^Throwable t]
  (throw (error-with-provenance message data t)))
