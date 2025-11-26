(ns indigo.tool.inspect
  (:require [clojure.repl :refer [source]]
            [clojure.string :as str]))

(defn find-var-info
  [var-name]
  (let [var-sym (symbol var-name)
        var-meta (meta (find-var var-sym))]
    (when var-meta
      {:file (:file var-meta)
       :line (:line var-meta)
       :doc (:doc var-meta)
       :source (with-out-str (source var-sym))})))

(defn find-var-tests
  [var-name]
  (let [ns-name (namespace (symbol var-name))
        test-ns-name (str ns-name "-test")]
    (try
      (require (symbol test-ns-name))
      (let [test-ns (find-ns (symbol test-ns-name))
            test-vars (vals (ns-publics test-ns))]
        (->> test-vars
             (filter (fn [v]
                       (and (:test (meta v))
                            (when-let [source (with-out-str (source (symbol v)))]
                              (str/includes? source var-name)))))
             (map (fn [v]
                    {:name (str v)
                     :source (with-out-str (source (symbol v)))}))))
      (catch Throwable _
        nil))))

(defn inspect-var-tool
  [{:keys [var-name]}]
  (let [var-info (find-var-info var-name)
        test-info (find-var-tests var-name)]
    {:var-info var-info
     :test-info test-info}))

(defn list-vars-and-tests
  [namespace-name]
  (let [ns-sym (symbol namespace-name)]
    (require ns-sym)
    (let [ns (find-ns ns-sym)
          public-vars (vals (ns-publics ns))]
      (map (fn [v]
             (let [var-name (str v)]
               {:var-name var-name
                :tests (find-var-tests var-name)}))
           public-vars))))

(defn list-vars-and-tests-tool
  [{:keys [namespace]}]
  (list-vars-and-tests namespace))
