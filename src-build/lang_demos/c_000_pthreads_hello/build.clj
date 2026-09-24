(ns lang-demos.c-000-pthreads-hello.build
  (:use [code.test :exclude [-main]])
  (:require [lang.core :as l]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [lang-demos.c-000-pthreads-hello.main :as main]))

(def +makefile+
  [{:CC :gcc
    :CFLAGS ["-I."]}
   ["out/%.o" {:- ["src/%.c"]}
    ["@mkdir -p out"]
    ["$(CC) -c -o $@ $< $(CFLAGS)"]]
   [:package {:- ["out/pthreads_hello.o"]}
    ["@mkdir -p bin"]
    ["$(CC) -o bin/pthreads_hello out/pthreads_hello.o" "-lpthread"]]
   [:run {:- [:package]}
    ["./bin/pthreads_hello"]]])

(def.make C-000-PTHREADS-HELLO
  {:build    ".build/demo/c-000-pthreads-hello"
   :github   {:repo "zcaudate/lang-demos.c-000-pthreads-hello"
              :description "Simple Posix Threads Example"}
   :orgfile  "Main.org"
   :triggers '#{lang-demos.c-000-pthreads-hello.main}
   :sections {:setup  [{:type :gitignore
                        :main ["bin" "out"]}
                       {:type :makefile
                        :main +makefile+}]}
   :default  [{:type :module.single
               :lang :c
               :main 'lang-demos.c-000-pthreads-hello.main
               :file "pthreads_hello.c"
               :target "src"}]})

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/build-all C-000-PTHREADS-HELLO)
  (make/gh:dwim-push C-000-PTHREADS-HELLO))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run-internal C-000-PTHREADS-HELLO :package)
  
  (make/run-internal C-000-PTHREADS-HELLO :run))

^{:eval false
  ;;
  ;; GH INITAL SETUP
  ;;
  :ui/action [:GITHUB :SETUP]}
(fact "initial setup of repo from github"

  (make/gh:dwim-init C-000-PTHREADS-HELLO))

^{:eval false
  ;;
  ;; GH PUSH NEWEST
  ;;
  :ui/action [:GITHUB :PUSH]}
(fact "pushes changes to github"

  (make/gh:dwim-push C-000-PTHREADS-HELLO))

(defn build-c-000-pthreads-hello
  []
  (require '[lang-demos.c-000-pthreads-hello.main])
  (make/build-all C-000-PTHREADS-HELLO)
  (make/run-internal C-000-PTHREADS-HELLO :package))

(defn -main
  []
  (build-c-000-pthreads-hello)
  (shutdown-agents)
  (System/exit 0))
