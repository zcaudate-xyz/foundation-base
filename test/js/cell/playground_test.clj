(ns js.cell.playground-test
  (:require [js.cell.playground :as play]
            [std.fs :as fs])
  (:use code.test))

^{:refer js.cell.playground/start-playground :added "4.0"}
(fact "starts the playground"

  (play/start-playground)
  => (contains {:root string?
                :port integer?
                :process java.lang.ProcessImpl}) )

^{:refer js.cell.playground/stop-playground :added "4.0"}
(fact "stops the playground"

  (play/stop-playground)
  => (any map? nil?))

^{:refer js.cell.playground/play-file :added "4.0"}
(fact "gets the file path in playground")

^{:refer js.cell.playground/play-url :added "4.0"}
(fact "gets the playground url"

  (play/play-url "hello")
  => #"http://127.0.0.1:\d+/hello")

^{:refer js.cell.playground/play-page :added "4.0"}
(fact "creates a page asset in the playground"

  (let [page (play/play-page {:name "e2e"
                              :title "e2e"
                              :body [:div {:id "root"}]
                              :scripts ["worker.js"]})
        path (play/play-file page)]
    page => string?
    (fs/exists? path) => true
    (play/play-url page) => #"http://127.0.0.1:\d+/e2e-.*\.html"))

^{:refer js.cell.playground/play-script :added "4.0"}
(fact "gets the script"

  (play/play-script '[(+ 1 2 3)] true)
  => "1 + 2 + 3;"

  (play/play-script '[(+ 1 2 3)])
  => "3ae0c35a0ad27c63af2003b2930f499e445694fb.js")

^{:refer js.cell.playground/play-worker :added "4.0"}
(fact "constructs the play worker"

  (play/play-worker true)
  => string?)

^{:refer js.cell.playground/play-files :added "4.0"}
(fact "copies files to the playground"

  (play/play-files [["project.clj" "project.clj"]])
  => (contains-in [[string? "project.clj"]]))
