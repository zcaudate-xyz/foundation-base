(ns melbourne.slim-link
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.ui-util :as ui-util]
             [js.react.ext-model :as ext-view]
             [js.react.ext-form :as ext-form]
             [xt.event.base-form :as event-form]
             [melbourne.ui-static :as ui-static]
             [melbourne.slim-common :as slim-common]
             [melbourne.slim-select :as slim-select]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js useViewLink
  [props]
  (var #{views
         form
         field
         viewKey
         viewArgs
         viewOpts} props)
  (var links (ext-view/listenView 
              (. views [viewKey])
              "success"))
  (var #{results
         lookup} links)
  (var args nil)
  (var link-id (ext-form/listenFieldValue form field))
  (r/watch [results]
    (when (and (data/not-empty? results)
               (lib/nil? link-id))
      (event-form/set-field form field (data/id-fn (data/first results)))))

  (when viewArgs
    (var data (ext-form/listenFormData form))
    (:= args (viewArgs data props))
    (ext-view/useRefreshArgs (. views [viewKey])
                             args
                             (or viewOpts {:remote "none"})))
  (return #{links
            args}))

(defn.js FormLinkDropdown
  "creates a Dropdown"
  {:added "0.1"}
  [props]
  (var aprops (Object.assign {} props (. props fieldProps)))
  (var #{views viewKey viewArgs viewOpts
         viewTemplate viewValueFn
         form field} aprops)

  (var #{links
         args} (-/useViewLink aprops))
  (return
   (r/% slim-select/FormDropdown
        (Object.assign {} {}
                     props
                     {:key (xt/x:json-encode args)
                      :data     (. links results)
                      :fieldProps
                      {:valueFn  data/id-fn
                       :format (fn [id]
                                 (return (data/template-entry
                                          (data/get-in links ["lookup" id])
                                          viewTemplate)))}}))))

(defn.js FormLinkReadOnly
  "creates a Dropdown"
  {:added "0.1"}
  [props]
  (var #{views viewKey viewArgs viewOpts
         viewTemplate
         form field} (Object.assign {} props (. props fieldProps)))
  (var #{args
         links} (-/useViewLink #{views
                                  form
                                  field
                                  viewKey
                                  viewArgs
                                 viewOpts}))
  (return
   (r/% slim-common/FormReadOnly
        (Object.assign {} props
                     {:template (fn [e]
                                  (return
                                   (data/template-entry
                                    (data/get-in links ["lookup" (. e [field])])
                                    viewTemplate)))}))))

(defn.js useViewLinkEntry
  [#{views
     viewKey
     viewArgs
     viewOpts}
   entry
   field]
  (var links (ext-view/listenView 
              (. views [viewKey])
              "success"))
  (var #{results
         lookup} links)

  (var args nil)
  (when viewArgs
    (:= args (viewArgs entry))
    (ext-view/useRefreshArgs (. views [viewKey])
                             args
                             (or viewOpts {})))
  (return #{links
            args}))

(defn.js FormLinkEntryReadOnly
  "creates a Dropdown"
  {:added "0.1"}
  [props]
  (var #{views viewKey viewArgs viewOpts
         viewTemplate
         entry field} (Object.assign {} props (. props fieldProps)))
  (var #{args
         links} (-/useViewLinkEntry #{views
                                       viewKey
                                       viewArgs
                                       viewOpts}
                                     entry field))
  (return
   (r/% slim-common/FormReadOnly
        (Object.assign {} props
                     {:template (fn [e]
                                  (return
                                   (data/template-entry
                                    (data/get-in links ["lookup" (. e [field])])
                                    viewTemplate)))}))))

(def.js MODULE (!:module))
