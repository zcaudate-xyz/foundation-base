(ns js.lib.rn-async-storage
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :js
  {:import [["@react-native-async-storage/async-storage" :as RNAsyncStorage]] :require [[xt.lang.common-lib :as k] [xt.lang.spec-base :as xt]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "RNAsyncStorage.default"
                                   :tag "js"}]
  [getItem
   setItem
   mergeItem
   removeItem
   
   multiGet
   multiSet
   multiMerge
   multiRemove
   getAllKeys
   clear
   useAsyncStorage])

(defn.js getJSON
  "gets the json data structure"
  {:added "4.0"}
  [key]
  (return
   (. (-/getItem key)
      (then (fn [res]
              (when (k/is-string? res)
                (try
                  (return (xt/x:json-decode res))
                  (catch e (return nil)))))))))

(defn.js setJSON
  "sets the json structure"
  {:added "4.0"}
  [key data]
  (return
   (-/setItem key (xt/x:json-encode data))))

(defn.js mergeJSON
  "merges json data on the same key"
  {:added "4.0"}
  [key data]
  (return
   (-/mergeItem key (xt/x:json-encode data))))

