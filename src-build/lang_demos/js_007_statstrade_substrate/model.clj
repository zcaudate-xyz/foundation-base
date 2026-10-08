(ns lang-demos.js-007-statstrade-substrate.model
  (:require [lang.core :as l]))

(l/script :js
  {:require [[lang-demos.js-007-statstrade-substrate.link :as link]]})

;; The familiar statsui arrangement. Request arguments and context are explicit;
;; entries describe the fields displayed/edited, independently of the transport.
(def.js TOPIC
  {:event-sync {:signal "topic/changed"}
   :views
   {:list {:fn link/call
           :input (fn:> [args context] [context "topic/list" args])}
    :detail {:fn link/call
             :input (fn:> [args context] [context "topic/detail" args])}}
   :actions
   {:modify {:fn link/call
             :input (fn:> [args context] [context "topic/modify" args])}}
   :entries
   {:list ["id" "title"]
    :detail ["id" "title" "revision"]
    :modify ["title"]}})
