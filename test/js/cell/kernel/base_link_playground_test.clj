(ns js.cell.kernel.base-link-playground-test
  (:require [js.cell.playground :as browser]
             [std.lang :as l]
             [xt.lang.common-notify :as notify])
  (:use code.test))

(def ^:private +tiny-worker-path+
  (str (System/getProperty "user.dir") "/node_modules/tiny-worker/lib/index.js"))

(defmacro playground-worker-url
  []
  `{:create-fn
    (fn [listener]
      (var Worker (require ~+tiny-worker-path+))
      (var worker (new Worker
                       (fn []
                         (eval (@! (browser/play-worker true))))))
      (. worker (addEventListener
                 "message"
                 (fn [e]
                   (listener e.data))
                 false))
      (return worker))})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-repl :as repl]
               [xt.lang.common-runtime :as rt]
              [js.cell.kernel.base-link :as base-link]
              [js.cell.kernel.base-link-eval :as base-link-eval]
              [js.cell.kernel.base-link-local :as base-link-local]
               [js.core :as j]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(browser/stop-playground)
              (l/rt:stop)]})

(defn.js make-worker-url
  []
  (return
   {:create-fn
    (fn [listener]
      (var Worker (require (+ (. process ["env"] ["PWD"])
                              "/node_modules/tiny-worker/lib/index.js")))
      (var worker (new Worker
                       (fn []
                         (eval (@! (browser/play-worker true))))))
      (. worker (addEventListener
                 "message"
                 (fn [e]
                   (listener e.data))
                 false))
      (return worker))}))

(defn.js make-link
  []
  (return
   (base-link/link-create
    (-/make-worker-url))))

^{:refer js.cell.kernel.base-link/link-create :added "4.0"}
(fact "integrates link-create with the playground worker"

  (notify/wait-on :js
    (var link (-/make-link))
    (. link
       ["worker"]
       (postMessage (@! (l/with:input
                          (!.js (repl/notify true)))))))
  => true

  (notify/wait-on :js
    (var link (-/make-link))
    (. (base-link/call link {:op "eval"
                             :body "1+1"})
       (then (repl/>notify))))
  => 2

  (notify/wait-on :js
    (var link (-/make-link))
    (. (base-link/call link {:op "eval"
                             :id "id-async"
                             :async true
                             :body (@! (l/with:input
                                         (!.js
                                          (j/future-delayed [100]
                                            (postMessage {:op "eval"
                                                          :id "id-async"
                                                          :status "ok"
                                                          :body "1"})))))})
       (then (repl/>notify))))
  => 1

  (notify/wait-on :js
    (var link (-/make-link))
    (. (base-link-local/ping-async link 100)
       (then (repl/>notify))))
  => (contains ["pong" integer?]))

^{:refer js.cell.kernel.base-link/link-active :added "4.0"}
(fact "tracks active playground calls"

  (vals
   (!.js
    (var link (-/make-link))
    (base-link-local/ping-async link 100)
    (base-link/link-active link)))
  => (contains-in
      [{"input" {"body" [100], "action" "@worker/ping.async", "op" "call"}}]))

^{:refer js.cell.kernel.base-link-eval/wait-post :added "4.0"}
(fact "posts directly to the playground worker"

  (base-link-eval/wait-post
   (. (-/make-link) ["worker"])
   (repl/notify true))
  => true

  (base-link-eval/wait-post
   (. (-/make-link) ["worker"])
   (@! (l/with:input
         (!.js
          (repl/notify true))))
   "eval"
   {}
   "id-action")
  => true)

^{:refer js.cell.kernel.base-link-eval/post-eval :added "4.0"
  :setup [(!.js (:= (!:G LK) (-/make-link)))]}
(fact "evaluates playground code through post-eval"

  (j/<! (base-link-eval/post-eval LK
          (base-link-eval/async-post [1 2 3 4])))
  => [1 2 3 4])

^{:refer js.cell.kernel.base-link-eval/wait-eval :added "4.0"
  :setup [(!.js (:= (!:G LK) (-/make-link)))]}
(fact "evaluates playground code through wait-eval"

  (base-link-eval/wait-eval LK
    [1 2 3 4])
  => [1 2 3 4]

  (base-link-eval/wait-eval [LK 100]
    (base-link-eval/async-post [1 2 3 4])
    true)
  => [1 2 3 4])
