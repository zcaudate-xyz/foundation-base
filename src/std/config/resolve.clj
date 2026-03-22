(ns std.config.resolve
  (:require [code.project :as project]
            [edamame.core :as edn]
            [std.config.common :as common]
            [std.config.global :as global]
            [std.config.secure :as secure]
            [std.fs :as fs]
            [std.lib.collection]
            [std.lib.env]
            [std.lib.invoke :refer [definvoke]]
            [std.string.common]
            [std.string.wrap])
  (:refer-clojure :exclude [resolve load]))

(def +config+  "config.edn")

(def +hara-dir+     (delay (str (fs/path (System/getProperty "user.home") ".hara"))))

(def +user-dir+     (delay (str (fs/path (System/getProperty "user.dir")))))

(def +project-dir+  (delay (str (fs/path "."))))

(def ^:dynamic *current* [])

(def ^:dynamic *path* [])

(defn ex-config
  "helper function for creating config exceptions
 
   (ex-config \"error\" {})"
  {:added "3.0"}
  ([msg]
   (ex-config msg))
  ([msg data]
   (ex-config msg data nil))
  ([msg data e]
   (ex-info msg
            (merge data {:path *path*})
            e)))

(defn- walk
  "modified std.lib.walk/walk function for config"
  {:added "3.0"}
  ([inner outer form]
   (binding [*current*   (conj *current* form)]
     (cond (list? form)
           (outer (apply list (mapv inner form)))

           (instance? clojure.lang.IMapEntry form)
           (let [[k v] form]
             (binding [*path*  (conj *path* k)]
               (outer [k (inner v)])))

           (seq? form)
           (outer (doall (mapv inner form)))

           (instance? clojure.lang.IRecord form)
           (outer (reduce (fn [r x] (conj r (inner x))) form form))

           (coll? form)
           (outer (into (empty form) (mapv inner form)))

           :else
           (outer form)))))

(defn- prewalk
  "modified std.lib.walk/prewalk function for config"
  {:added "3.0"}
  ([f form]
   (walk (partial prewalk f) identity (f form))))

(defn directive?
  "checks if a form is a directive
 
   (directive? \"hello\") => false
 
   (directive? [:eval '(+ 1 2 3)]) => true"
  {:added "3.0"}
  ([form]
   (and (not (instance? clojure.lang.MapEntry form))
        (vector? form)
        (keyword? (first form))
        (not (:data (meta form))))))

(defn resolve-directive
  "resolves directives with error handling
 
   (resolve-directive [:merge {:a 1} {:b 2}])
   => {:a 1, :b 2}"
  {:added "3.0"}
  ([form]
   (let [local {:form form}]
     (try
       (let [output (common/-resolve-directive form)]
         output)
       (catch clojure.lang.ExceptionInfo e
         (throw (ex-config (str "Directive Failed: " (.getMessage e))
                           (assoc local :data (ex-data e)))))
       (catch Throwable t
         (throw (ex-config (str "Directive Failed: " (.getMessage t))
                           local
                           t)))))))

(defn resolve
  "helper function for resolve
 
   (resolve 1) => 1
 
   (resolve [:str [\"hello\" \" \" \"there\"]])
   => \"hello there\""
  {:added "3.0"}
  ([form]
   (prewalk (fn [form]
              (if (directive? form)
                (resolve-directive form)
                form))
            form))
  ([form key]
   (binding [secure/*key* key]
     (resolve form))))

(defn resolve-type
  "converts a string to a type
 
   (resolve-type :text \"3\")
   => \"3\"
 
   (resolve-type :edn \"3\")
   => 3"
  {:added "3.0"}
  ([type content]
   (if content
     (case type
       :text content
       :edn (edn/parse-string content)
       :key (secure/resolve-key content)
       (common/-resolve-type type content)))))

(defn resolve-select
  "resolves selected sections of map data
 
   (resolve-select {:a {:b 1 :c 2}}
                   {:in   [:a]
                    :keys [:b]})
   => {:b 1}"
  {:added "3.0"}
  ([content {:keys [keys in exclude] :as select}]
   (let [content (if (vector? content)
                   (resolve content)
                   content)]
     (cond-> content
       in (get-in in)
       exclude (#(apply dissoc % exclude))
       keys (select-keys keys)))))

(defn resolve-content
  "resolves directive into content
 
   (resolve-content \"hello\" {}) => \"hello\"
 
   (resolve-content \"hello\" {:type :edn}) => 'hello
 
   (resolve-content \"yzRQQ+R5tWYeg7DNp9wvvA==\"
                    {:type :edn
                     :secured true
                     :key \"Bz6i3dhp912F8+P82v1tvA==\"})
   => {:a 1, :b 2}"
  {:added "3.0"}
  ([content {:keys [secured key default format type select] :as opts :or {type :text}}]
   (let [key    (if secured (secure/resolve-key key))
         content (if (and secured content)
                   (if key
                     (secure/decrypt-text content key opts)
                     (throw (ex-config "Key not present" {:input content :opts opts})))
                   content)
         content (or content default)]
     (cond-> (resolve-type type content)
       select (resolve-select select)))))

(definvoke resolve-directive-properties
  "reads from system properties
 
   (resolve-directive-properties [:properties \"user.name\"])
   ;; \"chris\"
   => string?"
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :properties}]
  ([[_ property opts]]
   (let [property (resolve property)]
     (resolve-content (System/getProperty property)
                      opts))))

(definvoke resolve-directive-env
  "reads from system env
 
   (resolve-directive-env [:env \"HOME\"])
   ;; \"/Users/chris\"
   => string?"
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :env}]
  ([[_ env opts]]
   (let [env (resolve env)]
     (resolve-content (System/getenv (or env ""))
                      opts))))

(definvoke resolve-directive-project
  "reads form the project
 
   (resolve-directive-project [:project :name])
   => symbol?"
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :project}]
  ([[_ proj opts]]
   (let [arr ((std.string.wrap/wrap std.string.common/split) proj #"\.")]
     (resolve-content (get-in (global/global :project) arr)
                      opts))))

(definvoke resolve-directive-format
  "formats values to form a string
 
   (resolve-directive-format [:format [\"http://%s:%s\" \"localhost\" \"8080\"]])
   => \"http://localhost:8080\""
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :format}]
  ([[_ args opts]]
   (let [args (map resolve args)]
     (resolve-content (apply format args)
                      opts))))

(definvoke resolve-directive-str
  "joins values to form a string
 
   (resolve-directive-str [:str [\"hello\"
                                 [:env \"SPACE\" {:default \" \"}]
                                 \"world\"]])
   => \"hello world\""
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :str}]
  ([[_ args opts]]
   (let [args (map resolve args)]
     (resolve-content ((std.string.wrap/wrap std.string.common/joinl) args)
                      opts))))

(definvoke resolve-directive-or
  "reads from the first non-nil value
 
   (resolve-directive-or [:or [:env \"SERVER\"] \"<server>\"])
   => \"<server>\""
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :or}]
  ([[_ & args]]
   (loop [args args]
     (cond (empty? args)
           nil

           :else
           (if-let [out (resolve (first args))]
             out
             (recur (rest args)))))))

(definvoke resolve-directive-case
  "switches from the selected element
 
   (System/setProperty \"std.config.profile\" \"dev\")
 
   (resolve-directive-case
    [:case [:properties \"std.config.profile\"]
     \"public\" \"public-profile\"
     \"dev\"    \"dev-profile\"])
   => \"dev-profile\""
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :case}]
  ([[_ sel & pairs]]
   (let [sel   (resolve sel)
         pairs (partition 2 pairs)]
     (loop [pairs pairs]
       (cond (empty? pairs)
             nil

             :else
             (let [[key val] (first pairs)]
               (if (= sel (resolve key))
                 (resolve val)
                 (recur (rest pairs)))))))))

(definvoke resolve-directive-error
  "throws an error with message
 
   (resolve-directive-error [:error \"errored\" {}])
   => (throws)"
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :error}]
  ([[_ msg error]]
   (throw (ex-config msg {}))))

(definvoke resolve-directive-merge
  "merges two entries together
 
   (resolve-directive-merge [:merge
                             {:a {:b 1}}
                             {:a {:c 2}}])
   => {:a {:c 2}}"
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :merge}]
  ([[_ & args]]
   (let [modifier (first (filter keyword? args))
         args     (remove keyword? args)
         merge-fn (case modifier
                    nil merge
                    :default merge
                    :nil (fn [& args] (apply merge (reverse args)))
                    :nested std.lib.collection/merge-nested
                    :nested-nil std.lib.collection/merge-nested-new)]
     (apply merge-fn (map resolve args)))))

(definvoke resolve-directive-eval
  "evaluates a config form
 
   (System/setProperty \"std.config.items\" \"3\")
 
   (resolve-directive-eval [:eval '(+ 1 2
                                      [:properties \"std.config.items\"
                                       {:type :edn}])])
   => 6"
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :eval}]
  ([[_ form]]
   (eval (prewalk (fn [form]
                    (cond (directive? form)
                          (resolve form)

                          (symbol? form)
                          (let [ns (namespace form)]
                            (when ns
                              (or (class? (resolve (symbol ns)))
                                  (require (symbol ns))))
                            form)

                          :else
                          form))
                  form))))

(defn resolve-map
  "resolves a map
 
   (resolve-map {:a {:b [:eval \"hello\"]}}
                [:a :b])
   => \"hello\""
  {:added "3.0"}
  ([input form]
   (let [key-fn (fn [input form]
                  (let [entry (get input form)]
                    (if (directive? entry)
                      (resolve entry)
                      entry)))]
     (cond (keyword? form)
           (key-fn input form)

           (vector? form)
           (reduce key-fn
                   input
                   form)))))

(definvoke resolve-directive-root
  "resolves a directive from the config root
 
   (binding [*current* [{:a {:b [:eval \"hello\"]}}]]
     (resolve-directive-root [:root [:a :b]]))
   => \"hello\""
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :root}]
  ([[_ val opts]]
   (resolve-content (resolve-map (first *current*) val)
                    opts)))

(definvoke resolve-directive-parent
  "resolves a directive from the map parent
 
   (binding [*current* [{:a {:b [:eval \"hello\"]}}
                        {:b [:eval \"hello\"]}]]
     (resolve-directive-parent [:parent :b]))
   => \"hello\""
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :parent}]
  ([[_ val opts]]
   (let [arr ((std.string.wrap/wrap std.string.common/split) val #"\.")]
     (resolve-content (resolve-map (last (filter map? *current*)) arr)
                      opts))))

(definvoke resolve-directive-global
  "resolves a directive form the global map
 
   (resolve-directive-global [:global :user.name])
   => string?"
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :global}]
  ([[_ val opts]]
   (let [arr ((std.string.wrap/wrap std.string.common/split) val #"\.")]
     (resolve-content (resolve-map (global/global :all) arr)
                      opts))))

(defn resolve-path
  "helper function for file resolvers
 
   (resolve-path [\"config/\" [:eval \"hello\"]])
   => \"config/hello\""
  {:added "3.0"}
  ([path]
   (let [path  (resolve path)
         path  (cond (string? path) path
                     (vector? path) (apply str path)
                     :else (throw (ex-config "Not supported" {:input path})))]
     path)))

(definvoke resolve-directive-file
  "resolves to file content
 
   (resolve-directive-file
    [:file \"test-data/std.config/hello.clear\"])
   => \"hello\""
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :file}]
  ([[_ path opts]]
   (let [path     (resolve-path path)
         path     (first (filter fs/exists? [(fs/path path)
                                             (fs/path @+user-dir+ path)]))
         output (if path
                  (resolve-content (slurp path) opts))]
     (if (directive? output)
       (resolve-directive output)
       output))))

(definvoke resolve-directive-resource
  "resolves to resource content
 
   (resolve-directive-resource
    [:resource \"std.config/hello.clear\"])
   => \"hello\""
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :resource}]
  ([[_ path opts]]
   (let [url  (std.lib.env/sys:resource path)]
     (let [output (if url
                    (resolve-content (slurp url) opts))]
       (if (directive? output)
         (resolve-directive output)
         output)))))

(definvoke resolve-directive-include
  "resolves to either current project or config directory
 
   (resolve-directive-include
    [:include \"test-data/std.config/hello.clear\" {:type :edn}])
   => 'hello"
  {:added "3.0"}
  [:method {:multi common/-resolve-directive
            :val :include}]
  ([[_ path opts]]
   (let [path  (resolve-path path)
         path  (or (first (filter fs/exists? [(fs/path path)
                                              (fs/path @+user-dir+ path)]))
                   (std.lib.env/sys:resource path)
                   (first (filter fs/exists?
                                  [(fs/path @+hara-dir+ path)])))
         output (if path
                  (resolve-content (slurp path) opts))]
     (if (directive? output)
       (resolve-directive output)
       output))))

(defn load
  "helper function for file resolvers
 
   (load \"test-data/std.config/config.edn\")
   => map?"
  {:added "3.0"}
  ([]
   (load @+config+))
  ([file]
   (resolve [:include file {:type :edn}])))

(comment
  (require 'std.config.gpg)
  (resolve [:merge {:a 1 :b 2 :c 3}
            [:include "test-data/std.config/config.edn" {:type :edn
                                                         :select {:in [:repositories]}}]]))
