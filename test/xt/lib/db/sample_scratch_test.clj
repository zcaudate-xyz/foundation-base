(ns xt.lib.db.sample-scratch-test
  (:use code.test)
  (:require [rt.postgres :as pg]
            [std.lang :as l]
            [rt.postgres.test.scratch-v1 :as v1]
            [xt.lib.db.sample-data-test :as data]
            [xt.lib.db.sample-user-test :as user]))

(l/script :xtalk
  {:require [[xt.lang.common-lib :as k]]})

(def +app+ (pg/app "scratch"))

(def +tree+
  (pg/bind-schema (:schema +app+)))

(defglobal.xt Schema
  (@! +tree+))

(defglobal.xt SchemaLookup
  (@! (pg/bind-app +app+)))

(def.xt MODULE (!:module))

