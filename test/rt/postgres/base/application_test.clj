(ns rt.postgres.base.application-test
  (:require [rt.postgres.base.application :refer :all]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.postgres.base.application/app-modules :added "4.0"}
(fact "checks for modules related to a given application"

  (with-redefs [l/get-book (fn [& _] {:modules {:m1 {:static {:application ["app"]}} :m2 {}}})]
    (app-modules "app"))
  => '({:static {:application ["app"]}}))

^{:refer rt.postgres.base.application/app-create-raw :added "4.0"}
(fact "creates a schema from tables and links"

  (app-create-raw {} {})
  => map?
  (app-create-raw {} {} {:tables {'User :table} :enums {} :functions {}})
  => (contains {:typed {:tables {'User :table} :enums {} :functions {}}}))

^{:refer rt.postgres.base.application/app-create :added "4.0"}
(fact "makes the app graph schema"

  (with-redefs [app-modules (fn [_] [{:id 'test.module
                                      :code {:entry {:op 'deftype
                                                     :static/schema-seed []}}}])]
    (with-redefs-fn {#'rt.postgres.base.application/app-create-typed
                     (fn [_ _] {:tables {'User :table}
                                :enums {'test/Status :enum}
                                :functions {'test/create-user :fn}})}
      #(-> (app-create "test.postgres")
           :typed)))
  => nil)

^{:refer rt.postgres.base.application/app-clear :added "4.0"}
(fact "clears the entry for an app"

  (with-redefs [*applications* (atom {"test.postgres" {}})]
    (app-clear "test.postgres")
    => {}
    @*applications* => {}))

^{:refer rt.postgres.base.application/app-rebuild :added "4.0"}
(fact "rebuilds the app schema"

  (with-redefs [*applications* (atom {"test.postgres" {:tables {} :pointers {}}})
                app-create-raw (fn [& _] {:rebuilt true})]
    (with-redefs-fn {#'rt.postgres.base.application/app-create-typed
                     (fn [& _] {:typed true})}
      #(app-rebuild "test.postgres")))
  => {:rebuilt true})

^{:refer rt.postgres.base.application/app-rebuild-tables :added "4.0"}
(fact "initiate rebuild of app schema"

  (with-redefs [*applications* (atom {})
                app-create (fn [& _] {:created true})]
    (app-rebuild-tables "test")
    => {:created true}))

^{:refer rt.postgres.base.application/app-list :added "4.0"}
(fact "rebuilds the app schema"

  (with-redefs [*applications* (atom {"test.postgres" {}})]
    (app-list))
  => '("test.postgres"))

^{:refer rt.postgres.base.application/app :added "4.0"}
(fact "gets an app"

  (with-redefs [*applications* (atom {"test.postgres" {:data 1}})]
    (app "test.postgres")
    => {:data 1}))

^{:refer rt.postgres.base.application/app-schema :added "4.0"}
(fact "gets the app schema"

  (with-redefs [*applications* (atom {"test.postgres" {:schema :schema}})]
    (app-schema "test.postgres")
    => :schema))

^{:refer rt.postgres.base.application/app-typed :added "4.1"}
(fact "gets the app typed payload"

  (with-redefs [*applications* (atom {"test.postgres" {:typed :typed}})]
    (app-typed "test.postgres")
    => :typed)

  (with-redefs [*applications* (atom {"test.postgres" {:tables {}
                                                       :pointers {}}})
                app-modules (fn [_] [])]
    (with-redefs-fn {#'rt.postgres.base.application/app-create-typed
                     (fn [_ _] {:typed true})}
      #(do (app-typed "test.postgres")
           (get-in @*applications* ["test.postgres" :typed]))))
  => {:typed true})


^{:refer rt.postgres.base.application/application-string :added "4.1"}
(fact "generates a string representation of an application"
  (application-string {:tables {'users/User {} 'posts/Post {}}})
  => "#pg.app [2]\nposts/Post users/User\n"

  (application-string {:tables {}})
  => "#pg.app [0]\n\n")

^{:refer rt.postgres.base.application/app-list-entries :added "4.1"}
(fact "returns all table entry keys from an application"
  (set (app-list-entries {:tables {'users/User {} 'posts/Post {}}}))
  => '#{users/User posts/Post}

  (app-list-entries {:tables {}})
  => nil)

^{:refer rt.postgres.base.application/app-get-entry :added "4.1"}
(fact "retrieves a specific entry from the app schema tree"
  (let [app {:schema {:tree {"users/User" {:id 1} "posts/Post" {:id 2}}}}]
    (app-get-entry app "users/User"))
  => {:id 1}

  (app-get-entry {:schema {:tree {}}} "nonexistent")
  => nil)

^{:refer rt.postgres.base.application/app-get-deps :added "4.1"}
(fact "extracts dependency namespaces from a table entry"
  (let [app {:tables {'users/User [{:ref {:ns 'rt.postgres.base.grammar.types}}
                                   {:ref {:ns 'rt.postgres.base.grammar.shapes}}]}}]
    (app-get-deps app 'users/User))
  => '#{rt.postgres.base.grammar.shapes}

  (app-get-deps {:tables {}} 'nonexistent)
  => #{})

^{:refer rt.postgres.base.application/module-typed :added "4.1"}
(fact "analyzes modules and returns merged typed information"
  (let [modules [{:id 'test.module
                  :code {:ns 'test.module
                         :forms [{:op 'deftype
                                  :static/schema-seed []
                                  :ns 'test.module
                                  :name 'User
                                  :vec []}]}}]]
    (module-typed modules))
  => (contains {:tables map? :enums map? :functions map?}))

^{:refer rt.postgres.base.application/app-create-typed :added "4.1"}
(fact "creates typed information from tables and modules"
  (let [tables {'users/User [{:type :text}]}
        modules [{:id 'test.module
                  :code {:ns 'test.module
                         :forms [{:op 'deftype
                                  :static/schema-seed []
                                  :ns 'test.module
                                  :name 'Post
                                  :vec []}]}}]]
    (app-create-typed tables modules))
  => (contains {:tables map? :enums map? :functions map?}))