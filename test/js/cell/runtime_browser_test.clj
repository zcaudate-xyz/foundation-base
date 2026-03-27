(ns js.cell.runtime-browser-test
  (:use code.test)
  (:require [clojure.string :as str]
            [js.cell.runtime.emit :as emit]
            [js.cell.playground :as play]
            [js.cell.runtime.link :as runtime-link]
            [rt.chromedriver.impl]
            [std.lang :as l]
            [std.lib.template :as template]
            [xt.lang.base-notify :as notify]))

(l/script :js
  {:runtime :chromedriver.instance
   :config {:url (play/play-url
                  (play/play-page {:name "runtime-browser"}))}
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [js.cell.kernel :as cl]
             [js.cell.kernel.base-link-local :as base-link-local]
             [js.cell.runtime.env-sharedworker :as env-sharedworker]
             [js.cell.runtime.env-webworker :as env-webworker]
             [js.cell.runtime.link :as runtime-link]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defmacro webworker-cell-check
  []
  (template/$
    (notify/wait-on :js
      (var cell (cl/make-cell
                 (runtime-link/make-webworker-link ~(emit/webworker-script))))
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
      (var cell (cl/make-cell
                 (runtime-link/make-sharedworker-link ~(emit/sharedworker-script))))
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

^{:refer js.cell.runtime.env-webworker/script :added "4.0"}
(fact "emits a WebWorker bootstrap script"
  ^:hidden
  (str/includes? (emit/webworker-script) "self")
  => true)

^{:refer js.cell.runtime.env-sharedworker/script :added "4.0"}
(fact "emits a SharedWorker bootstrap script"
  ^:hidden
  (str/includes? (emit/sharedworker-script) "onconnect")
  => true)

^{:refer js.cell.runtime.browser/make-webworker-cell :added "4.0"}
(fact "boots the kernel in a browser WebWorker"
  ^:hidden
  (webworker-cell-check)
  => (contains-in {"echo" ["HELLO" integer?]}))

^{:refer js.cell.runtime.browser/make-sharedworker-cell :added "4.0"}
(fact "boots the kernel in a browser SharedWorker"
  ^:hidden
  (sharedworker-cell-check)
  => ["hello"])
