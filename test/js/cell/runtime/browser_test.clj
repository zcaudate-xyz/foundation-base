(ns js.cell.runtime.browser-test
  (:use code.test)
  (:require [js.cell.playground :as play]
            [js.cell.runtime.browser :as runtime-browser]
            [js.cell.runtime.emit :as emit]
            [rt.basic.type-common :as common]
            [rt.chromedriver :as chromedriver]
            [rt.chromedriver.impl]
            [std.lang :as l]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify]))

(defn runtime-browser-url
  []
  (play/play-url
   (play/play-page {:name "runtime-browser-scaffold"})))

(def ^:private +chromium-available+
  (common/program-exists?
   (or (System/getenv "CHROME")
       "chromium")))

(when +chromium-available+
  (l/script :js
    {:runtime :chromedriver.instance
     :config {:url (runtime-browser-url)}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.common-runtime :as rt]
               [js.cell.kernel :as cl]
               [js.cell.kernel.base-link-local :as base-link-local]
               [js.cell.runtime.browser :as runtime-browser]]}))

(fact:global
  {:setup [(when +chromium-available+
             (l/rt:restart :js))
           (when +chromium-available+
             (chromedriver/goto (runtime-browser-url) 5000))
           (when +chromium-available+
             (l/rt:scaffold-imports :js))]
   :teardown [(when +chromium-available+
                (l/rt:stop))]})

(defmacro webworker-cell-check
  []
  (template/$
    (notify/wait-on :js
      (var cell (runtime-browser/make-webworker-cell ~(emit/webworker-script)))
      (. (. cell ["init"])
         (then
           (fn []
             (. (. (cl/add-model "hello"
                                 {:echo {:handler base-link-local/echo
                                         :defaultArgs ["HELLO"]}}
                                 cell)
                  ["init"])
                (then
                  (fn []
                    (repl/notify (cl/model-vals "hello" cell)))))))))))

(defmacro sharedworker-cell-check
  []
  (template/$
    (notify/wait-on :js
      (var cell (runtime-browser/make-sharedworker-cell ~(emit/sharedworker-script)))
      (. (. cell ["init"])
         (then
           (fn []
             (. (. (cl/add-model "hello"
                                 {:echo {:handler base-link-local/echo
                                         :defaultArgs ["HELLO"]}}
                                 cell)
                  ["init"])
                (then
                  (fn []
                    (repl/notify (cl/list-models cell)))))))))))

^{:refer js.cell.runtime.browser/make-webworker-cell :added "4.1"}
(fact "creates a kernel cell backed by a browser WebWorker"
  ^:hidden
  (if +chromium-available+
    (webworker-cell-check)
    :skip)
  => (if +chromium-available+
       (contains-in {"echo" ["HELLO" integer?]})
       :skip))

^{:refer js.cell.runtime.browser/make-sharedworker-cell :added "4.1"}
(fact "creates a kernel cell backed by a browser SharedWorker"
  ^:hidden
  (if +chromium-available+
    (sharedworker-cell-check)
    :skip)
  => (if +chromium-available+
       ["hello"]
       :skip))
