(ns rt.basic.type-basic
  (:require [rt.basic.server-basic :as server]
            [rt.basic.type-bench :as bench]
            [rt.basic.type-common :as common]
            [rt.basic.type-container :as container]
            [std.concurrent :as cc]
            [std.json :as json]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lib.collection :as collection]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.protocol.context :as protocol.context]))

(defn start-basic
  "starts the basic rt"
  {:added "4.0"}
  ([rt]
   (start-basic rt server/create-basic-server))
  ([{:keys [id lang container bench program port process] :as rt} f]
   (let [server (server/start-server id lang port
                                     f
                                     ;; TODO link common options
                                     (or (:encode process)
                                         {}))
         merge-rt (fn [m]
                    (merge (if (map? m)
                             m
                             {})
                           (eval (select-keys rt [:program
                                                  :make
                                                  :exec]))))
         [attach key] (cond container
                            [(container/start-container
                              lang
                              (merge {:suffix id}
                                     (merge-rt container))
                              (:port server)
                              rt)
                             :container]
                             
                            (not (false? bench))
                            [(bench/start-bench
                              lang
                              (merge-rt bench)
                              (:port server)
                              rt)
                             :bench])
         rt   (cond-> rt
                key (assoc key attach)
                key (doto (server/wait-ready)))]
     rt)))

(defn stop-basic
  "stops the basic rt"
  {:added "4.0"}
  [{:keys [id lang bench container] :as rt}]
  (let [_ (when-let [curr (and (not (false? bench))
                               (bench/get-bench (or (:port rt)
                                                    (server/get-port rt))))]
            (bench/stop-bench curr))
        _ (when container
            (container/stop-container container))
        _ (server/stop-server id lang)]
    rt))

(defn raw-eval-basic
  "raw eval for basic rt"
  {:added "4.0"}
  ([{:keys [id lang process] :as rt} body]
   (let [{:keys [raw-eval] :as record} (server/get-server id lang)]
     ((or raw-eval server/raw-eval-basic-server)
      record body (:timeout process)))))

(defn invoke-ptr-basic
  "invoke for basic rt"
  {:added "4.0"}
  ([{:keys [process lang layout] :as rt} ptr args]
   (default/default-invoke-script rt ptr args raw-eval-basic process)))

(defn rt-basic-string
  "string for basic rt"
  {:added "4.0"}
  [{:keys [id lang]}]
  (let [server (server/get-server id lang)]
    (str "#rt.basic"
         (if server
           (#'server/rt-server-string-props server)
           [lang :no-server]))))

(defn rt-basic-port
  "return the basic port of the rt"
  {:added "4.0"}
  [{:keys [id lang]}]
  (let [server (server/get-server id lang)]
    (:port server)))

(impl/defimpl RuntimeBasic [id]
  :string rt-basic-string
  :protocols [std.protocol.component/IComponent
              :suffix "-basic"
              :method {-kill stop-basic}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-basic
                       -invoke-ptr  invoke-ptr-basic}])

(defn rt-basic:create
  "creates a basic rt"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           process] :as m
    :or {runtime :basic}}]
  (let [process (collection/merge-nested (common/get-options lang :basic :default)
                                process)]
    (map->RuntimeBasic (merge  m
                               {:id (or id (f/sid))
                                :tag runtime
                                :runtime runtime
                                :process process
                                :lifecycle process}))))

(defn rt-basic
  "creates and starts a basic rt
 
   (def +rt+ (rt-basic {:lang :lua
                        :program :luajit}))
   
   (h/stop +rt+)"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           program
           process] :as m}]
  (-> (rt-basic:create m)
      (component/start)))

(comment
  (./import)
  )
