(ns std.lib.os
  (:require [std.lib.impl :refer [defimpl]]
            [std.lib.foundation :as h]
            [std.lib.env :as env]
            [std.string.common :as str]
            [std.protocol.component :as protocol.component]
            [std.lib.future :as f])
  (:import (java.io File)))

(def ^:dynamic *native-compile* false)

(defn native?
  "checks if execution is in graal
 
   (native?) ;;false
   => boolean?"
  {:added "3.0"}
  ([]
   (.startsWith (System/getProperty "java.vm.name")
                "Substrate VM")))

(defn os
  "returns the current os
 
   (os) ;;\"Mac OS X\"
   => string?"
  {:added "3.0"}
  ([]
   (System/getProperty "os.name")))

(defn os-arch
  "TODO"
  {:added "4.0"}
  ([]
   (System/getProperty "os.arch")))


(defn sh-wait
  "waits for sh process to complete"
  {:added "3.0"}
  ([^Process process]
   (doto process
     (.waitFor))))

(defn sh-output
  "returns the sh output"
  {:added "3.0"}
  ([^Process process]
   {:exit (.exitValue process)
    :out  (slurp (.getInputStream process))
    :err  (slurp (.getErrorStream process))}))


(defn sh-write
  "writes string or bytes to the process"
  {:added "4.0"}
  ([^Process process content]
   (let [^"[B" bs (if (string? content)
                    (.getBytes ^String content)
                    content)]
     (doto process
       (-> (.getOutputStream)
           (doto (.write bs) (.flush)))))))

(defn sh-read
  "reads value from stdout
 
   (sh-read
    (sh \"ls\" {:wait true
              :output false}))
   => string?"
  {:added "4.0"}
  ([^Process process]
   (let [is (.getInputStream process)
         available (.available is)]
     (if (pos? available)
       (String. (.readNBytes is available))))))

(defn sh-error
  "reads value from stderr"
  {:added "4.0"}
  ([^Process process]
   (let [is (.getErrorStream process)
         available (.available is)]
     (if (pos? available)
       (String. (.readNBytes is available))))))

(defn sh-close
  "closes the sh output"
  {:added "4.0"}
  ([^Process process]
   (doto process
     (-> (.getOutputStream)
         (.close)))))

(defn sh-exit
  "calls `destroy` on the process"
  {:added "4.0"}
  ([^Process process]
   (doto process
     (-> (.destroy)))))

(defn sh-kill
  "calls `destroyForcibly` on the process"
  {:added "4.0"}
  ([^Process process]
   (doto process
     (-> (.destroyForcibly)))))

