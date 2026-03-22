(ns lib.docker
  (:require [lib.docker.common :as common]
            [lib.docker.ryuk :as ryuk]
            [std.lib.foundation]))

(std.lib.foundation/intern-in
 common/get-ip
 common/has-container?
 common/list-containers
 common/start-container
 common/stop-container
 common/raw-command
 common/raw-exec
 
 ryuk/start-ryuk
 ryuk/stop-ryuk
 ryuk/start-reaped
 ryuk/stop-all-reaped)

(defn start-runtime
  "starts a runtime with attached container"
  {:added "4.0"}
  [rt container]
  (let [{:keys [id suffix host no-reap]} container
        {:keys [lang tag module]} rt
        start-fn (if no-reap
                   common/start-container
                   (do (ryuk/start-ryuk)
                       ryuk/start-reaped))
        container (assoc container
                         :id (or id (str (name tag) "-" suffix))
                         :labels {"rt/lang"   (name lang)
                                  "rt/module" (name module)})
        {:keys [container-ip
                container-id]} (start-fn container)]
    (assoc rt
           :host container-ip
           :container (assoc container :container-id container-id))))

(defn stop-runtime
  "stops a runtime with attached container"
  {:added "4.0"}
  [rt container]
  (let [{:keys [id secondary permanent]} container]
    
    (when (and (not permanent)
               (not secondary))
      (common/stop-container container))
    rt))
