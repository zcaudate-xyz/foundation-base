(ns std.config.ext.gpg
  (:require [lib.openpgp :as openpgp]
            [std.config.common :as common]
            [std.lib.invoke :as invoke]))

(invoke/definvoke resolve-type-gpg-public
  "resolves content to a PGP public key
 
   (resolve-type-gpg-public :gpg.public (slurp \"config/keys/test@test.com.public\"))
   => org.bouncycastle.openpgp.PGPPublicKey"
  {:added "3.0"}
  [:method {:multi common/-resolve-type
            :val :gpg.public}]
  ([_ content]
   (openpgp/parse-public-key content)))

(invoke/definvoke resolve-type-gpg
  "resolves content to a PGP pair
 
   (resolve-type-gpg :gpg (slurp \"config/keys/test@test.com\"))
   => (contains {:public  org.bouncycastle.openpgp.PGPPublicKey
                 :private org.bouncycastle.openpgp.PGPPrivateKey})"
  {:added "3.0"}
  [:method {:multi common/-resolve-type
            :val :gpg}]
  ([_ content]
   (->> (openpgp/parse-secret-key content)
        (openpgp/key-pair)
        (zipmap [:public :private]))))
