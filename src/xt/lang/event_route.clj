(ns xt.lang.event-route
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.event-common :as event-common]]})

(defspec.xt RoutePath
  [:xt/array :xt/str])

(defspec.xt RouteParamMap
  [:xt/dict :xt/str :xt/str])

(defspec.xt RouteParams
  [:xt/dict :xt/str RouteParamMap])

(defspec.xt RouteInterim
  [:xt/record
   ["path" RoutePath]
   ["params" RouteParams]])

(defspec.xt RouteTree
  [:xt/dict :xt/str :xt/any])

(defspec.xt RouteDiff
  [:xt/dict :xt/str :xt/bool])

(defspec.xt RouteEvent
  [:xt/record
   ["type" :xt/str]
   ["params" RouteDiff]
   ["path" RouteDiff]
   ["meta" [:xt/maybe xt.lang.event-common/EventListenerMeta]]])

(defspec.xt EventRoute
  [:xt/record
   ["::" :xt/str]
   ["listeners" xt.lang.event-common/EventListenerMap]
   ["tree" RouteTree]
   ["history" [:xt/array :xt/str]]])

(defspec.xt interim-from-url
  [:fn [:xt/str] RouteInterim])

(defspec.xt interim-to-url
  [:fn [RouteInterim] :xt/str])

(defspec.xt path-to-tree
  [:fn [RoutePath [:xt/maybe :xt/bool]] RouteTree])

(defspec.xt interim-to-tree
  [:fn [RouteInterim [:xt/maybe :xt/bool]] RouteTree])

(defspec.xt path-from-tree
  [:fn [RouteTree] RoutePath])

(defspec.xt path-params-from-tree
  [:fn [RouteTree RoutePath] RouteParamMap])

(defspec.xt interim-from-tree
  [:fn [RouteTree] RouteInterim])

(defspec.xt changed-params-raw
  [:fn [[:xt/maybe RouteParamMap] [:xt/maybe RouteParamMap]] RouteDiff])

(defspec.xt changed-params
  [:fn [RouteTree RouteTree [:xt/maybe RoutePath]] RouteDiff])

(defspec.xt changed-path-raw
  [:fn [RoutePath RoutePath] RouteDiff])

(defspec.xt changed-path
  [:fn [RouteTree RouteTree] RouteDiff])

(defspec.xt get-url
  [:fn [EventRoute] :xt/str])

(defspec.xt get-segment
  [:fn [EventRoute RoutePath] [:xt/maybe :xt/str]])

(defspec.xt get-param
  [:fn [EventRoute :xt/str [:xt/maybe RoutePath]] [:xt/maybe :xt/str]])

(defspec.xt get-all-params
  [:fn [EventRoute [:xt/maybe RoutePath]] RouteParamMap])

(defspec.xt make-route
  [:fn [:xt/any] EventRoute])

(defspec.xt add-url-listener
  [:fn [EventRoute
        :xt/str
        [:fn [RouteEvent] :xt/any]
        [:xt/maybe xt.lang.event-common/EventListenerMeta]]
       xt.lang.event-common/EventListenerEntry])

(defspec.xt add-path-listener
  [:fn [EventRoute
        RoutePath
        :xt/str
        [:fn [RouteEvent] :xt/any]
        [:xt/maybe xt.lang.event-common/EventListenerMeta]]
       xt.lang.event-common/EventListenerEntry])

(defspec.xt add-param-listener
  [:fn [EventRoute
        :xt/str
        :xt/str
        [:fn [RouteEvent] :xt/any]
        [:xt/maybe xt.lang.event-common/EventListenerMeta]]
       xt.lang.event-common/EventListenerEntry])

(defspec.xt add-full-listener
  [:fn [EventRoute
        RoutePath
        :xt/str
        :xt/str
        [:fn [RouteEvent] :xt/any]
        [:xt/maybe xt.lang.event-common/EventListenerMeta]]
       xt.lang.event-common/EventListenerEntry])

(defspec.xt set-url
  [:fn [EventRoute :xt/str [:xt/maybe :xt/bool]] [:xt/array :xt/str]])

