(ns rt.chromedriver.util
  (:require [std.json :as json]
            [std.lib :as h]
            [rt.chromedriver.connection :as conn]))

(comment
  ;;
  ;; GENERATE
  ;;
  (require '[rt.chromedriver.spec :as spec])
  (h/template-entries [spec/tmpl-connection]
  [[runtime-evaluate ["Runtime" "evaluate"]]
   [page-navigate ["Page" "navigate"]]
   [page-capture-screenshot ["Page" "captureScreenshot"]]
   [target-info ["Target" "getTargetInfo"]]
   [target-create ["Target" "createTarget"]]
   [target-close ["Target" "closeTarget"]]]))
  
(defn runtime-evaluate
  "performs runtime eval on connection"
  {:added "4.0"}
  [conn expression & [{:keys [allow-unsafe-eval-blocked-by-csp
                              await-promise
                              context-id
                              disable-breaks
                              generate-preview
                              include-command-line-api
                              object-group
                              repl-mode
                              return-by-value
                              silent
                              throw-on-side-effect
                              timeout
                              unique-context-id
                              user-gesture],
                       :as m}
                      timeout
                      opts]]
  (conn/send conn "Runtime.evaluate" (merge {"expression" expression} m)
             timeout
             opts))

(defn page-navigate
  "navigates to a new url"
  {:added "4.0"}
  [conn
   url
   &
   [{:keys
     [frame-id
      referrer
      referrer-policy
      transition-type],
     :as m}
    timeout
    opts]]
  (conn/send conn "Page.navigate" (merge {"url" url} m) timeout opts))

(defn page-capture-screenshot
  "captures a screenshot from the browser"
  {:added "4.0"}
  [conn & [{:keys [capture-beyond-viewport
                   clip
                   format
                   from-surface
                   quality],
            :as m}
           timeout
           opts]]
  (conn/send conn "Page.captureScreenshot" (merge {} m) timeout opts))

(defn target-info
  "gets the target info"
  {:added "4.0"}
  [conn & [{:keys [target-id], :as m} timeout opts]]
  (conn/send conn "Target.getTargetInfo" (merge {} m) timeout opts))

(defn target-create
  "creates a new target"
  {:added "4.0"}
  [conn url & [{:keys
                [background
                 browser-context-id
                 enable-begin-frame-control
                 height
                 new-window
                 width],
                :as m}
               timeout
               opts]]
  (conn/send conn "Target.createTarget"
             (merge {"url" url} m)
             timeout
             opts))

(defn target-close
  "closes a current target"
  {:added "4.0"}
  [conn target-id & [{:keys [], :as m} timeout opts]]
  (conn/send conn "Target.closeTarget"
             (merge {"targetId" target-id} m)
             timeout
             opts))
