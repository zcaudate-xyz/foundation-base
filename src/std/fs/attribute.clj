(ns std.fs.attribute
  (:require [std.fs.common :as common]
            [std.fs.path :as path]
            [std.string :as str]
            [std.lib :as h])
  (:import (java.nio.file Path FileSystems Files LinkOption)
           (java.nio.file.attribute FileAttribute FileTime PosixFilePermissions PosixFilePermission)))

(def ^:dynamic *empty* (make-array FileAttribute 0))

(defn to-mode-string
  "transforms mode numbers to mode strings
 
   (to-mode-string \"455\")
   => \"r--r-xr-x\"
 
   (to-mode-string \"777\")
   => \"rwxrwxrwx\""
  {:added "3.0"}
  ([s]
   (->> s
        (map (fn [ch]
               (case ch
                 \0 "---"
                 \1 "--x"
                 \2 "-w-"
                 \3 "-wx"
                 \4 "r--"
                 \5 "r-x"
                 \6 "rw-"
                 \7 "rwx")))
        (apply str))))

(defn to-mode-number
  "transforms mode numbers to mode strings
 
   (to-mode-number \"r--r-xr-x\")
   => \"455\"
 
   (to-mode-number \"rwxrwxrwx\")
   => \"777\""
  {:added "3.0"}
  ([s]
   (->> (partition 3 s)
        (map #(apply str %))
        (map (fn [mode]
               (let [hist (frequencies mode)]
                 (reduce-kv (fn [out k v]
                              (+ out (or (if (= 1 v)
                                           (case k
                                             \r 4
                                             \w 2
                                             \x 1
                                             nil))
                                         0)))
                            0
                            hist))))
        (apply str))))

(defonce +file-permissions+ (h/enum-map> PosixFilePermission))

(defn to-permissions
  "transforms mode to permissions
 
   (to-permissions \"455\")
   => (contains [:owner-read
                 :group-read
                 :group-execute
                 :others-read
                 :others-execute] :in-any-order)"
  {:added "3.0"}
  ([s]
   (->> (to-mode-string s)
        (PosixFilePermissions/fromString)
        (map (comp keyword str/spear-case str)))))

(defn from-permissions
  "transforms permissions to mode
 
   (from-permissions [:owner-read
                      :group-read
                      :group-execute
                      :others-read
                      :others-execute])
   => \"455\""
  {:added "3.0"}
  ([modes]
   (->> (map +file-permissions+ modes)
        set
        (PosixFilePermissions/toString)
        (to-mode-number))))

(defn owner
  "returns the owner of the file
 
   (owner \"project.clj\")
   => string?"
  {:added "3.0"}
  ([path]
   (str (Files/getOwner (path/path path) common/*no-follow*))))

(defn lookup-owner
  "lookup the user registry for the name
 
   (lookup-owner \"WRONG\")
   => (throws)"
  {:added "3.0"}
  ([owner]
   (-> (FileSystems/getDefault)
       (.getUserPrincipalLookupService)
       (.lookupPrincipalByName owner))))

(defn set-owner
  "sets the owner of a particular file
 
   (set-owner \"test\" \"WRONG\")
   => (throws)"
  {:added "3.0"}
  ([path owner]
   (let [path (path/path path)
         principle (lookup-owner owner)]
     (Files/setOwner path principle))))

(defn lookup-group
  "lookup the user registry for the name
 
   (lookup-group \"WRONG\")
   => (throws)"
  {:added "3.0"}
  ([group]
   (-> (FileSystems/getDefault)
       (.getUserPrincipalLookupService)
       (.lookupPrincipalByGroupName group))))

(deftype Attribute [_name _value]
  FileAttribute
  (name  [attr] _name)
  (value [attr] _value))

(defn attr
  "creates an attribute for input to various functions"
  {:added "3.0"}
  ([name value]
   (attr name value nil))
  ([name value prefix]
   (Attribute. (if prefix
                 (str prefix ":" name)
                 name)
               value)))

(defn attr-value
  "adjusts the attribute value for input"
  {:added "3.0"}
  ([k v]
   (let [nk (name k)]
     (cond (.endsWith nk "owner") (lookup-owner v)
           (.endsWith nk "group") (lookup-group v)
           (.endsWith nk "fileKey") (throw (Exception. "Cannot make fileKey"))
           (.endsWith nk "permissions") (PosixFilePermissions/fromString v)
           (#{:ctime :last-access-time :last-modified-time :creation-time} k)
           (FileTime/fromMillis v)
           :else v))))

(defn map->attr-array
  "converts a clojure map to an array of attrs"
  {:added "3.0"}
  ([m]
   (map->attr-array m (name common/*system*)))
  ([m prefix]
   (->> m
        (reduce-kv (fn [out k v]
                     (conj out (attr ((str/wrap str/camel-case) (name k))
                                     (attr-value k v)
                                     prefix)))
                   [])
        (into-array FileAttribute))))

(defn attrs->map
  "converts the map of attributes into a clojure map"
  {:added "3.0"}
  ([attrs]
   (reduce (fn [out [k v]]
             (assoc out ((str/wrap str/spear-case) (keyword k))
                    (cond (= k "owner") (str v)
                          (= k "group") (str v)
                          (= k "fileKey") (str v)
                          (= k "permissions") (PosixFilePermissions/toString v)
                          (instance? FileTime v) (.toMillis ^FileTime v)
                          :else v)))
           {}
           attrs)))

(defn attributes
  "shows all attributes for a given path
 
   (attributes \"project.clj\")
   ;;    {:owner \"chris\",
   ;;     :group \"staff\",
   ;;     :permissions \"rw-r--r--\",
   ;;     :file-key \"(dev=1000004,ino=2351455)\",
   ;;     :ino 2351455,
   ;;     :is-regular-file true.
   ;;     :is-directory false, :uid 501,
   ;;     :is-other false, :mode 33188, :size 4342,
   ;;     :gid 20, :ctime 1476755481000,
   ;;     :nlink 1,
   ;;     :last-access-time 1476755481000,
   ;;     :is-symbolic-link false,
   ;;     :last-modified-time 1476755481000,
   ;;     :creation-time 1472282953000,
   ;;     :dev 16777220, :rdev 0}
   => map"
  {:added "3.0"}
  ([path]
   (-> (path/path path)
       (Files/readAttributes (str (name common/*system*) ":*")
                             ^"[Ljava.nio.file.LinkOption;" common/*no-follow*)
       (attrs->map))))

(defn set-attributes
  "sets all attributes for a given path
 
   (set-attributes \"project.clj\"
                   {:owner \"chris\",
                    :group \"staff\",
                    :permissions \"rw-rw-rw-\"})
   ;;=> #path:\"/Users/chris/Development/chit/lucidity/project.clj\""
  {:added "3.0"}
  ([path m]
   (reduce-kv (fn [_ k v]
                (-> (path/path path)
                    (Files/setAttribute (str (name common/*system*) ":"
                                             ((str/wrap str/camel-case) (name k)))
                                        (attr-value k v)
                                        common/*no-follow*)))
              nil
              m)))
