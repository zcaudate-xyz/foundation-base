(ns jvm.classloader.url-classloader
  (:require [std.string :as str]
            [jvm.classloader.common :as common]
            [jvm.protocol :as protocol.classloader]
            [std.object.query :as reflect]
            [std.lib :as h])
  (:import (java.net URL URLClassLoader)))

(defonce loader-access-ucp  (reflect/query-class URLClassLoader ["ucp" :#]))

(defonce +ucp-type+ (:type loader-access-ucp))

(defonce ucp-get-urls (reflect/query-class +ucp-type+ ["getURLs" :#]))

(defonce ucp-access-lmap (reflect/query-class +ucp-type+ ["lmap" :#]))

(defonce ucp-access-urls (reflect/query-class +ucp-type+ ["urls" :#]))

(defonce ucp-access-path (reflect/query-class +ucp-type+ ["path" :#]))

(defonce ucp-access-loaders (reflect/query-class +ucp-type+ ["loaders" :#]))

(defonce ucp-get-urls (reflect/query-class +ucp-type+ ["getURLs" :#]))

(defonce ucp-add-url  (reflect/query-class +ucp-type+ ["addURL" :#]))

(defn ucp-remove-url
  "removes the url"
  {:added "4.0"}
  [ucp ^URL entry]
  (let [^java.util.ArrayList paths (ucp-access-path ucp)      ;; util.ArrayList
        ^java.util.Stack urls (ucp-access-urls ucp)       ;; util.Stack
        ^java.util.ArrayList loaders (ucp-access-loaders ucp) ;; util.ArrayList 
        ^java.util.HashMap lmap (ucp-access-lmap ucp)       ;; util.HashMap

        ;;find entry in paths
        paths-entry (h/element-at #(= (str %) (str entry)) paths)
        _  (if paths-entry (.remove paths paths-entry))

        ;;find entry in stack and delete it
        urls-entry (h/element-at #(= (str %) (str entry)) urls)
        _ (if urls-entry (.remove urls urls-entry))

        ;; find loader in lookup
        url-key (h/element-at #(= %
                                  (str (.getProtocol entry)
                                       "://"
                                       (.getFile entry)))
                              (keys lmap))
        loader-entry (.get lmap url-key)

        ;; remove entries from loader and stack 
        _ (if url-key (.remove lmap url-key))
        _ (if loader-entry (.remove loaders loader-entry))]
    [paths-entry urls-entry url-key loader-entry]))

(defmethod print-method URLClassLoader
  ([^URLClassLoader v ^java.io.Writer w]
   (.write w (str "#loader@"
                  (.hashCode v)
                  (->> (protocol.classloader/-all-urls v)
                       (mapv #(-> (str %)
                                  (str/split #"/")
                                  last)))))))

(extend-type (identity +ucp-type+) ;; jdk.internal.loader.URLClassPath
  protocol.classloader/ILoader
  (-has-url?    [ucp path]
    (boolean (protocol.classloader/-get-url ucp path)))
  (-get-url     [ucp path]
    (h/element-at #(= (str %) (str (common/to-url path)))
                  (ucp-access-path ucp)))
  (-all-urls    [ucp]
    (seq (ucp-get-urls ucp)))
  (-add-url     [ucp path]
    (if (not (protocol.classloader/-has-url? ucp path))
      (ucp-add-url ucp  (common/to-url path))))
  (-remove-url  [ucp path]
    (if (protocol.classloader/-has-url? ucp path)
      (ucp-remove-url ucp (common/to-url path)))))

(extend-type URLClassLoader
  protocol.classloader/ILoader
  (-has-url?    [loader path]
    (protocol.classloader/-has-url?   (loader-access-ucp loader) path))
  (-get-url     [loader path]
    (protocol.classloader/-get-url    (loader-access-ucp loader) path))
  (-all-urls    [loader]
    (protocol.classloader/-all-urls   (loader-access-ucp loader)))
  (-add-url     [loader path]
    (protocol.classloader/-add-url    (loader-access-ucp loader) path))
  (-remove-url  [loader path]
    (protocol.classloader/-remove-url (loader-access-ucp loader) path)))
