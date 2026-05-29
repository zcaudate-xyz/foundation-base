(ns lib.supabase.api
  (:require [clojure.string :as string]
            [lib.supabase.common :as common]
            [lib.supabase.template :as template]
            [net.openapi.generate :as generate]
            [std.string.case :as case]))

(def +spec-path+
  "resources/assets/lib.supabase/openapi.json")

(def +group-roots+
  {:auth "/auth/v1"
   :admin "/auth/v1/admin"
   :rest "/rest/v1"
   :realtime "/realtime/v1"})

(defn operation-group
  [operation]
  (-> (or (first (:tags operation)) "default")
      case/spear-case
      keyword))

(defn route-id
  [operation]
  (keyword (name (operation-group operation))
           (:fn-name operation)))

(defn route-root
  [group]
  (get +group-roots+ group "/v1"))

(defn route-path-template
  [group path]
  (let [root (route-root group)
        relative (if (and (string? path)
                          (string/starts-with? path root))
                   (or (not-empty (subs path (count root)))
                       "/")
                   path)]
    (string/replace relative #"\{[^}]+\}" "%s")))

(defn route-args
  [operation]
  (->> (:args operation)
       (filter #(= :path (:kind %)))
       (mapv (fn [{:keys [symbol]}]
               (-> symbol name keyword)))))

(defn route-type
  [operation]
  (if (seq (get-in operation [:call-map :auth-names]))
    :service
    :public))

(defn route-entry-data
  [operation]
  (let [group (operation-group operation)]
   {:group group
   :method (:method operation)
   :type (route-type operation)
   :args (route-args operation)
   :path (route-path-template group (:path operation))
   :summary (:summary operation)
   :description (:description operation)
   :fn-name (:fn-name operation)}))

(def +routes+
  (->> (generate/api-functions +spec-path+)
       (map (fn [operation]
              [(route-id operation)
               (route-entry-data operation)]))
       (into (sorted-map))))

(def +roots+
  (->> +routes+
       vals
       (map :group)
       distinct
       sort
       (map (fn [group] [group (route-root group)]))
       (into {})))

(defn route-summary
  "Returns the grouped Supabase API route summary."
  {:added "4.1.4"}
  []
  (reduce-kv (fn [out route-key entry]
               (assoc-in out [(:group entry) (keyword (name route-key))] entry))
             {}
             +routes+))

(defn route-entry
  "Returns a generated route entry by route id."
  {:added "4.1.4"}
  [route-key]
  (get +routes+ route-key))

(defn route-group
  "Returns the route group for a generated route id."
  {:added "4.1.4"}
  [route-key]
  (:group (route-entry route-key)))

(defn route-path
  "Formats a generated route path."
  {:added "4.1.4"}
  [route-key & args]
  (let [{:keys [path]} (route-entry route-key)]
    (apply format path args)))

(defn route-method
  "Returns the default HTTP method for a generated route id."
  {:added "4.1.4"}
  [route-key]
  (or (:method (route-entry route-key))
      :get))

(defn route-request
  "Returns request options for a generated route id, merging any overrides."
  {:added "4.1.4"}
  [route-key opts & route-args]
  (let [{:keys [path group] :as entry} (route-entry route-key)]
    (merge (dissoc entry :path :args)
           opts
           {:group group
            :route (apply format path route-args)})))

(defn root-path
  "Returns the root path prefix for a generated route group."
  {:added "4.1.4"}
  [group]
  (get +roots+ group))

(defn group-route
  "Formats a route path for a generated group and route key."
  {:added "4.1.4"}
  [group route-key & args]
  (apply route-path (keyword (name group) (name route-key)) args))

(defn wrapper-args
  [operation]
  (->> (:arglist operation)
       (remove #{'client 'opts})
       vec))

(defn wrapper-defaults
  [route-key operation]
  (cond-> {:route-id route-key
           :type (route-type operation)}
    (seq (get operation :tags))
    (assoc :tags (:tags operation))))

(defn wrapper-path-args
  [operation]
  (->> (:args operation)
       (filter #(= :path (:kind %)))
       (mapv :symbol)))

(defn wrapper-body
  [operation]
  (some (fn [{:keys [kind symbol]}]
          (when (= :body kind)
            symbol))
        (:args operation)))

(defn route-wrapper-input
  ([route-key operation]
   (route-wrapper-input route-key operation 'route-request))
  ([route-key operation route-request-sym]
  (let [path-args (wrapper-path-args operation)
        body-arg (wrapper-body operation)]
    {'fsym (symbol (:fn-name operation))
     'doc (or (:summary operation)
              (:description operation)
              (:fn-name operation))
     'base-args (vec (concat ['client] (wrapper-args operation)))
     'opts-args (vec (concat ['client] (wrapper-args operation) ['opts]))
     'body-form `(common/supabase-call
                  (merge (~route-request-sym ~route-key
                                        (merge ~'opts {:client ~'client})
                                        ~@path-args)
                         ~(wrapper-defaults route-key operation))
                  ~body-arg)})))

(defn route-wrapper-form
  [route-key operation]
  (template/supabase-defn-form (route-wrapper-input route-key operation)))

(defn route-wrapper-string
  [route-key operation]
  (template/supabase-defn-string (route-wrapper-input route-key operation)))

(defn route-wrapper-render-string
  [route-key operation]
  (template/supabase-defn-string
   (route-wrapper-input route-key operation 'api/route-request)))

(defn route-wrapper-forms
  []
  (->> (generate/api-functions +spec-path+)
       (map (fn [operation]
              (route-wrapper-form (route-id operation) operation)))
       vec))

(defn route-wrapper-strings
  []
  (->> (generate/api-functions +spec-path+)
       (map (fn [operation]
              (route-wrapper-render-string (route-id operation) operation)))
       vec))

(defn render-api-file
  "Renders a file body containing generated `defn` wrappers."
  {:added "4.1.4"}
  [ns-sym]
  (str "(ns " ns-sym "\n"
       "  (:require [lib.supabase.common :as common]\n"
       "            [lib.supabase.api :as api]))\n\n"
       (string/join "\n\n" (route-wrapper-strings))
       "\n"))

(defn write-api-file
  "Writes a generated Supabase API namespace to disk."
  {:added "4.1.4"}
  [output-path ns-sym]
  (let [output (render-api-file ns-sym)]
    (spit output-path output)
    output))

(doseq [form (route-wrapper-forms)]
  (eval form))