(defn ^Process sh
  "creates a sh process
 
   @(sh \"ls\")
   => string?
 
   @(sh {:args [\"ls\"]})
   => string?
 
   (sh {:args [\"ls\"]
        :wait false})
   => Process
 
   (sh {:args [\"ls\"]
        :wrap false})
   => string?"
  {:added "3.0"}
  ([m]
   (if (string? m)
     (sh m {})
     (let [args (:args m)
           _    (if (empty? args) (h/error "Args cannot be empty."))
           args (conj (vec args) (dissoc m :args))]
           (apply sh args))))
  ([^String s & args]
   (let [[opts args] (if (map? (last args))
                       [(last args) (cons s (butlast args))]
                       [{} (cons s args)])
         {:keys [print wait trim wrap output inherit root ignore-errors env async]
          :or {inherit false
               wrap   true
               trim   true}} opts
         root      (or root (System/getenv "PWD"))
         _         (if print (env/prn args))
         wait      (if (nil? wait)
                     (or async (not inherit))
                     wait)
         output    (if (nil? output)
                     (if-not wait
                       false
                       (not inherit))
                     output)
         builder   (ProcessBuilder. ^"[Ljava.lang.String;" (into-array String args))
         merge-env (fn [m env]
                     (reduce (fn [^java.util.Map m [k v]]
                               (doto m (.put (str k) (str v)))) m env))
         process (-> builder
                     (.directory (File. ^String root))
                     (cond-> inherit (.inheritIO))
                     (cond-> env (doto (-> (.environment)
                                           (merge-env env))))
                     (.start))
         thunk (fn []
                 (let [out-fn (fn [process]
                                (let [{:keys [out err]} (sh-output process)]
                                  (cond (and (empty? out)
                                             (not-empty err)
                                             (not ignore-errors))
                                        (h/error err)

                                        :else
                                        (cond-> out
                                          trim    str/trim
                                          wrap    (h/wrapped identity)))))]
                   (cond-> process
                     wait    (doto (.waitFor))
                     output  out-fn)))]
     (if async
       (f/future (thunk))
       (thunk)))))

(defn sys:wget-bulk
  "download files using call to wget"
  {:added "4.0"}
  [root-url root-path files & [args]]
  (f/on:all
   (map (fn [file]
          (let [[file output] (if (vector? file)
                                file
                                [file nil])]
            (f/on:exception
             (sh {:args (vec (concat ["wget" (str root-url file)]
                                     (if output ["-O" output])
                                     args)) 
                  :async true
                  :root (str "resources/" root-path)})
             identity)))
        files)))

(extend-type Process
  protocol.component/IComponent
  (-start         [p] p)
  (-stop          [p] (.destroy p))
  (-kill          [p] (.destroyForcibly p))
  protocol.component/IComponentQuery
  (-started?      [p] (.isAlive p))
  (-stopped?      [p] (not (.isAlive p)))
  (-remote?       [p] false))

(defn tmux-with-args
  "creates tmux args
 
   (tmux-with-args [\"tmux\"] {:root \".\"
                             :env {:hello \"world\"}})
   => [\"tmux\" \"-c\" \".\" \"-e\" \"HELLO=world\"]"
  {:added "4.0"}
  ([cmd {:keys [root env]}]
   (cond-> cmd
     root (conj "-c" root)
     env  (concat (mapcat (fn [[k v]]
                            ["-e" (str (str/upper-case (h/strn k))
                                       "="
                                       v)])
                          env))
     :then vec)))

(defn tmux:kill-server
  "kills the tmux server"
  {:added "4.0"}
  ([]
   (sh "tmux" "kill-server" {:wrap false})))

(defn tmux:list-sessions
  "lists all tmux sessions"
  {:added "4.0"}
  ([]
   (-> (sh "tmux" "list-sessions" "-F" "#{session_name}" {:wrap false})
       (str/split-lines))))

(defn tmux:has-session?
  "checks if tmux session exists"
  {:added "4.0"}
  ([session]
   (->> (sh "tmux" "has-session" "-t" (str session) {:output false})
        (sh-output)
        :exit
        (zero?))))

(defn tmux:new-session
  "creates a new tmux session"
  {:added "4.0"}
  ([session & [opts]]
   (let [cmd ["tmux" "new-session"
              "-d" "-s" (str session)
              "-n" "CMD"]]
     (sh-output (apply sh (concat (tmux-with-args cmd opts) [{:wrap false
                                                              :output false}]))))))

(defn tmux:kill-session
  "kills a tmux session"
  {:added "4.0"}
  ([session]
   (sh "tmux" "kill-session" "-t" (str session) {:wrap false})))

(defn tmux:list-windows
  "lists all windows in a session"
  {:added "4.0"}
  ([session]
   (-> (sh "tmux" "list-windows" "-t" (str session)
           "-F" "#{window_name}" {:wrap false})
       (str/split-lines))))

(defn tmux:has-window?
  "checks if window exists in a session"
  {:added "4.0"}
  ([session key]
   (contains? (set (tmux:list-windows session))
              (str key))))

(defn tmux:run-command
  "runs a command in a window"
  {:added "4.0"}
  ([session key cmd]
   (let [cmd (if (string? cmd)
               cmd
               (str/join " " cmd))]
     (sh-output (sh "tmux" "send-keys" "-t" (str session ":" key)
                    cmd "C-m" {:wrap false
                               :output false})))))

(defn tmux:new-window
  "opens a new window in the session"
  {:added "4.0"}
  ([session key & [opts]]
   (if (tmux:has-window? session key)
     :window-already-present
     (->> (tmux-with-args ["tmux" "new-window" "-n" (str key)] opts)
          (tmux:run-command session "CMD")))))

(defn tmux:kill-window
  "kills the tumx window"
  {:added "4.0"}
  ([session key]
   (->> ["tmux" "kill-window" "-t" (str session ":" key)]
        (tmux:run-command session "CMD"))))



;;

(comment
  (sh "afplay")
  
  ;;(sh "tmux" "kill-window" "-t" "DEV:CMD")
  (tmux:fin "hello" "bash")
  (tmux:list-sessions)
  (tmux:list-windows "DEV")
  
  (sh "tmux" "send-keys" "-t" "DEV:CMD" "tmux" "C-m")
  (tmux:kill-session "DEV")
  (tmux:kill-server)
  (tmux:list-windows "DEV")
  (tmux:new-session "DEV")
  (tmux:run-command "DEV" "hello" ["ls"])
  (tmux:new-window "DEV" "hello" {:env {"HELLO" 1}
                                  :root "/"})
  (tmux:has-window? "DEV" "hello")
  (tmux:kill-window "DEV" "hello")
  (defn tmux:get-window
    ([session name]))
  (tmux:new-session "hello")
  (defn tmux:ensure-session
    ([name])))



;;
;;
;;

(defn say
  "enables audio debugging
 
   (say \"hello there\")"
  {:added "3.0"}
  ([& phrase]
   (apply sh "say" (map str phrase))))

(defn os-notify
  "notifies the os using `alerter``"
  {:added "3.0"}
  ([^String title ^String  message]
   (case (os)
     "Mac OS X" (sh "alerter" "-message" message "-title" title "-sound" "BELL" {:async true})
     "Linux"    (sh "notify-send" (str "'" title "'") (str "'" message "'") {:async true})
     (h/error "OS Notify" {:title title :message message}))))

(defn os-run
  "runs a function with os resources"
  {:added "4.0"}
  ([& commands]
   (let [[arr m] (if (map? (last commands))
                   [(butlast commands) (last commands)]
                   [commands {}])
         m (assoc m :async true)]
     (if (= (os) "Mac OS X")
       (apply sh "ttab" (conj (vec arr) m))
       (h/error "TODO")))))

(defn beep
  "initiates a beep sound
 
   (beep)"
  {:added "3.0"}
  ([]
   ;; Also (eval '(.beep (java.awt.Toolkit/getDefaultToolkit)))
   (if (= (os) "Mac OS X")
     (try 
       (sh "afplay" "/System/Library/Sounds/Funk.aiff" {:inherit true :async true})
       (catch Throwable t))
     (sh "/bin/bash" "-c" "echo -ne '\\007' > /dev/tty" {:inherit true :async true}))))

(defn clip
  "clips a string to clipboard
 
   (clip \"hello\")"
  {:added "3.0"}
  ([s]
   ;;(if (= "Linux" (os)))
   ;;nil
   (let [s  (if (string? s)
              s
              (env/pp-str s)) 
         board (.getSystemClipboard (java.awt.Toolkit/getDefaultToolkit))
         ts    (java.awt.datatransfer.StringSelection. s)]
     (.setContents board ts nil)
     s)))

(defn clip:nil
  "clips a string to clipboard with no return"
  {:added "4.0"}
  ([s]
   (do (clip s)
       nil)))

(defn paste
  "pastes a string from clipboard
 
   (paste)
   => \"hello\""
  {:added "4.0"}
  ([]
   ;;(if (= "Linux" (os)))
   ;;nil
   (let [board (.getSystemClipboard (java.awt.Toolkit/getDefaultToolkit))]
     (-> (.getContents board nil)
         (.getTransferData  java.awt.datatransfer.DataFlavor/stringFlavor)))))

(defn url-encode
  "encodes string to url"
  {:added "4.0"}
  ([^String s]
   (java.net.URLEncoder/encode s "UTF-8")))

(defn url-decode
  "decodes string from url"
  {:added "4.0"}
  ([^String s]
   (java.net.URLDecoder/decode s "UTF-8")))

(comment

  (say "hello")
  
  (sh "ttab" "ls" "-a" {:root "src-app/demo"})
  (os-run "ls")
  (os-notify "hello" "World")

  (sh "ttab" "ls -la" {:async true})

  (sh "bash" "-c" "osascript -e 'tell application \"Terminal\" to activate'"))
