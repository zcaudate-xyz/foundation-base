


(def +lisp+
  {:scheme    {:default  {:eval.raw   :racket
                          :eval       :racket
                          :shell      :racket
                          :ws-client  :racket}
               :env      {:racket    {:exec    "racket"
                                      :flavor  "scheme"
                                      :support {:oneshot ["-e"]
                                                :interactive ["-i"]
                                                :json false
                                                :ws-client ["net/rfc6455" true]}}
                          :guile     {:exec    "guile"
                                      :flavor  "scheme"
                                      :support {:oneshot ["-c"]
                                                :interactive ["-i"]
                                                :json false
                                                :ws-client false}}}}
   :lisp      {:default  {}
               :env      {:z3       {:exec   "z3"
                                     :flavor "z3"
                                     :support {:oneshot ["-e"]
                                               :interactive false
                                               :json false
                                               :ws-client false}}
                          :emacs    {:exec   "emacs"
                                     :flavor "emacs"
                                     :support {:oneshot ["-e"]
                                               :interactive false
                                               :json false
                                               :ws-client false}}}}})