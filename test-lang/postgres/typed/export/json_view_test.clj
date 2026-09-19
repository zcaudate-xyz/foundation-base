(ns postgres.typed.export.json-view-test
  (:require [postgres.typed.export.json-view :as export])
  (:use code.test))

(def +entries+
  [['postgres.sample.scratch-v0/log-all
    {:id "log_all"
     :input []
     :return "jsonb"
     :schema "scratch_v0"
     :flags {}
     :view {:table "Log"
            :type "select"
            :tag "all"
            :query {}}}]
   ['postgres.sample.scratch-v0/log-default
    {:id "log_default"
     :input [{:symbol "i_log_id" :type "uuid"}]
     :return "jsonb"
     :schema "scratch_v0"
     :flags {}
     :view {:table "Log"
            :type "return"
            :tag "default"
            :query {}}}]])

^{:refer postgres.typed.export.json-view/generate-views :added "4.1"}
(fact "generates the versioned table/type/tag view publication"
  (let [publication (export/generate-views +entries+)]
    [(get publication :version)
     (vec (keys (get publication :views)))
     (vec (keys (get-in publication [:views "Log"])))
     (get-in publication [:views "Log" "select" "all" :id])
     (get-in publication [:views "Log" "return" "default" :input])]
    => [1 ["Log"] ["select" "return"] "log_all"
        [{:symbol "i_log_id" :type "uuid"}]]))

^{:refer postgres.typed.export.json-view/generate-views
 :id duplicate-view-key
 :added "4.1"}
(fact "rejects duplicate table/type/tag publication keys"
  (try
    (export/generate-views (conj +entries+ (first +entries+)))
    false
    (catch clojure.lang.ExceptionInfo ex
      (= "Duplicate typed view table/type/tag keys" (.getMessage ex))))
  => true)
