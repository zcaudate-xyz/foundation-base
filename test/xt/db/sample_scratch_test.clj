(ns xt.db.sample-scratch-test
  (:use code.test)
  (:require [rt.postgres :as pg]
            [std.lang :as l]
            [rt.postgres.script.test.scratch-v1 :as v1]
            [xt.db.sample-data-test :as data]
            [xt.db.sample-user-test :as user]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(def +app+ (pg/app "scratch"))

(def +tree+
  (pg/bind-schema (:schema +app+)))

(defglobal.xt Schema
  (@! +tree+))

(defglobal.xt SchemaLookup
  (@! (pg/bind-app +app+)))

(def.xt MODULE (!:module))

