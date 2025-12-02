(ns code.manage.var
  (:require [code.framework :as base]
            [std.string :as str]
            [code.query :as query]
            [code.edit :as nav]))

(defn create-candidates
  "creates candidates for search"
  {:added "3.0"}
  ([nsform var sym]
   (if-let [[_ & {:keys [as refer]}] nsform]
     (-> (set [var
               (if as (symbol (str as) (str sym)))
               (if (or (= refer :all)
                       ((set (if (coll? refer) refer [refer])) sym))
                 sym)])
         (disj nil))
     #{var})))

(defn find-candidates
  "finds candidates in current namespace"
  {:added "3.0"}
  ([nav var]
   (let [nsp    (symbol (namespace var))
         sym    (symbol (name var))
         nsform (-> (query/$* nav
                              [{:first :require} '| {:first nsp}])
                    (first))
         candidates (create-candidates nsform var sym)]
     [candidates nsp sym])))

(defn find-usages
  "top-level find-usage query"
  {:added "3.0"}
  ([ns {:keys [var] :as params} lookup project]
   (let [path   (lookup ns)
         code   (slurp path)
         nav   (nav/parse-root code)
         [candidates nsp sym] (find-candidates nav var)]
     (base/locate-code ns
                       (assoc params
                              :query [{:or (map #(hash-map :is %) candidates)}])
                       lookup
                       project))))
