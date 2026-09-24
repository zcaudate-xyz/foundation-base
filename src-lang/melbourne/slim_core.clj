(ns melbourne.slim-core
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react :as r]
             [js.react.ext-route :as ext-route]
             [xt.lang.common-data :as data]]
   :export [MODULE]})

(defn.js useLocalPrimitives
  "creates crud control primitives"
  {:added "4.0"}
  [override]
  (:= override (or override {}))
  (var [showCreate
        setShowCreate] (or (. override create)
                           (r/local false)))
  (var [showModify
        setShowModify] (or (. override modify)
                           (r/local false)))
  (var [showDetail
        setShowDetail] (or (. override detail)
                           (r/local)))
  (var [orderBy
        setOrderBy]    (or (. override orderBy)
                           (r/local true)))
  (var [showScroll
        setShowScroll] (or (. override scroll)
                           (r/local true)))
  (var [showHeader
        setShowHeader] (or (. override header)
                           (r/local true)))
  (return #{orderBy
            setOrderBy
            showHeader
            setShowHeader
            showScroll
            setShowScroll
            showDetail
            setShowDetail
            showModify
            setShowModify
            showCreate
            setShowCreate}))

(defn.js useRoutePrimitives
  "creates crud control on route"
  {:added "4.0"}
  [route override]
  (:= override (or override {}))
  (var [showCreate
        setShowCreate] (or (. override create)
                           (ext-route/useRouteParamFlag
                            route
                            "section"
                            "create")))
  (var [showModify
        setShowModify] (or (. override modify)
                           (ext-route/useRouteParam
                            route
                            "modify")))
  (var [showDetail
        setShowDetail] (or (. override detail)
                           (ext-route/useRouteParam
                            route
                            "detail")))
  (var [showHeader
        setShowHeader] (or (. override header)
                           (r/local true)))
  (var [showScroll
        setShowScroll] (or (. override scroll)
                           (r/local true)))
  (var [orderBy
        setOrderBy]    (or (. override orderBy)
                           (ext-route/useRouteParam
                            route
                            "orderBy")))
  (return #{orderBy
            setOrderBy
            showDetail
            setShowDetail
            showModify
            setShowModify
            showCreate
            showHeader
            setShowHeader
            showScroll
            setShowScroll
            setShowCreate}))

(defn.js useListControl
  "creates show list control based on show"
  {:added "4.0"}
  [#{orderBy
     setOrderBy
     showDetail
     setShowDetail
     showModify
     setShowModify
     showCreate
     setShowCreate}]
  (var routeKey (:? showModify
                    "modify"
                    showDetail
                    "detail"
                    showCreate
                    "create"
                    :else "list"))
  (var [backAction
        setBackAction] (r/local nil))
  (var [showList
        setShowList] [(== routeKey "list")
                      (fn []
                        (when (and showDetail
                                   setShowDetail)
                          (setShowDetail nil))
                        (when (and showModify
                                   setShowModify)
                          (setShowModify nil))
                        (when (and showCreate
                                   setShowCreate)
                          (setShowCreate false)))])
  (return #{routeKey
            showList
            setShowList
            backAction
            setBackAction}))

(defn.js useRouteControl
  "uses the basic route controls"
  {:added "4.0"}
  [route override m]
  (var control (-/useRoutePrimitives route override))
  (return (Object.assign (-/useListControl control)
                         control)))

(defn.js useLocalControl
  "uses the local controls"
  {:added "4.0"}
  [m]
  (var control (-/useLocalPrimitives))
  (return (Object.assign (-/useListControl control)
                         control
                         m)))

(defn.js getParentProps
  "selects props passed from a child to its parent"
  {:added "4.0"}
  [props]
  (return (data/obj-pick props ["entry"
                                "data"
                                "parent"
                                "display"
                                "control"
                                "actions"])))

(defn.js useParentControl
  "packages parent props"
  {:added "4.0"}
  [props control opts]
  (var #{showDetail
         showCreate
         showModify
         showList} control)
  (var parent (-/getParentProps props))
  (r/init []
    (. parent control (setShowScroll false))
    (return (fn:> (. parent control (setShowScroll true)))))
  (r/watch [showDetail
            showCreate
            showModify]
    (cond (or (and showCreate
                   (not= "disable" (data/get-in opts ["create"])))
              (and showModify
                   (not= "disable" (data/get-in opts ["modify"])))
              (and showDetail
                   (not= "disable" (data/get-in opts ["detail"]))))
          (do (. parent control (setShowHeader false)))
          :else
          (. parent control (setShowHeader true)))
    (return (fn:> (. parent control (setShowHeader true)))))
  (return parent))

(def.js MODULE (!:module))
