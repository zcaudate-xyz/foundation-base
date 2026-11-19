(ns dupasupa.workbench-test
  (:use code.test)
  (:require [app.manage.server :as server]
            [app.manage.integration :as integration]
            [dupasupa.build :as build]
            [clojure.java.io :as io]
            [lang.core :as l]))

(l/script- :js
  {:runtime :basic :test-mode true :require [[xt.lang.common-repl :as repl]]})
(def BROWSER_CONTEXT (atom {}))
(fact:global {:setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(defn.js ^{:- [:async]} inspectWorkbench
  [origin token modulePath]
  (var localRequire
       (. (require "node:module")
          (createRequire (@! (.getCanonicalPath (io/file "package.json"))))))
  (var Window (localRequire "window"))
  (var win (new Window {"url" origin "pretendToBeVisual" true}))
  (var original {})
  (var keys ["window" "document" "navigator" "requestAnimationFrame" "cancelAnimationFrame"])
  (for:array [key keys]
             (:= (. original [key]) (Object.getOwnPropertyDescriptor globalThis key))
             (Object.defineProperty globalThis
                                    key
                                    {"configurable" true
                                     "writable" true
                                     "value" (:? (== key "window")
                                                 win
                                                 (== (typeof (. win [key])) "function")
                                                 (. (. win [key]) (bind win))
                                                 :else
                                                 (. win [key]))}))
  (var meta (win.document.createElement "meta"))
  (:= meta.name "app-manage-token")
  (:= meta.content token)
  (win.document.head.appendChild meta)
  (var root (win.document.createElement "div"))
  (:= root.id "app-manage-root")
  (win.document.body.appendChild root)
  (var originalFetch globalThis.fetch)
  (:= globalThis.fetch (fn [url options] (return (originalFetch (new URL url origin) options))))
  (var waitFor
       (async (fn [predicate]
                (var index 0)
                (for [(:= index 0) (< index 500)
                      (:= index (+ index 1))]
                  (when (predicate)
                    (return true))
                  (await (new Promise (fn [resolve] (setTimeout resolve 20)))))
                (throw (new Error "Workbench did not reach the expected state.")))))
  (try (var url (. (require "node:url") (pathToFileURL modulePath)))
       (await (import url.href))
       (await (waitFor (fn [] (return (win.document.querySelector "[data-view=pages]")))))
       (. (win.document.querySelector "[data-view=pages]") (click))
       (await (waitFor
               (fn [] (return (win.document.querySelector "[data-load-case=campaign-lifecycle]")))))
       (. (win.document.querySelector "[data-load-case=campaign-lifecycle]") (click))
       (var draft (JSON.parse (. (win.document.querySelector "#verification-request") value)))
       (. (win.document.querySelector "[data-verification=run-page-cases]") (click))
       (await (waitFor (fn []
                         (var summary (win.document.querySelector ".verification-result summary"))
                         (return (and summary
                                      (. summary.textContent (includes "campaign-lifecycle"))
                                      (. summary.textContent (includes "Passed")))))))
       (return [(. (win.document.querySelectorAll ".page-definition") length) (. draft.steps length)
                (. (. (win.document.querySelector ".verification-result summary") textContent)
                   (includes "Passed")) (. root.textContent (includes "Verification workbench"))])
       (catch error (throw error))
       (finally (. win (dispatchEvent (new win.Event "beforeunload")))
                (await (new Promise (fn [resolve] (setTimeout resolve 30))))
                (:= globalThis.fetch originalFetch)
                (win.close)
                (for:array [key keys]
                           (if (. original [key])
                             (Object.defineProperty globalThis key (. original [key]))
                             (delete (. globalThis [key])))))))

(defn run-browser
  []
  (integration/await-js (-/inspectWorkbench (@! (:origin (deref BROWSER_CONTEXT)))
                                            (@! (:token (deref BROWSER_CONTEXT)))
                                            (@! (:module (deref BROWSER_CONTEXT))))))

^{:refer app.manage.server/start! :id campaign-workbench :timeout 120000}
(fact
  "the emitted manager UI loads page definitions and replays saved real DB/Auth campaign checks through its HTTP action queue"
  (let [s (server/start! (constantly build/PROJECT) {:port 0})
        root (.toFile (java.nio.file.Files/createTempDirectory
                       "dupasupa-manager-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))]
    (try (doseq [[path content] (:assets s)]
           (let [file (io/file root path)]
             (io/make-parents file)
             (spit file content)))
         (reset! BROWSER_CONTEXT {:origin (str "http://127.0.0.1:" (:port s))
                                  :token (:token s)
                                  :module (.getCanonicalPath (io/file root "app.mjs"))})
         (run-browser)
         (finally (reset! BROWSER_CONTEXT {})
                  (server/stop! s)
                  (doseq [file (reverse (file-seq root))] (.delete file)))))
  =>
  [2 12 true true])
