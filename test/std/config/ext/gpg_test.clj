(ns std.config.ext.gpg-test
  (:require [lib.openpgp]
            [std.config.ext.gpg :refer :all])
  (:use code.test))

^{:refer std.config.ext.gpg/resolve-type-gpg-public :added "3.0"}
(fact "resolves content to a PGP public key"

  (resolve-type-gpg-public :gpg.public (slurp "config/keys/test@test.com.public"))
  => org.bouncycastle.openpgp.PGPPublicKey)

^{:refer std.config.ext.gpg/resolve-type-gpg :added "3.0"}
(fact "resolves content to a PGP pair"

  (resolve-type-gpg :gpg (slurp "config/keys/test@test.com"))
  => (contains {:public  org.bouncycastle.openpgp.PGPPublicKey
                :private org.bouncycastle.openpgp.PGPPrivateKey}))
