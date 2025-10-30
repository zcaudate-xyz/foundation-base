(ns lib.docker.compose
  (:require [std.lib :as h]
            [std.string :as str]))

(defn create-compose-single
  "executes a shell command"
  {:added "4.0" :guard true}
  [compose {:keys [environment
                   depends-on
                   network
                   ip-address
                   override]}]
  (let [keys (mapv first compose)
        vals (mapv second compose)
        lu   (zipmap keys vals)
        m    (cond-> (update-in lu [:environment] merge environment)
               (not-empty depends-on) (assoc :depends_on depends-on))
        keys (cond-> keys
               (not (get lu :environment))   (conj :environment)
               :then (conj :depends_on))
        override (if (map? (:ports override))
                   (assoc override :ports
                          (str/join ","
                                    (map (fn [[k v]]
                                           (str k ":" v))
                                         (:ports override))))
                   override)
        out  (->> keys 
                  (map (fn [k] [k (get m k)]))
                  (filterv second))
        out  (if (empty? override)
               out
               (reduce conj out override))
        out  (if (:networks lu)
               out
               (conj out 
                     [:networks
                      {network
                       (if ip-address
                         {:ipv4_address ip-address}
                         {})}]))]
    out))

(defn create-compose
  "executes a docker command"
  {:added "4.0"}
  [{:keys [config
           network
           entries]}]
  (let [config  (h/map-entries (fn [[k m]]
                                 [k (assoc m :name (name k))])
                               config)
        order   (h/map-vals (fn [{:keys [name ip]}]
                              (if ip
                                (h/parse-long (last (str/split ip #"\.")))
                                name))
                            config)
        prep    (h/map-vals (fn [{:keys [type] :as m}]
                              ((or (get entries type)
                                   (h/error "NOT FOUND:" {:type type}))
                               m))
                            config)
        environments (h/map-vals (fn [{:keys [deps environment]}]
                                   (->> deps
                                        (map (fn [k]
                                               (get-in prep [k :export])))
                                        (apply merge environment)))
                                 config)
        depends-on  (h/map-vals (fn [{:keys [deps]}]
                                  (mapv name deps))
                                config)]
    (->> prep
         (map (fn [[k {:keys [compose]}]]
                
                [k (create-compose-single
                    compose
                    {:environment (get environments k)
                     :depends-on  (get depends-on k)
                     :network      network
                     :ip-address  (get-in config [k :ip])
                   :override    (dissoc (get config k)
                                        :type :name
                                        :ip :environment :deps)})]))
         (sort-by (comp order first))
         vec)))
