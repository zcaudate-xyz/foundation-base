(ns code.framework.generate
  (:require [std.block.navigate :as nav]
            [std.block :as b]
            [clojure.set :as set]
            [std.lib :as h]))

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

