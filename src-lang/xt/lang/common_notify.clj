(ns xt.lang.common-notify
  (:require [clojure.set]
            [hara.lang :as l]
            [hara.lang.pointer :as ptr]
            [hara.lang.script-macro :as macro]
            [hara.common.util :as ut]
            [hara.lang.type-notify :as notify]
            [std.lib.atom :as atom]
            [std.lib.context.pointer]
            [std.lib.foundation :as f]
            [std.protocol.context :as protocol.context]))

(def ^:dynamic *override-id* nil)

(def ^:dynamic *override-host* nil)

(def ^:dynamic *override-port* nil)

;;
;; NOTIFY FN
;;

(defn notify-defaults
  "creates the ceremony for webpages
 
   (notify/notify-defaults {:type :webpage})
   => '{:host window.location.hostname,
        :port window.location.port,
        :scheme (:? (== window.location.protocol \"https:\") \"https\" \"http\"),
        :type :webpage}"
  {:added "4.0"}
  [{:keys [type] :as notify}]
  (cond (= type :webpage)
        (merge {:host 'window.location.hostname
                :port 'window.location.port
                :scheme '(:? (== window.location.protocol
                                 "https:")
                             "https"
                             "http")}
               notify)

        :else
        notify))


(defn notify-ceremony
  "creates the ceremony in order to get the port and method type"
  {:added "4.0"}
  [runtime]
  (let [{:keys [id lang type container notify]} runtime]
    (if (not id)
      (f/error "Runtime Id Required." {:runtime runtime})
      (let [app (l/default-notify)
            notify (notify-defaults notify)
            [protocol port] (case (or (:tag runtime) type)
                              (:websocket
                               :javafx
                               :browser
                               :browser.instance
                               :playground) [:http (:http-port app)]
                              [:socket (:socket-port app)])
            host (or *override-host*
                     (:host notify)
                     (if container
                       "host.docker.internal"
                       "127.0.0.1"))
            port (or *override-port*
                     (:port notify)
                     port)
            opts (select-keys notify [:path :scheme])]
        [id port lang protocol host opts]))))

(defn notify-ceremony-rt
  "gets the rt for the current ceremony"
  {:added "4.0"}
  [lang]
  (cond (keyword? lang)
        (if (clojure.core/get (set (l/rt:list)) lang)
          (l/rt lang))
        
        (vector? lang)
        (first (l/annex:start (first lang)))

        (list? lang)
        (eval lang)
        
        (satisfies? std.protocol.context/IContext lang)
        lang

        (nil? lang)
        (l/rt (or (first (l/rt:list))
                  (f/error "No runtimes found.")))
        
        :else
        (f/error "Not a valid runtime.")))

;;
;; WAIT
;;

(defn wait-on-call
  "generic wait-on-helper for oneshots"
  {:added "4.0"}
  ([f]
   (wait-on-call nil f))
  ([timeout f]
   (let [app     (notify/default-notify)
         [id p]  (notify/watch-oneshot app (or timeout 2000))
         out (binding [*override-id* id]
               (f))]
     (if (:input ptr/*input*)
       out
       (if @p
         (ptr/ptr-output-json @p)
         :timeout)))))

(defn wait-on-fn
  "wait-on helper for in runtime calls"
  {:added "4.0"}
  [lang code timeout & [meta]]
  (macro/call-thunk
   meta
   (fn []
     (wait-on-call timeout
                   (fn []
                     (let [rt (notify-ceremony-rt lang)]
                       (std.lib.context.pointer/rt-invoke-ptr
                        rt 
                        (ut/lang-pointer (:lang rt) {:module (:module rt)})
                        code)))))))

(defmacro ^{:style/indent 1}
  wait-on
  "sets up a code context and waits for oneshot notification"
  {:added "4.0"}
  [lang & code]
  (let [default-timeout (case lang
                          (:dart :ruby) 10000
                          2000)
        [lang timeout] (cond (vector? lang)
                             lang

                             :ele
                             [lang default-timeout])]
    `(wait-on-fn ~lang (quote ~code)
                 ~timeout
                 ~(meta &form))))
