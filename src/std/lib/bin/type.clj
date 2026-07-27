(ns std.lib.bin.type
  (:require [std.lib.invoke :refer [definvoke]]
            [std.protocol.binary :as protocol.binary])
  (:import (hara.lib.bin ByteBufferInputStream ByteBufferOutputStream) (java.io InputStream OutputStream FileInputStream FileOutputStream PipedInputStream PipedOutputStream ByteArrayInputStream ByteArrayOutputStream InputStreamReader OutputStreamWriter) (java.util BitSet) (java.net URL URI) (java.io File) (java.nio ByteBuffer ByteOrder) (java.nio.file Path) (java.nio.channels ByteChannel FileChannel Channels))
  (:refer-clojure :exclude [bytes]))

(def +native+ (ByteOrder/nativeOrder))

;; Supported Formats
;;
;; 1.  bitstr
;; 2.  bitseq
;; 3.  bitset
;; 4.  number
;; 5.  byte[]
;; 6.  io.inputstream
;; 7.  nio.bytebuffer
;; 8.  io.file
;; 9.  nio.path
;; 10. nio.channel
;; 11. net.url
;; 12. net.uri

;; bitseq to number is little endian (start bit is largest)
;; byte[] to bitseq is little endian (start bit is largest)
;; buffer and stream to byte[] will be according to the instance settings (usually little endian)

