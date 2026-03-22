(ns std.lib.security
  (:require [std.lib.encode :as encode]
            [std.lib.foundation :as h]
            [std.lib.security.cipher :as cipher]
            [std.lib.security.key :as key]
            [std.lib.security.provider :as provider]
            [std.lib.security.verify :as verify])
  (:import (java.security Security)))

(h/intern-in cipher/encrypt
             cipher/decrypt

             key/generate-key
             key/generate-key-pair
             key/->key
             key/key->map

             provider/list-providers
             provider/list-services
             provider/cipher
             provider/key-generator
             provider/key-pair-generator
             provider/key-store
             provider/mac
             provider/message-digest
             provider/signature

             verify/digest
             verify/hmac
             verify/sign
             verify/verify)

(defn sha1
  "function for sha1 hash
 
   (sha1 \"123\")
   => \"40bd001563085fc35165329ea1ff5c5ecbdbbeef\""
  {:added "3.0"}
  ([^String body]
   (-> (.getBytes body)
       (verify/digest "SHA1")
       (encode/to-hex))))

(defn md5
  "function for md5 hash
 
   (md5 \"123\")
   => \"202cb962ac59075b964b07152d234b70\""
  {:added "4.0"}
  ([^String body]
   (-> (.getBytes body)
       (verify/digest "MD5")
       (encode/to-hex))))
