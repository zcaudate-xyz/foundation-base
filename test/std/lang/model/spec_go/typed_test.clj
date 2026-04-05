(ns std.lang.model.spec-go.typed-test
  (:use code.test)
  (:require [clojure.string :as str]
            [std.lang.model.spec-go.typed :refer :all]))

(def sample-current-ns 'sample.user)

(def sample-user-type
  {:kind :named :name 'sample.user/User})

(def sample-record-type
  {:kind :record
   :fields [{:name "id"
             :type {:kind :primitive :name :xt/str}
             :optional? false}
            {:name "display-name"
             :type {:kind :maybe
                    :item {:kind :primitive :name :xt/str}}
             :optional? true}]})

(def sample-analysis
  {:specs [{:ns "sample.user"
            :name "User"
            :type sample-record-type}
           {:ns "sample.user"
            :name "UserMap"
            :type {:kind :dict
                   :key {:kind :primitive :name :xt/str}
                   :value sample-user-type}}
           {:ns "sample.user"
            :name "lookupUser"
            :type {:kind :fn
                   :inputs [{:kind :named :name 'sample.user/UserMap}
                            {:kind :primitive :name :xt/str}]
                   :output {:kind :maybe :item sample-user-type}}}
           {:ns "sample.user"
            :name "DEFAULT_PAGE_SIZE"
            :type {:kind :primitive :name :xt/int}}]
   :functions [{:ns "sample.user"
                :name "lookupUser"
                :inputs [{:name 'users
                          :type {:kind :named :name 'sample.user/UserMap}}
                         {:name 'id
                          :type {:kind :primitive :name :xt/str}}]
                :output {:kind :maybe :item sample-user-type}}]
   :values [{:ns "sample.user"
             :name "DEFAULT_PAGE_SIZE"
             :type {:kind :primitive :name :xt/int}}
            {:ns "sample.user"
             :name "default-id"
             :type {:kind :primitive :name :xt/str}}]})

^{:refer std.lang.model.spec-go.typed/sanitize-ident :added "4.1"}
(fact "sanitizes go identifiers"
  [(sanitize-ident "hello-world")
   (sanitize-ident "123hello")]
  => ["hello_world" "_123hello"])

^{:refer std.lang.model.spec-go.typed/capitalize-ident :added "4.1"}
(fact "capitalizes go identifiers"
  [(capitalize-ident "display-name")
   (capitalize-ident "")]
  => ["Display_name" "X"])

^{:refer std.lang.model.spec-go.typed/named-go-ident :added "4.1"}
(fact "creates named go identifiers"
  [(named-go-ident 'sample.user/User sample-current-ns)
   (named-go-ident 'other.ns/User sample-current-ns)
   (named-go-ident "user-map" sample-current-ns)]
  => ["User" "other_ns_User" "user_map"])

^{:refer std.lang.model.spec-go.typed/maybe-go-type :added "4.1"}
(fact "converts maybe types to go pointer types"
  [(maybe-go-type {:kind :named :name 'sample.user/User} sample-current-ns)
   (maybe-go-type {:kind :primitive :name :xt/str} sample-current-ns)]
  => ["*User" "any"])

^{:refer std.lang.model.spec-go.typed/lossy-go-type :added "4.1"}
(fact "handles lossy go types"
  [(lossy-go-type :union)
   (binding [*emit-options* {:strict? true
                             :lossy-fallback "any"}]
     (try
       (lossy-go-type :union)
       false
       (catch Exception _
         true)))]
  => ["any" true])

^{:refer std.lang.model.spec-go.typed/emit-go-type :added "4.1"}
(fact "emits go types"
  [(emit-go-type {:kind :primitive :name :xt/str} nil)
   (emit-go-type {:kind :array
                  :item {:kind :primitive :name :xt/int}} nil)
   (emit-go-type {:kind :record
                  :fields [{:name "id"
                            :type {:kind :primitive :name :xt/str}
                            :optional? false}]}
                 nil)
   (emit-go-type {:kind :maybe :item sample-user-type} sample-current-ns)]
  => ["string" "[]int" "map[string]any" "*User"])

^{:refer std.lang.model.spec-go.typed/emit-struct-field :added "4.1"}
(fact "emits struct fields"
  [(emit-struct-field {:name "id"
                       :type {:kind :primitive :name :xt/str}
                       :optional? false}
                      sample-current-ns)
   (emit-struct-field {:name "display-name"
                       :type {:kind :maybe
                              :item {:kind :primitive :name :xt/str}}
                       :optional? true}
                      sample-current-ns)]
  => ["  Id string `json:\"id\"`"
      "  Display_name string `json:\"display-name\"`"])

^{:refer std.lang.model.spec-go.typed/emit-struct-type :added "4.1"}
(fact "emits struct types"
  (emit-struct-type sample-record-type sample-current-ns)
  => (str "struct {\n"
          "  Id string `json:\"id\"`\n"
          "  Display_name string `json:\"display-name\"`\n"
          "}"))

^{:refer std.lang.model.spec-go.typed/emit-spec-declaration :added "4.1"}
(fact "emits spec declarations"
  [(emit-spec-declaration {:ns "sample.user"
                           :name "User"
                           :type sample-record-type})
   (emit-spec-declaration {:ns "sample.user"
                           :name "UserMap"
                           :type {:kind :dict
                                  :key {:kind :primitive :name :xt/str}
                                  :value sample-user-type}})]
  => ["type User map[string]any"
      "type UserMap map[any]any"])

^{:refer std.lang.model.spec-go.typed/emit-function-declaration :added "4.1"}
(fact "emits function declarations"
  (emit-function-declaration {:ns "sample.user"
                              :name "lookupUser"
                              :inputs [{:name 'users
                                        :type {:kind :named :name 'sample.user/UserMap}}
                                       {:name 'id
                                        :type {:kind :primitive :name :xt/str}}]
                              :output {:kind :maybe :item sample-user-type}})
  => "type lookupUser func(arg0 UserMap, arg1 string) *User")

^{:refer std.lang.model.spec-go.typed/emit-value-declaration :added "4.1"}
(fact "emits value declarations"
  (emit-value-declaration {:ns "sample.user"
                           :name "default-id"
                           :type {:kind :primitive :name :xt/str}})
  => "var default_id string")

^{:refer std.lang.model.spec-go.typed/emit-analysis-declarations :added "4.1"}
(fact "emits analysis declarations"
  (emit-analysis-declarations sample-analysis)
  => (str "type User map[string]any\n\n"
          "type UserMap map[any]any\n\n"
          "type lookupUser func(arg0 UserMap, arg1 string) *User\n\n"
          "var DEFAULT_PAGE_SIZE int\n\n"
          "var default_id string"))

^{:refer std.lang.model.spec-go.typed/emit-namespace-declarations :added "4.1"}
(fact "emits namespace declarations"
  (let [out (emit-namespace-declarations 'std.lang.model.spec-xtalk-typed-fixture)]
    [(str/includes? out "type User map[string]any")
     (str/includes? out "type UserMap map[any]any")
     (str/includes? out "type find_user func(arg0 UserMap, arg1 string) *User")])
  => [true true true])

^{:refer std.lang.model.spec-go.typed/emitted-specs :added "4.1"}
(fact "filters specs shadowed by callable and value declarations"
  (mapv :name (emitted-specs sample-analysis))
  => ["User" "UserMap"])
