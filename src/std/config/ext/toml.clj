(ns std.config.ext.toml
  (:require [script.toml :as toml]
            [std.config.common :as common]
            [std.lib.invoke :refer [definvoke]]))

(definvoke resolve-type-toml
  "resolves toml config
 
   (resolve-type-toml nil (toml/write {:a 1 :b 2}))
   => {:a 1, :b 2}"
  {:added "3.0"}
  [:method {:multi common/-resolve-type
            :val :toml}]
  ([_ content]
   (toml/read content :keywordize true)))

(comment
  (toml/read (slurp "../ops/site/tahto/base/traefik/sites.toml")
             :keywordize true))
