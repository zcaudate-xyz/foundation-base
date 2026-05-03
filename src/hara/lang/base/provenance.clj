(ns hara.lang.base.provenance)

(def +field-keys+
  [:hara.lang/phase
   :hara.lang/subsystem
   :hara.lang/lang
   :hara.lang/module
   :hara.lang/namespace
   :hara.lang/entry
   :hara.lang/symbol
   :hara.lang/line
   :hara.lang/form])

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
  (let [inner (or (:hara.lang/provenance data) {})
        base  (merge inner
                     (select-keys data +field-keys+))]
    (if (empty? base)
      {}
      (let [line (or (:hara.lang/line base)
                     (line-of (:hara.lang/entry base))
                     (line-of (:hara.lang/form base))
                     (line-of data))]
        (compact
         (assoc base
                :hara.lang/module (module-id (:hara.lang/module base))
                :hara.lang/namespace (namespace-id (:hara.lang/namespace base))
                :hara.lang/line line))))))

(defn provenance
  [& inputs]
  (->> inputs
       (map #(if (map? %) (frame %) {}))
       (remove empty?)
       (apply merge {})))

(defn provenance-stack
  [data]
  (let [stack (:hara.lang/provenance-stack data)]
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
  (= (select-keys left [:hara.lang/phase
                        :hara.lang/subsystem
                        :hara.lang/module
                        :hara.lang/namespace
                        :hara.lang/line])
     (select-keys right [:hara.lang/phase
                         :hara.lang/subsystem
                         :hara.lang/module
                         :hara.lang/namespace
                         :hara.lang/line])))

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
  (let [merged (apply provenance (:hara.lang/provenance mopts) inputs)]
    (if (empty? merged)
      mopts
      (assoc mopts :hara.lang/provenance merged))))

(defn error-with-provenance
  [message data ^Throwable t]
  (let [cause-data  (ex-data t)
        current     (frame data)
        inner-stack (provenance-stack cause-data)
        stack       (append-frame (vec inner-stack) current)
        merged      (if (seq stack)
                      (reduce merge {} (reverse stack))
                      {})
        wrapped?    (:hara.lang/wrapped cause-data)
        plain-data  (dissoc data
                            :hara.lang/provenance
                            :hara.lang/provenance-stack)
        payload     (cond-> (merge cause-data plain-data merged)
                      (seq merged)
                      (assoc :hara.lang/provenance merged)

                      (seq stack)
                      (assoc :hara.lang/provenance-stack stack)

                      true
                      (assoc :hara.lang/wrapped true
                             :hara.lang/cause-class (.getName (class t))
                             :hara.lang/cause-message (.getMessage t))

                      (and cause-data
                           (not wrapped?))
                      (assoc :hara.lang/cause-data cause-data))]
    (ex-info (if-let [cause-message (.getMessage t)]
               (str message ": " cause-message)
               message)
             payload
             t)))

(defn throw-with-provenance
  [message data ^Throwable t]
  (throw (error-with-provenance message data t)))
