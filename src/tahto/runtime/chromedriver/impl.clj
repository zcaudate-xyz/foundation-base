(ns tahto.runtime.chromedriver.impl
  (:require [std.protocol.context :as protocol.context]
            [clojure.java.io :as io]
            [tahto.core.pointer :as ptr]
            [tahto.core.impl :as impl]
            [tahto.core.runtime :as default]
            [tahto.core.type-shared :as shared]
            [tahto.runtime.basic.type-common :as common]
            [std.lib.encode :as encode]
            [std.lib :as h :refer [defimpl]]
            [std.json :as json]
            [std.string :as str]
            [net.http :as http]
            [lib.docker :as docker]
            [tahto.runtime.basic.type-bench :as bench]
            [tahto.runtime.basic.impl.process-js :as js]
            [tahto.runtime.chromedriver.connection :as conn]
            [tahto.runtime.chromedriver.util :as util]
            [xt.lang.common-lib :as lib]))

(defn- playwright-browser-paths
  []
  (let [root (io/file (System/getProperty "user.home")
                      ".cache"
                      "ms-playwright")]
    (if (.exists root)
      (->> (file-seq root)
           (filter #(.isFile ^java.io.File %))
           (map #(.getAbsolutePath ^java.io.File %))
           (filter (fn [path]
                     (or (.endsWith path "/chrome")
                         (.endsWith path "/chrome-headless-shell"))))
           sort)
      [])))

(defn- resolve-chrome
  []
  (or (System/getenv "CHROME")
      (some (fn [cmd]
              (when (common/program-exists? cmd)
                cmd))
            ["google-chrome-stable"
             "google-chrome"
             "chromium"
             "chromium-browser"
             "chrome-headless-shell"])
      (first (playwright-browser-paths))
      "chromium"))

(def ^:dynamic *chrome*
  (resolve-chrome))

(def +bootstrap+
  (impl/emit-entry-deps
   lib/return-eval
   {:lang :js
    :layout :flat
    :emit {:lang/format :commonjs}}))

(defn start-browser-bench
  "starts the browser bench"
  {:added "4.0"}
  [{:keys [lang port bench] :as rt}]
  (let [exec (or (:exec bench)
                 [*chrome*
                  "--no-sandbox"
                  "--headless"
                  "--disable-web-security"
                  (str "--remote-debugging-port=" port)
                  "--remote-debugging-address=0.0.0.0"])]
    (-> (if (not (get @bench/*active* port))
          (swap! bench/*active*
                 (fn [m]
                   (assoc m port (bench/create-bench-process
                                  lang port (dissoc bench :exec)
                                  exec []))))
          @bench/*active*)
        (get port))))


(defn start-browser-container
  "starts a browser container"
  {:added "4.0"}
  [rt]
  rt)

(defn start-browser
  "starts the browser bench and connection"
  {:added "4.0"}
  ([{:keys [id state host port container bench url] :as rt}]
   (let [_   (cond container
                   (start-browser-container)
                   
                   (not (or (false? bench)
                            (not= host "localhost")))
                   (start-browser-bench rt))
         _   (h/wait-for-port host port)
         conn (conn/conn-create {:host host
                                 :port port})
         _   (when url
               @(util/page-navigate conn url))
         _  (reset! state conn)
         _  @(util/runtime-evaluate conn +bootstrap+)]
     rt)))

(defn stop-browser-raw
  "stops the browser"
  {:added "4.0"}
  ([{:keys [state host port container bench] :as rt}]
   (let  [_ (conn/conn-close @state)
          _ (reset! state nil)
          _ (if (not  (or container
                          (false? bench)
                          (not= host "localhost")))
              (bench/stop-bench-process port))]
     rt)))

(def ^{:arglists '([pg])}
  stop-browser
  (h/wrap-stop stop-browser-raw
               [{:key :container
                 :teardown  docker/stop-runtime}]))

(def kill-browser stop-browser)

(defn raw-eval-browser
  "evaluates the browser"
  {:added "4.0"}
  ([{:keys [state] :as rt} body]
   (when @state
     (get @(util/runtime-evaluate @state body)
          "value"))))

(defn invoke-ptr-browser
  "invokes the browser pointer"
  {:added "4.0"}
  ([rt ptr args]
   (default/default-invoke-script
    rt ptr args raw-eval-browser
    {:main {:in (fn [body]
                  (impl/emit-as
                   :js [(list 'return-eval body)]))}
     :emit {:native {:suppress true}
            :body {:transform default/return-transform}
            :lang/jsx false}
     :json :full})))

(defn- rt-browser-string [{:keys [host port eval-path]}]
  (str "#rt.chromedriver" [host port eval-path]))

(defimpl BrowserRuntime [id state]
  :string rt-browser-string
  :protocols [std.protocol.component/IComponent
              :suffix "-browser"
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval raw-eval-browser
                       -invoke-ptr invoke-ptr-browser}])

(defn browser:create
  "creates a browser"
  {:added "4.0"}
  [{:keys [id port] :as m
    :or {id   (h/sid)
         port (h/port:check-available 0)}}]
  (map->BrowserRuntime (merge
                        {:id id
                         :tag :browser
                         :state (atom nil)
                         :host "localhost"
                         :port port
                         :lifecycle {:main {}
                                     :emit {}
                                     :json :full}}
                        m)))

(defn browser
  "starts the browser"
  {:added "4.0"}
  ([]
   (browser {}))
  ([m]
   (-> (browser:create m)
       (h/start))))

(defn wrap-browser-state
  "wrapper for the browser"
  {:added "4.0"}
  [f]
  (fn [{:keys [state] :as browser} & args]
    (when @state
      (apply f @state args))))

(def +init+
  [(default/install-type!
    :js :chromedriver.instance
    {:type :tahto/rt.chromedriver
     :config {:layout :full
           :emit {:lang/jsx false}}
     :instance {:create browser:create}})
   
   (default/install-type!
    :js :chromedriver
    {:type :tahto/rt.chromedriver.shared
     :instance
     {:create (fn [m]
                (-> {:rt/client {:type :tahto/rt.chromedriver 
                                 :constructor browser:create}}
                    (merge m)
                    (shared/rt-shared:create)))}})])

(comment
  (def ^:dynamic *chrome* "/Applications/Chromium.app/Contents/MacOS/Chromium"))
