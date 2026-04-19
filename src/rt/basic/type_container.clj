(ns rt.basic.type-container
  (:require [lib.docker :as docker]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib.foundation :as f]
            [std.make :as make]))

(def ^:dynamic *container* nil)

(defn start-container-process
  "starts the container"
  {:added "4.0"}
  [lang
   {:keys [id suffix group remove ports image exec bootstrap] :as config
    :or {remove true}}
   port
   {:keys [host] :as rt}]
  (let [script (bootstrap port {:host (or host "host.docker.internal")})
        container {:id (or id (f/sid))
                   :suffix suffix
                   :group (or group "testing")
                   :image image
                   :ports ports
                   :remove remove
                   :cmd   (conj exec script)}]
    (def ^:dynamic *container* container)
    (docker/start-runtime rt container)))

(defn start-container
  "starts a container"
  {:added "4.0"}
  [lang config port rt]
  (let [base-program (or (:program config) (:program rt))
         base-process (or (:process config) (:process rt))
         container-exec (or (get-in config [:container :exec])
                            (:exec config))
         base-exec    (or container-exec
                          (:exec rt))
         [program process exec] (oneshot/rt-oneshot-setup
                                 lang
                                 base-program
                                 base-process
                                 base-exec
                                 (:runtime rt))
         bootstrap (or (:bootstrap config) (:bootstrap process))
         config    (merge process
                         (:container process)
                         config
                         (:container config)
                         {:lang      lang
                          :program   program
                          :exec      (or (get-in config [:container :exec])
                                         (:exec config)
                                         exec)
                          :runtime   (:runtime rt)
                          :bootstrap bootstrap})]
     (start-container-process lang config port rt)))

(defn stop-container
  "stops a container"
  {:added "4.0"}
  [{:keys [id] :as m}]
  (docker/stop-container m))


(comment
  (lib.docker.common/start-container
   (assoc *container* :cmd ["luajit" "-e" "print(1+1)"])
   )
  )
