(ns lib.aether.authentication-test
  (:require [lib.aether.authentication :refer :all]
            [std.object :as object])
  (:use code.test)
  (:import (org.eclipse.aether.repository Authentication) (org.eclipse.aether.util.repository AuthenticationBuilder ChainedAuthentication StringAuthentication SecretAuthentication)))

^{:refer lib.aether.authentication/auth-map :added "3.0"}
(fact "creates a map of the `:authentications` element"

  (auth-map (-> (AuthenticationBuilder.)
                (.addUsername "chris")
                (.addPassword "lucid")))
  => {:username "chris" :password "lucid"})

(fact "creates a `StringAuthentication` from a vector"

  (object/from-data [:username "chris"]
                    StringAuthentication)
  ;;=> #auth.string[:username "chris"]
  )

(fact "creates a `SecretAuthentication` from a vector"

  (object/from-data [:password "lucid"]
                    SecretAuthentication)
  ;;=> #auth.secret[:password "hope"]
  )

(fact "creates a `ChainedAuthentication` from a map"

  (object/from-data {:username "chris" :password "lucid"}
                    ChainedAuthentication)
  ;;=> #auth.chained{:username "chris", :password "lucid"
  )

(fact "creates a `ChainedAuthentication` from a map"

  (object/from-data {:username "chris" :password "lucid"}
                    AuthenticationBuilder)
  ;;=> #builder.auth{:username "chris", :password "lucid"}
  )

(comment

  (.? SecretAuthentication)
  (.? StringAuthentication))
