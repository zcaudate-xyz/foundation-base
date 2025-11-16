(ns lua.core.socket
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str])
  (:refer-clojure :exclude [assert byte format load max min print remove sort time type]))

(l/script :lua
  lua.core
  {:import [["socket" :as socket]
            ["socket.url" :as socket-url]]})

(def$.lua socket socket)

(def$.lua url socket-url)

(def$.lua dns socket.dns)

(h/template-entries [l/tmpl-macro {:inst "sock"
                                   :prefix "t:"
                                   :tag "lua"}]
  [[start  [] {:optional [detached joinable]}]
   [join   []]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "url"
                                   :prefix "url:"
                                   :tag "lua"}]
  [absolute
   build
   build-path
   escape
   parse
   parse-path
   unescape])


(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "socket"
                                   :prefix "sock:"
                                   :tag "lua"}]
  [gettime
   newtry
   protect
   select
   sink
   skip
   sleep
   source
   tcp
   try
   upd])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "socket.dns"
                                   :prefix "dns:"
                                   :tag "lua"}]
  [toip tohostname gethostname])

(h/template-entries [l/tmpl-macro {:inst "sock"
                                   :prefix "s:"
                                   :tag "lua"}]
  [[accept  []]
   [bind    [address port]]
   [close   []]
   [connect [address port]]
   [getpeername []]
   [getsockname []]
   [getstats  []]
   [listen   [backlog]]
   [receive  [pattern] {:optional [prefix]}]
   [receivefrom  [size]]
   [send     [data] {:optional [i j]}]
   [sendto   [datagrapm ip port]]
   [setoption [option] {:optional [value]}]
   [setpeername [address port]]
   [setsockname [address port]]
   [setstats  [received sent age]]
   [settimeout  [value] {:optional [mode]}]
   [shutdown  [mode]]])
