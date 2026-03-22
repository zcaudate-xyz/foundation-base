(ns python.test
  (:require [std.lang :as l]
            [std.lib.env]))

(l/script :python
  {:runtime :remote-port
   :config {:port 12678}
   :require [[xt.lang.base-lib :as k]]})

(comment
  (into {} (l/rt :python))
  (!.py
   (+ 1 2 3))
  
  (!.py
   (k/arr-map [1 2 3 4]
              k/inc))
  (require 'std.concurrent.print)
  (./create-tests)
  
  (def +r+
    (std.concurrent/relay
     {:type :socket
      :host "localhost"
      :port 12678}))
  
  (def +r+
    (std.concurrent/relay
     {:type :socket
      :host "localhost"
      :port 5000}))
  
  @(std.concurrent/send +r+
                        (str (std.json/write "globals()[\"A\"] = 1")
                             "\n"))
  
  @(std.concurrent/send +r+ "<PING>")
  
  @(std.concurrent/send +r+
                        (std.lib.env/do:pp
                         (std.json/write
                          (std.lang/emit-script
                           (rt.basic.impl.process-python/default-body-wrap
                            ['(+ 1 2 3 4)])
                           {:lang :python})))))
