(ns lib.jdbc.impl
  "Protocol implementations. Mainly private api"
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [lib.jdbc.protocol :as protocol]
            [lib.jdbc.types :as types]
            [lib.jdbc.resultset :refer [result-set->lazyseq result-set->vector]]
            [lib.jdbc.constants :as constants])
  (:import (lib.jdbc.types Cursor)
           (java.net URI)
           (java.util Properties)
           (java.sql Connection
                     DriverManager
                     PreparedStatement
                     Statement)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Connection constructors implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare ^:private dbspec->connection)
(declare ^:private uri->dbspec)
(declare ^:private querystring->map)
(declare ^:private map->properties)

(extend-protocol protocol/IConnection
  java.sql.Connection
  (-connection [this] this)

  javax.sql.DataSource
  (-connection [ds]
    (.getConnection ds))

  clojure.lang.IPersistentMap
  (-connection [dbspec]
    (dbspec->connection dbspec))

  java.net.URI
  (-connection [uri]
    (-> (uri->dbspec uri)
        (dbspec->connection)))

  java.lang.String
  (-connection [uri]
    (protocol/-connection (java.net.URI. uri))))

(defn- dbspec->connection
  "Create a connection instance from dbspec."
  [{:keys [subprotocol subname user password
           name vendor host port datasource classname]
    :as dbspec}]
  (cond
    (and name vendor)
    (let [host   (or host "127.0.0.1")
          port   (if port (str ":" port) "")
          dbspec (-> (dissoc dbspec :name :vendor :host :port)
                     (assoc :subprotocol vendor
                            :subname (str "//" host port "/" name)))]
      (dbspec->connection dbspec))

    (and subprotocol subname)
    (let [url (format "jdbc:%s:%s" subprotocol subname)
          options (dissoc dbspec :subprotocol :subname)]

      (when classname
        (Class/forName classname))

      (DriverManager/getConnection url (map->properties options)))

    ;; NOTE: only for backward compatibility
    (and datasource)
    (protocol/-connection datasource)

    :else
    (throw (IllegalArgumentException. "Invalid dbspec format"))))

