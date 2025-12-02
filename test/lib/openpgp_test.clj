(ns lib.openpgp-test
  (:use code.test)
  (:require [std.lib.encode :as encode]
            [std.fs :as fs]
            [lib.openpgp :refer :all]
            [std.lib.security :as security]
            [std.string :as str]))

(def +public-key-string+
  ["-----BEGIN PGP PUBLIC KEY BLOCK-----"
   ""
   "mQENBFvDT18BCADNGMwfI4UaqbaDR86oU6hTQI4xCcrPa4uPQydWjzsokgjgZCmc"
   "9Ba81wlP8olNf71f4MEd68wo+zyAos5+E5Lc4/Wqcwfxwf6PDz365vAWZya+5WaI"
   "D+GUp05Phk2oK42BmKtVbndhFlwXuycl9dRGPR3KH2WD7R/McBIBfYZNKsckf99v"
   "1NVPZ/vD61TP2TvXORRAGFXC8BlBz+bFH/DwFhZ/okEMQoJ5L+Jm0j5toA72LhjT"
   "S0N/KExLNfyRj1z2bzmaFO7qfdnrhK7ipWQSN5jBezF7KJy91BVgVtLhgVpDQXQx"
   "vhmKhpV5v8eTBAB+vK0egfSyGie/ZoTOUOJTABEBAAG0I1Rlc3QgVGVzdCAodGVz"
   "dGluZykgPHRlc3RAdGVzdC5jb20+iQE5BBMBCAAjBQJbw09fAhsDBwsJCAcDAgEG"
   "FQgCCQoLBBYCAwECHgECF4AACgkQZ1P44W01/CQ50Af/exwHBZmOePjiBseVE1/9"
   "vIBYcVTHsGRsO+IMAI4+467ZBzOVG6jAw3jr9cOKh8rIvB/Cx7x6PkorEjHo+cyJ"
   "DzJAPKSqEVAabtGt5BT0SzbPmlLp5qtUvCTEqBC9KK/IVQboOWdCag3aM0UK+YwC"
   "WbOzuZAMeH3pdbazgZATliAQ4br9rOvecdA9/E9HiA4Qp9bYyeCge+PmA/NNaIMb"
   "+TycRKRrutv6I8jZHO8AJMJ7ETyO41FIAJmAPDeHs3kcbbEQUb59w0vUHfvKUmZH"
   "jnw5VoDtfokfJ9dqxGChsFbzPuelrLWqLDqiOTysUImU3O4ZtrvvS68T7Gy6LQIs"
   "KLkBDQRbw09fAQgAolrf1STn6UrgpPZ1kwcrfrMOMJcL4NM4KvkpGf+7Nc0mje4q"
   "+bOyWYFA0M3EsiqiG74BGcWDExNrPwx2fbrAQb7zslUL28RAs+TcBd5q7XcRdrgh"
   "5hvi4VHwAryD4zeMdXNJZUDZSWQEZjgbC49DKh2QlBL1Jjk6WFgQBMzh4S2duNlq"
   "ED7HHYDV9Kfjm9/Uiu0kuD/K9lkWC7MR21UAx/BE4guxP9n42kSCQdRu89ZP7Hd9"
   "GG1+6v0s6P+KaoHAGunQRrlWXFykolAY+aOu8jSEViEv5BNKFbffHJ4me1Fnkzji"
   "wLK2sxnzhTySoel9B3p9bXJgXuMrDVEtXPwtOwARAQABiQEfBBgBCAAJBQJbw09f"
   "AhsMAAoJEGdT+OFtNfwk3rYH/Agn8NP6xIGoeGDzXunLHIdaHwy3IsFrL/JpAKjT"
   "XQK0JYyPONM+NAsUqnSWh/7bRknxuPSbh4gU/ZFV86m7RJI65VHRUT7Wa4O/f8Yq"
   "bpHJRubYVTcV2TcA+cjmZMZCCbt5KkNFHZQ7ZQG4ht3bKRwDSrB/3r8xAyQerOY3"
   "aLAgrsTit5EDPCT1Myxu/6Kmp/AcU7hxh5ag0dvKiphNdhyTXXWXSg6asjkboMo3"
   "5Htdd01lgrnxP7SW/ZehRW9FnOzOX4QTIeAxduD1jxTUdB5kGeAG6RoczWirY1ZT"
   "cMQl0COu/ON36+5saQ5cuSE3vVT34jH9CInJr8/DZy97zhE="
   "=0Oky"
   "-----END PGP PUBLIC KEY BLOCK-----"])

(def +secret-key-string+
  ["-----BEGIN PGP PRIVATE KEY BLOCK-----"
   ""
   "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"
   "=bA77"
   "-----END PGP PRIVATE KEY BLOCK-----"])

(def +public-key+  (parse-public-key (str/joinl +public-key-string+ "\n")))

(def +secret-key+  (parse-secret-key (str/joinl +secret-key-string+ "\n")))

(def +private-key+ (second (key-pair +secret-key+)))

^{:refer lib.openpgp/fingerprint :added "3.0"}
(fact "returns the fingerprint of a public key"

  (fingerprint +public-key+)
  => "E710D59C5346D3C0A1C578AE6753F8E16D35FC24")

^{:refer lib.openpgp/parse-public-key-ring :added "3.0"}
(fact "parses a public key ring from string"

  (-> (str/joinl +public-key-string+ "\n")
      (parse-public-key-ring))
  => org.bouncycastle.openpgp.PGPPublicKeyRing)

