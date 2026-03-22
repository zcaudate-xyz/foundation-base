(ns code.test.base.match
  (:require [clojure.set]
            [std.lib.foundation]))

(defn match-base
  "determines whether a term matches with a filter
   (match-base {:unit #{:web}}
               {:unit #{:web}}
               false)
   => [true false false]
   (match-base {:refer 'user/foo
                :namespace 'user}
               {:refers '[user/other]
                :namespaces '[foo bar]}
               true)
   => [true false false]"
  {:added "3.0"}
  ([fmeta {:keys [unit refers namespaces] :as filter} default]
   [(if-not (empty? unit)
      (->> (:unit fmeta)
           (clojure.set/intersection unit)
           (empty?)
           (not))
      default)
    (if-not (empty? refers)
      (let [refer (:refer fmeta)
            refers (set refers)
            ns    (if (symbol? refer)
                    (symbol (namespace refer)))]
        (boolean (or (if ns (refers ns))
                     (refers refer))))
      default)
    (if-not (empty? namespaces)
      (or (->> namespaces
               (map (fn [namespace]
                      (cond (symbol? namespace)
                            (= (str (:ns fmeta))
                               (str namespace))

                            (std.lib.foundation/regexp? namespace)
                            (boolean (re-find namespace (str (:ns fmeta)))))))
               (some true?))
          false)
      default)]))

(defn match-include
  "determines whether inclusion is a match
   (match-include {:unit #{:web}}
                  {:unit #{:web}})
   => true
 
   (match-include {:refer 'user/foo
                   :namespace 'user}
                  {})
   => true"
  {:added "3.0"}
  ([fmeta filter]
   (not (some false? (match-base fmeta filter true)))))

(defn match-exclude
  "determines whether exclusion is a match
   (match-exclude {:unit #{:web}}
                  {:unit #{:web}})
   => true
   (match-exclude {:refer 'user/foo
                   :namespace 'user}
                  {})
   => false"
  {:added "3.0"}
  ([fmeta filter]
   (or (some true? (match-base fmeta filter false))
       false)))

(defn match-options
  "determines whether a set of options can match
   (match-options {:unit #{:web}
                   :refer 'user/foo}
                  {:include [{:tags #{:web}}]
                   :exclude []})
   => true
 
   (match-options {:unit #{:web}
                   :refer 'user/foo}
                  {:include [{:tags #{:web}}]
                   :exclude [{:refers '[user/foo]}]})
   => false
 
   (match-options {:unit #{:web}
                   :ns 'user
                   :refer 'user/foo}
                  {:include [{:namespaces [#\"us\"]}]})
   => true"
  {:added "3.0"}
  ([{:keys [refer unit] :as fmeta} {:keys [include exclude] :as settings}]
   (cond (and unit (empty? include))
         false

         :else
         (and (if (empty? include)
                true
                (->> include
                     (map #(match-include fmeta %))
                     (some true?)))
              (->> exclude
                   (map #(match-exclude fmeta %))
                   (every? false?))))))
