(ns std.lang.base.compile-links
  (:require [std.lib :as h]
            [std.string :as str]
            [std.fs :as fs]))

(def ^:dynamic
  *link-defaults*
  {:root-libs   "libs"
   :root-prefix "."
   :path-suffix ""
   :path-separator "/"
   :path-replace {}
   :file-replace {}})

(defn get-link-lookup
  "gets a lookup value"
  {:added "4.0"}
  [link-ns ns-lookup]
  (or (get ns-lookup link-ns)
      (reduce (fn [_ [pattern v]]
                (if (h/match-filter pattern link-ns)
                  (reduced v)))
              nil
              ns-lookup)))

(defn link-attributes
  "gets link attributes"
  {:added "4.0"}
  [root-ns link-ns link-options]
  (let [{:keys [root-libs
                root-prefix
                path-separator
                path-suffix
                path-replace
                ns-suffix
                ns-label]}  (merge *link-defaults* link-options)
        
        apply-replace (fn [s]
                        (reduce (fn [s [pat sub]]
                                  (str/replace s pat sub))
                                s
                                path-replace))
        
        link-ns-str   (str link-ns)
        link-ns-arr   (str/split link-ns-str #"\.")
        root-ns-str   (str root-ns)
        root-ns-arr   (str/split root-ns-str #"\.")
        is-lib?       (not (.startsWith ^String
                                        link-ns-str
                                        root-ns-str))
        
        rel-segments  (if is-lib?
                        (cons root-libs (butlast link-ns-arr))
                        (drop (count root-ns-arr) (butlast link-ns-arr)))
        
        link-rel      (str/join path-separator (map apply-replace rel-segments))
        
        link-suffix (or (get-link-lookup link-ns ns-suffix)
                        (if (map? path-suffix)
                          (or (get-link-lookup link-ns (dissoc path-suffix :default))
                              (:default path-suffix))
                          path-suffix))

        link-label  (apply-replace (or (get-link-lookup link-ns ns-label)
                                       (last link-ns-arr)))
        link-path   (apply-replace (str link-rel path-separator link-label link-suffix))]
    {:is-lib? is-lib?
     :rel    link-rel
     :suffix link-suffix
     :label  link-label
     :path   link-path}))