^{:refer lib.openpgp/parse-public-key :added "3.0"}
(fact "parses a public key from string"

  (-> (str/joinl +public-key-string+ "\n")
      (parse-public-key))
  ;; #public "E710D59C5346D3C0A1C578AE6753F8E16D35FC24"
  => org.bouncycastle.openpgp.PGPPublicKey)

^{:refer lib.openpgp/parse-secret-key-ring :added "3.0"}
(fact "parses a secret key ring from string"

  (-> (str/joinl +secret-key-string+ "\n")
      (parse-secret-key-ring))
  => org.bouncycastle.openpgp.PGPSecretKeyRing)

^{:refer lib.openpgp/parse-secret-key :added "3.0"}
(fact "parses a secret key from string"

  (-> (str/joinl +secret-key-string+ "\n")
      (parse-secret-key))
  ;; #secret "E710D59C5346D3C0A1C578AE6753F8E16D35FC24"
  => org.bouncycastle.openpgp.PGPSecretKey)

^{:refer lib.openpgp/key-pair :added "3.0"}
(fact "returns a public and private key pair from a secret key"

  (key-pair +secret-key+)
  ;;[#public "E710D59C5346D3C0A1C578AE6753F8E16D35FC24" #private "7445568256057146404"]
  => (contains [org.bouncycastle.openpgp.PGPPublicKey
                org.bouncycastle.openpgp.PGPPrivateKey]))

^{:refer lib.openpgp/encrypt :added "3.0"}
(fact "encrypts bytes given a public key"

  (->> (encrypt (.getBytes "Hello World")
                {:public +public-key+})

       (encode/to-base64)) ^:hidden
  ;; ["hQEMA2dT+OFtNfwkAQgAy4YMHQ70vRNgsoSQTFCLeHDMM6k6X+tkfs1xQzG5sdxlqXKxsaeX2z//i6iz"
  ;;  "kfEpo2MuzvecpKb3Eu4WPYGqqHcStHtyAUJYT4BxC2HZf3Nd8v6wpo4C++EdrIEUOY4cgQbpv1xBSd0c"
  ;;  "csoQPq9JPhGjBX3n9I+Nf6QDqjyC8EQixdeTJyb5SW23yDdPQGZd++CmWFxgnoMaQR6fJJpv0iO+1mpz"
  ;;  "QK+1xi4eTeDhdzAP36Mh8Wtj97xS+JWYyHq6MG6BsxofWmpWjZbp6xSnHytj/IWLTS7AOyCyQfOwfM81"
  ;;  "yxI4kFCHOejsGa3N1G6SpzSWkO0f3uXKVLidxdAtudJ6AUFwusdCsWWV1maNhOUre9F5sDawSz3fJea3"
  ;;  "3SF/8t0TGY0YrwL0gN1zr60AL+zykyJJiLB8b9tshXsNbO4BNdmKJ/IAi2JYk7GMIha/JSUtsHMetL2d"]
  => string?)

^{:refer lib.openpgp/decrypt :added "3.0"}
(fact "decrypts the encrypted information"

  (-> (.getBytes "Hello World")
      (encrypt {:public  +public-key+})
      ^bytes (decrypt {:private +private-key+})
      (String.))
  => "Hello World")

^{:refer lib.openpgp/generate-signature :added "3.0"}
(fact "generates a signature given bytes and a keyring"

  (generate-signature (.getBytes "Hello World")
                      {:public  +public-key+
                       :private +private-key+})
  ;; #gpg.signature ["iQEcBAABCAAGBQJbw1U8AAoJEGdT+OFtNf... "]
  => org.bouncycastle.openpgp.PGPSignature)

^{:refer lib.openpgp/crc-24 :added "3.0"}
(fact "returns the crc24 checksum "

  (crc-24 (byte-array [100 100 100 100 100 100]))
  => ["=6Fko" [-24 89 40] 15227176])

^{:refer lib.openpgp/write-sig-file :added "3.0"}
(fact "writes bytes to a GPG compatible file"

  (let [signature (-> (generate-signature (.getBytes "Hello World")
                                          {:public  +public-key+
                                           :private +private-key+})
                      (.getEncoded))]
    (write-sig-file "test-scratch/project.clj.asc"
                    signature))
  => nil)

^{:refer lib.openpgp/read-sig-file :added "3.0"}
(fact "reads bytes from a GPG compatible file"

  (read-sig-file "test-scratch/project.clj.asc")
  => bytes?)

^{:refer lib.openpgp/sign :added "3.0"}
(fact "generates a output gpg signature for an input file"

  (sign "project.clj"
        "test-scratch/project.clj.asc"
        {:public  +public-key+
         :private +private-key+})
  => bytes?)

^{:refer lib.openpgp/pgp-signature :added "3.0"}
(fact "returns a gpg signature from encoded bytes"

  (-> (generate-signature (.getBytes "Hello World")
                          {:public  +public-key+
                           :private +private-key+})
      (.getEncoded)
      (pgp-signature))
  => org.bouncycastle.openpgp.PGPSignature)

^{:refer lib.openpgp/verify :added "3.0"}
(fact "verifies that the signature works"

  (verify "project.clj"
          "test-scratch/project.clj.asc"
          {:public  +public-key+})
  => true)
