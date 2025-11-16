(ns js.react.ext-route
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-route :as event-route]
             [js.react :as r]
             [js.core :as j]]})

(defn.js makeRoute
  "makes a react compatible route"
  {:added "4.0"}
  [initial]
  (return (r/const (event-route/make-route initial))))

(defn.js listenRouteTree
  "listens for changes on the route tree"
  {:added "4.0"}
  [route]
  (var getFn (r/const (fn:> (k/clone-nested (. route ["tree"])))))
  (var [tree changeTree] (r/local getFn))
  (r/init []
    (var listener-id (j/randomId 4))
    (event-route/add-url-listener
     route listener-id (fn:> (changeTree getFn)))
    (return (fn:> (event-route/remove-listener route listener-id))))
  (return tree))

(defn.js listenRouteUrl
  "listens for all changes on the route url"
  {:added "4.0"}
  [route]
  (var getFn (r/const (fn:> (event-route/get-url route))))
  (var [url changeUrl] (r/local getFn))
  (r/init []
    (var listener-id (j/randomId 4))
    (event-route/add-url-listener
     route listener-id (fn:> (changeUrl getFn)))
    (return (fn:> (event-route/remove-listener route listener-id))))
  (return url))

(defn.js useRouteUrl
  "getter and setter for route url"
  {:added "4.0"}
  [route]
  (var url    (-/listenRouteUrl route))
  (var setUrl (r/const
               (fn [url terminate]
                 (return (event-route/set-url route url terminate)))))
  (return [url setUrl]))

(defn.js listenRouteSegment
  "listens for changes on a route segment"
  {:added "4.0"}
  [route path defaultSegment]
  (var listener-id (r/const (j/randomId 4)))
  (var [segment changeSegment] (r/local (or (event-route/get-segment route path)
                                            defaultSegment)))
  (r/watch [path]
    (event-route/add-path-listener
     route path listener-id
     (fn:> (changeSegment (or (event-route/get-segment route path)
                              defaultSegment))))
    (return (fn:> (event-route/remove-listener route listener-id))))
  
  (return segment))

(defn.js useRouteSegment
  "getter and setter for route segment"
  {:added "4.0"}
  [route path defaultSegment]
  (var pathRef (r/useFollowRef path))
  (var segment (-/listenRouteSegment route (r/curr pathRef)))
  (var setSegment
       (r/const (fn [segment]
                  (return (event-route/set-segment route (r/curr pathRef) segment)))))
  (r/init []
    (when (and (k/nil? segment)
               defaultSegment)
      (setSegment defaultSegment)))
  (return [(or segment  defaultSegment)
           setSegment]))

(defn.js listenRouteParam
  "listens for changes on a route param"
  {:added "4.0"}
  [route param defaultVal]
  (var listener-id (r/const (j/randomId 4)))
  (var [value changeValue] (r/local (or (event-route/get-param route param)
                                        defaultVal)))
  (r/watch [param]
    (event-route/add-param-listener
     route param listener-id
     (fn:> (changeValue (or (event-route/get-param route param)
                            defaultVal))))
    (return (fn:> (event-route/remove-listener route listener-id))))
  (return value))

(defn.js useRouteParam
  "getter and setter for route param"
  {:added "4.0"}
  [route param defaultVal defaultFn]
  (:= defaultFn (or defaultFn k/identity))
  (var paramRef (r/useFollowRef param))
  (var value    (defaultFn (-/listenRouteParam route param)))
  (var setValue
       (r/const
        (fn [value]
          (return (event-route/set-param route (r/curr paramRef) value)))))
  (r/init []
    (when (and (k/nil? value)
               defaultVal)
      (setValue defaultVal)))
  (return [(or value defaultVal)
           setValue]))

(defn.js useRouteParamFlag
  "binary flag for route param"
  {:added "4.0"}
  [route param flagVal defaultVal]
  (var [value setValue] (-/useRouteParam route param defaultVal))
  (var [flag setFlag]
       [(== value flagVal)
        (fn [flag]
          (:? flag (setValue flagVal) (setValue nil)))])
  (return [flag setFlag]))

