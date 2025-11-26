(ns std.lib.env
  (:require [clojure.pprint :as pprint]
            [std.lib.atom :as at]
            [std.lib.time :as t]
            [std.lib.foundation :as f]
            [std.string.common :as str])
  (:refer-clojure :exclude [prn require pr with-out-str])
  (:import (java.io StringWriter
                    PrintWriter)))

(def ^:dynamic *debug* false)

(defonce +debug+ (atom #{}))

(def ^:dynamic *local* true)

(defonce +local+ (atom {:print clojure.core/print
                        :println clojure.core/println
                        :prn clojure.core/prn
                        :pprint pprint/pprint
                        :pprint-str #(clojure.core/with-out-str (pprint/pprint %))}))

(defmacro with-system
  "with system print instead of local"
  {:added "3.0"}
  ([& body]
   `(binding [*local* false]
      ~@body)))

(defmacro with-out-str
  "gets the local string
 
   (print/with-out-str (print/print \"hello\"))
   => \"hello\""
  {:added "3.0"}
  ([& body]
   `(binding [*local* false]
      (clojure.core/with-out-str ~@body))))

(defn ns-sym
  "returns the namespace symbol"
  {:added "3.0"}
  ([]
   (if (instance? clojure.lang.Namespace *ns*)
     (.getName *ns*)
     (throw (ex-info "Not a namespace" {:input *ns*})))))

(defn ns-get
  "gets a symbol in the current namespace"
  {:added "3.0"}
  ([sym]
   (ns-get clojure.core/*ns* sym))
  ([ns k]
   (let [elem (resolve (symbol (str ns) (str k)))]
     (if elem
       @elem
       (ex-info (format "Cannot find %s in namespace" k)
                {:ns ns})))))

(defn require
  "a concurrency safe require"
  {:added "3.0"}
  ([sym]
   (#'clojure.core/serialized-require sym)))

(defn dev?
  "checks if current environment is dev"
  {:added "3.0"}
  ([]
   (-> (try
         (the-ns 'nrepl.core)
         (catch Exception e))
       boolean)))

(defn ^java.net.URL sys:resource
  "finds a resource on class path"
  {:added "3.0"}
  ([n] (sys:resource n (.getContextClassLoader (Thread/currentThread))))
  ([n ^ClassLoader loader] (.getResource loader n)))

(defn sys:resource-cached
  "caches the operation on a resource call"
  {:added "4.0"}
  [atom path f]
  (let [url   (sys:resource path)
        fpath (.getFile url)
        t     (if fpath (.lastModified (java.io.File. fpath)))]
    (cond (not t)
          (f url)

          :else
          (at/swap-return! atom
            (fn [m]
              (let [{:keys [time content]} (get m path)]
                (if (= time t)
                  [content m]
                  (let [content (f url)]
                    [content (assoc m path {:time t :content content})]))))))))

(defonce +sys:resource-content+
  (atom {}))

(defn sys:resource-content
  "reads the content"
  {:added "3.0"}
  ([path]
   (sys:resource-cached +sys:resource-content+
                        path
                        slurp)))

(defn ^java.net.URL sys:ns-url
  "gets the url for ns"
  {:added "4.0"}
  ([]
   (sys:ns-url (.getName *ns*)))
  ([ns]
   (-> (name ns)
       (.replace \- \_)
       (.replace \. \/)
       (str ".clj")
       (sys:resource))))

(defn sys:ns-file
  "gets the file for ns"
  {:added "4.0"}
  ([]
   (sys:ns-file (.getName *ns*)))
  ([ns]
   (-> (.getFile (sys:ns-url ns))
       (java.io.File.)
       (str))))

(defn sys:ns-dir
  "gets the dir for ns"
  {:added "4.0"}
  ([]
   (sys:ns-dir (.getName *ns*)))
  ([ns]
   (-> (.getFile (sys:ns-url ns))
       (java.io.File.)
       (.getParent))))

(defn close
  "closes any object implementing `java.io.Closable`"
  {:added "3.0"}
  ([^java.io.Closeable obj]
   (.close obj)))

(defn local:set
  "sets the local functions"
  {:added "3.0"}
  ([k v & more]
   (apply swap! +local+ assoc k v more)))

(defn local:clear
  "clears the local functions"
  {:added "3.0"}
  ([k & more]
   (apply swap! +local+ dissoc k more)))

(defn local
  "applies the local function"
  {:added "3.0"}
  ([k & args]
   (apply (get @+local+ k) args)))

(defn p
  "shortcut to `(local :println)`"
  {:added "3.0"}
  ([& args]
   (apply local :println args)))

(defn pr
  "shortcut to `(local :println)`"
  {:added "3.0"}
  ([& args]
   (apply local :print args)))

(defn pp-str
  "pretty prints a string"
  {:added "4.0"}
  [& args]
  (str/join "\n" (map #(local :pprint-str %) args)))

(defn pp-fn
  "the pp print function"
  {:added "4.0"}
  ([& body]
   (doseq [v body]
     (p (pp-str v) "\n"))))

(defmacro ^{:style/indent 1} pp
  "shortcut to `(local :pprint)`"
  {:added "3.0"}
  ([& body]
   (let [{:keys [line column]} (meta &form)]
     `(do (local :println (format "%s (%d:%d)"
                                  ~(str (ns-sym))
                                  ~line
                                  ~column)
                 "\n\n"
                 ~@(map (fn [v]
                          `(str (pp-str ~v) "\n\n"))
                        body))))))

(defmacro do:pp
  "doto `pp`"
  {:added "4.0"}
  ([v]
   `(doto ~v
      ~(with-meta `(pp) (meta &form)))))

(defn pl-add-lines
  "helper function for pl"
  {:added "3.0"}
  ([body]
   (pl-add-lines body [nil nil] [2 0]))
  ([body [start end]]
   (pl-add-lines body [start end] [2 0]))
  ([body [start end] [pad-h pad-v]]
   (let [lines (vec (str/split-lines body))
         max   (count lines)
         start (dec (or start 1))
         start (if (neg? start) 0 start)
         end   (or end (inc max))
         end   (if (< max end) max end)]
     (->> (map (fn [i text]
                 (format (str " %0" pad-h "d  %s") i text))
               (range (inc start)  max)
               (subvec lines start end))
          (str/join (apply str "\n" (repeat pad-v "\n")))))))

(defmacro pl
  "print with lines"
  {:added "3.0"}
  ([body]
   (with-meta `(pl ~body nil) (meta &form)))
  ([body range]
   (let [{:keys [line column]} (meta &form)]
     `(do (local :println (format "%s (%d:%d)"
                                  ~(str (ns-sym))
                                  ~line
                                  ~column))
          (local :println
                 (pl-add-lines (with-out-str (println ~body))
                               ~range)
                 "\n")))))

(defmacro do:pl
  "doto `pl`"
  {:added "4.0"}
  ([v]
   `(doto ~v
      ~(with-meta `(pl) (meta &form)))))

(defmacro pl:fn
  "creates a pl function"
  {:added "4.0"}
  ([]
   (list 'fn '[body]
         (with-meta
           '(std.lib.env/pl body)
           (meta &form)))))

(defmacro prn
  "`prn` but also includes namespace and file info"
  {:added "3.0"}
  ([& body]
   (let [{:keys [line column]} (meta &form)]
     `(do (local :println (format "%s (%d:%d)"
                                  ~(str (ns-sym))
                                  ~line
                                  ~column))
          (local :println
                 ~@(map (fn [v]
                          `(pr-str ~v))
                        body)
                 "\n")))))

(defmacro do:prn
  "doto `prn`"
  {:added "4.0"}
  ([& args]
   `(doto ~(last args)
      ~(with-meta `(prn ~@(butlast args)) (meta &form)))))

(defmacro prn:fn
  "creates a prn function"
  {:added "4.0"}
  ([]
   (list 'fn '[& args]
         (with-meta
           '(std.lib.env/prn (vec args))
           (meta &form)))))

(defn prf
  "pretty prints with format"
  {:added "4.0"}
  [v & [no-pad]]
  (when (not no-pad) (local :println ""))
  (if (string? v)
    (local :println (pl-add-lines v []))
    (do (when (not no-pad)
          (local :println ""))
        (local :pprint v))))

(defmacro prfn
  "`prn` but also includes namespace and file info"
  {:added "3.0"}
  ([& body]
   (let [{:keys [line column]} (meta &form)]
     `(do (local :println (format "%s (%d:%d)"
                                  ~(str (ns-sym))
                                  ~line
                                  ~column))
          (doseq [v# [~@body]]
            (if (string? v#)
              (local :println (pl-add-lines v# []))
              (local :pprint v#)))
          (local :println "")))))

(defmacro ^{:style/indent 1} meter
  {:added "3.0"}
  ([label & body]
   (let [[label body] (if (keyword? label)
                        [label body]
                        [nil (cons label body)])]
     `(let [~'out (volatile! nil)]
        ~(with-meta `(prn ~@(if label [label])
                          (t/bench-ms {:no-gc true} (vreset! ~'out (do ~@body)))
                          @~'out)
           (meta &form))
        @~'out))))

(defn wrap-print
  [f & [format-fn]]
  (fn [& args]
    (let [res (apply f args)]
      (p ((or format-fn
              identity) res))
      res)))

(defmacro meter-out
  "measures and output meter"
  {:added "4.0"}
  ([& body]
   `(let [~'out (volatile! nil)]
      [(t/bench-ms {:no-gc true} (vreset! ~'out (do ~@body)))
       @~'out])))

(defn throwable-string
  "creates a string from a throwable
 
   (throwable-string (ex-info \"ERROR\" {}))
   => string?"
  {:added "3.0"}
  ([^Throwable t]
   (let [errors (StringWriter.)
         _ (.printStackTrace t (PrintWriter. errors))]
     (str errors))))

(defmacro explode
  "prints the stacktrace for an exception
 
   (explode (throw (ex-info \"Error\" {})))"
  {:added "3.0"}
  ([& body]
   (let [{:keys [line column]} (meta &form)]
     `(try ~@body (catch Throwable ~'t
                    (local :println (format "%s (%d:%d)"
                                            ~(str (ns-sym))
                                            ~line
                                            ~column)
                           "\n"
                           (throwable-string ~'t)))))))

;;

(defn match-filter
  "matches given a range of filters"
  {:added "4.0"}
  [filt id]
  (cond (or (fn?  filt)
            (var? filt))
        (f/suppress (filt id))

        (or (string? filt)
            (symbol? filt))
        (.startsWith (str id) (str filt))

        (f/regexp? filt)
        (boolean (re-find filt (str id)))
        
        (set? filt)   (filt id)
        
        (list? filt)  (every? #(match-filter % id)
                              filt)
        
        
        (vector? filt) (some #(match-filter % id)
                             filt)
        
        :else
        (throw (ex-info "Filt not valid" {:filt filt}))))

(defn dbg-print
  "TODO"
  {:added "4.0"}
  [ns-str {:keys [line column]} & args]
  (when (or *debug*
            (some (fn [filt]
                    (match-filter filt ns-str))
                  (seq @+debug+)))
    (local :println (format "%s (%d:%d)"
                            ns-str
                            line
                            column))
    (doseq [arg args]
      (local :pprint arg))))

(defmacro dbg
  "TODO"
  {:added "4.0"}
  ([& body]
   (let [{:keys [line column]} (meta &form)]
     `(do (dbg-print ~(meta &form)
                     ~(str (ns-sym))
                     ~@body)))))

(defmacro with:dbg
  "TODO"
  {:added "4.0"}
  [flag & body]
  `(binding [*debug* ~flag]
     ~@body))

(defn dbg-global
  "TODO"
  {:added "4.0"}
  ([] *debug*)
  ([flag]
   (alter-var-root #'*debug* (fn [_] flag))))

(defn dbg:add-filters
  "TODO"
  {:added "4.0"}
  ([& filters]
   (swap! +debug+
          (fn [coll] (apply conj coll filters)))))

(defn dbg:remove-filters
  "TODO"
  {:added "4.0"}
  ([& filters]
   (swap! +debug+
          (fn [coll] (apply disj coll filters)))))

