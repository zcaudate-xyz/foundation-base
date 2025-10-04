(ns jvm.classloader.base-classloader
  (:require [std.string :as str]
            [jvm.classloader.common :as common]
            [jvm.protocol :as protocol.classloader]
            [std.object.query :as reflect]
            [std.lib :as h]
            [std.fs :as fs])
  (:import (java.net URL URLClassLoader)))

(defonce +base+ (ClassLoader/getSystemClassLoader))

(defn all-class-urls
  "runs all class urls"
  {:added "4.0"}
  []
  (let [classpath (System/getProperty "java.class.path")
        separator (System/getProperty "path.separator")
        entries   (.split classpath separator)]
    (mapv (fn [path]
            (.. (fs/path path)
                (toUri)
                (toURL)))
          entries)))

(def +class-urls+ (delay (all-class-urls)))

(extend-type (type +base+)
  protocol.classloader/ILoader
  (-has-url?    [loader path]
    (boolean  (protocol.classloader/-get-url loader path)))
  (-get-url     [loader path]
    (first (filter (fn [s]
                     (= (str (fs/relativize
                              (fs/path path)
                              (fs/path s)))
                        ""))
                   @+class-urls+)))
  (-all-urls    [loader]
    @+class-urls+)
  (-add-url     [loader path]
    (throw (ex-info "Cannot add path:" {:path path})))
  (-remove-url  [loader path]
    (throw (ex-info "Cannot remove path:" {:path path}))))

