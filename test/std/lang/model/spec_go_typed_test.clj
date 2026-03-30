(ns std.lang.model.spec-go-typed-test
  (:require [clojure.string :as str]
            [std.lang.model.spec-go.typed :as go-typed])
  (:use code.test))

^{:refer std.lang.model.spec-go.typed/emit-go-type :added "4.1"}
(fact "maps xtalk primitive and container types to go"
  [(go-typed/emit-go-type {:kind :primitive :name :xt/str} nil)
   (go-typed/emit-go-type {:kind :array
                           :item {:kind :primitive :name :xt/int}} nil)
   (go-typed/emit-go-type {:kind :dict
                           :key {:kind :primitive :name :xt/str}
                           :value {:kind :primitive :name :xt/num}} nil)
   (go-typed/emit-go-type {:kind :record
                           :fields [{:name "id"
                                     :type {:kind :primitive :name :xt/str}
                                     :optional? false}]}
                          nil)
   (go-typed/emit-go-type {:kind :maybe
                           :item {:kind :named :name 'sample.user/User}} 'sample.user)]
  => ["string" "[]int" "map[any]any" "map[string]any" "*User"])

^{:refer std.lang.model.spec-go.typed/emit-analysis-declarations :added "4.1"}
(fact "emits spec/function/value declarations in go syntax"
  (go-typed/emit-analysis-declarations
   {:specs [{:ns "sample.user"
              :name "User"
              :type {:kind :record
                     :fields [{:name "id"
                               :type {:kind :primitive :name :xt/str}
                               :optional? false}]}}
             {:ns "sample.user"
              :name "UserMap"
              :type {:kind :dict
                     :key {:kind :primitive :name :xt/str}
                     :value {:kind :named :name 'sample.user/User}}}]
     :functions [{:ns "sample.user"
                  :name "find-user"
                  :inputs [{:name 'users
                            :type {:kind :named :name 'sample.user/UserMap}}
                           {:name 'id
                            :type {:kind :primitive :name :xt/str}}]
                  :output {:kind :maybe :item {:kind :named :name 'sample.user/User}}}]
     :values [{:ns "sample.user"
               :name "default-id"
               :type {:kind :primitive :name :xt/str}}]})
  => (str "type User map[string]any\n\n"
          "type UserMap map[any]any\n\n"
          "type find_user func(arg0 UserMap, arg1 string) *User\n\n"
          "var default_id string"))

(fact "can emit declarations from typed fixture namespace"
  (let [out (go-typed/emit-namespace-declarations
             'std.lang.model.spec-xtalk-typed-fixture)]
    [(str/includes? out "type User map[string]any")
     (str/includes? out "type UserMap map[any]any")
     (str/includes? out "type find_user func(arg0 UserMap, arg1 string) *User")])
  => [true true true])

(fact "does not duplicate same-name callable and value specs"
  (let [out (go-typed/emit-analysis-declarations
             {:specs [{:ns "sample.user"
                       :name "lookupUser"
                       :type {:kind :fn
                              :inputs [{:kind :named :name 'sample.user/UserMap}
                                       {:kind :named :name 'sample.user/UserId}]
                              :output {:kind :maybe
                                       :item {:kind :named :name 'sample.user/User}}}}
                      {:ns "sample.user"
                       :name "DEFAULT_PAGE_SIZE"
                       :type {:kind :primitive :name :xt/int}}]
              :functions [{:ns "sample.user"
                           :name "lookupUser"
                           :inputs [{:name 'users
                                     :type {:kind :named :name 'sample.user/UserMap}}
                                    {:name 'id
                                     :type {:kind :named :name 'sample.user/UserId}}]
                           :output {:kind :maybe
                                    :item {:kind :named :name 'sample.user/User}}}]
              :values [{:ns "sample.user"
                        :name "DEFAULT_PAGE_SIZE"
                        :type {:kind :primitive :name :xt/int}}]})]
    [(= 1 (count (re-seq #"type lookupUser func" out)))
     (= 1 (count (re-seq #"DEFAULT_PAGE_SIZE" out)))])
  => [true true])

(fact "supports strict mode for lossy type conversions"
  [(go-typed/emit-go-type {:kind :union
                           :types [{:kind :primitive :name :xt/str}
                                   {:kind :primitive :name :xt/int}]}
                          nil)
   (try
     (go-typed/emit-analysis-declarations
      {:specs [{:ns "sample.user"
                :name "LooseType"
                :type {:kind :union
                       :types [{:kind :primitive :name :xt/str}
                               {:kind :primitive :name :xt/int}]}}]
       :functions []
       :values []}
      {:strict? true})
     false
     (catch Exception _
       true))]
  => ["any" true])
