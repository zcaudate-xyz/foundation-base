(ns play.go-000-user-directory.main
  (:require [hara.lang :as l]
            [hara.lang.model.spec-go]))

(l/script :go)

(defn.go ^{:- [:string]}
  FormatUserKey
  [:string orgId
   :string userId]
  (return (+ orgId ":" userId)))

(defn.go ^{:- [:int]}
  NextOffset
  [:int offset
   :int pageSize]
  (return (+ offset pageSize)))
