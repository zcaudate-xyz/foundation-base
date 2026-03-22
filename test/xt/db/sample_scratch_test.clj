(ns xt.db.sample-scratch-test
  (:require [rt.postgres :as pg]
            [std.lang :as l]
            [xt.db.sample-data-test :as data]
            [xt.db.sample-user-test :as user])
  (:use code.test))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]
   })

(def +app+ (pg/app "scratch"))

(def +tree+
  (pg/bind-schema (:schema +app+)))

(defglobal.xt Schema
  (@! +tree+))

(defglobal.xt SchemaLookup
  (@! (pg/bind-app +app+)))

(def.xt MODULE (!:module))

