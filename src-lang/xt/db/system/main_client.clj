(ns xt.db.system.main-client
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.lib-supabase :as lib-supabase]
             [xt.net.conn-sql :as conn-sql]
             [js.net.conn-sqlite :as js-sqlite]
             [js.net.conn-postgres :as js-postgres]
             [js.net.http-fetch :as js-fetch]]})

(defn.js create-client
  [type defaults]
  (cond (== type "sqlite")
        (return
         (js-sqlite/create defaults))

        (== type "postgres")
        (return
         (js-postgres/create defaults))

        (== type "supabase")
        (do (var #{host
                   port
                   secured
                   basepath
                   apikey})
          (return
           (lib-supabase/create-client
            (js-fetch/create-methods)
            host
            port
            secured
            basepath
            apikey)))))

(l/script :xtalk)

(defabstract.xt create-client
  [type defaults])
