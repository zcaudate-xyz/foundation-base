(ns documentation.lib-openpgp
  (:use code.test))

[[:hero {:title "lib.openpgp"
         :subtitle "OpenPGP key parsing, encryption, signatures, and verification"
         :lead "Work with Bouncy Castle OpenPGP keys and byte-oriented encryption or signature operations through Clojure functions."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The namespace parses armored public and secret key rings, derives usable key pairs, encrypts and decrypts byte arrays, and reads or writes detached signature files."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Parse keys"}]]

(comment
  (require '[lib.openpgp :as openpgp])

  (def public-key
    (openpgp/parse-public-key public-key-text))

  (def secret-key
    (openpgp/parse-secret-key secret-key-text))

  (def pair (openpgp/key-pair secret-key))
  (openpgp/fingerprint public-key))

[[:section {:title "Encrypt and decrypt bytes"}]]

(comment
  (let [[public private] pair
        encrypted (openpgp/encrypt
                   (.getBytes "hello")
                   {:public public})]
    (String.
     (openpgp/decrypt encrypted
                      {:private private}))))

[[:section {:title "Create and verify a detached signature"}]]

(comment
  (let [[public private] pair]
    (openpgp/sign "artifact.jar"
                  "artifact.jar.asc"
                  {:public public :private private})
    (openpgp/verify "artifact.jar"
                    "artifact.jar.asc"
                    {:public public})))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.openpgp"}]]
