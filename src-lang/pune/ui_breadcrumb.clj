(ns pune.ui-breadcrumb
  (:require [lang.core :as l]
            [std.lib :as h]
            [std.lib.link :as link]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [melbourne.ui-static :as ui-static]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js Breadcrumb
    "Constructs a Breadcrumb"
    {:added "0.1"}
  [#{[design
      mini
      variant
      style
      root
      rootOnly
      branchOnly
      path
      text
      noBanner]}]
  (var routePath (data/arr-concat [(:.. (data/arrayify (:? branchOnly [] root)))]
                               (data/arrayify (:? rootOnly
                                               []
                                               path))))
  (var routeString (j/map routePath
                          (fn:> [s] s (j/toUpperCase (string/tag-string s)))))
  (:= text (or text
               (j/join routeString
                       "   /   ")) )
  (return
   [:% ui-static/Text
    {:design design
     :variant (Object.assign
               {:font "h3"
                :fg (:? noBanner
                        {:key "primary"
                         :tone "flatten"}
                        {:key "background"
                         :tone "sharpen"})}
               variant)
     :numberOfLines 1
     :style [{:paddingVertical 5
              :fontWeight "900"
              #_#_:textAlign ""}
             (data/arrayify style)]}
    text]))

(def.js MODULE (!:module))
