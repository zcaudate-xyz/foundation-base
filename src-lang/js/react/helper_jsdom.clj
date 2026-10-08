(ns js.react.helper-jsdom
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [js.react :as r]]})

(defn.js ^{:public true}
  setup
  "creates an isolated JSDOM window and installs its browser globals"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (var Window (require "window"))
  (var win (new Window {"url" (or (. opts ["url"]) "http://localhost/")}))
  (var document (. win ["document"]))
  (var html (or (. opts ["html"])
                "<!doctype html><html><body><div id=\"root\"></div></body></html>"))
  (:= (. document body innerHTML) html)
  (var root-id (or (. opts ["rootId"]) "root"))
  (var root (document.getElementById root-id))
  (when (xt/x:nil? root)
    (:= root (document.createElement "div"))
    (:= (. root id) root-id)
    (. document.body (appendChild root)))
  (var target globalThis)
  (var keys ["window" "document" "navigator"
             "Node" "Element" "HTMLElement"
             "Event" "CustomEvent" "MutationObserver"
             "requestAnimationFrame" "cancelAnimationFrame"
             "getComputedStyle" "IS_REACT_ACT_ENVIRONMENT"])
  (var previous {})
  (xt/for:array [key keys]
    (xt/x:set-key previous key
                  (Object.getOwnPropertyDescriptor target key)))
  (var install (fn [key value]
                 (Object.defineProperty
                  target
                  key
                  {"configurable" true
                   "enumerable" true
                   "writable" true
                   "value" value})))
  (install "window" win)
  (install "document" document)
  (install "navigator" (. win ["navigator"]))
  (install "Node" (. win ["Node"]))
  (install "Element" (. win ["Element"]))
  (install "HTMLElement" (. win ["HTMLElement"]))
  (install "Event" (. win ["Event"]))
  (install "CustomEvent" (. win ["CustomEvent"]))
  (install "MutationObserver" (. win ["MutationObserver"]))
  (install "requestAnimationFrame"
           (or (. win ["requestAnimationFrame"])
               (fn [callback] (return (setTimeout callback 0)))))
  (install "cancelAnimationFrame"
           (or (. win ["cancelAnimationFrame"])
               (fn [id] (clearTimeout id))))
  (install "getComputedStyle" (. win ["getComputedStyle"]))
  (install "IS_REACT_ACT_ENVIRONMENT" true)
  (return {"window" win
           "document" document
           "root" root
           "keys" keys
           "target" target
           "previous" previous
           "root-instance" nil
           "closed" false}))

(defn.js ^{:public true}
  render
  "renders a React component into the JSDOM root and waits for effects"
  {:added "4.1"}
  [env component props]
  (var React (require "react"))
  (var root (or (. env ["root-instance"])
                (r/createDOMRoot (. env ["root"]))))
  (xt/x:set-key env "root-instance" root)
  (return
   (React.act
    (fn []
      (. root (render
               (r/createElement component
                                (or props {}))))))))

(defn.js ^{:public true}
  teardown
  "unmounts React, restores globals, and closes the JSDOM window"
  {:added "4.1"}
  [env]
  (when (or (xt/x:nil? env)
            (. env ["closed"]))
    (return true))
  (var root (. env ["root-instance"]))
  (var failure nil)
  (try
    (when root
      (var React (require "react"))
      (React.act (fn [] (. root (unmount)))))
    (catch e
      (:= failure e)))
  (var target (. env ["target"]))
  (var previous (. env ["previous"]))
  (xt/for:array [key (. env ["keys"])]
    (var descriptor (. previous [key]))
    (if descriptor
      (Object.defineProperty target key descriptor)
      (xt/x:del-key target key)))
  (when (. env ["window"] ["close"])
    (. (. env ["window"]) (close)))
  (xt/x:set-key env "closed" true)
  (when failure
    (throw failure))
  (return true))