(defn bitstr-to-bitseq
  "creates a binary sequence from a bitstr
 
   (bitstr-to-bitseq \"100011\")
   => [1 0 0 0 1 1]"
  {:added "3.0"}
  ([^String s]
   (vec (map-indexed (fn [i ch]
                       (case ch \0 0 \1 1
                             (throw (ex-info "Not a binary character." {:index i
                                                                        :char ch}))))
                     s))))

(defn bitseq-to-bitstr
  "creates a bitstr from a binary sequence
 
   (bitseq-to-bitstr [1 0 0 0 1 1])
   => \"100011\""
  {:added "3.0"}
  ([bseq]
   (apply str bseq)))

(defn bitseq-to-number
  "creates a number from a binary sequence
 
   (bitseq-to-number [1 0 0 0 1 1])
   => 49
 
   (bitseq-to-number (repeat 10 1))
   => 1023"
  {:added "3.0"}
  ([arr]
   (bitseq-to-number arr 1))
  ([arr factor]
   (cond (empty? arr) 0

         (> 64 (count arr))
         (*' (->> arr
                  (map-indexed (fn [i x]
                                 (bit-shift-left x i)))
                  (apply +))
             factor)

         :else
         (+' (bitseq-to-number (take 63 arr) factor)
             (bitseq-to-number (drop 63 arr) (*' factor
                                                 (+' Long/MAX_VALUE 1)))))))

(defn bitseq-to-bitset
  "creates a bitset from a binary sequence
 
   (bitseq-to-bitset [1 0 0 0 1 1])
   ;; => #bs[1 0 0 0 1 1]"
  {:added "3.0"}
  ([bseq]
   (let [bs (BitSet. (count bseq))]
     (doall (map-indexed (fn [i b]
                           (.set bs ^int i (not (zero? b))))
                         bseq))
     bs)))

(defn bitset-to-bitseq
  "creates a binary sequence from a bitset
   (-> (bitseq-to-bitset [1 0 0 0 1 1])
       (bitset-to-bitseq))
   => [1 0 0 0 1 1]"
  {:added "3.0"}
  ([^BitSet bs]
   (bitset-to-bitseq bs (.length bs)))
  ([^BitSet bs length]
   (vec (for [i (range length)]
          (if (.get bs i) 1 0)))))

(defn bitset-to-bytes
  "creates a byte array from a bitset
 
   (-> (bitseq-to-bitset [1 0 0 0 1 1])
       (bitset-to-bytes)
       seq)
   => [49]"
  {:added "3.0"}
  ([^BitSet bs]
   (.toByteArray bs)))

(defn bytes-to-bitset
  "creates a bitset from bytes
 
   (-> (byte-array [49])
       (bytes-to-bitset)
       (bitset-to-bitseq 8))
   => [1 0 0 0 1 1 0 0]"
  {:added "3.0"}
  ([^bytes arr]
   (BitSet/valueOf arr)))

(defn long-to-bitseq
  "creates a binary sequence from a long
 
   (long-to-bitseq 1023)
   => [1 1 1 1 1 1 1 1 1 1]
 
   (long-to-bitseq 49 8)
   => [1 0 0 0 1 1 0 0]"
  {:added "3.0"}
  ([x]
   (let [m (loop [m 0
                  x x]
             (if (zero? x)
               m
               (recur (inc m) (bit-shift-right x 1))))]
     (long-to-bitseq x m)))
  ([x m]
   (let [arr (vec (repeat m 0))]
     (reduce (fn [out i]
               (assoc out i (bit-and 1 (bit-shift-right x i))))
             arr
             (range m)))))

(defn bigint-to-bitset
  "creates a bitset from a bigint
 
   (-> (bigint-to-bitset 9223372036854775808N)
       (bitset-to-bitseq))
   => (conj (vec (repeat 63 0))
            1)"
  {:added "3.0"}
  ([^clojure.lang.BigInt x]
   (-> (.toBigInteger x)
       (.toByteArray)
       (reverse)
       (byte-array)
       (bytes-to-bitset))))

(defn- input-stream-to-bytes [^InputStream istream]
  (let [ostream (ByteArrayOutputStream.)]
    (.transferTo istream ostream)
    (.toByteArray ostream)))

(defn- bytebuffer-to-bytes [^java.nio.ByteBuffer buff]
  (.array buff))

(defn bitstr?
  "checks if a string consists of only 1s and 0s
 
   (bitstr? \"0101010101\")
   => true
 
   (bitstr? \"hello\")
   => false"
  {:added "3.0"}
  ([x]
   (and (string? x)
        (= x (re-find #"^[01]+" x)))))

;; 1. bitstr 

(extend-type java.lang.String
  protocol.binary/IBinary
  (-to-bitstr [x] (if (bitstr? x)
                    x
                    (protocol.binary/-to-bitstr (.getBytes x))))
  (-to-bitseq [x] (if (bitstr? x)
                    (bitstr-to-bitseq x)
                    (protocol.binary/-to-bitseq (.getBytes x))))
  (-to-bitset [x] (if (bitstr? x)
                    (-> x bitstr-to-bitseq bitseq-to-bitset)
                    (protocol.binary/-to-bitset (.getBytes x))))
  (-to-bytes  [x] (if (bitstr? x)
                    (-> x bitstr-to-bitseq bitseq-to-bitset bitset-to-bytes)
                    (.getBytes x)))
  (-to-number [x] (if (bitstr? x)
                    (-> x bitstr-to-bitseq bitseq-to-number)
                    (protocol.binary/-to-number (.getBytes x)))))

;; 2. bitseq

(extend-type clojure.lang.PersistentVector
  protocol.binary/IBinary
  (-to-bitstr [x] (bitseq-to-bitstr x))
  (-to-bitseq [x] x)
  (-to-bitset [x] (bitseq-to-bitset x))
  (-to-bytes  [x] (-> x bitseq-to-bitset bitset-to-bytes))
  (-to-number [x] (bitseq-to-number x)))

;; 3. bitset

(extend-type java.util.BitSet
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (bitset-to-bitseq x))
  (-to-bitset [x] x)
  (-to-bytes  [x] (bitset-to-bytes x))
  (-to-number [x] (-> x bitset-to-bitseq bitseq-to-number)))

;; 4. number

(extend-type java.lang.Long
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x long-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (long-to-bitseq x))
  (-to-bitset [x] (-> x long-to-bitseq bitseq-to-bitset))
  (-to-bytes  [x] (-> x long-to-bitseq bitseq-to-bitset bitset-to-bytes))
  (-to-number [x] x))

(extend-type clojure.lang.BigInt
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x bigint-to-bitset bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (-> x bigint-to-bitset bitset-to-bitseq))
  (-to-bitset [x] (bigint-to-bitset x))
  (-to-bytes  [x] (-> x bigint-to-bitset bitset-to-bytes))
  (-to-number [x] x))

;; 5. byte[]

(extend-type (Class/forName "[B")
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x bytes-to-bitset bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (-> x bytes-to-bitset bitset-to-bitseq))
  (-to-bitset [x] (bytes-to-bitset x))
  (-to-bytes  [x] x)
  (-to-number [x] (-> x bytes-to-bitset bitset-to-bitseq bitseq-to-number))

  protocol.binary/IByteSource
  (-to-input-stream [bs]
    (ByteArrayInputStream. bs))

  protocol.binary/IByteSink
  (-to-output-stream [bs]
    (doto (ByteArrayOutputStream.)
      (.write ^bytes bs))))

;; 6. io.inputstream

(extend-type java.io.InputStream
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x input-stream-to-bytes bytes-to-bitset bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (-> x input-stream-to-bytes bytes-to-bitset bitset-to-bitseq))
  (-to-bitset [x] (-> input-stream-to-bytes bytes-to-bitset))
  (-to-bytes  [x] (input-stream-to-bytes x))
  (-to-number [x] (-> x input-stream-to-bytes bytes-to-bitset bitset-to-bitseq bitseq-to-number))

  protocol.binary/IByteSource
  (-to-input-stream [x] x)

  protocol.binary/IByteChannel
  (-to-channel [x]
    (Channels/newChannel x)))

;; 7. nio.bytebuffer

(extend-type java.nio.ByteBuffer
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x bytebuffer-to-bytes bytes-to-bitset bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (-> x bytebuffer-to-bytes bytes-to-bitset bitset-to-bitseq))
  (-to-bitset [x] (-> bytebuffer-to-bytes bytes-to-bitset))
  (-to-bytes  [x] (bytebuffer-to-bytes x))
  (-to-number [x] (-> x bytebuffer-to-bytes bytes-to-bitset bitset-to-bitseq bitseq-to-number))

  protocol.binary/IByteSource
  (-to-input-stream [buff]
    (ByteBufferInputStream. buff))

  protocol.binary/IByteSink
  (-to-output-stream [buff]
    (ByteBufferOutputStream. buff)))

;; 8. io.file

(extend-type java.io.File
  protocol.binary/IByteSource
  (-to-input-stream [file]
    (FileInputStream. file))

  protocol.binary/IByteSink
  (-to-output-stream [file]
    (FileOutputStream. file))

  protocol.binary/IByteChannel
  (-to-channel [file]
    (.getChannel (FileInputStream. file))))

;; 9.  nio.path

(extend-type java.nio.file.Path
  protocol.binary/IByteSource
  (-to-input-stream [path]
    (FileInputStream. (.toFile path)))

  protocol.binary/IByteSink
  (-to-output-stream [path]
    (FileOutputStream. (.toFile path)))

  protocol.binary/IByteChannel
  (-to-channel [path]
    (FileChannel/open path (into-array java.nio.file.OpenOption []))))

;; 10. nio.channel

(extend-type java.nio.channels.Channel
  protocol.binary/IByteSource
  (-to-input-stream [ch]
    (cond (instance? java.nio.channels.AsynchronousChannel ch)
          (Channels/newInputStream ^java.nio.channels.AsynchronousByteChannel ch)

          :else
          (Channels/newInputStream ^java.nio.channels.ReadableByteChannel ch)))

  protocol.binary/IByteSink
  (-to-output-stream [ch]
    (cond (instance? java.nio.channels.AsynchronousChannel ch)
          (Channels/newOutputStream ^java.nio.channels.AsynchronousByteChannel ch)

          :else
          (Channels/newOutputStream ^java.nio.channels.WritableByteChannel ch)))

  protocol.binary/IByteChannel
  (-to-channel [ch] ch))

;; 11. net.url

(extend-type java.net.URL
  protocol.binary/IByteSource
  (-to-input-stream [url]
    (.openStream url))

  protocol.binary/IByteChannel
  (-to-channel [url]
    (Channels/newChannel (.openStream url))))

;; 12. net.uri

(extend-type java.net.URI
  protocol.binary/IByteSource
  (-to-input-stream [uri]
    (.openStream (.toURL uri)))

  protocol.binary/IByteChannel
  (-to-channel [uri]
    (Channels/newChannel (.openStream (.toURL uri)))))

(defn- prepare-input-stream [obj]
  (cond (instance? java.io.InputStream obj)
        obj

        (instance? ByteBuffer obj)
        obj

        (satisfies? protocol.binary/IBinary obj)
        (ByteArrayInputStream. (protocol.binary/-to-bytes obj))

        :else
        obj))

(definvoke ^InputStream input-stream
  "creates an inputstream from various representations
 
   (input-stream 9223372036854775808N)
   => java.io.ByteArrayInputStream"
  {:added "3.0"}
  [:compose {:val (comp protocol.binary/-to-input-stream prepare-input-stream)
             :arglists '([obj])}])

(defn ^OutputStream output-stream
  "creates an inputstream from various representations
 
   (output-stream (fs/file \"project.clj\"))
   => java.io.FileOutputStream"
  {:added "3.0"}
  ([obj]
   (protocol.binary/-to-output-stream obj)))

(defn channel
  "creates a channel from various representations
 
   (channel (fs/file \"project.clj\"))
   => java.nio.channels.Channel"
  {:added "3.0"}
  ([obj]
   (protocol.binary/-to-channel obj)))

(defn- prepare-binary [obj]
  (cond (bytes? obj) obj

        (satisfies? protocol.binary/IByteSource obj)
        (protocol.binary/-to-bytes (input-stream obj))

        :else obj))

(definvoke bitstr
  "converts to a bitstr binary representation
 
   (binary/bitstr (byte-array [49]))
   => \"100011\""
  {:added "3.0"}
  [:compose {:val (comp protocol.binary/-to-bitstr prepare-binary)
             :arglists '([obj])}])

(definvoke bitseq
  "converts to a bitseq binary representation
 
   (binary/bitseq (byte-array [49]))
   => [1 0 0 0 1 1]"
  {:added "3.0"}
  [:compose {:val (comp protocol.binary/-to-bitseq prepare-binary)
             :arglists '([obj])}])

(definvoke bitset
  "converts to a bitset binary representation
 
   (-> (binary/bitset \"100011\")
       (bitset-to-bitseq))
   => [1 0 0 0 1 1]"
  {:added "3.0"}
  [:compose {:val (comp protocol.binary/-to-bitset prepare-binary)
             :arglists '([obj])}])

(definvoke bytes
  "converts to a byte array binary representation
 
   (-> (binary/bytes \"100011\")
       (seq))
   => [49]"
  {:added "3.0"}
  [:compose {:val (comp protocol.binary/-to-bytes prepare-binary)
             :arglists '([obj])}])

(definvoke number
  "converts to a number binary representation
 
   (binary/number \"100011\")
   => 49"
  {:added "3.0"}
  [:compose {:val (comp protocol.binary/-to-number prepare-binary)
             :arglists '([obj])}])
