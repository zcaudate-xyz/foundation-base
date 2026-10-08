(ns js.react.ext-route-test
    (:require [lang.core :as l]
              [js.react.helper-jsdom :as helper-source])
    (:use code.test))

(l/script- :js
           {:runtime :basic
            :require [[xt.lang.spec-base :as xt]
                      [js.react :as r]
                      [js.react.helper-jsdom :as helper]
                      [js.react.ext-route :as ext-route]
                      [xt.event.base-route :as event-route]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defn.js await-dom
         [f]
         (return
          (new Promise
               (fn [resolve]
                   (setTimeout (fn [] (resolve (f))) 0)))))

^{:refer js.react.ext-route/makeRoute :added "4.0"}
(fact "creates a React route from an initial URL"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home?tab=one"))
            (xt/x:set-key controls "route" route)
            (return (r/createElement "span" nil (ext-route/listenRouteUrl route)))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var route (. controls ["route"]))
                 (return {"type" (. route ["::"])
                          "url" (event-route/get-url route)
                          "path" (event-route/path-from-tree (. route ["tree"]))}))))))
  => {"type" "event.route"
      "url" "home?tab=one"
      "path" ["home"]})

^{:refer js.react.ext-route/listenRouteTree :added "4.0"}
(fact "tracks route tree changes"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home"))
            (var tree (ext-route/listenRouteTree route))
            (xt/x:set-key controls "route" route)
            (return (r/createElement "span" nil (JSON.stringify tree)))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var route (. controls ["route"]))
                 (event-route/set-url route "/next")
                 (return (-/await-dom
                          (fn []
                              (return (event-route/path-from-tree (. route ["tree"])))))))))))
  => ["next"])

^{:refer js.react.ext-route/listenRouteUrl :added "4.0"}
(fact "tracks route URL changes"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home"))
            (var url (ext-route/listenRouteUrl route))
            (xt/x:set-key controls "route" route)
            (return (r/createElement "span" nil url))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var route (. controls ["route"]))
                 (event-route/set-url route "/next?tab=two")
                 (return (-/await-dom
                          (fn []
                              (return (ext-route/listenRouteUrl route))))))))))
  => "next?tab=two")

^{:refer js.react.ext-route/useRouteUrl :added "4.0"}
(fact "returns a route URL setter"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home"))
            (var [url setUrl] (ext-route/useRouteUrl route))
            (xt/x:set-key controls "route" route)
            (xt/x:set-key controls "setUrl" setUrl)
            (return (r/createElement "span" nil url))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 ((. controls ["setUrl"]) "/next" true)
                 (return (-/await-dom
                          (fn []
                              (return (event-route/get-url (. controls ["route"])))))))))))
  => "next")

^{:refer js.react.ext-route/listenRouteSegment :added "4.0"}
(fact "tracks a route segment"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home"))
            (var segment (ext-route/listenRouteSegment route ["home"] "fallback"))
            (xt/x:set-key controls "route" route)
            (return (r/createElement "span" nil segment))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var route (. controls ["route"]))
                 (event-route/set-segment route ["home"] "detail")
                 (return (-/await-dom
                          (fn []
                              (return (event-route/get-segment route ["home"]))))))))))
  => "detail")

^{:refer js.react.ext-route/useRouteSegment :added "4.0"}
(fact "returns a route segment setter"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home"))
            (var [segment setSegment] (ext-route/useRouteSegment route ["home"] "fallback"))
            (xt/x:set-key controls "route" route)
            (xt/x:set-key controls "setSegment" setSegment)
            (return (r/createElement "span" nil segment))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 ((. controls ["setSegment"]) "detail")
                 (return (-/await-dom
                          (fn []
                              (return (event-route/get-segment (. controls ["route"]) ["home"]))))))))))
  => "detail")

^{:refer js.react.ext-route/listenRouteParam :added "4.0"}
(fact "tracks a route parameter"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home?tab=one"))
            (var value (ext-route/listenRouteParam route "tab" "fallback"))
            (xt/x:set-key controls "route" route)
            (return (r/createElement "span" nil value))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var route (. controls ["route"]))
                 (event-route/set-param route "tab" "two")
                 (return (-/await-dom
                          (fn []
                              (return (event-route/get-param route "tab" nil))))))))))
  => "two")

^{:refer js.react.ext-route/useRouteParam :added "4.0"}
(fact "returns a route parameter setter"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home"))
            (var [value setValue] (ext-route/useRouteParam route "tab" "fallback"))
            (xt/x:set-key controls "route" route)
            (xt/x:set-key controls "setValue" setValue)
            (return (r/createElement "span" nil value))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 ((. controls ["setValue"]) "two")
                 (return (-/await-dom
                          (fn []
                              (return (event-route/get-param (. controls ["route"]) "tab" nil))))))))))
  => "two")

^{:refer js.react.ext-route/useRouteParamFlag :added "4.0"}
(fact "maps a route parameter to a binary flag"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var route (ext-route/makeRoute "/home"))
            (var [flag setFlag] (ext-route/useRouteParamFlag route "enabled" "yes" nil))
            (xt/x:set-key controls "route" route)
            (xt/x:set-key controls "setFlag" setFlag)
            (return (r/createElement "span" nil (JSON.stringify flag)))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 ((. controls ["setFlag"]) true)
                 (return (-/await-dom
                          (fn []
                              (var route (. controls ["route"]))
                              (return {"flag" (== "yes" (event-route/get-param route "enabled" nil))
                                       "url" (event-route/get-url route)})))))))))
  => {"flag" true "url" "home?enabled=yes"})
