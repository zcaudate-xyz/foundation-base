(ns hara.runtime.godot.client
  (:require [clojure.string :as str]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as std-impl]
            [std.lib.network :as network]
            [hara.lang.impl :as impl]
            [hara.lang.pointer :as ptr]
            [hara.lang.runtime :as rt]
            [hara.lang.type-shared :as shared]
            [hara.runtime.basic.impl.process-gdscript :as gdscript]
            [hara.runtime.basic.type-common :as common]
            [hara.lang.book :as book]
            [lib.godot.bench :as bench])
  (:import [java.io BufferedReader InputStreamReader]
           [java.net Socket]
           [java.util.concurrent.atomic AtomicInteger]))

(declare map->GodotClient)

(defn client:create
  "creates a Godot client connected to host:port"
  {:added "4.1"}
  [{:keys [host port] :as m}]
  (map->GodotClient (merge {:host (or host "127.0.0.1")
                            :port port}
                           m)))

(defn- connect-socket
  "opens the socket and I/O streams for the client"
  [{:keys [host port] :as client}]
  (let [socket (Socket. ^String host ^int port)
        reader (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        output (.getOutputStream socket)]
    (assoc client
           :socket socket
           :reader reader
           :output output
           :msgid (AtomicInteger. 0))))

(defn- disconnect-socket
  [{:keys [^Socket socket ^BufferedReader reader] :as client}]
  (when reader
    (try (.close reader)
         (catch Throwable _)))
  (when socket
    (try (.close socket)
         (catch Throwable _)))
  (dissoc client :socket :reader :output :msgid))

(defn- next-msgid
  [client]
  (.incrementAndGet ^AtomicInteger (:msgid client)))

(defn raw-eval-godot
  "Sends GDScript source to the Godot server and returns the decoded value."
  {:added "4.1"}
  [{:keys [^BufferedReader reader
           ^java.io.OutputStream output]
    :as rt}
   code]
  (locking rt
    (let [id (next-msgid rt)
          req (json/write {:id id :body code})
          _ (doto output
              (.write (.getBytes (str req "\n")))
              (.flush))
          response (.readLine reader)
          parsed (json/read response json/+keyword-mapper+)]
      (if (== id (:id parsed))
        (if (= "ok" (:status parsed))
          (let [inner (:body parsed)]
            (if (= :error (:type inner))
              (throw (ex-info "Godot eval error" {:code code :error (:value inner)}))
              (:value inner)))
          (throw (ex-info "Godot eval error" {:code code :error (:body parsed)})))
        (throw (ex-info "Godot response id mismatch" {:expected id :response parsed}))))))

(defn- ptr-call-symbol
  "Extracts the called symbol from a call pointer's form."
  {:added "4.1"}
  [ptr]
  (let [form (:form @ptr)]
    (when (seq? form)
      (first form))))

(defn- shorten-defn-name
  "Changes a namespaced GDScript function definition to use its short name."
  {:added "4.1"}
  [def-code short-name]
  (clojure.string/replace-first def-code #"func\s+\w+" (str "func " short-name)))

(defn- ptr-defn-form
  "Returns the emitted GDScript for the pointer's definition, if any."
  {:added "4.1"}
  [ptr meta]
  (when-let [sym (ptr-call-symbol ptr)]
    (let [module-id (symbol (or (namespace sym)
                                (str (:module ptr))))
          id        (symbol (name sym))
          section   (or (:section @ptr) :code)]
      (when-let [entry (get-in (:book meta) [:modules module-id section id])]
        (when-let [form (:form entry)]
          (shorten-defn-name (impl/emit-script form meta)
                             (clojure.string/replace (name sym) "-" "_")))))))

(defn invoke-ptr-godot
  "Invokes a pointer in the Godot runtime."
  {:added "4.1"}
  ([rt ptr args]
   (let [meta     (ptr/ptr-invoke-meta ptr (update (select-keys rt [:library :lang :layout :emit])
                                          :emit
                                          (fnil merge {})
                                          {:body {:transform #'gdscript/default-body-transform}}))
         call     (ptr/ptr-invoke-script ptr args meta)

         def-code (ptr-defn-form ptr meta)
         body     (if def-code
                    (str def-code "\n" call)
                    call)
         in-fn    (fn [body]
                    (gdscript/wrap-godot-eval
                     ((get-in rt [:main :in] identity) body)))
         main     {:in in-fn}]
     (ptr/ptr-invoke rt raw-eval-godot body main :full))))

(defn- rt-godot-string
  [{:keys [id host port]}]
  (str "#rt.godot" [id host port]))

(defn- start-godot
  "Starts the Godot client, launching a bench if no port is provided."
  {:added "4.1"}
  [{:keys [port bench] :as rt}]
  (let [{:keys [port root-dir] :as entry}
        (cond (and (nil? port) bench)
              (bench/bench-start {:port port} bench)

              (nil? port)
              (throw (ex-info "Godot runtime requires :port or :bench" {:rt rt}))

              :else
              {:port port})]
    (-> (connect-socket (assoc rt :port port))
        (assoc :bench-root root-dir
               :bench bench))))

(std-impl/defimpl GodotClient [id]
  :string rt-godot-string
  :protocols [std.protocol.component/IComponent
              :suffix "-godot"
              :method {-start start-godot
                       -stop (fn [{:keys [port bench] :as rt}]
                               (try (when bench
                                      (bench/bench-stop {:port port :bench bench} bench))
                                    (catch Throwable _))
                               (disconnect-socket rt))
                       -kill (fn [{:keys [port bench] :as rt}]
                               (try (when bench
                                      (bench/bench-stop {:port port :bench bench} bench))
                                    (catch Throwable _))
                               (disconnect-socket rt))}
              std.protocol.context/IContext
              :prefix "rt/default-"
              :method {-raw-eval raw-eval-godot
                       -invoke-ptr invoke-ptr-godot}])

(defn godot:create
  "Creates a Godot runtime client."
  {:added "4.1"}
  [{:keys [id host port bench] :as m}]
  (map->GodotClient (merge
                     {:id (or id (f/sid))
                      :tag :godot
                      :host (or host "127.0.0.1")
                      :port port
                      :bench bench
                      :lifecycle {:main {}
                                  :emit {}
                                  :json :full}}
                     m)))

(defn godot
  "Creates and starts a Godot runtime client."
  {:added "4.1"}
  ([m]
   (-> (godot:create m)
       (component/start))))

(defn godot-shared:create
  "Creates a shared Godot runtime client."
  {:added "4.1"}
  [m]
  (-> {:rt/client {:type :hara/rt.godot
                   :constructor godot:create}
       :rt/temp true}
      (merge m)
      (cond-> (:id m) (assoc :rt/id (:id m)))
      (shared/rt-shared:create)))

(def +init+
  [(rt/install-type!
    :gdscript :godot.instance
    {:type :hara/rt.godot
     :config {:layout :full}
     :instance {:create godot:create}})
   (rt/install-type!
    :gdscript :godot
    {:type :hara/rt.godot.shared
     :config {:layout :full}
     :instance {:create godot-shared:create}})])
