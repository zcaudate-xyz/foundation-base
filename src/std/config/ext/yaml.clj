(ns std.config.ext.yaml
  (:require [script.yaml :as yaml]
            [std.config.common :as common]
            [std.lib.invoke :refer [definvoke]]))

(definvoke resolve-type-yaml
  "resolves yaml config
 
   (resolve-type-yaml nil (yaml/write {:a 1 :b 2}))
   => {:a 1, :b 2}"
  {:added "3.0"}
  [:method {:multi common/-resolve-type
            :val :yaml}]
  ([_ content]
   (yaml/read content)))

(comment
  (yaml/read (slurp "docker-compose.yml")))
