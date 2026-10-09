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

^{:refer js.react.ext-route/makeRoute :added "4.0"}
(fact "creates a React route from an initial URL"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (return (r/createElement "span" nil (ext-route/listenRouteUrl route))))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home?tab=one")}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var result {"type" (. route ["::"])
                  "url" (event-route/get-url route)
                  "path" (event-route/path-from-tree (. route ["tree"]))})
     (return result)))
  => {"type" "event.route"
      "url" "home?tab=one"
      "path" ["home"]})

^{:refer js.react.ext-route/listenRouteTree :added "4.0"}
(fact "tracks route tree changes"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (var tree (ext-route/listenRouteTree route))
     (return (r/createElement "span" nil (JSON.stringify tree))))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home")}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (React.act (fn [] (event-route/set-url route "/next"))))
         (then (fn [_]
                 (return (helper/await-dom
                          (fn []
                            (var result (event-route/path-from-tree (. route ["tree"])))
                            (return result))))))))))
  => ["next"])

^{:refer js.react.ext-route/listenRouteUrl :added "4.0"}
(fact "tracks route URL changes"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (var url (ext-route/listenRouteUrl route))
     (return (r/createElement "span" nil url)))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home")}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (React.act (fn [] (event-route/set-url route "/next?tab=two"))))
         (then (fn [_]
                 (var result document.body.textContent)
                 (return result)))))))
  => "next?tab=two")

^{:refer js.react.ext-route/useRouteUrl :added "4.0"}
(fact "returns a route URL setter"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (var [url setUrl] (ext-route/useRouteUrl route))
     (xt/x:set-key (. props ["controls"]) "setUrl" setUrl)
     (return (r/createElement "span" nil url)))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home")
              "controls" {}}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var controls (. props ["controls"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (React.act (fn [] ((. controls ["setUrl"]) "/next" true))))
         (then (fn [_]
                 (var result (event-route/get-url route))
                 (return result)))))))
  => "next")

^{:refer js.react.ext-route/listenRouteSegment :added "4.0"}
(fact "tracks a route segment"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (var segment (ext-route/listenRouteSegment route ["home"] "fallback"))
     (return (r/createElement "span" nil segment)))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home")}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (React.act (fn [] (event-route/set-segment route ["home"] "detail"))))
         (then (fn [_]
                 (var result document.body.textContent)
                 (return result)))))))
  => "detail")

^{:refer js.react.ext-route/useRouteSegment :added "4.0"}
(fact "returns a route segment setter"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (var [segment setSegment] (ext-route/useRouteSegment route ["home"] "fallback"))
     (xt/x:set-key (. props ["controls"]) "setSegment" setSegment)
     (return (r/createElement "span" nil segment)))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home")
              "controls" {}}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var controls (. props ["controls"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (React.act (fn [] ((. controls ["setSegment"]) "detail"))))
         (then (fn [_]
                 (var result (event-route/get-segment route ["home"]))
                 (return result)))))))
  => "detail")

^{:refer js.react.ext-route/listenRouteParam :added "4.0"}
(fact "tracks a route parameter"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (var value (ext-route/listenRouteParam route "tab" "fallback"))
     (return (r/createElement "span" nil value)))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home?tab=one")}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (React.act (fn [] (event-route/set-param route "tab" "two"))))
         (then (fn [_]
                 (var result document.body.textContent)
                 (return result)))))))
  => "two")

^{:refer js.react.ext-route/useRouteParam :added "4.0"}
(fact "returns a route parameter setter"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (var [value setValue] (ext-route/useRouteParam route "tab" "fallback"))
     (xt/x:set-key (. props ["controls"]) "setValue" setValue)
     (return (r/createElement "span" nil value)))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home")
              "controls" {}}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var controls (. props ["controls"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (React.act (fn [] ((. controls ["setValue"]) "two"))))
         (then (fn [_]
                 (var result (event-route/get-param route "tab" nil))
                 (return result)))))))
  => "two")

^{:refer js.react.ext-route/useRouteParamFlag :added "4.0"}
(fact "maps a route parameter to a binary flag"
  (helper-source/test
   (fn [props]
     (var route (. props ["route"]))
     (var [flag setFlag] (ext-route/useRouteParamFlag route "enabled" "yes" nil))
     (xt/x:set-key (. props ["controls"]) "setFlag" setFlag)
     (return (r/createElement "span" nil (JSON.stringify flag))))
   (fn [_]
     (return {"route" (ext-route/makeRoute "/home")
              "controls" {}}))
   (fn [props document _]
     (var route (. props ["route"]))
     (var controls (. props ["controls"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (React.act (fn [] ((. controls ["setFlag"]) true))))
         (then (fn [_]
                 (var result {"flag" (== "yes" (event-route/get-param route "enabled" nil))
                              "url" (event-route/get-url route)})
                 (return result)))))))
  => {"flag" true "url" "home?enabled=yes"})
