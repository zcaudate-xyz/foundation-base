(ns rt.nginx
  (:require [lib.docker :as docker]
            [lua.nginx]
            [net.http :as http]
            [rt.basic.impl.process-lua :as lua]
            [rt.nginx.config :as config]
            [rt.nginx.script :as script]
            [std.fs :as fs]
            [std.json :as json]
            [std.lang :as l]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lang.interface.type-shared :as shared]
            [std.lib.collection]
            [std.lib.component]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.impl :refer [defimpl]]
            [std.lib.network]
            [std.lib.os]
            [std.protocol.context :as protocol.context]
            [std.string.common]))

(defn error-logs
  "gets the running nginx error log"
  {:added "4.0"}
  ([]
   (error-logs (l/rt:inner :lua)))
  ([{:keys [state] :as rt}]
   (slurp (str (second @state) "/error.log"))))

(defn access-logs
  "gets the running nginx access log"
  {:added "4.0"}
  ([]
   (access-logs (l/rt:inner :lua)))
  ([{:keys [state] :as rt}]
   (slurp (str (second @state) "/access.log"))))

(defn nginx-conf
  "accesses the running ngx conf"
  {:added "4.0"}
  ([]
   (nginx-conf (l/rt:inner :lua)))
  ([{:keys [state] :as rt}]
   (slurp (last @state))))

(defn dir-tree
  "gets the running nginx access log"
  {:added "4.0"}
  ([]
   (dir-tree (l/rt:inner :lua)))
  ([{:keys [state] :as rt}]
   (fs/list (second @state)
            {:recursive true})))

