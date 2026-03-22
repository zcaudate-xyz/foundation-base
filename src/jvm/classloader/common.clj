(ns jvm.classloader.common
  (:require [std.lib.foundation :as f])
  (:import (java.net URL) (java.io DataInputStream FileInputStream)))

(defn to-url
  "constructs a `java.net.URL` object from a string
 
   (str (to-url \"/dev/null\"))
   => \"file:/dev/null/\""
  {:added "3.0"}
  ([^String path]
   (cond (f/url? path)
         path

         (string? path)
         (let [path (if-not (or (.endsWith path ".jar")
                                (.endsWith path "/"))
                      (str path "/")
                      path)
               path (if-not (.startsWith path "file:")
                      (str "file:" path)
                      path)]
           (URL. path))

         :else (to-url (str path)))))

(defn bytecode-version
  "gets the bytecode version of a class file"
  {:added "4.0"}
  [^String path]
  (with-open [stream (DataInputStream. (FileInputStream. path))]
    (let [_magic (.readInt stream) ;; Skip the magic number (0xCAFEBABE)
          minor-version (.readUnsignedShort stream) ;; Read the minor version
          major-version (.readUnsignedShort stream)] ;; Read the major version
      {:minor-version minor-version
       :major-version major-version
       :jdk-version (case major-version
                      45 "JDK 1.1"
                      46 "JDK 1.2"
                      47 "JDK 1.3"
                      48 "JDK 1.4"
                      49 "JDK 5.0"
                      50 "JDK 6"
                      51 "JDK 7"
                      52 "JDK 8"
                      53 "JDK 9"
                      54 "JDK 10"
                      55 "JDK 11"
                      56 "JDK 12"
                      57 "JDK 13"
                      58 "JDK 14"
                      59 "JDK 15"
                      60 "JDK 16"
                      61 "JDK 17"
                      62 "JDK 18"
                      63 "JDK 19"
                      64 "JDK 20"
                      65 "JDK 21"
                      66 "JDK 22"
                      67 "JDK 23"
                      68 "JDK 24"
                      69 "JDK 25"
                      70 "JDK 26"
                      "Unknown")})))
