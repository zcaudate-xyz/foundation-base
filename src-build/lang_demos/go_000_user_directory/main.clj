(ns lang-demos.go-000-user-directory.main
  (:require [lang.core :as l]
            [lang.model.builtin.spec-go]))

(l/script :go)

^{:clj-kondo/ignore [:unresolved-symbol]}
(defstruct.go User
  [[OrganizationID string]
   [ID string]
   [DisplayName string]
   [Roles [:> slice string]]])

^{:clj-kondo/ignore [:unresolved-symbol]}
(defstruct.go UserDirectory
  [[Users [:> map string (:* User)]]])

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

(defn.go ^{:- [:*UserDirectory]}
  ResetUserDirectory
  [:*UserDirectory directory]
  (if (== directory nil)
    (return nil))
  (:= directory.Users (make [:> map string (:* User)]))
  (return directory))

(defn.go ^{:- [:*UserDirectory]}
  PutUser
  [:*UserDirectory directory
   :User user]
  (if (== directory nil)
    (return nil))
  (if (== directory.Users nil)
    (:= directory.Users (make [:> map string (:* User)])))
  (:= (. directory.Users [(FormatUserKey user.OrganizationID user.ID)])
      (:& user))
  (return directory))

(defn.go ^{:- [:*User]}
  LookupUser
  [:*UserDirectory directory
   :string orgId
   :string userId]
  (if (== directory nil)
    (return nil))
  (return (. directory.Users [(FormatUserKey orgId userId)])))

(defn.go ^{:- [:bool]}
  RemoveUser
  [:*UserDirectory directory
   :string orgId
   :string userId]
  (if (== directory nil)
    (return false))
  (var key := (FormatUserKey orgId userId))
  (if (== (. directory.Users [key]) nil)
    (return false))
  (delete directory.Users key)
  (return true))

(defn.go ^{:- [:int]}
  UserCount
  [:*UserDirectory directory]
  (if (== directory nil)
    (return 0))
  (return (len directory.Users)))