(defspec.xt set-path
  [:fn [EventRoute [:xt/maybe RoutePath] [:xt/maybe RouteParamMap]] [:xt/array :xt/str]])

(defspec.xt set-segment
  [:fn [EventRoute RoutePath :xt/str] [:xt/array :xt/str]])

(defspec.xt set-param
  [:fn [EventRoute :xt/str [:xt/maybe :xt/str] [:xt/maybe RoutePath]] [:xt/array :xt/str]])

(defspec.xt reset-route
  [:fn [EventRoute [:xt/maybe :xt/str]] [:xt/array :xt/str]])


;;
;;
;;

(defn.xt interim-from-url
  "creates interim from url"
  {:added "4.0"}
  [url]
  (var arr (xt/x:str-split (xt/x:cat "/" url) "?"))
  (var body (xt/x:first arr))
  (var search nil)
  (when (< 1 (xt/x:len arr))
    (:= search (xt/x:second arr)))
  (var path (-> (xt/x:str-split body "/")
                (xt/x:arr-filter (fn [x]
                                   (return (< 0 (xt/x:len x)))))))
  (var params {})
  (when search
    (xt/for:array [pair (xt/x:str-split search "&")]
                 (var [key val] (xt/x:str-split pair "="))
                 (xt/x:set-key params key val)))
  (cond (xtd/obj-empty? params)
        (return {:path path :params {}})

        :else
        (return {:path path :params {(xt/x:json-encode path) params}})))

