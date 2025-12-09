(ns xt.db.sample-scratch-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [xt.db.sample-user-test :as user]
            [xt.db.sample-data-test :as data]
            [rt.postgres :as pg]))

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

