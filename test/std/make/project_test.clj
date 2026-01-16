(ns std.make.project-test
  (:use code.test)
  (:require [std.make.project :refer :all]
            [std.lib :as h]))

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
  ^:hidden
  
  (require '[std.make.common :as common])
  (def.make TEST_MAKE
    {:tag "test.make"
     :default [{:type :raw :file "hello.txt" :main "hello"}]
     :triggers #{"test.trigger"}})
  
  (common/triggers-get TEST_MAKE)
  => #{"test.trigger"})

^{:refer std.make.project/build-triggered :added "4.0"}
(fact "builds for a triggering namespace")

(fact "check watch functionality"
  (fn? watch) => true
  (fn? file-watcher-handler) => true)

(comment
  
  (defn add [x y]
    (+ x y))


  (add 1 2)
  "a5efc546-d6e7-4c57-8283-6dbc6c68d29d"



  (comment
    (keys (get-in @code.test.base.runtime/*registry* [(h/ns-sym)]))
    (keys (into {} (first (vals (get-in @code.test.base.runtime/*registry* [(h/ns-sym) :facts])))))
    (:path :wrap :desc :full :global :added :ns :type :source :function :column :line :id :code :refer)

    (map resolve (map :refer (vals (get-in @code.test.base.runtime/*registry* [(h/ns-sym) :facts]))))
    (#'std.make.project/makefile-parse #'std.make.project/build-all #'std.make.project/def-make-fn #'std.make.project/def.make)
    
    
    (meta #'std.make.project/makefile-parse))

  (require '[code.test.base.runtime :as rt])

  (defn vars-list
    ([]
     (vars-list (h/ns-sym)))
    ([ns]
     (let [tns  (code.project/test-ns ns)]
       (keep (comp resolve :refer) (vals (get-in @rt/*registry* [tns :facts]))))))

  (defn vars-trace
    ([]
     (vars-trace (h/ns-sym)))
    ([ns]
     (mapv (juxt identity  h/add-trace) (vars-list ns))))

  (defn vars-trace-check
    ([]
     (vars-trace-check (h/ns-sym)))
    ([ns]
     (mapv h/has-trace? (vars-list ns))))

  (defn vars-untrace
    ([]
     (vars-untrace (h/ns-sym)))
    ([ns]
     (mapv (juxt identity h/remove-trace) (vars-list ns))))

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
    
    (rt/find-fact (h/ns-sym)
                  {:line 10})
    (create-form-fn (h/ns-sym)
                    {:line 10})
    
    
    (create-form)
    
    
    (defmacro create-form
      []
      `(create-form-fn (h/ns-sym) ~(meta &form)))
    
    


    (trace-install (h/ns-sym))

    (defn add [x y]
      (+ x y))

    (h/add-trace #'add))

  (comment
    
    (meta #'add)
    (h/add-trace #'add)
    (h/get-trace #'add)
    (h/remove-trace #'add)
    
    (add 1 2)))