(defn.xt interim-to-url
  "creates url from interim"
  {:added "4.0"}
  [interim]
  (var #{path params} interim)
  (var param-arr [])
  (xt/for:object [[key val] (or (xt/x:get-key params (xt/x:json-encode path))
                               {})]
    (when val
      (xt/x:arr-push param-arr (xt/x:cat key "=" val))))
  (return
   (xt/x:cat (xt/x:str-join "/" path)
           (:? (xtd/arr-not-empty? param-arr)
               (xt/x:cat "?" (xt/x:str-join "&" param-arr))
               ""))))

(defn.xt path-to-tree
  "turns a path to tree"
  {:added "4.0"}
  [path terminate]
  (var out {})
  (var arr [])
  (xt/for:array [[i v] path]
    (xt/x:set-key out (xt/x:json-encode arr) v)
    (xt/x:arr-push arr v))
  (when terminate
    (xt/x:set-key out (xt/x:json-encode arr) nil))
  (return out))

(defn.xt interim-to-tree
  "converts interim to tree"
  {:added "4.0"}
  [interim terminate]
  (var #{path params} interim)
  (var tree (-/path-to-tree path terminate))
  (xt/x:set-key tree "params" params)
  (return tree))

(defn.xt path-from-tree
  "gets the path from tree"
  {:added "4.0"}
  [tree]
  (var path [])
  (var v    (xt/x:get-key tree (xt/x:json-encode path)))
  (while (xtd/arr-not-empty? v)
    (xt/x:arr-push path v)
    (:= v (xt/x:get-key tree (xt/x:json-encode path))))
  (return path))

(defn.xt path-params-from-tree
  "gets path params from tree"
  {:added "4.0"}
  [tree path]
  (return
   (or (-> tree
           (xt/x:get-key "params")
           (xt/x:get-key (xt/x:json-encode path)))
       {})))

(defn.xt interim-from-tree
  "converts interim from tree"
  {:added "4.0"}
  [tree]
  (var #{params} tree)
  (var path (-/path-from-tree tree))
  (return {:path path
           :params (or params {})}))

(defn.xt changed-params-raw
  "checks for changed params"
  {:added "4.0"}
  [pparams nparams]
  (:= pparams (or pparams {}))
  (:= nparams (or nparams {}))
  (var diff-fn
       (fn [m other]
         (var out {})
         (xt/for:object [[k v] m]
           (when (not= v (xt/x:get-key other k))
             (xt/x:set-key out k true)))
         (return out)))
  (return (xt/x:obj-assign
           (diff-fn pparams nparams)
           (diff-fn nparams pparams))))

(defn.xt changed-params
  "gets diff between params"
  {:added "4.0"}
  [ptree ntree path]
  (var pparams (-/path-params-from-tree ptree (or path [])))
  (var nparams (-/path-params-from-tree ntree (or path [])))
  (return (-/changed-params-raw pparams nparams)))

(defn.xt changed-path-raw
  "checks that path has changed"
  {:added "4.0"}
  [ppath npath]
  (var all {})
  (var arr [])
  (var changed false)
  
  (xt/for:array [[i v] npath]
    (var pv (xt/x:get-idx ppath i))
    (when (not= pv v)
      (:= changed true))
    (when changed
      (xt/x:set-key all (xt/x:json-encode arr) true))
    (xt/x:arr-push arr v))
  (return all))

(defn.xt changed-path
  "gets changed routes"
  {:added "4.0"}
  [ptree ntree]
  (var ppath (-/path-from-tree ptree))
  (var npath (-/path-from-tree ntree))
  (return (-/changed-path-raw ppath npath)))

(defn.xt get-url
  "gets the url for the route"
  {:added "4.0"}
  [route]
  (var #{tree} route)
  (return (-/interim-to-url (-/interim-from-tree tree))))

(defn.xt get-segment
  "gets the value for a segment segment"
  {:added "4.0"}
  [route path]
  (var #{tree} route)
  (:= path (event-common/arrayify-path path))
  (var pkey (xt/x:json-encode path))
  (return (xt/x:get-key tree pkey)))

(defn.xt get-param
  "gets the param value"
  {:added "4.0"}
  [route param path]
  (var #{tree} route)
  (:= path (or path (-/path-from-tree tree)))
  (:= path (event-common/arrayify-path path))
  (return (xt/x:get-key (-/path-params-from-tree tree path)
                     param)))

(defn.xt get-all-params
  "gets all params in the route"
  {:added "4.0"}
  [route path]
  (var #{tree} route)
  (:= path (or path (-/path-from-tree tree)))
  (:= path (event-common/arrayify-path path))
  (return (-/path-params-from-tree tree path)))


;;
;;
;;

(defn.xt make-route
  "makes a route"
  {:added "4.0"}
  [initial]
  (var input   (:? (xt/x:is-function? initial) (initial) initial))
  (var interim (-/interim-from-url input))
  (var tree    (-/interim-to-tree interim false))
  (return
   (event-common/blank-container
    "event.route"
    {:tree      tree
     :history   []})))

(defn.xt add-url-listener
  "adds a url listener"
  {:added "4.0"}
  [route listener-id callback meta]
  (return
   (event-common/add-listener
    route listener-id "route.url"
    callback
    meta
    (fn:> true))))

(defn.xt add-path-listener
  "adds a path listener"
  {:added "4.0"}
  [route path listener-id callback meta]
  (:= path (event-common/arrayify-path path))
  (var pkey (xt/x:json-encode path))
  (return
   (event-common/add-listener
    route listener-id "route.path"
    callback
    (xt/x:obj-assign
     {:route/path path}
     meta)
    (fn [event]
      (return (xt/x:get-key (. event ["path"])
                         pkey))))))

(defn.xt add-param-listener
  "adds a param listener"
  {:added "4.0"}
  [route param listener-id callback meta]
  (return
   (event-common/add-listener
    route listener-id "route.param"
    callback
    (xt/x:obj-assign
     {:route/param param}
     meta)
    (fn [event]
      (return (xt/x:get-key (. event ["params"])
                         param))))))

(defn.xt add-full-listener
  "adds a full listener"
  {:added "4.0"}
  [route path param listener-id callback meta]
  (:= path (event-common/arrayify-path path))
  (var pkey (xt/x:json-encode path))
  (return
   (event-common/add-listener
    route listener-id "route.full"
    callback
    (xt/x:obj-assign
     {:route/path  path
      :route/param param}
     meta)
    (fn [event]
      (return (and (xt/x:get-key (. event ["path"])
                              pkey)
                   (xt/x:get-key (. event ["params"])
                              param)))))))

(def.xt ^{:arglists '([route listener-id])}
  remove-listener
  event-common/remove-listener)

(def.xt ^{:arglists '([route])}
  list-listeners
  event-common/list-listeners)

(defn.xt set-url
  "sets the url for a route"
  {:added "4.0"}
  [route url terminate]
  (var #{tree listeners} route)
  (var ninterim (-/interim-from-url url))
  (var ninterim-params (xt/x:get-key ninterim "params"))
  (var all-params (xt/x:get-key tree "params"))
  
  ^CHANGES
  (var ppath   (-/path-from-tree tree))
  (var npath   (xt/x:get-key ninterim "path"))
  (var pkey    (xt/x:json-encode npath))
  
  (var pparams (xt/x:get-key all-params pkey))
  (var nparams (xt/x:get-key ninterim-params pkey))
  (:= nparams (or nparams {}))

  (var dpath   (-/changed-path-raw ppath npath))
  (var dparams (-/changed-params-raw pparams nparams))
  
  ^MERGE
  (xt/x:obj-assign tree (-/path-to-tree npath terminate))
  (cond (xtd/obj-empty? nparams)
        (xt/x:del-key all-params pkey)
        
        :else
        (xt/x:set-key all-params pkey nparams))

  (var #{history} route)
  (xtd/arr-pushl history url 50)
  (return
   (event-common/trigger-listeners
    route
    {:type "route.url"
     :params dparams
     :path   dpath})))

(defn.xt set-path
  "sets the path and param"
  {:added "4.0"}
  [route path params]
  (var #{tree} route)
  (var all-params (xt/x:get-key tree "params"))

  ^CHANGES
  (var ppath    (-/path-from-tree tree))
  (var npath    (or path ppath))
  (:= npath (event-common/arrayify-path npath))
  (var pkey    (xt/x:json-encode npath))
  
  (var pparams  (xt/x:get-key all-params pkey))
  (var nparams  (or params pparams))
  (:= nparams (or nparams {}))

  (var dpath   (-/changed-path-raw ppath npath))
  (var dparams (-/changed-params-raw pparams nparams))

  ^MERGE
  (xt/x:obj-assign tree (-/path-to-tree npath true))
  (cond (xtd/obj-empty? nparams)
        (xt/x:del-key all-params pkey)
        
        :else
        (xt/x:set-key all-params pkey nparams))

  (var #{history} route)
  (xtd/arr-pushl history (-/get-url route) 50)
  (return
   (event-common/trigger-listeners
    route
    {:type "route.path"
     :params dparams
     :path   dpath})))

(defn.xt set-segment
  "sets the current segment"
  {:added "4.0"}
  [route path value]
  (var #{tree} route)
  (:= path (event-common/arrayify-path path))
  (var pkey   (xt/x:json-encode path))
  (var pvalue (xt/x:get-key tree pkey))
  (xt/x:set-key tree pkey value)
  
  (var #{history} route)
  (xtd/arr-pushl history (-/get-url route) 50)
  (return
   (event-common/trigger-listeners
    route
    {:type "route.path"
     :params {}
     :path   {pkey true}})))

(defn.xt set-param
  "sets a param in a route"
  {:added "4.0"}
  [route param value path]
  (var #{tree} route)
  (:= path  (or path (-/path-from-tree tree)))
  (:= path (event-common/arrayify-path path))
  (var pkey (xt/x:json-encode path))
  (var all-params (xt/x:get-key tree "params"))
  (var pparams (or (xt/x:get-key all-params pkey) {}))
  (var pvalue  (xt/x:get-key pparams param))
  (cond (not= pvalue value)
        (do (cond (xt/x:nil? value)
                  (xt/x:del-key pparams param)
                  
                  :else
                  (xt/x:set-key pparams param value))

            (cond (xtd/obj-empty? pparams)
                  (xt/x:del-key all-params pkey)
                  
                  :else
                  (xt/x:set-key all-params pkey pparams))

            (var #{history} route)
            (xtd/arr-pushl history (-/get-url route) 50)
            (return
             (event-common/trigger-listeners
              route
              {:type "route.params"
               :params {param true}
               :path   {}})))

        :else
        (return [])))

(defn.xt reset-route
  "resets the route, clearing all params"
  {:added "4.0"}
  [route url]
  (xt/x:set-key route "history" [])
  (xt/x:set-key route "tree"
             (-/interim-to-tree (-/interim-from-url (or url "")) true))
  (-/set-url route (or url "") true))
