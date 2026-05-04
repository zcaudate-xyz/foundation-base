(ns hara.runtime.chromedriver
  (:require [std.lib :as h]
            [hara.lang :as l]
            [hara.runtime.chromedriver.spec :as spec]
            [hara.runtime.chromedriver.impl :as impl]
            [hara.runtime.chromedriver.connection :as conn]
            [hara.runtime.chromedriver.util :as util])
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
