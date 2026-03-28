(ns std.lang.base.grammar-xtalk-audit-test
  (:require [clojure.string :as str]
            [std.lang.base.grammar-xtalk-audit :as audit])
  (:use code.test))

(fact "xtalk audit exposes the extended xtalk categories"
  (set (audit/xtalk-categories))
  => (contains #{:xtalk-ws :xtalk-notify :xtalk-service}))

(fact "xtalk audit exposes websocket and service symbols"
  (set (audit/xtalk-symbols))
  => (contains #{'x:ws-connect
                 'x:ws-send
                 'x:ws-close
                 'x:notify-socket
                 'x:client-basic
                 'x:client-ws
                 'x:server-basic
                 'x:server-ws}))

(fact "support matrix distinguishes implemented from missing xtalk features"
  (let [status (:status (audit/support-matrix
                         [:js :lua :python :bash :c :glsl]
                         ['x:ws-connect 'x:client-basic 'x:len]))]
    [(get-in status [:js 'x:ws-connect])
     (get-in status [:lua 'x:client-basic])
     (get-in status [:python 'x:client-basic])
     (get-in status [:bash 'x:len])
     (get-in status [:c 'x:len])
     (get-in status [:glsl 'x:len])])
  => [:implemented
      :implemented
      :implemented
      :missing
      :missing
      :missing])

(fact "visualization renders summary and matrix views"
  (str/includes? (audit/visualize-support {:langs [:js :bash]
                                           :features ['x:len 'x:ws-connect]
                                           :view :summary})
                 "language")
  => true

  (str/includes? (audit/visualize-support {:langs [:js :bash]
                                           :features ['x:len 'x:ws-connect]
                                           :view :matrix})
                 "x:ws-connect")
  => true)
