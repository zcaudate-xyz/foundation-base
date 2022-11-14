(ns std.config.ext.json
  (:require [std.json :as json]
            [std.config.common :as common]
            [std.lib :refer [definvoke]]))

(definvoke resolve-type-json
  "resolves json config
 
   (resolve-type-json nil (json/write {:a 1 :b 2}))
   => {:b 2, :a 1}"
  {:added "3.0"}
  [:method {:multi common/-resolve-type
            :val :json}]
  ([_ content]
   (json/read content json/+keyword-case-mapper+)))
