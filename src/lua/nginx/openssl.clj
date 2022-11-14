(ns lua.nginx.openssl
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str])
  (:refer-clojure :exclude [print flush time re-find]))

(l/script :lua
  {:bundle {:hmac    [["resty.openssl.hmac" :as ngxhmac]]
            :digest  [["resty.openssl.digest" :as ngxdigest]]}
   :import  [["resty.openssl.hmac" :as ngxhmac]]
   :export [MODULE]})

(defn.lua hmac
  "creates an encrypted hmac string
   
   (!.lua
    (n/encode-base64 (ssl/hmac \"HELLO\"
                               \"HELLO\")))
   => \"WF4qn8J+qzxv9/39ISakTMTNSBc=\""
  {:added "4.0"}
  [key data algo]
  (local cipher (ngxhmac.new key (or algo "sha1")))
  (. cipher (update data))
  (return (. cipher (final))))

(def.lua MODULE (!:module))