(defn uri->dbspec
  "Parses a dbspec as uri into a plain dbspec. This function
   accepts `java.net.URI` or `String` as parameter."
  {:added "4.0"}
  [^URI uri]
  (let [host (.getHost uri)
        port (.getPort uri)
        path (.getPath uri)
        scheme (.getScheme uri)
        userinfo (.getUserInfo uri)]
    (merge
     {:subname (if (pos? port)
                 (str "//" host ":" port path)
                 (str "//" host path))
      :subprotocol scheme}
     (when userinfo
       (let [[user password] (str/split userinfo #":")]
         {:user user :password password}))
     (querystring->map uri))))

(defn- querystring->map
  "Given a URI instance, return its querystring as
  plain map with parsed keys and values."
  [^URI uri]
  (when-let [^String query (.getQuery uri)]
    (when-not (str/blank? query)
      (->> (for [^String kvs (.split query "&")] (into [] (.split kvs "=")))
           (into {})
           (walk/keywordize-keys)))))

(defn- map->properties
  "Convert hash-map to java.utils.Properties instance. This method is used
  internally for convert dbspec map to properties instance, but it can
  be usefull for other purposes."
  [data]
  (let [p (Properties.)]
    (dorun (map (fn [[k v]] (.setProperty p (name k) (str v))) (seq data)))
    p))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IExecute implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(extend-protocol protocol/IExecute
  java.lang.String
  (-execute [sql conn opts]
    (with-open [^Statement stmt (.createStatement ^Connection conn)]
      (.execute stmt ^String sql)
      (loop [counts []]
        (let [uc (.getUpdateCount stmt)
              counts (if (>= uc 0) (conj counts uc) counts)]
          (if (and (not (.getMoreResults stmt)) (= -1 (.getUpdateCount stmt)))
            (seq counts)
            (recur counts))))))

  clojure.lang.IPersistentVector
  (-execute [sqlvec conn opts]
    (with-open [^PreparedStatement stmt (protocol/-prepared-statement sqlvec conn opts)]
      (let [counts (.executeUpdate stmt)]
        (if (:returning opts)
          (with-open [rs (.getGeneratedKeys stmt)]
            (result-set->vector conn rs opts))
          counts))))

  PreparedStatement
  (-execute [^PreparedStatement stmt ^Connection conn opts]
    (.executeUpdate stmt)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IFetch implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(extend-protocol protocol/IFetch
  java.lang.String
  (-fetch [^String sql ^Connection conn opts]
    (with-open [^PreparedStatement stmt (protocol/-prepared-statement sql conn opts)]
      (let [^ResultSet rs (.executeQuery stmt)]
        (result-set->vector conn rs opts))))

  clojure.lang.IPersistentVector
  (-fetch [^clojure.lang.IPersistentVector sqlvec ^Connection conn opts]
    (with-open [^PreparedStatement stmt (protocol/-prepared-statement sqlvec conn opts)]
      (let [^ResultSet rs (.executeQuery stmt)]
        (result-set->vector conn rs opts))))

  PreparedStatement
  (-fetch [^PreparedStatement stmt ^Connection conn opts]
    (let [^ResultSet rs (.executeQuery stmt)]
      (result-set->vector conn rs opts))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PreparedStatement constructors implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare ^:private prepared-statement*)

(extend-protocol protocol/IPreparedStatement
  String
  (-prepared-statement [sql conn options]
    (prepared-statement* conn [sql] options))

  clojure.lang.IPersistentVector
  (-prepared-statement [sql-with-params conn options]
    (prepared-statement* conn sql-with-params options))

  PreparedStatement
  (-prepared-statement [o _ _] o))

(defn- prepared-statement*
  "Given connection and query, return a prepared statement."
  ([^Connection conn sqlvec] (prepared-statement* conn sqlvec {}))
  ([^Connection conn sqlvec {:keys [result-type result-concurency fetch-size
                                    max-rows holdability returning]
                             :or {result-type :forward-only
                                  result-concurency :read-only}
                             :as options}]
   (let [sqlvec (if (string? sqlvec) [sqlvec] sqlvec)
         ^String sql (first sqlvec)
         params (rest sqlvec)

         ^PreparedStatement
         stmt (cond
                returning
                (if (or (= :all returning) (true? returning))
                  (.prepareStatement conn sql java.sql.Statement/RETURN_GENERATED_KEYS)
                  (.prepareStatement conn sql
                                     #^"[Ljava.lang.String;" (into-array String (mapv name returning))))

                holdability
                (.prepareStatement conn sql
                                   (result-type constants/resultset-options)
                                   (result-concurency constants/resultset-options)
                                   (holdability constants/resultset-options))
                :else
                (.prepareStatement conn sql
                                   (result-type constants/resultset-options)
                                   (result-concurency constants/resultset-options)))]

     ;; Set fetch-size and max-rows if provided by user
     (when fetch-size (.setFetchSize stmt fetch-size))
     (when max-rows (.setMaxRows stmt max-rows))
     (when (seq params)
       (->> params
            (map-indexed #(protocol/-set-stmt-parameter! %2 conn stmt (inc %1)))
            (dorun)))
     stmt)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Default implementation for type conversions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(extend-protocol protocol/ISQLType
  Object
  (-as-sql-type [this conn] this)
  (-set-stmt-parameter! [this conn ^PreparedStatement stmt ^Long index]
    (.setObject stmt index (protocol/-as-sql-type this conn)))

  nil
  (-as-sql-type [this conn] nil)
  (-set-stmt-parameter! [this conn ^PreparedStatement stmt index]
    (.setObject stmt index (protocol/-as-sql-type nil conn))))


(extend-protocol protocol/ISQLResultSetReadColumn
  Object
  (-from-sql-type [this conn metadata i] this)

  Boolean
  (-from-sql-type [this conn metadata i] (= true this))

  nil
  (-from-sql-type [this conn metadata i] nil))

(defn cursor->lazyseq
  "converts a cursor to a lazyseq"
  {:added "4.0"}
  [cursor opts]
  (let [^PreparedStatement stmt (.-stmt ^Cursor cursor)
        ^Connection conn (.getConnection stmt)
        ^ResultSet rs (.executeQuery stmt)]
    (result-set->lazyseq conn rs opts)))
