(ns rt.base.suite
  (:require [std.concurrent :as cc]
            [std.lang :as l]
            [std.lib.component :as component]
            [std.string.prose :as prose]))

(l/script :bash
  {:runtime :basic})

(comment
  (defonce -proc-
    (cc/relay {:type :process
               :args ["tail" "-f" "/usr/local/var/log/rsyslog-remote.log"]}))
  @(cc/send -proc- {:op :string})
  
  (component/stop -proc-)

  (component/start -proc-)

  (defonce -nc-
    (cc/relay {:type :process
               :args ["nc" "-u" "127.0.0.1" "10514"]}))

  (do (cc/send -nc-    {:op :partial
                        :line (str "hello - " (rand))})
      @(cc/send -proc- {:op :line}))
  
  )



;;
;;
;;
;;
'[syslog - logger journalctl
  rsyslog
  jq
  [ls mkdir ...]
  [jobs bg fg disown kill]
  groff
  nc
  chroot]

()


(!.sh
 (nc -w0 -u "127.0.0.1" 10514
     (<< "<155>1 2021-11-04T02:50:36.410674+08:00 lampadas chris 177445 - Error in connect")))

(!.sh
 (nc -w0 -u "127.0.0.1" 10514
     (<< "Hello World")))


(comment
  (!.sh
   (| (echo (:Q "<155> 1 2021-11-04T02:50:36.410674+08:00 lampadas chris 177445 - Error in connect"))
      (nc -w0 -u "127.0.0.1" 10514))))

(comment
  (!.sh
   (| (echo (:Q "<155> 1 2021-11-04T02:50:36.410674+08:00 lampadas chris 177445 - Error in connect"))
      (nc -w0 -u "127.0.0.1" 10514))))



(comment
  (l/with:print-all
   (!.sh
    (do (| (echo
            (:Q (@! (prose/|
                     "\\# Header"
                     ".nf"
                     ".mk a"
                     "Sometown, Earth"
                     "vs@johndoethetrueone.com.invalid"
                     ".br"
                     ".rt \\nau"
                     ".ps +16p"
                     ".ce 1"
                     ".sp +10p"
                     "John \"The Doe\" Doe"
                     ".ps"
                     ".rt \\nau"
                     ".rj 2"
                     "linkedin.com/in/j.doe"
                     "github.com/j.doe"
                     ".fi"))))
           (groff -ms > out.ps))
        (open out.ps)))
   
   (l/with:print-all
    (!.sh
     (echo "hello")
     (echo "hello")))))
