(ns js.cell.playground-test
  (:use code.test)
  (:require [js.cell.playground :as play]))

^{:refer js.cell.playground/start-playground :added "4.0" :unchecked true}
(fact "starts the playground"
  ^:hidden
  
  (play/start-playground)
  => (contains {:root string?
                :port integer?
                :process java.lang.ProcessImpl}) )

^{:refer js.cell.playground/stop-playground :added "4.0" :unchecked true}
(fact "stops the playground"
  ^:hidden
  
  (play/stop-playground)
  => (any map? nil?))

^{:refer js.cell.playground/play-file :added "4.0" :unchecked true}
(fact "gets the file path in playground")

^{:refer js.cell.playground/play-url :added "4.0" :unchecked true}
(fact "gets the playground url"
  ^:hidden
  
  (play/play-url "hello")
  => #"http://127.0.0.1:\d+/hello")

^{:refer js.cell.playground/play-script :added "4.0" :unchecked true}
(fact "gets the script"
  ^:hidden
  
  (play/play-script '[(+ 1 2 3)] true)
  => "1 + 2 + 3;"

  (play/play-script '[(+ 1 2 3)])
  => "3ae0c35a0ad27c63af2003b2930f499e445694fb.js")

^{:refer js.cell.playground/play-worker :added "4.0" :unchecked true}
(fact "constructs the play worker"
  ^:hidden
  
  (play/play-worker true)
  => string?)

^{:refer js.cell.playground/play-files :added "4.0" :unchecked true}
(fact "copies files to the playground"
  ^:hidden
  
  (play/play-files [["project.clj" "project.clj"]])
  => (contains-in [[string? "project.clj"]]))
