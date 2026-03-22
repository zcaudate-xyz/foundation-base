(ns lib.openpgp
  (:require [clojure.string]
            [std.fs :as fs]
            [std.lib.bin :as binary]
            [std.lib.encode :as encode]
            [std.lib.env :as env])
  (:import (org.bouncycastle.openpgp PGPPublicKey PGPPrivateKey PGPSecretKey PGPPublicKeyRing PGPSecretKeyRing PGPObjectFactory PGPUtil PGPEncryptedDataGenerator PGPEncryptedData PGPLiteralDataGenerator PGPLiteralData PGPSignatureGenerator PGPSignature PGPSignatureList PGPEncryptedDataList PGPPublicKeyEncryptedData PGPCompressedData PGPLiteralData) (org.bouncycastle.bcpg CRC24 BCPGInputStream) (org.bouncycastle.openpgp.jcajce JcaPGPObjectFactory) (org.bouncycastle.openpgp.operator.jcajce JcePBESecretKeyDecryptorBuilder JcaKeyFingerprintCalculator JcaPGPKeyConverter) (org.bouncycastle.openpgp.operator.bc BcKeyFingerprintCalculator BcPublicKeyDataDecryptorFactory BcPublicKeyKeyEncryptionMethodGenerator BcPGPDataEncryptorBuilder BcPGPContentSignerBuilder BcPGPContentVerifierBuilderProvider) (java.io ByteArrayOutputStream) (java.security SecureRandom) (java.util Collections)))



(defn fingerprint
  "returns the fingerprint of a public key
 
   (fingerprint +public-key+)
   => \"E710D59C5346D3C0A1C578AE6753F8E16D35FC24\""
  {:added "3.0"}
  ([^PGPPublicKey pub]
   (-> pub
       (.getFingerprint)
       (encode/to-hex)
       (.toUpperCase))))

(defmethod print-method PGPPublicKey
  ([^PGPPublicKey v ^java.io.Writer w]
   (.write w (str "#public \"" (fingerprint v) "\""))))

(defmethod print-method PGPSecretKey
  ([^PGPSecretKey v ^java.io.Writer w]
   (.write w (str "#secret \"" (fingerprint (.getPublicKey v)) "\""))))

(defmethod print-method PGPPrivateKey
  ([^PGPPrivateKey v ^java.io.Writer w]
   (.write w (str "#private \"" (.getKeyID v) "\""))))

