(ns rt.chromedriver
  (:require [std.lib :as h]
            [std.lang :as l]
            [rt.chromedriver.spec :as spec]
            [rt.chromedriver.impl :as impl]
            [rt.chromedriver.connection :as conn]
            [rt.chromedriver.util :as util])
  (:refer-clojure :exclude [send get-method]))

(h/intern-in
 spec/get-domain
 spec/get-method
 spec/list-domains
 spec/list-methods
 impl/browser
 impl/browser:create)

(h/template-entries [spec/tmpl-browser]
  [[send conn/send]
   [evaluate util/runtime-evaluate]
   [screenshot util/page-capture-screenshot]
   [target-close  util/target-close]
   [target-create util/target-create]
   [target-info   util/target-info]
   [page-navigate util/page-navigate]])

(defn goto
  "goto a given page"
  {:added "4.0"}
  [url & [timeout rt]]
  (let [rt (or rt (l/rt :js))]
    @(page-navigate rt url {} (or timeout 5000))
    @(evaluate rt impl/+bootstrap+)
    @(target-info rt)))
