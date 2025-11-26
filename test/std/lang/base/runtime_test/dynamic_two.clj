(ns std.lang.base.runtime-test.dynamic-two
  (:use code.test)
  (:require [std.lang :as l]
            [std.lang.base.runtime-test.dynamic-one :as one]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
            [std.lang.base.runtime-test.dynamic-one :as one]]})

(defn.js ^{:static/template :T.string}
  world-id []
  (return (one/hello-id)))

(defn get-emitted-world-id []
  (l/emit-as :js '[(world-id)]))
