(ns std.concurrent.print
  (:require [std.concurrent.atom :as atom]
            [std.lib.foundation :as h]
            [std.lib.env :as env]
            [std.lib.resource :as res]
            [clojure.pprint :as pprint])
  (:refer-clojure :exclude [print println prn with-out-str]))

(defonce ^:dynamic *executor* nil)

(defn print-handler
  "handler for local print"
  {:added "3.0"}
  ([_ items]
   (doseq [item items]
     (clojure.core/print item))
   (flush)))

(res/res:variant-add
 :hara/concurrent.atom.executor
 {:id  :std.concurrent.print
  :alias :hara/print
  :mode {:allow #{:global} :default :global}
  :config {:handler print-handler}
  :hook {:post-setup    (fn [exe] (h/set! *executor* exe))
         :post-teardown (fn [exe] (h/set! *executor* nil))}})

(defn get-executor
  "gets the print executor"
  {:added "3.0"}
  []
  (or *executor*
      (res/res :hara/print)))

(defn submit
  "submits an entry for printing"
  {:added "3.0"}
  ([& entries]
   (apply (:submit (get-executor)) entries)))

(defn print
  "prints using local handler"
  {:added "3.0"}
  ([& items]
   (if env/*local*
     (do (apply submit items) nil)
     (apply clojure.core/print items))))

(defn println
  "convenience function for println"
  {:added "3.0"}
  ([& items]
   (->> (apply clojure.core/println items)
        (clojure.core/with-out-str)
        (print))))

(defn prn
  "convenience function for prn"
  {:added "3.0"}
  ([& items]
   (->> (apply clojure.core/prn items)
        (clojure.core/with-out-str)
        (print))))

(defn pprint-str
  "convenience function for pprint-str"
  {:added "3.0"}
  ([item]
   (let [s (clojure.core/with-out-str
             (pprint/pprint item))]
     (subs s 0 (dec (count s))))))

(defn pprint
  "cenvenience function for pprint"
  {:added "3.0"}
  ([item]
   (print (pprint-str item))))

(env/local:set :prn prn
               :println println
               :print print
               :pprint pprint
               :pprint-str pprint-str)
