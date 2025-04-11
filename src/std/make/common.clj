(ns std.make.common
  (:require [std.lib.collection :as c]
            [std.lib.atom :as atom]
            [std.lib.os :as os]
            [std.lib.impl :refer [defimpl]]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.future :as f]
            [std.lib.time :as time]
            [std.string.common :as str]
            [std.fs :as fs]))

(defonce ^:dynamic *triggers* (atom {}))

(defmacro with:triggers
  "sets the mock output flag"
  {:added "4.0"}
  [[triggers] & body]
  `(binding [*triggers* ~triggers]
     ~@body))

(defn triggers-purge
  "purges triggers"
  {:added "4.0"}
  ([]
   (reset! *triggers* {})))

(defn triggers-set
  "sets a trigger"
  {:added "4.0"}
  ([mcfg triggers]
   (swap! *triggers* assoc mcfg triggers)))

(defn triggers-clear
  "clears a trigger"
  {:added "4.0"}
  ([mcfg]
   (swap! *triggers* dissoc mcfg)))

(defn triggers-get
  "gets a trigger"
  {:added "4.0"}
  ([mcfg]
   (get @*triggers* mcfg)))

(defn triggers-list
  "lists all trigers"
  {:added "4.0"}
  ([]
   (c/map-keys (fn [mcfg]
                 (:tag @(:instance mcfg)))
               @*triggers*)))

(defn get-triggered
  "gets all configs given a trigger namespace"
  {:added "4.0"}
  ([] (get-triggered (env/ns-sym)))
  ([ns]
   (keep (fn [[mcfg triggers]]
           (if (some (fn [tns]
                       (if (string? tns)
                         (str/starts-with? (str ns)
                                           tns)
                         (= tns ns)))
                     triggers)
             mcfg))
         @*triggers*)))

;;
;; 
;;

(def ^:dynamic *tmux* true)

(def ^:dynamic *internal-shell* nil)

(defmacro with:internal-shell
  "sets the mock output flag"
  {:added "4.0"}
  [& body]
  `(binding [*internal-shell* true]
     ~@body))

(defn- make-config-string
  "returns the annex string"
  {:added "4.0"}
  ([{:keys [instance]}]
   (let [{:keys [sections
                 triggers] :as m} @instance]
     (str "#make.config " (assoc (select-keys m [:id :container :build :root :params])
                                 :sections (conj (keys sections) :default)
                                 :triggers (keys triggers))))))

(defimpl MakeConfig [instance]
  :final true
  :string make-config-string)

(defn make-config?
  "checks that object is a `make` config"
  {:added "4.0"}
  ([obj]
   (instance? MakeConfig obj)))

(defn get-config-tag
  [mcfg]
  (when (make-config? mcfg)
    (:tag @(:instance mcfg))))

(defn- make-config-defaults
  ([]
   {:ns (.getName *ns*)
    :build ".build"
    :orgfile "Main.org"}))

(defn make-config-map
  "creates a make-config map"
  {:added "4.0"}
  ([{:keys [ns build root params orgfile github default sections triggers] :as m}]
   (let [_ (or default (h/error "Requires a default entry." {:input m}))
         {:keys [ns] :as m}  (merge (make-config-defaults)
                                    m)
         root    (or root (str (fs/file ".")))]
     (assoc m :root root))))

(defn make-config
  "function to create a `make` config"
  {:added "4.0"}
  ([new-config]
   (map->MakeConfig {:instance (atom (make-config-map new-config))})))

(defn make-config-update
  "updates the make-config"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg} new-config]
   (reset! instance (make-config-map new-config))))

(defn make-dir
  "gets the dir specified by the config"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [ns root build]} @instance]
     (str root "/" build))))

(defn make-run
  "runs the `make` executable"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg} & [command]]
   (let [command (or command :build)
         {:keys [tag ns root build]} @instance
         out-dir (make-dir mcfg)
         cmd     ["make" (h/strn command)]]
     (cond *internal-shell*
           (let [opts {:root out-dir
                       :inherit true
                       :wait true
                       :args cmd}
                 _    (do (env/p "--------------------------------------------------------------------")
                          (env/p "MAKE" tag command)
                          (env/p "DIR " (:root opts))
                          (env/p "--------------------------------------------------------------------"))
                 [ms res]  (env/meter-out (os/sh opts))
                 _   (do (env/p)
                         (env/p "ELAPSED" (time/format-ms ms))
                         (env/p))]
             [ms true])
           
           (not *tmux*)
           (apply os/os-run (conj cmd {:root out-dir}))

           :else
           (let [tns  (-> (str (str/upper-case (h/strn command))
                               (if tag (str "-" tag)))
                          (.replaceAll "\\." "-"))]
             (do (os/tmux:new-session "DEV")
                 (os/tmux:new-window  "DEV" tns {:root out-dir})
                 (Thread/sleep 200)
                 (os/tmux:run-command "DEV" tns cmd)))))))

(defn make-run-close
  ([{:keys [instance] :as mcfg} & [command]]
   (let [command (or command :build)
         {:keys [tag ns]} @instance
         tns  (-> (str (str/upper-case (h/strn command))
                       (if tag (str "-" tag)))
                  (.replaceAll "\\." "-"))]
     (os/tmux:kill-window  "DEV" tns))))

(defn make-run-internal
  [mcfg & commands]
  (with:internal-shell
   (mapv (partial make-run mcfg)
         commands)))

(defn make-shell
  "opens a terminal at the location of the `make` directory"
  {:added "4.0"}
  ([mcfg]
   (let [out-dir (make-dir mcfg)]
     (os/os-run "ls" {:root out-dir}))))

(defn- make-tmpl
  ([k]
   (let [sym (symbol (str "make-run-" (h/strn k)))]
     `(defn ~sym
        ([~'mcfg] (make-run ~'mcfg ~k))))))

(def ^:private +make-keywords+)

(h/template-entries [make-tmpl]
  #{:init
    :package
    :release
    :dev
    :test
    :start
    :stop})

(defn make-dir-setup
  "sets up the `make` directory"
  {:added "4.0"}
  [mcfg]
  (let [out-dir (make-dir mcfg)]
    (fs/create-directory out-dir)))

(defn make-dir-exists?
  "checks that the `make` directory exists"
  {:added "4.0"}
  [mcfg]
  (fs/exists? (make-dir mcfg)))

(defn make-dir-teardown
  "deletes the the `make` directory"
  {:added "4.0"}
  ([mcfg]
   (let [out-dir (make-dir mcfg)]
     (if (fs/exists? out-dir)
       [true (fs/delete out-dir)]
       [false]))))

