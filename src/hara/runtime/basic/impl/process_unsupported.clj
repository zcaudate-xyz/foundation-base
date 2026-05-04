


;;
;; Progressive
;;

(def +no-lang+
  {:fsharp    {:default  {:eval.raw   false
                          :eval       false
                          :shell      :dotnet
                          :ws-client  false}
               :env      {:dotnet    {:exec    "dotnet"
                                      :support {:oneshot false
                                                :interactive ["fsi"]
                                                :json false
                                                :ws-client false}}}}
   :ruby      {:default  {:eval.raw   :cruby
                          :eval       :cruby
                          :shell      :cruby
                          :ws-client  :cruby}
               :env      {:cruby     {:exec    "ruby"
                                      :support {:oneshot ["-s" "-e"]
                                                :interactive ["-i"]
                                                :json ["json" true]
                                                :ws-client ["faye-websocket" false]}}}}
   :matlab    {:default  {:eval.raw   :octave
                          :eval       :octave
                          :shell      false
                          :ws-client  false}
               :env      {:octave    {:exec   "octave"
                                      :support {:oneshot ["--no-gui" "-W" "--eval"]
                                                :interactive ["--no-gui" "-W" "-i"]
                                                :json false
                                                :ws-client false}}}}})