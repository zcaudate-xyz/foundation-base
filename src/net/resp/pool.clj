(ns net.resp.pool
  (:require [net.resp.connection :as conn]
            [std.concurrent :as cc]
            [std.lib.component]
            [std.lib.component.track :as track]))

(defn pool
  "creates the connection pool"
  {:added "3.0"}
  ([{:keys [id path tag initial] :as m}]
   (-> (:options m)
       (assoc :tag tag
              :track {:path path}
              :resource {:create (fn []
                                   (track/track:with-metadata [{:remote/client id}]
                                     (conn/connection (select-keys m [:host :port]))))
                         :stop std.lib.component/stop
                         :initial (or initial 0)})
       (cc/pool:create))))

(defn pool:apply
  "applys a function to connection arguments"
  {:added "3.0"}
  ([pool f args]
   (cc/pool:with-resource [connection pool]
                          (try
                            (let [out  (apply f connection args)]
                              (if conn/*close* (cc/pool:dispose:mark))
                              out)
                            (catch Throwable t
                              (cc/pool:dispose:mark)
                              (throw t))))))

(defn wrap-pool
  "wraps a function taking pool"
  {:added "3.0"}
  ([f]
   (wrap-pool f false))
  ([f self]
   (wrap-pool f self false))
  ([f self assoc?]
   (fn [{:keys [pool] :as remote}]
     (let [out (f pool)]
       (if self
         (cond-> remote
           assoc? (assoc :pool out))
         out)))))

(defn wrap-connection
  "wraps a function taking pool resource
 
   ((wrap-connection
     (fn [connection]
       (h/string (conn/connection:request-single connection [\"PING\"]))))
   {:pool |pool|})"
  {:added "3.0"}
  ([f]
   (fn [{:keys [pool]} & args]
     (pool:apply pool f args))))

(def ^{:arglists '([remote])}       pool:health   (wrap-connection conn/connection:health))
(def ^{:arglists '([remote])}       pool:start    (wrap-pool std.lib.component/start true true))
(def ^{:arglists '([remote])}       pool:stop     (wrap-pool std.lib.component/stop true true))
(def ^{:arglists '([remote])}       pool:kill     (wrap-pool std.lib.component/kill true true))
(def ^{:arglists '([remote])}       pool:started? (wrap-pool std.lib.component/started?))
(def ^{:arglists '([remote])}       pool:stopped? (wrap-pool std.lib.component/stopped?))
(def ^{:arglists '([remote level])} pool:info     (wrap-pool std.lib.component/info))

(def ^{:arglists '([remote])}           pool:read         (wrap-connection conn/connection:read))
(def ^{:arglists '([remote command])}   pool:write        (wrap-connection conn/connection:write))
(def ^{:arglists '([remote])}           pool:close        pool:stop)
(def ^{:arglists '([remote command] [remote command opts])}
  pool:request-single   (wrap-connection conn/connection:request-single))
(def ^{:arglists '([_ data opts])}
  pool:process-single   conn/connection:process-single)
(def ^{:arglists '([remote command] [remote commands opts])}
  pool:request-bulk (wrap-connection conn/connection:request-bulk))
(def ^{:arglists '([_ data opts])}
  pool:process-bulk   conn/connection:process-bulk)

