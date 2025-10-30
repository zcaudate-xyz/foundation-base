(ns lib.aether.authentication
  (:require [std.object :as object]
            [std.object.query :as reflect])
  (:import (org.eclipse.aether.repository Authentication)
           (org.eclipse.aether.util.repository AuthenticationBuilder ChainedAuthentication
                                               SecretAuthentication StringAuthentication)))

(object/vector-like

 StringAuthentication
 {:tag "auth.string"
  :read (fn [auth]
          [(keyword (reflect/apply-element auth "key" []))
           (reflect/apply-element auth "value" [])])
  :write (fn [[k v]]
           (.build (doto (AuthenticationBuilder.)
                     (.addString (name k) ^String v))))}
 
 SecretAuthentication
 {:tag "auth.secret"
  :read (fn [auth]
          [(keyword (reflect/apply-element auth "key" []))
           (->> (reflect/apply-element auth "value" [])
                (vector)
                (reflect/apply-element auth "xor")
                (apply str))])
  :write (fn [[k v]]
           (.build (doto (AuthenticationBuilder.)
                     (.addSecret (name k) ^String v))))})

(defn auth-map
  "creates a map of the `:authentications` element
 
   (auth-map (-> (AuthenticationBuilder.)
                 (.addUsername \"chris\")
                 (.addPassword \"lucid\")))
   => {:username \"chris\" :password \"lucid\"}"
  {:added "3.0"}
  ([auth]
   (->> (seq (reflect/apply-element auth "authentications" []))
        (map object/to-data)
        (into {}))))

(object/map-like
 ChainedAuthentication
 {:tag "auth.chained"
  :read {:to-map auth-map}
  :write {:from-map
          (fn [m]
            (.build ^AuthenticationBuilder
                    (object/from-data m AuthenticationBuilder)))}} Authentication
 {:tag "auth"
  :write {:from-map
          (fn [m]
            (.build ^AuthenticationBuilder
             (object/from-data m AuthenticationBuilder)))}}

 AuthenticationBuilder
 {:tag "builder.auth"
  :read {:to-map auth-map}
  :write {:empty (fn [] (AuthenticationBuilder.))
          :methods (object/write-all-setters
                    AuthenticationBuilder
                    {:prefix "add"
                     :select {:password java.lang.String}})}})
