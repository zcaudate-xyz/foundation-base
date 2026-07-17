(ns xt.ui.state.session
  "Platform-neutral authentication and access projection without routing."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt project [session profile capabilities]
  (return {"authenticated" (== true (xt/x:get-key (or session {}) "authenticated"))
           "user_id" (xt/x:get-key (or session {}) "user_id")
           "profile" (or profile {})
           "capabilities" (or capabilities {})}))

(defn.xt capable? [state capability-id]
  (return (== true (xt/x:get-path state ["capabilities" capability-id]))))
