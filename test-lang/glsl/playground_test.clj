^{:seedgen/skip true}
(ns glsl.playground-test
  "Browser playground test for GLSL effects.

   Uses the :runtime :playground js runtime to serve a browser page, then
   creates three GLSL effect tabs and renders each effect to its own canvas."
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.js-playground :as js-playground]
            [hara.runtime.chromedriver :as chromedriver]
            [std.json :as json]
            [std.lib.component :as component]
            [glsl.playground-shader-sources :as sources]))

(l/script- :js
  {:runtime :playground
   :config {:port 0}
   :test-mode true
   :require [[xt.lang.spec-base :as xt]
             [hara.runtime.js-playground.client :as client]
             [js.react :as r]
             [js.react.ui-webgl :as ui-webgl]]
   :emit {:lang/jsx false}})

(defn.js CanvasWrapper
  "React wrapper that mounts a raw DOM canvas into its div"
  {:added "4.1"}
  [{:# [canvas]}]
  (return [:div {:ref (fn [el]
                        (when el
                          (:= (. el innerHTML) "")
                          (. el (appendChild canvas))))
                 :style {:width "100%" :height "100%"}}]))

(defn.js show-effect
  "creates a new playground tab and renders the effect into it"
  {:added "4.1"}
  [tab-id]
  (var frag-src (. (!:G __glsl_sources__) [tab-id]))
  (window.PLAYGROUND.createTab tab-id tab-id)
  (var result (ui-webgl/render-fragment-shader frag-src {"width" 256 "height" 256 "time" 0}))
  (var canvas (. result ["canvas"]))
  (when (== "undefined" (typeof (!:G __glsl_canvases__)))
    (:= (!:G __glsl_canvases__) {}))
  (:= (. (!:G __glsl_canvases__) [tab-id]) canvas)
  (window.PLAYGROUND.setTabContent tab-id [:% -/CanvasWrapper {:canvas canvas}])
  (return tab-id))

(defn.js read-center-pixel
  "reads the RGBA pixel at the center of the rendered effect canvas"
  {:added "4.1"}
  [tab-id]
  (var canvas (. (!:G __glsl_canvases__) [tab-id]))
  (return (ui-webgl/read-center-pixel canvas)))

(defn- wait-for-channel
  "waits up to 5s for the playground websocket channel to be connected"
  [rt]
  (let [channel (:channel rt)]
    (loop [i 0]
      (when (and (< i 50) (not @channel))
        (Thread/sleep 100)
        (recur (inc i))))))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (def +sources+ {:gradient (sources/gradient-src)
                          :checkerboard (sources/checkerboard-src)
                          :ripple (sources/ripple-src)})
          (def +page+ (js-playground/play-page
                       (l/rt :js)
                       {:name "glsl-playground"
                        :title "GLSL Playground"
                        :tabs [{:id "stage" :label "Stage"}]
                        :head (str "<script type=\"text/javascript\">"
                                   "window.__glsl_sources__="
                                   (json/write +sources+)
                                   ";</script>")}))
          (def +url+ (let [{:keys [host port]} (l/rt :js)]
                       (str "http://" host ":" port "/" +page+)))
          (def +browser+ (chromedriver/browser {}))
          (chromedriver/goto +url+ 5000 +browser+)
          (wait-for-channel (l/rt :js))]
  :teardown [(l/rt:stop :js)
             (component/stop +browser+)]})

^{:refer glsl.playground-test/CANARY :adopt true :added "4.1"}
(fact "basic eval reaches the playground-served browser"

  (!.js (+ 1 2 3))
  => 6)

^{:refer glsl.playground-test/three-effects-in-tabs :added "4.1"}
(fact "three glsl effects are rendered in separate playground tabs"

    (!.js
      (do (-/show-effect "gradient")
          (-/show-effect "checkerboard")
          (-/show-effect "ripple")
          (window.PLAYGROUND.switchTab "gradient")
          (window.PLAYGROUND.getActiveTab)))
    => "gradient"

    (!.js
      (var panel (document.getElementById "tab-gradient"))
      (var canvas (. (!:G __glsl_canvases__) ["gradient"]))
      (. panel (contains canvas)))
    => true

    (!.js (-/read-center-pixel "gradient"))
    => (fn [pixels]
         (and (vector? pixels)
              (> (nth pixels 0) 100)
              (< (nth pixels 1) 200)
              (= (nth pixels 3) 255)))

    (!.js (-/read-center-pixel "checkerboard"))
    => (fn [pixels]
         (and (vector? pixels)
              (let [avg (/ (+ (nth pixels 0)
                              (nth pixels 1)
                              (nth pixels 2))
                           3)]
                (or (< avg 20) (> avg 235)))
              (= (nth pixels 3) 255)))

    (!.js (-/read-center-pixel "ripple"))
    => (fn [pixels]
         (and (vector? pixels)
              (> (nth pixels 2) 150)
              (= (nth pixels 3) 255))))