(defn all-nginx-ports
  "gets all nginx ports on the system"
  {:added "4.0"}
  ([]
   (->> (std.lib.os/sh "lsof" "-i" "-P" "-n" {:wrap false})
        (std.string.common/split-lines)
        (drop 1)
        (filter #(std.string.common/starts-with? % "nginx"))
        (keep #(re-find #"^nginx\s*(\d+).*\:(\d+) \(LISTEN\)$" %))
        (map (fn [arr]
               (mapv std.lib.foundation/parse-long (drop 1 arr))))
        (group-by second)
        (std.lib.collection/map-vals (comp set (partial map first))))))

(defn all-nginx-active
  "gets all active nginx processes for a port"
  {:added "4.0"}
  [port]
  (get (all-nginx-ports) port))

(defn make-conf
  "creates a config"
  {:added "4.0"}
  [{:keys [conf-fn] :as rt}]
  (let [conf-fn (cond (nil? conf-fn) config/create-conf
                      (fn? conf-fn) conf-fn
                      :else (eval conf-fn))
        conf-fn (if (vector? conf-fn)
                  (apply (first conf-fn) (rest conf-fn))
                  conf-fn)
        conf (script/write (conf-fn rt))
        _    (if (not-empty ptr/*print*)
               (std.lib.env/pl conf))]
    conf))

(defn make-temp
  "makes a temp directory and conf"
  {:added "4.0"}
  [rt]
  (let [conf     (make-conf rt)
        tmp-dir  (str (fs/create-tmpdir))
        tmp-file (str (fs/create-tmpfile conf))
        tmp-log  (str (fs/create-directory (str tmp-dir "/logs")))]
    [tmp-dir tmp-file conf]))

(defn start-test-server-shell
  "starts in shell"
  {:added "4.0"}
  [{:keys [port]
    :as rt}]
  (let [[tmp-dir tmp-file] (make-temp rt)
        sh-args  ["nginx" "-p" tmp-dir "-c" tmp-file]
        sh-err   (std.lib.foundation/with-thrown
                   (std.lib.os/sh {:args sh-args})
                   nil)
        wait-err (std.lib.foundation/with-thrown
                   (std.lib.network/wait-for-port "localhost" port {:timeout 2000})
                   nil)]
    (cond wait-err
          (do (std.lib.env/p (std.string.common/join " " sh-args))
              #_(throw (or sh-err
                           wait-err))
              [:errored tmp-dir])
          
          :else
          [:started tmp-dir tmp-file])))

(defn start-test-server-container
  "starts in container"
  {:added "4.0"}
  ([{:keys [port container]
     :as rt}]
   (let [{:keys [exec image]} container
         [tmp-dir tmp-file] (make-temp rt)
         
         sh-args  ["docker" "run" "--rm" #_#_"--network" "host"
                   "-d"
                   "-p" (str port ":" port) 
                   "-v" (str tmp-dir ":" tmp-dir)
                   "-v" (str tmp-file ":" tmp-file)
                   (or image (std.lib.foundation/error "No image found" {:image image}))
                   (or exec  (std.lib.foundation/error "No exec found"  {:exec exec}))
                   "-g" "daemon off;"
                   "-p" tmp-dir "-c" tmp-file]
         sh-err   (std.lib.foundation/with-thrown
                    (std.lib.os/sh {:args sh-args})
                    nil)
         wait-err (std.lib.foundation/with-thrown
                    (std.lib.network/wait-for-port "localhost" port {:timeout 2000})
                    (Thread/sleep 1000)
                    nil)]
     (cond wait-err
           (do (std.lib.env/p (std.string.common/join " " sh-args))
               #_(throw (or sh-err
                            wait-err))
               [:errored tmp-dir])
           
           :else
           [:started tmp-dir tmp-file]))))

(defn start-test-server
  "starts the test server for a given port"
  {:added "4.0"}
  ([{:keys [port container]
     :as rt}]
   (let [available  (std.lib.network/port:check-available port)
         ports (all-nginx-ports)
         arch  (keyword (std.lib.os/os-arch))]
     #_(std.lib.env/prn {:container container
               :port port})
     (cond (get ports port)
           [:running]

           (not available)
           (std.lib.foundation/error "Port not available" {:port port})
           
           (and container
                (contains? (:only container) arch))
           (start-test-server-container (assoc rt :port available))

           :else
           (start-test-server-shell (assoc rt :port available))))))

(defn kill-single-nginx
  "kills nginx processes for a single port"
  {:added "4.0"}
  [port]
  (let [ports  (all-nginx-ports)
        active (get ports port)]
    (cond (empty? active)
          []

          :else
          (let [exec (std.lib.os/sh "bash" "-c" (str "kill " (std.string.common/join " " active))
                           {:wait false})]
            [active exec]))))

(defn kill-all-nginx
  "kills all runnig nginx processes"
  {:added "4.0"}
  []
  (let [ports (all-nginx-ports)
        exec  (std.lib.os/sh {:args ["bash" "-c"
                            (std.string.common/join
                             " | "
                             ["ps aux"
                              "grep '[n]ginx: .* process'"
                              "awk '{print $2}'"
                              "xargs kill"])]
                     :wait false})]
    [ports exec]))

(defn stop-test-server
  "stops the nginx test server"
  {:added "4.0"}
  ([{:keys [port no-server]}]
   (let [ports     (all-nginx-ports)
         processes (get ports port)]
     (if processes
       (do (apply std.lib.os/sh "kill" "-9" (map str processes))
           [:stopped processes])
       (if (and (= "Linux" (std.lib.os/os))
                (not= "root" @(std.lib.os/sh "whoami")))
         (kill-all-nginx)
         [:stopped])))))

;;;;
;;
;; TEST CLIENT
;;

(defn- start-nginx
  ([{:keys [id state host port container no-server] :as rt}]
   (cond (or no-server
             (not= host "localhost"))
         rt
         
         :else
         (let [out (start-test-server rt)]
           (reset! state out)
           rt))))

(defn- stop-nginx-raw
  ([{:keys [state host port container no-server] :as rt}]
   (cond (or container
             no-server
             (not= host "localhost"))
         rt

         :else
         (do (stop-test-server rt)
             (reset! state nil)
             rt))))

(def ^{:arglists '([pg])}
  stop-nginx
  (std.lib.component/wrap-stop stop-nginx-raw
               [{:key :container
                 :teardown  docker/stop-runtime}]))

(def kill-nginx stop-nginx)

(defn raw-eval-nginx
  "posts a raw lua string to the dev server"
  {:added "4.0"}
  ([{:keys [scheme host port eval-path dev] :as rt} body]
   (let [res   (http/post (str (or scheme "http") "://" host ":" port "/" eval-path)
                          {:body body})]
     (cond (= 200 (:status res))
           (:body res)

           (= 451 (:status res))
           (std.lib.foundation/error "ERROR MESSAGE" res)

           :else
           (std.lib.foundation/error "NOT OK" res)))))

(defn invoke-ptr-nginx
  "evaluates lua ptr and arguments"
  {:added "4.0"}
  ([rt ptr args]
   (default/default-invoke-script
    rt ptr args raw-eval-nginx
    {:main {}
     :emit {:body {:transform lua/default-body-transform}}
     :json :full})))

(defn- rt-nginx-string [{:keys [host port eval-path]}]
  (str "#rt.nginx" [host port eval-path]))

(defimpl NginxRuntime [id state]
  :string rt-nginx-string
  :protocols [std.protocol.component/IComponent
              :suffix "-nginx"
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval raw-eval-nginx
                       -invoke-ptr invoke-ptr-nginx}])

(defn nginx:create
  "creates a dev nginx runtime"
  {:added "4.0"}
  [{:keys [id port] :as m
    :or {id   (std.lib.foundation/sid)
         port (std.lib.network/port:check-available 0)}}]
  (map->NginxRuntime (merge
                      {:id id
                       :tag :nginx
                       :state (atom nil)
                       :host "localhost"
                       :port port
                       :eval-path "eval"
                       :no-server false
                       :lifecycle {:main {}
                                   :emit {:body {:transform lua/default-body-transform}}
                                   :json :full}}
                      m)))

(defn nginx
  "creates and starts a dev nginx runtime"
  {:added "4.0"}
  ([]
   (nginx {}))
  ([m]
   (-> (nginx:create m)
       (std.lib.component/start))))

(def +init+
  [(default/install-type!
    :lua :nginx.instance
    {:type :hara/rt.nginx.instance
     :config {:layout :full}
     :instance {:create nginx:create}})
   
   (default/install-type!
    :lua :nginx
    {:type :hara/rt.nginx
     :instance
     {:create (fn [m]
                (-> {:rt/client {:type :hara/rt.nginx 
                                 :constructor nginx:create}}
                    (merge m)
                    (shared/rt-shared:create)))}})])


(comment
  (./create-tests)
  )