(defn ^PGPPublicKeyRing parse-public-key-ring
  "parses a public key ring from string
 
   (-> (str/joinl +public-key-string+ \"\\n\")
       (parse-public-key-ring))
   => org.bouncycastle.openpgp.PGPPublicKeyRing"
  {:added "3.0"}
  ([input]
   (-> (binary/input-stream input)
       (PGPUtil/getDecoderStream)
       (PGPPublicKeyRing. ^BcKeyFingerprintCalculator (BcKeyFingerprintCalculator.)))))

(defn parse-public-key
  "parses a public key from string
 
   (-> (str/joinl +public-key-string+ \"\\n\")
       (parse-public-key))
   ;; #public \"E710D59C5346D3C0A1C578AE6753F8E16D35FC24\"
   => org.bouncycastle.openpgp.PGPPublicKey"
  {:added "3.0"}
  ([input]
   (->> (parse-public-key-ring input)
        (.getPublicKeys)
        (iterator-seq)
        first))
  ([input id]
   (->> (parse-public-key-ring input)
        (.getPublicKeys)
        (iterator-seq)
        (filter #{id})
        (first))))

(defn ^PGPSecretKeyRing parse-secret-key-ring
  "parses a secret key ring from string
 
   (-> (str/joinl +secret-key-string+ \"\\n\")
       (parse-secret-key-ring))
   => org.bouncycastle.openpgp.PGPSecretKeyRing"
  {:added "3.0"}
  ([input]
   (-> (binary/input-stream input)
       (PGPUtil/getDecoderStream)
       (PGPSecretKeyRing. ^BcKeyFingerprintCalculator (BcKeyFingerprintCalculator.)))))

(defn parse-secret-key
  "parses a secret key from string
 
   (-> (str/joinl +secret-key-string+ \"\\n\")
       (parse-secret-key))
   ;; #secret \"E710D59C5346D3C0A1C578AE6753F8E16D35FC24\"
   => org.bouncycastle.openpgp.PGPSecretKey"
  {:added "3.0"}
  ([input]
   (-> (parse-secret-key-ring input)
       (.getSecretKeys)
       (iterator-seq)
       first))
  ([input id]
   (->> (parse-secret-key-ring input)
        (.getSecretKeys)
        (iterator-seq)
        (filter #{id})
        (first))))

(defn key-pair
  "returns a public and private key pair from a secret key
 
   (key-pair +secret-key+)
   ;;[#public \"E710D59C5346D3C0A1C578AE6753F8E16D35FC24\" #private \"7445568256057146404\"]
   => (contains [org.bouncycastle.openpgp.PGPPublicKey
                 org.bouncycastle.openpgp.PGPPrivateKey])"
  {:added "3.0"}
  ([^PGPSecretKey secret-key]
   (let [decryptor (-> (JcePBESecretKeyDecryptorBuilder.)
                       (.setProvider "BC")
                       (.build (char-array "")))]
     [(.getPublicKey secret-key)
      (.extractPrivateKey secret-key decryptor)])))

(defn encrypt
  "encrypts bytes given a public key
 
   (->> (encrypt (.getBytes \"Hello World\")
                 {:public +public-key+})
 
       (encode/to-base64))"
  {:added "3.0"}
  ([^bytes clear {:keys [public]}]
   (let [bstream (ByteArrayOutputStream.)
         ldata   (PGPLiteralDataGenerator.)
         lname   (str (fs/create-tmpfile))
         lstream (.open ldata bstream PGPLiteralData/BINARY
                        lname (count clear) (java.util.Date.))
         lstream (doto ^java.io.OutputStream lstream
                   (.write clear)
                   (.close))
         cpk-builder (-> (BcPGPDataEncryptorBuilder. PGPEncryptedData/CAST5)
                         (.setSecureRandom (SecureRandom.))
                         (.setWithIntegrityPacket true))
         cpk     (doto (PGPEncryptedDataGenerator. cpk-builder)
                   (.addMethod (BcPublicKeyKeyEncryptionMethodGenerator. public)))
         bytes   (.toByteArray bstream)
         ostream (ByteArrayOutputStream.)
         estream (doto (.open cpk ostream (count bytes))
                   (.write bytes)
                   (.close))]
     (.close ostream)
     (.toByteArray ostream))))

(defn decrypt
  "decrypts the encrypted information
 
   (-> (.getBytes \"Hello World\")
       (encrypt {:public  +public-key+})
       ^bytes (decrypt {:private +private-key+})
       (String.))
   => \"Hello World\""
  {:added "3.0"}
  ([encrypted {:keys [private]}]
   (let [obj-factory  (-> (binary/input-stream encrypted)
                          (PGPUtil/getDecoderStream)
                          (PGPObjectFactory. (BcKeyFingerprintCalculator.)))
         enc-data     (-> ^PGPEncryptedDataList (.nextObject obj-factory)
                          (.getEncryptedDataObjects)
                          (iterator-seq)
                          ^PGPPublicKeyEncryptedData (first))
         key-id       (.getKeyID enc-data)
         bytes        (-> (.getDataStream enc-data
                                          (BcPublicKeyDataDecryptorFactory. private))
                          (JcaPGPObjectFactory.)
                          ^PGPLiteralData (.nextObject)
                          (.getDataStream)
                          (binary/bytes))
         bytes        (try (-> (JcaPGPObjectFactory. ^java.io.InputStream (binary/input-stream bytes))
                               ^PGPLiteralData (.nextObject)
                               (.getDataStream)
                               (binary/bytes))
                           (catch Exception e bytes))]
     bytes)))

(defn ^PGPSignature generate-signature
  "generates a signature given bytes and a keyring
 
   (generate-signature (.getBytes \"Hello World\")
                       {:public  +public-key+
                        :private +private-key+})
   ;; #gpg.signature [\"iQEcBAABCAAGBQJbw1U8AAoJEGdT+OFtNf... \"]
   => org.bouncycastle.openpgp.PGPSignature"
  {:added "3.0"}
  ([^bytes bytes {:keys [^PGPPublicKey public private]}]
   (let [sig-gen  (-> (BcPGPContentSignerBuilder.
                       (.getAlgorithm public)
                       PGPUtil/SHA256)
                      (PGPSignatureGenerator.))
         sig-gen  (doto sig-gen
                    (.init PGPSignature/BINARY_DOCUMENT private)
                    (.update bytes))]
     (.generate sig-gen))))

(defmethod print-method PGPSignature
  ([^PGPSignature v ^java.io.Writer w]
   (.write w (str "#gpg.signature [\"" (encode/to-base64 (.getEncoded v)) "\"]"))))

(defn crc-24
  "returns the crc24 checksum 
 
   (crc-24 (byte-array [100 100 100 100 100 100]))
   => [\"=6Fko\" [-24 89 40] 15227176]"
  {:added "3.0"}
  ([input]
   (let [crc (CRC24.)
         _   (doseq [i (seq input)]
               (.update crc i))
         val (.getValue crc)
         bytes (-> (biginteger val)
                   (.toByteArray)
                   seq)
         bytes (case (count bytes)
                 4 (rest bytes)
                 3 bytes
                 (nth (iterate #(cons 0 %)
                               bytes)
                      (- 3 (count bytes))))]
     [(->> (byte-array bytes)
           (encode/to-base64)
           (str "="))
      bytes
      val])))

(defn write-sig-file
  "writes bytes to a GPG compatible file
 
   (let [signature (-> (generate-signature (.getBytes \"Hello World\")
                                           {:public  +public-key+
                                           :private +private-key+})
                       (.getEncoded))]
     (write-sig-file \"test-scratch/project.clj.asc\"
                     signature))"
  {:added "3.0"}
  ([sig-file bytes]
   (->> (concat ["-----BEGIN PGP SIGNATURE-----"
                 ""]
                (->> bytes
                     (encode/to-base64)
                     (partition-all 64)
                     (map #(apply str %)))
                [(first (crc-24 bytes))
                 "-----END PGP SIGNATURE-----"])
        (clojure.string/join "\n")
        (spit sig-file))))

(defn read-sig-file
  "reads bytes from a GPG compatible file
 
   (read-sig-file \"test-scratch/project.clj.asc\")
   => bytes?"
  {:added "3.0"}
  ([sig-file]
   (->> (slurp sig-file)
        (clojure.string/split-lines)
        (reverse)
        (drop-while (fn [^String input]
                      (not (and (.startsWith input "=")
                                (= 5 (count input))))))
        (rest)
        (take 6)
        (reverse)
        (clojure.string/join "")
        (encode/from-base64))))

(defn sign
  "generates a output gpg signature for an input file
 
   (sign \"project.clj\"
         \"test-scratch/project.clj.asc\"
         {:public  +public-key+
          :private +private-key+})
   => bytes?"
  {:added "3.0"}
  ([input sig-file {:keys [public private] :as opts}]
   (env/prn opts)
   (let [input-bytes (fs/read-all-bytes input)
         sig-bytes  (-> (generate-signature input-bytes opts)
                        (.getEncoded))]
     (write-sig-file sig-file sig-bytes)
     sig-bytes)))

(defn ^PGPSignature pgp-signature
  "returns a gpg signature from encoded bytes
 
   (-> (generate-signature (.getBytes \"Hello World\")
                           {:public  +public-key+
                            :private +private-key+})
       (.getEncoded)
       (pgp-signature))
   => org.bouncycastle.openpgp.PGPSignature"
  {:added "3.0"}
  ([bytes]
   (let [input-stream (BCPGInputStream. (java.io.ByteArrayInputStream. bytes)) 
         fingerprint-calc (BcKeyFingerprintCalculator.)
         object-factory (PGPObjectFactory. input-stream fingerprint-calc)
         obj (.nextObject object-factory)]
     (cond (instance? PGPSignature obj)
           obj

           (instance? PGPSignatureList obj)
           (first obj)

           :else
           (throw (ex-info "Expected PGPSignature, got different object type."
                           {:object obj}))))))

(defn verify
  "verifies that the signature works
 
   (verify \"project.clj\"
           \"test-scratch/project.clj.asc\"
           {:public  +public-key+})
   => true"
  {:added "3.0"}
  ([input sig-file {:keys [public]}]
   (let [bytes (fs/read-all-bytes input)
         sig-bytes (read-sig-file sig-file)
         sig (if (zero? (count sig-bytes))
               (throw (Exception. (str "Not a valid signature file: " sig-file)))
               (pgp-signature sig-bytes))]
     (-> (doto ^PGPSignature sig
           (.init (BcPGPContentVerifierBuilderProvider.) ^PGPPublicKey public)
           (.update ^bytes bytes))
         (.verify)))))
