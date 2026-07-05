(ns hara.runtime.chromedriver
  (:require [std.lib :as h]
            [hara.lang :as l]
            [hara.runtime.chromedriver.spec :as spec]
            [hara.runtime.chromedriver.impl :as impl]
            [hara.runtime.chromedriver.connection :as conn]
            [hara.runtime.chromedriver.util :as util])
  (:refer-clojure :exclude [send get-method]))

(h/intern-in
 spec/get-domain
 spec/get-method
 spec/list-domains
 spec/list-methods
 impl/browser
 impl/browser:create)

(h/template-entries [spec/tmpl-browser]
  [[send conn/send]
   [evaluate util/runtime-evaluate]
   [screenshot util/page-capture-screenshot]
   [target-close  util/target-close]
   [target-create util/target-create]
   [target-info   util/target-info]
   [page-navigate util/page-navigate]])

(defn goto
  "goto a given page"
  {:added "4.0"}
  [url & [timeout rt]]
  (let [rt (or rt (l/rt :js))]
    @(page-navigate rt url {} (or timeout 5000))
    @(evaluate rt impl/+bootstrap+)
    @(target-info rt)))

;;
;; TAB MANAGEMENT
;;

(defn current-tab
  "returns the active tab handle of the browser"
  {:added "4.0"}
  [browser]
  (select-keys @(:state browser) [:target-id :session-id]))

(defn tab-list
  "lists all open tabs"
  {:added "4.0"}
  [browser]
  (get @(conn/send @(:state browser) "Target.getTargets") "targetInfos"))

(defn- wait-for-ready
  "polls document.readyState on a connection until complete or timeout"
  {:added "4.0"}
  [conn timeout]
  (let [end (+ (System/currentTimeMillis) timeout)]
    (loop []
      (let [state (get @(util/runtime-evaluate conn "document.readyState")
                       "value")]
        (if (= state "complete")
          state
          (if (> (System/currentTimeMillis) end)
            (throw (ex-info "Timeout waiting for document ready"
                            {:state state
                             :timeout timeout}))
            (do (Thread/sleep 50)
                (recur))))))))

(defn tab-create
  "creates a new tab and returns a tab handle"
  {:added "4.0"}
  [browser url & [opts timeout extra-opts]]
  (let [conn @(:state browser)
        {:strs [targetId]} @(util/target-create conn url opts timeout extra-opts)
        {:strs [sessionId]} @(conn/send conn "Target.attachToTarget"
                                       {:targetId targetId
                                        :flatten true})
        tab {:target-id targetId
             :session-id sessionId}]
    (wait-for-ready (assoc conn :session-id sessionId)
                    (or timeout 5000))
    tab))

(defn tab-switch
  "switches the browser runtime to the given tab handle.
   bootstraps the tab by default so that !.js forms work there."
  {:added "4.0"}
  [browser tab & [{:keys [bootstrap] :or {bootstrap true}}]]
  (swap! (:state browser) assoc
         :target-id (:target-id tab)
         :session-id (:session-id tab))
  (when bootstrap
    @(evaluate browser impl/+bootstrap+))
  tab)

(defn tab-close
  "closes the given tab"
  {:added "4.0"}
  [browser tab]
  @(conn/send @(:state browser) "Target.closeTarget"
              {:targetId (:target-id tab)}))

(defmacro with-tab
  "evaluates body with the browser switched to tab, then restores the
   previously active tab"
  {:added "4.0"}
  [browser tab & body]
  `(let [prev# (current-tab ~browser)]
     (tab-switch ~browser ~tab)
     (try
       ~@body
       (finally
         (tab-switch ~browser prev# {:bootstrap false})))))
