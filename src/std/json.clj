(ns std.json
  (:require [std.lib.atom :as at]
            [std.lib.env :as env]
            [std.string.case])
  (:import (hara.lib.json DateSerializer FunctionalKeyDeserializer FunctionalSerializer KeywordSerializer KeywordKeyDeserializer PersistentHashMapDeserializer PersistentVectorDeserializer SymbolSerializer VarSerializer RatioSerializer FunctionalKeywordSerializer) (com.fasterxml.jackson.core JsonGenerator$Feature JsonParser JsonParser$Feature) (com.fasterxml.jackson.databind JsonSerializer ObjectMapper SerializationFeature DeserializationFeature) (com.fasterxml.jackson.databind.module SimpleModule) (java.io InputStream Writer File OutputStream DataOutput Reader) (java.net URL) (com.fasterxml.jackson.datatype.jsr310 JavaTimeModule))
  (:refer-clojure :exclude [read]))

(defn clojure-module
  "adds serializers for clojure objects
 
   (clojure-module {})
   => com.fasterxml.jackson.databind.module.SimpleModule"
  {:added "3.0"}
  ([{:keys [encode-key-fn decode-key-fn encoders date-format]
     :or {encode-key-fn true, decode-key-fn false}}]
   (doto (SimpleModule. "Clojure")
     (.addDeserializer java.util.List (PersistentVectorDeserializer.))
     (.addDeserializer java.util.Map (PersistentHashMapDeserializer.))
     (.addSerializer clojure.lang.Keyword (KeywordSerializer. false))
     (.addSerializer clojure.lang.Ratio (RatioSerializer.))
     (.addSerializer clojure.lang.Symbol (SymbolSerializer.))
     (.addSerializer clojure.lang.Var (VarSerializer.))
     (.addSerializer java.util.Date (if date-format
                                      (DateSerializer. date-format)
                                      (DateSerializer.)))
     (as-> module
           (doseq [[type encoder] encoders]
             (cond
               (instance? JsonSerializer encoder) (.addSerializer module type encoder)
               (fn? encoder) (.addSerializer module type (FunctionalSerializer. encoder))
               :else (throw (ex-info
                             (str "Can't register encoder " encoder " for type " type)
                             {:type type, :encoder encoder})))))
     (cond->
      (true? decode-key-fn) (.addKeyDeserializer Object (KeywordKeyDeserializer.))
      (fn? decode-key-fn) (.addKeyDeserializer Object (FunctionalKeyDeserializer. decode-key-fn))
      (true? encode-key-fn) (.addKeySerializer clojure.lang.Keyword (KeywordSerializer. true))
      (fn? encode-key-fn) (.addKeySerializer clojure.lang.Keyword (FunctionalKeywordSerializer. encode-key-fn))))))

(defn ^ObjectMapper object-mapper
  "creates an object-mapper for json output
 
   (object-mapper {})
   => com.fasterxml.jackson.databind.ObjectMapper"
  {:added "3.0"}
  ([] (object-mapper {}))
  ([options]
   (doto (ObjectMapper.)
     (.configure JsonParser$Feature/ALLOW_SINGLE_QUOTES true)
     (.registerModule (clojure-module options))
     (.registerModule (JavaTimeModule.))
     (cond-> (:pretty options) (.enable SerializationFeature/INDENT_OUTPUT)
             (:bigdecimals options) (.enable DeserializationFeature/USE_BIG_DECIMAL_FOR_FLOATS)
             (:escape-non-ascii options) (doto (-> .getFactory (.enable JsonGenerator$Feature/ESCAPE_NON_ASCII))))
     (as-> mapper
           (doseq [module (:modules options)]
             (.registerModule mapper module)))
     (.disable SerializationFeature/WRITE_DATES_AS_TIMESTAMPS))))

(def ^ObjectMapper +default-mapper+
  (object-mapper {}))

(def ^ObjectMapper +keyword-mapper+
  (object-mapper {:decode-key-fn keyword}))

