(ns python.test-server
  (:require [std.lang :as l]
            [std.lib.env :as env]))

(l/script :python
  {:runtime :basic
   :require [[python.remote-port-server :as ss]]})



(comment
  (!.py
   (+ 1 2 3))
  (env/pp {:a 1})
  ^*(!.py
     (ss/start-async 12677))
  
  (./create-tests))
