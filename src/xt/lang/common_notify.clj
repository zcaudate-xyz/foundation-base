(ns xt.lang.common-notify
  (:require [clojure.set]
            [std.lang :as l]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.script-macro :as macro]
            [std.lang.base.util :as ut]
            [std.lang.interface.type-notify :as notify]
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
            [protocol port] (case (or type (:tag runtime))
                              (:websocket
                               :javafx
                               :browser
                               :browser.instance) [:http (:http-port app)]
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
         [id p]  (notify/watch-oneshot app (or timeout 1000))
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
  (let [[lang timeout] (cond (vector? lang)
                             lang

                             :ele
                             [lang 2000])]
    `(wait-on-fn ~lang (quote ~code)
                 ~timeout
                 ~(meta &form))))

(defn capture-key-match?
  [id x]
  (cond (= id x)
        true

        (map? x)
        (boolean
         (or (capture-key-match? id (get x 0))
             (capture-key-match? id (get x 1))
             (capture-key-match? id (get x "0"))
             (capture-key-match? id (get x "1"))
             (some #(capture-key-match? id %) (vals x))))

        (sequential? x)
        (boolean (some #(capture-key-match? id %) x))

        :else
        false))

(defn captured
  "gets captured results"
  {:added "4.0"}
  [& [n lang]]
  (let [[lang n] (if (keyword? n)
                    [n nil] [lang n])
        {:keys [id]} (notify-ceremony-rt lang)
        pred  (fn [{:strs [key]}]
                (capture-key-match? id key))
        captured-fn (fn []
                      (filter pred @notify/*notify-capture*))
        entries (loop [attempt 0
                       out (captured-fn)]
                  (if (or (seq out)
                          (>= attempt 20))
                    out
                    (do (Thread/sleep 25)
                        (recur (inc attempt) (captured-fn)))))]
    (cond->> entries
      :then (reverse)
      n (take n)
      :then (map #(get % "value"))
      :then vec)))

(defn captured:count
  "gets the captured count for rt"
  {:added "4.0"}
  [& [lang]]
  (count (captured lang)))

(defn captured:clear
  "clears captured items for rt"
  {:added "4.0"}
  ([& [lang]]
   (let [{:keys [id]} (notify-ceremony-rt lang)
         pred  (fn [{:strs [key]}]
                 (capture-key-match? id key))]
     (atom/swap-return! notify/*notify-capture*
       (fn [v]
         [(count (filter pred v))
          (vec (remove pred v))])))))

(defn captured:clear-all
  "clears all captured items"
  {:added "4.0"}
  ([]
   (atom/swap-return! notify/*notify-capture*
     (fn [v]
       [(count v) []]))))
