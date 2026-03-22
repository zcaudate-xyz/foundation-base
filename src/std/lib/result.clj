(ns std.lib.result
  (:require [std.lib.foundation :as h]
            [std.lib.impl :as impl :refer [defimpl]]
            [std.protocol.return :as protocol.return]))

(defn- result-string
  ([{:keys [type status data] :as res}]
   (let [label (if type (name type) "result")]
     (str "#" label (if status
                      (str "." (name status)))
          (into {} (dissoc res :status :type))))))

(defimpl Result [type status data]
  :final  true
  :string result-string
  :protocols [std.protocol.return/IReturn
              :body {-get-value     (if-not (= status :error)
                                      data)
                     -get-error     (if (= status :error) data)
                     -has-error?    (= status :error)
                     -get-status    status
                     -get-metadata  (merge (dissoc obj :status :type) (meta obj))
                     -is-container? true}])

(defn result
  "creates a result used for printing
 
   (result {:status :warn :data [1 2 3 4]})
   ;; #result{:status :warn, :data [1 2 3 4]}
   => std.lib.result.Result"
  {:added "3.0"}
  ([m]
   (map->Result m)))

(defn result?
  "checks if an object is a result
 
   (-> (result {:status :warn :data [1 2 3 4]})
       result?)
   => true"
  {:added "3.0"}
  ([obj]
   (instance? Result obj)))

(defn ->result
  "converts data into a result
 
   (->result :hello [1 2 3])
   ;;#result.return{:data [1 2 3], :key :hello}
   => std.lib.result.Result"
  {:added "3.0"}
  ([key data]
   (cond (result? data)
         (assoc data :key key)

         :else
         (result {:key key :status :return :data data}))))

(defn result-data
  "accesses the data held by the result
 
   (result-data 1) => 1
 
   (result-data (result {:status :warn :data [1 2 3 4]}))
   => [1 2 3 4]"
  {:added "3.0"}
  ([res]
   (if (result? res) (:data res) res)))
