(ns std.config.global
  (:require [std.string :as str]
            [std.fs :as fs]
            [code.project :as project]
            [std.lib :as h :refer [definvoke]]))

(def +session+ (atom {}))

(def +cache (atom {:properties nil
                   :env nil
                   :project nil
                   :home nil
                   :all nil}))

(defrecord Global [])

(defmethod print-method Global
  ([v ^java.io.Writer w]
   (.write w (str "#global" (vec (keys v))))))

(defn global?
  "checks if object is of type global
 
   (global? (map->Global {}))
   => true"
  {:added "3.0"}
  ([x]
   (instance? Global x)))

(defn global-raw
  "constructs a global object
 
   (global-raw {[:a] 1
                [:b] 2}
               identity)
   => global?"
  {:added "3.0"}
  ([m key-fn]
   (->> (h/map-keys key-fn m)
        (sort)
        (reverse)
        (reduce (fn [out [k v]]
                  (if (get-in out k)
                    (assoc-in out (conj k :name) v)
                    (assoc-in out k v)))
                (Global.)))))

(defn global-env-raw
  "returns the global object for system env
 
   (:home (global-env-raw))
   => string?"
  {:added "3.0"}
  ([]
   (global-raw (System/getenv)
               (fn [k]
                 ((str/wrap str/path-split) (keyword (str/lower-case k)) "_")))))

(defn global-properties-raw
  "returns the global object for system properties
 
   (:java (global-properties-raw))
   ;; {:compile {:path \"./target/classes\"}, :debug \"false\"}
   => map?"
  {:added "3.0"}
  ([]
   (global-raw (System/getProperties)
               (fn [k] ((str/wrap str/path-split) (keyword k) ".")))))

(defn global-project-raw
  "returns the global object for thecurrent project
 
   (:group (global-project-raw))
   => \"xyz.zcaudate\""
  {:added "3.0"}
  ([]
   (try (project/project)
        (catch clojure.lang.ExceptionInfo e))))

(defn global-env-file-raw
  "gets env from env.edn"
  {:added "4.0"}
  ([]
   (let [path (fs/path "./env.edn")]
     (if (fs/exists? path)
       (read-string (slurp path))))))

(defn global-home-raw
  "returns the global object for all global types
 
   (global-home-raw)
   => anything"
  {:added "3.0"}
  ([]
   (let [path (fs/path (System/getProperty "user.home") ".hara" "global.edn")]
     (if (fs/exists? path)
       (read-string (slurp path))))))

(defn global-session-raw
  "returns the global object within the current session
 
   (global-session-raw)
   => {}"
  {:added "3.0"}
  ([]
   @+session+))

(defn global-all-raw
  "returns the global object for all global types
 
   (:group (global-all-raw))
   => \"xyz.zcaudate\""
  {:added "3.0"}
  ([]
   (h/merge-nested (global-env-raw)
                   (global-properties-raw)
                   (global-home-raw)
                   (global-project-raw)
                   (global-env-file-raw)
                   (global-session-raw))))

(def +global+
  {:env        global-env-raw
   :properties global-properties-raw
   :home       global-home-raw
   :project    global-project-raw
   :env-file   global-env-file-raw
   :session    global-session-raw
   :all        global-all-raw})

(defn global
  "returns the entire global map
 
   (global :all)"
  {:added "3.0"}
  ([]
   (global :all))
  ([k]
   (global k {:cached false}))
  ([k {:keys [cached]}]
   (if-let [prop (and cached
                      (get @+cache k))]
     prop
     (let [prop ((get +global+ k))]
       (swap! +cache assoc k prop)
       prop))))
