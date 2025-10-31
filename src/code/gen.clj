(ns code.gen
  (:require [code.edit :as nav]
            [std.block :as b]
            [clojure.set :as set]
            [std.lib :as h]))

(defn get-template-params
  [code]
  (->> (nav/navigator code)
       (iterate (fn [nav]
                  (nav/find-next nav
                                 (fn [block]
                                   (= :unquote (:tag (std.block/info block)))))))
       (drop 1)
       (take-while identity)
       (map (comp nav/value nav/down))))

(defn get-template
  [code-str]
  (let [code   (b/parse-first code-str)
        params (get-template-params code)]
    {:code code
     :params params}))

(defn fill-template
  [template input]
  (let [{:keys [code
                params]} template
        missing  (set/difference (set params)
                                 (set (keys input)))
        _ (when (not-empty missing)
            (h/error "Missing params: " {:missing missing
                                         :input input}))]
    (reduce (fn [nav param]
              (-> (nav/find-next-token nav (list 'unquote param))
                  (nav/replace (get input param))))
            (nav/navigator code)
            params)))



(get-template PUBLIC_QUERY)
