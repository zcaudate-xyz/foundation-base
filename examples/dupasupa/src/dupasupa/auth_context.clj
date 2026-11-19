(ns dupasupa.auth-context
  (:require [lang.core :as l]))
(l/script :js
  {:import [["react" :as React]] :static {:export [AuthContext useAuth]}})
(def.js AuthContext (React.createContext nil))
(defn.js useAuth [] (return (React.useContext -/AuthContext)))
