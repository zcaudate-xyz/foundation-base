(ns std.config.secure
  (:require [std.config.global :as global]
            [std.lib.encode :as encode]
            [std.lib.security :as security]))

(defonce +master-key+ (get-in (global/global) [:tahto :key]))

(defonce +master-defaults+ {:type "AES"
                            :format "RAW"
                            :mode :secret})

(def ^:dynamic *key*
  (if +master-key+
    (try
      (security/->key (assoc +master-defaults+ :encoded +master-key+))
      (catch Throwable t))))

(defn resolve-key
  "resolves the key for decryption
 
   (def -key- (resolve-key \"3YO19A4QAlRLDc8qmhLD7A==\"))
   => #'-key-"
  {:added "3.0"}
  ([key]
   (cond (nil? key) *key*
         (instance? java.security.Key key) key
         (map? key) (security/->key (merge +master-defaults+ key))
         (string? key) (security/->key (assoc +master-defaults+ :encoded key))
         :else (throw (ex-info "Not supported" {:input key})))))

(defn decrypt-text
  "decrypts text based on key
 
   (decrypt-text \"2OJZ7B2JpH7Pa5p9XpDc6w==\" -key-)
   => \"hello\"
 
   (decrypt-text \"d8e259ec1d89a47ecf6b9a7d5e90dceb\"
                 -key-
                 {:format :hex})
   => \"hello\""
  {:added "3.0"}
  ([encrypted]
   (decrypt-text encrypted *key*))
  ([encrypted key]
   (decrypt-text encrypted key {}))
  ([encrypted key {:keys [format]
                   :or {format :base-64}}]
   (if (nil? key) (throw (ex-info "Key not present" {:key key
                                                     :input encrypted})))
   (let [bytes (case format
                 :base-64 (encode/from-base64 encrypted)
                 :hex     (encode/from-hex encrypted))]
     (String. ^bytes (security/decrypt bytes key)))))

(defn encrypt-text
  "encrypts text based on key
 
   (encrypt-text \"hello\" -key-)
   => \"2OJZ7B2JpH7Pa5p9XpDc6w==\"
 
   (encrypt-text \"hello\" -key- {:format :hex})
   => \"d8e259ec1d89a47ecf6b9a7d5e90dceb\""
  {:added "3.0"}
  ([text]
   (encrypt-text text *key*))
  ([text key]
   (encrypt-text text key {}))
  ([text key {:keys [format]
              :or {format :base-64}}]
   (if (nil? key) (throw (ex-info "Key not present" {:key key :input text})))
   (let [bytes (security/encrypt (.getBytes ^String text) key)]
     (case format
       :base-64 (encode/to-base64 bytes)
       :hex     (encode/to-hex bytes)))))