(def ^ObjectMapper +keyword-case-mapper+
  (object-mapper {:decode-key-fn (comp keyword std.string.case/spear-case)}))

(def ^ObjectMapper +keyword-js-mapper+
  (object-mapper {:decode-key-fn (comp keyword #(.replaceAll ^String % "_" "-"))}))

(def ^ObjectMapper +keyword-spear-mapper+
  (object-mapper {:decode-key-fn (comp keyword #(.replaceAll ^String % "\\s" "-"))}))

;;
;; Protocols
;;

(defprotocol ReadValue
  (-read-value [this mapper]))

(extend-protocol ReadValue

  nil
  (-read-value [_ _])

  File
  (-read-value [this ^ObjectMapper mapper]
    (.readValue mapper this ^Class Object))

  URL
  (-read-value [this ^ObjectMapper mapper]
    (.readValue mapper this ^Class Object))

  String
  (-read-value [this ^ObjectMapper mapper]
    (.readValue mapper this ^Class Object))

  Reader
  (-read-value [this ^ObjectMapper mapper]
    (.readValue mapper this ^Class Object))

  InputStream
  (-read-value [this ^ObjectMapper mapper]
    (.readValue mapper this ^Class Object)))

(defprotocol WriteValue
  (-write-value [this value mapper]))

(extend-protocol WriteValue
  File
  (-write-value [this value ^ObjectMapper mapper]
    (.writeValue mapper this value))

  OutputStream
  (-write-value [this value ^ObjectMapper mapper]
    (.writeValue mapper this value))

  DataOutput
  (-write-value [this value ^ObjectMapper mapper]
    (.writeValue mapper this value))

  Writer
  (-write-value [this value ^ObjectMapper mapper]
    (.writeValue mapper this value)))

;;
;; public api
;;

(defn read
  "reads json as clojure data
 
   (read \"{\\\"a\\\":1,\\\"b\\\":2}\")
   => {\"a\" 1, \"b\" 2}"
  {:added "3.0"}
  ([object]
   (-read-value object +default-mapper+))
  ([object ^ObjectMapper mapper]
   (-read-value object mapper)))

(defn ^String write
  "writes clojure data to json
 
   (write {:a 1 :b 2}) => \"{\\\"a\\\":1,\\\"b\\\":2}\""
  {:added "3.0"}
  ([object]
   (.writeValueAsString +default-mapper+ object))
  ([object ^ObjectMapper mapper]
   (.writeValueAsString mapper object)))

(defn ^String write-pp
  "pretty print json output"
  {:added "4.0"}
  ([object]
   (.writeValueAsString (.writerWithDefaultPrettyPrinter +default-mapper+) object))
  ([object ^ObjectMapper mapper]
   (.writeValueAsString (.writerWithDefaultPrettyPrinter mapper) object)))

(defn ^"[B" write-bytes
  "writes clojure data to json bytes
 
   (String. (write-bytes {:a 1 :b 2}))
   => \"{\\\"a\\\":1,\\\"b\\\":2}\""
  {:added "3.0"}
  ([object]
   (.writeValueAsBytes +default-mapper+ object))
  ([object ^ObjectMapper mapper]
   (.writeValueAsBytes mapper object)))

(defn write-to
  "writes clojure data to a sink
 
   (def -out- (java.io.ByteArrayOutputStream.))
 
   (write-to -out- {:a 1 :b 2})
 
   (.toString ^java.io.ByteArrayOutputStream -out-)
   => \"{\\\"a\\\":1,\\\"b\\\":2}\""
  {:added "3.0"}
  ([sink object]
   (-write-value sink object +default-mapper+))
  ([sink object ^ObjectMapper mapper]
   (-write-value sink object mapper)))

(defonce ^:private +sys:resource-json+
  (atom {}))

(defn sys:resource-json
  "returns cached json map of on a resource"
  {:added "4.0"}
  [path]
  (env/sys:resource-cached +sys:resource-json+
                           path
                           (fn [url]
                             (read (slurp url) +keyword-case-mapper+))))
