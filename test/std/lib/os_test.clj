(ns std.lib.os-test
  (:use code.test)
  (:require [std.lib.os :refer :all]))

^{:refer std.lib.os/native? :added "3.0"}
(fact "checks if execution is in graal"

  (native?) ;;false
  => boolean?)

^{:refer std.lib.os/os :added "3.0"}
(fact "returns the current os"

  (os) ;;"Mac OS X"
  => string?)

^{:refer std.lib.os/sh-wait :added "3.0"}
(fact "waits for sh process to complete")

^{:refer std.lib.os/sh-output :added "3.0"}
(fact "returns the sh output")

^{:refer std.lib.os/sh-write :added "4.0"}
(fact "writes string or bytes to the process")

^{:refer std.lib.os/sh-read :added "4.0"}
(fact "reads value from stdout"

  (sh-read
   (sh "ls" {:wait true
             :output false}))
  => string?)

^{:refer std.lib.os/sh-error :added "4.0"}
(fact "reads value from stderr")

^{:refer std.lib.os/sh-close :added "4.0"}
(fact "closes the sh output")

^{:refer std.lib.os/sh-exit :added "4.0"}
(fact "calls `destroy` on the process")

^{:refer std.lib.os/sh-kill :added "4.0"}
(fact "calls `destroyForcibly` on the process")

^{:refer std.lib.os/sh :added "3.0"}
(fact "creates a sh process"

  @(sh "ls")
  => string?

  @(sh {:args ["ls"]})
  => string?

  (sh {:args ["ls"]
       :wait false})
  => Process

  (sh {:args ["ls"]
       :wrap false})
  => string?)

^{:refer std.lib.os/sys:wget-bulk :added "4.0"}
(fact "download files using call to wget")

^{:refer std.lib.os/tmux-with-args :added "4.0"}
(fact "creates tmux args"

  (tmux-with-args ["tmux"] {:root "."
                            :env {:hello "world"}})
  => ["tmux" "-c" "." "-e" "HELLO=world"])

^{:refer std.lib.os/tmux:kill-server :added "4.0"}
(fact "kills the tmux server")

^{:refer std.lib.os/tmux:list-sessions :added "4.0"}
(fact "lists all tmux sessions")

^{:refer std.lib.os/tmux:has-session? :added "4.0"}
(fact "checks if tmux session exists")

^{:refer std.lib.os/tmux:new-session :added "4.0"}
(fact "creates a new tmux session")

^{:refer std.lib.os/tmux:kill-session :added "4.0"}
(fact "kills a tmux session")

^{:refer std.lib.os/tmux:list-windows :added "4.0"}
(fact "lists all windows in a session")

^{:refer std.lib.os/tmux:has-window? :added "4.0"}
(fact "checks if window exists in a session")

^{:refer std.lib.os/tmux:run-command :added "4.0"}
(fact "runs a command in a window")

^{:refer std.lib.os/tmux:new-window :added "4.0"}
(fact "opens a new window in the session")

^{:refer std.lib.os/tmux:kill-window :added "4.0"}
(fact "kills the tumx window")

^{:refer std.lib.os/say :added "3.0"}
(comment "enables audio debugging"

  (say "hello there"))

^{:refer std.lib.os/os-notify :added "3.0"}
(fact "notifies the os using `alerter``")

^{:refer std.lib.os/os-run :added "4.0"}
(fact "runs a function with os resources")

^{:refer std.lib.os/beep :added "3.0"}
(comment "initiates a beep sound"

  (beep))

^{:refer std.lib.os/clip :added "3.0"}
(comment "clips a string to clipboard"

  (clip "hello"))

^{:refer std.lib.os/clip:nil :added "4.0"}
(fact "clips a string to clipboard with no return")

^{:refer std.lib.os/paste :added "4.0"}
(comment "pastes a string from clipboard"

  (paste)
  => "hello")

^{:refer std.lib.os/url-encode :added "4.0"}
(fact "encodes string to url"
  ^:hidden
  
  (url-encode "http://www.goggle.com")
  => "http%3A%2F%2Fwww.goggle.com")

^{:refer std.lib.os/url-decode :added "4.0"}
(fact "decodes string from url"
  ^:hidden
  
  (url-decode "http%3A%2F%2Fwww.goggle.com")
  => "http://www.goggle.com")


^{:refer std.lib.os/os-arch :added "4.0"}
(fact "TODO")