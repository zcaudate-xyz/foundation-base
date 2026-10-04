(ns std.make.project-test
  {:clj-kondo/config '{:linters {:refer-all {:level :off}
                                  :unresolved-symbol {:level :off}
                                  :unresolved-namespace {:level :off}
                                  :invalid-arity {:level :off}}}}
  (:require [std.lib.env :as env]
            [std.lib.trace :as trace]
            [std.make.common :as common]
            [std.make.compile :as compile]
            [std.make.project :refer :all])
  (:use code.test))

^{:refer std.make.project/makefile-parse :added "4.0"}
(fact "parses a makefile for it's sections")

^{:refer std.make.project/build-default :added "4.0"}
(fact "builds the default section")

^{:refer std.make.project/changed-files :added "4.0"}
(fact "gets changed files from result")

^{:refer std.make.project/is-changed? :added "4.0"}
(fact "checks that project result is changed")

^{:refer std.make.project/build-all :added "4.0"}
(fact "builds all sections in a make config")

^{:refer std.make.project/build-at :added "4.0"}
(fact "builds a custom section")

^{:refer std.make.project/def-make-fn :added "4.0"}
(fact "def.make implemention")

^{:refer std.make.project/def.make :added "4.0"}
(fact "macro to instantiate a section"

  (require '[std.make.common :as common])
  (def.make TEST_MAKE
    {:tag "test.make"
     :default [{:type :raw :file "hello.txt" :main "hello"}]
     :triggers #{"test.trigger"}})

  (common/triggers-get TEST_MAKE)
  => #{"test.trigger"})

^{:refer std.make.project/build-triggered :added "4.0"}
(fact "builds for a triggering namespace")

^{:refer std.make.project/build-triggered-single :added "4.1"}
(fact "builds only the triggered namespace module"
  (let [target-config (common/make-config
                       {:id 'test.make-single
                        :tag "test.make-single"
                        :default [{:type :module.directory :main 'test.trigger/root}
                                  {:type :custom :file "worker.js"}
                                  {:type :directory :main [["assets" "assets"]]}]})
        empty-config (common/make-config
                      {:id 'test.make-single-empty
                       :tag "test.make-single-empty"
                       :default [{:type :module.directory :main 'test.trigger/root}]})
        current-config (common/make-config
                        {:id 'test.make-single-current
                         :tag "test.make-single-current"
                         :default [{:type :module.directory :main 'test.trigger/root}]})
        target-triggers (atom {})
        empty-triggers (atom {})
        current-triggers (atom {})
        target-calls (atom [])
        empty-calls (atom [])]
    [(common/with:triggers [target-triggers]
       (common/triggers-set target-config #{"test.trigger"})
       (with-redefs [compile/compile
                     (fn [_ & directives]
                       (swap! target-calls conj
                              {:directives (vec directives)
                               :filter compile/*compile-filter*})
                       :built)]
         [(mapv first (build-triggered-single 'test.trigger/page))
          (mapv #(select-keys % [:directives :filter]) @target-calls)]))
     (common/with:triggers [empty-triggers]
       (common/triggers-set empty-config #{"test.trigger"})
       (with-redefs [compile/compile
                     (fn [_ & _]
                       (swap! empty-calls conj true)
                       :built)]
         [(build-triggered-single 'other.namespace)
          @empty-calls]))
     (common/with:triggers [current-triggers]
       (common/triggers-set current-config #{"test.trigger"})
       (with-redefs [env/ns-sym (constantly 'test.trigger/page)
                     compile/compile (constantly :built)]
         (build-triggered-single)))])
  => [[['test.make-single]
       [{:directives [[:default :module.directory]]
         :filter #{'test.trigger/page}}]]
      [[] []]
      [['test.make-single-current :built]]])

(fact "check watch functionality"
  (fn? watch) => true
  (fn? file-watcher-handler) => true)

(comment

  (defn add [x y]
    (+ x y))


  (add 1 2)
  "a5efc546-d6e7-4c57-8283-6dbc6c68d29d"



  (comment
    (keys (get-in @code.test.base.runtime/*registry* [(env/ns-sym)]))
    (keys (into {} (first (vals (get-in @code.test.base.runtime/*registry* [(env/ns-sym) :facts])))))
    (:path :wrap :desc :full :global :added :ns :type :source :function :column :line :id :code :refer)

    (map resolve (map :refer (vals (get-in @code.test.base.runtime/*registry* [(env/ns-sym) :facts]))))
    (#'std.make.project/makefile-parse #'std.make.project/build-all #'std.make.project/def-make-fn #'std.make.project/def.make)


    (meta #'std.make.project/makefile-parse))

  (require '[code.test.base.runtime :as rt])

  (defn vars-list
    ([]
     (vars-list (env/ns-sym)))
    ([ns]
     (let [tns  (code.project/test-ns ns)]
       (keep (comp resolve :refer) (vals (get-in @rt/*registry* [tns :facts]))))))

  (defn vars-trace
    ([]
     (vars-trace (env/ns-sym)))
    ([ns]
     (mapv (juxt identity  trace/add-base-trace) (vars-list ns))))

  (defn vars-trace-check
    ([]
     (vars-trace-check (env/ns-sym)))
    ([ns]
     (mapv trace/has-trace? (vars-list ns))))

  (defn vars-untrace
    ([]
     (vars-untrace (env/ns-sym)))
    ([ns]
     (mapv (juxt identity trace/remove-trace) (vars-list ns))))

  (comment

    (get-line)

    (defmacro get-line
      []
      (meta &form))


    (map :line (vals (rt/all-facts)))

    (defn create-form-fn
      [ns {:keys [line]}]
      (rt/find-fact ns {:line line}) #_(let [{:keys [refer]} (rt/find-fact {:line line})]
                                         refer))

    (rt/find-fact (env/ns-sym)
                  {:line 10})
    (create-form-fn (env/ns-sym)
                    {:line 10})


    (create-form)


    (defmacro create-form
      []
      `(create-form-fn (env/ns-sym) ~(meta &form)))




    (trace-install (env/ns-sym))

    (defn add [x y]
      (+ x y))

    (trace/add-base-trace #'add))

  (comment

    (meta #'add)
    (trace/add-base-trace #'add)
    (trace/get-trace #'add)
    (trace/remove-trace #'add)

    (add 1 2)))


^{:refer std.make.project/file-watcher-heal :added "4.1"}
(fact "heals a file's content and writes it back if changed"
  (let [tmp (str (java.io.File/createTempFile "test" ".clj"))
        content "(defn foo [x] x)"]
    (spit tmp content)
    (file-watcher-heal tmp 'my.ns)
    (slurp tmp))
  => string?)

^{:refer std.make.project/file-watcher-handler :added "4.1"}
(fact "file-watcher-handler is a function"
  (fn? file-watcher-handler)
  => true)

^{:refer std.make.project/watch :added "4.1"}
(fact "watch is a function that starts directory watchers"
  (fn? watch)
  => true)

^{:refer std.make.project/watch-project :added "4.1"}
(fact "watch-project is a function that watches all project paths"
  (fn? watch-project)
  => true)
