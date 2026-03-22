(ns std.lib.io
  (:import (java.nio.charset Charset) (java.io InputStream OutputStream Reader Writer)))

(defn charset:default
  "returns the default charset
 
   (charset:default)
   => \"UTF-8\""
  {:added "3.0"}
  ([]
   (str (Charset/defaultCharset))))

(defn charset:list
  "returns the list of available charset
 
   (charset-list)
   => (\"Big5\" \"Big5-HKSCS\" ... \"x-windows-iso2022jp\")"
  {:added "3.0"}
  ([]
   (keys (Charset/availableCharsets))))

(defn ^Charset charset
  "constructs a charset object from a string
   (charset \"UTF-8\")
   => java.nio.charset.Charset"
  {:added "3.0"}
  ([s]
   (Charset/forName s)))

(defn input-stream?
  "checks if object is an input-stream"
  {:added "3.0"}
  ([x]
   (instance? InputStream x)))

(defn output-stream?
  "checks if object is an output-stream"
  {:added "3.0"}
  ([x]
   (instance? OutputStream x)))

(defn reader?
  "checks that object is a reader"
  {:added "3.0"}
  ([x]
   (instance? Reader x)))

(defn writer?
  "checks that object is a writer"
  {:added "3.0"}
  ([x]
   (instance? Writer x)))
