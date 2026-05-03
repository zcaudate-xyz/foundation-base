(ns hara.rt.postgres.base.grammar.form-deftype-hydrate-test
  (:require [hara.rt.postgres.base.application :as app]
            [hara.rt.postgres.base.grammar.form-deftype-hydrate :refer :all])
  (:use code.test))

(defn with-demo-links
  [f]
  (with-redefs [resolve (fn [sym]
                          (cond
                            (= sym 'demo/Task)
                            (atom {:id 'Task
                                   :module 'demo
                                   :lang :postgres
                                   :section :code
                                   :op-key :deftype
                                   :static/dbtype :table})

                            (= sym 'demo/Status)
                            (atom {:id 'Status
                                   :module 'demo
                                   :lang :postgres
                                   :section :code
                                   :op-key :defenum
                                   :static/dbtype :enum})

                            (= sym 'clojure.core/inc)
                            #'clojure.core/inc))
                hara.lang.base.library-snapshot/get-book
                (fn [_ _]
                  {:modules {'demo {:code {'Task {:id 'Task
                                                  :module 'demo
                                                  :lang :postgres
                                                  :section :code
                                                  :static/dbtype :table}
                                         'Status {:id 'Status
                                                  :module 'demo
                                                  :lang :postgres
                                                  :section :code
                                                  :static/dbtype :enum}}}}})
                hara.lang.base.book/get-base-entry
                (fn [book module id section]
                  (get-in book [:modules module section id]))]
    (f)))

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-check-link :added "4.1"}
(fact "pg-deftype-hydrate-check-link validates snapshot links"
  (with-demo-links
    #(pg-deftype-hydrate-check-link {}
                                    nil
                                    {:module 'demo :id 'Task :section :code}
                                    :table))
  => true

  (with-redefs [resolve (fn [sym]
                          (when (= sym 'demo/Task)
                            (atom {:id 'Task
                                   :module 'demo
                                   :lang :postgres
                                   :section :code
                                   :static/dbtype :table})))
                 hara.lang.base.library-snapshot/get-book (fn [_ _] {:modules {}})
                 hara.lang.base.book/get-base-entry (constantly nil)]
    (pg-deftype-hydrate-check-link {}
                                    nil
                                    {:module 'demo :id 'Task :section :code}
                                    :table))
  => true)

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-link :added "4.1"}
(fact "pg-deftype-hydrate-link resolves self and external links"
  (with-demo-links
    #(pg-deftype-hydrate-link 'Task {:id 'demo} {:ns '-/Task}))
  => [{:section :code
       :lang :postgres
        :module 'demo
        :id 'Task}
      false]

  (with-demo-links
    #(pg-deftype-hydrate-link 'Task {:id 'demo} {:ns '-/Status}))
  => [{:section :code
       :lang :postgres
       :module 'demo
       :id 'Status}
      false]
 
  (with-demo-links
    #(pg-deftype-hydrate-link 'Task {:id 'demo} {:ns 'demo/Task}))
  => [{:id 'Task
        :module 'demo
       :lang :postgres
       :section :code}
      true])

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-process-sql :added "4.1"}
(fact "pg-deftype-hydrate-process-sql resolves process vars"
  (with-demo-links
    #(pg-deftype-hydrate-process-sql {:process ['clojure.core/inc]}
                                     :id
                                     {:sql {}}
                                     nil))
  => {:process ['clojure.core/inc]})

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-process-foreign :added "4.1"}
(fact "pg-deftype-hydrate-process-foreign annotates foreign links"
  (with-demo-links
    #(pg-deftype-hydrate-process-foreign
      {:task {:ns 'demo/Task}}
      (fn [_] [{:module 'demo :id 'Task :section :code} true])
      {}
      {:id 'demo}))
  => {:task {:ns :Task
             :link {:module 'demo
                    :id 'Task
                    :section :code}}})

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-process-ref :added "4.1"}
(fact "pg-deftype-hydrate-process-ref supports vector and linked refs"
  (pg-deftype-hydrate-process-ref
   :task
    {:ref ["public" "Task" :uuid {:label "Task"}]}
    (fn [_] nil)
    {}
    nil)
  => [:task
      {:type :ref
       :required true
       :ref {:ns "public.Task"
             :current {:id "Task"
                       :schema "public"
                       :type :uuid}
             :label "Task"}
       :scope :-/ref}]

  (with-demo-links
    #(pg-deftype-hydrate-process-ref
      :task
      {:type :ref :ref {:ns 'demo/Task}}
      (fn [_] [{:module 'demo :id 'Task :section :code} true])
      {}
      {:id 'demo}))
  => [:task
      {:type :ref
       :ref {:ns :Task
             :link {:module 'demo
                    :id 'Task
                    :section :code}}}])

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-process-enum :added "4.1"}
(fact "pg-deftype-hydrate-process-enum resolves enum vars"
  (with-demo-links
    #(pg-deftype-hydrate-process-enum
      :status
      {:type :enum :enum {:ns 'demo/Status}}
      (fn [_] [{:module 'demo :id 'Status :section :code} true])
      {}
      {:id 'demo}))
  => [:status
      {:type :enum
         :enum {:ns 'demo/Status}}]

  (with-demo-links
    #(pg-deftype-hydrate-process-enum
      :status
      {:type :enum :enum {:ns '-/Status}}
      (fn [_] [{:module 'demo :id 'Status :section :code} false])
      {}
      {:id 'demo}))
  => [:status
      {:type :enum
        :enum {:ns 'demo/Status}}])

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-attr :added "4.1"}
(fact "pg-deftype-hydrate-attr delegates enum attrs to the enum processor"
  (with-demo-links
    #(pg-deftype-hydrate-attr
      :status
      {:type :enum :enum {:ns 'demo/Status}}
      {:snapshot {}
       :module {:id 'demo}
        :resolve-link-fn (fn [_] [{:module 'demo :id 'Status :section :code} true])
         :capture (volatile! [])}))
  => [:status
      {:type :enum
        :enum {:ns 'demo/Status}}])

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-spec :added "4.1"}
(fact "pg-deftype-hydrate-spec hydrates each attribute in order"
  (with-demo-links
    #(pg-deftype-hydrate-spec
      [:id {:type :uuid :primary true}
       :task {:type :ref :ref {:ns 'demo/Task}}
       :status {:type :enum :enum {:ns 'demo/Status}}]
      {:resolve-link-fn (fn [ref]
                          (if (= 'demo/Task (get-in ref [:ns]))
                             [{:module 'demo :id 'Task :section :code} true]
                             [{:module 'demo :id 'Status :section :code} true]))
        :snapshot {}
        :module {:id 'demo}
        :capture (volatile! [])}))
  => [:id {:type :uuid :primary true}
      :task {:type :ref
             :ref {:ns :Task
                   :link {:module 'demo
                          :id 'Task
                          :section :code}}}
      :status {:type :enum
               :enum {:ns 'demo/Status}}])

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate :added "4.1"}
(fact "pg-deftype-hydrate adds schema metadata to the form"
  (with-demo-links
    #(with-redefs [hara.rt.postgres.base.grammar.common/pg-hydrate-module-static
                   (fn [_] {:static/application ["demo"]})
                   std.lib.schema/schema
                   (fn [x] {:vec x})]
       (pg-deftype-hydrate '(deftype.pg Demo [:id {:type :uuid :primary true}] {})
                           nil
                           {:module {:id 'demo}
                            :snapshot {}})))
  => [#:static{:application ["demo"]
               :schema-seed {:vec [:Demo [:id {:type :uuid :primary true}]]}
               :schema-primary {:type :uuid
                                :id :id}}
      '(deftype.pg Demo [:id {:type :uuid :primary true}] {})])

^{:refer hara.rt.postgres.base.grammar.form-deftype-hydrate/pg-deftype-hydrate-hook :added "4.1"}
(fact "pg-deftype-hydrate-hook updates the application tables and pointers"
  (let [calls (atom [])]
    (with-redefs [app/*applications* (atom {})
                  app/app-rebuild (fn [name] (swap! calls conj name))]
      (pg-deftype-hydrate-hook {:id 'Task
                                :module 'demo
                                :lang :postgres
                                :section :code
                                :static/schema-seed {:vec [:Task [:id {:type :uuid}]]}
                                :static/application ["demo"]})
      (get-in @app/*applications* ["demo" :tables 'Task])))
  => [:id {:type :uuid}]

  (let [calls (atom [])]
    (with-redefs [app/*applications* (atom {})
                  app/app-rebuild (fn [name] (swap! calls conj name))]
      (pg-deftype-hydrate-hook {:id 'Task
                                :module 'demo
                                :lang :postgres
                                :section :code
                                :static/schema-seed {:vec [:Task [:id {:type :uuid}]]}
                                :static/application ["demo"]})
      @calls))
  => ["demo"])
