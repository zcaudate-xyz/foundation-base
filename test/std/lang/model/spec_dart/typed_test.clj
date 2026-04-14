(ns std.lang.model.spec-dart.typed-test
  (:use code.test)
  (:require [clojure.string :as str]
            [std.lang.model.spec-dart.typed :refer :all]))

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
                   :value sample-user-type}}]
   :functions [{:ns "sample.user"
                :name "find-user"
                :inputs [{:name 'users
                          :type {:kind :named :name 'sample.user/UserMap}}
                         {:name 'id
                          :type {:kind :primitive :name :xt/str}}]
                :output {:kind :maybe :item sample-user-type}}]
   :values [{:ns "sample.user"
             :name "default-id"
             :type {:kind :primitive :name :xt/str}}]})

^{:refer std.lang.model.spec-dart.typed/sanitize-ident :added "4.1"}
(fact "sanitizes dart identifiers"
  [(sanitize-ident "hello-world")
   (sanitize-ident "123hello")]
  => ["hello_world" "_123hello"])

^{:refer std.lang.model.spec-dart.typed/lower-camel :added "4.1"}
(fact "converts identifiers to lower camel case"
  [(lower-camel "display-name")
   (lower-camel "UserMap")]
  => ["displayName" "userMap"])

^{:refer std.lang.model.spec-dart.typed/upper-camel :added "4.1"}
(fact "converts identifiers to upper camel case"
  [(upper-camel "display-name")
   (upper-camel "userMap")]
  => ["DisplayName" "UserMap"])

^{:refer std.lang.model.spec-dart.typed/named-dart-ident :added "4.1"}
(fact "creates named dart identifiers"
  [(named-dart-ident 'sample.user/User sample-current-ns)
   (named-dart-ident 'other.ns/User sample-current-ns)
   (named-dart-ident "user-map" sample-current-ns)]
  => ["User" "OtherNsUser" "UserMap"])

^{:refer std.lang.model.spec-dart.typed/emit-dart-type :added "4.1"}
(fact "emits dart types"
  [(emit-dart-type {:kind :primitive :name :xt/str} nil)
   (emit-dart-type {:kind :array
                    :item {:kind :primitive :name :xt/int}} nil)
   (emit-dart-type {:kind :dict
                    :key {:kind :primitive :name :xt/str}
                    :value {:kind :primitive :name :xt/num}} nil)
   (emit-dart-type {:kind :maybe
                    :item sample-user-type} sample-current-ns)]
  => ["String" "List<int>" "Map<String, double>" "User?"])

^{:refer std.lang.model.spec-dart.typed/lossy-dart-type :added "4.1"}
(fact "handles lossy dart types"
  [(lossy-dart-type :union)
   (binding [*emit-options* {:strict? true
                             :lossy-fallback "Object?"}]
     (try
       (lossy-dart-type :union)
       false
       (catch Exception _
         true)))]
  => ["Object?" true])

^{:refer std.lang.model.spec-dart.typed/maybe-unwrapped :added "4.1"}
(fact "unwraps maybe types for optional fields"
  [(maybe-unwrapped {:kind :maybe
                     :item {:kind :primitive :name :xt/str}}
                    true)
   (maybe-unwrapped {:kind :maybe
                     :item {:kind :primitive :name :xt/str}}
                    false)]
  => [{:kind :primitive :name :xt/str}
      {:kind :maybe :item {:kind :primitive :name :xt/str}}])

^{:refer std.lang.model.spec-dart.typed/emit-class-field :added "4.1"}
(fact "emits class fields"
  [(emit-class-field {:name "id"
                      :type {:kind :primitive :name :xt/str}
                      :optional? false}
                     sample-current-ns)
   (emit-class-field {:name "display-name"
                      :type {:kind :maybe
                             :item {:kind :primitive :name :xt/str}}
                      :optional? true}
                     sample-current-ns)]
  => ["  final String id;"
      "  final String? displayName;"])

^{:refer std.lang.model.spec-dart.typed/emit-class-constructor :added "4.1"}
(fact "emits class constructors"
  (emit-class-constructor "User" (:fields sample-record-type))
  => "  const User({required this.id, this.displayName});")

^{:refer std.lang.model.spec-dart.typed/emit-record-class :added "4.1"}
(fact "emits record classes"
  (emit-record-class "User" sample-record-type sample-current-ns)
  => (str "class User {\n"
          "  final String id;\n"
          "  final String? displayName;\n"
          "  const User({required this.id, this.displayName});\n"
          "}"))

^{:refer std.lang.model.spec-dart.typed/emit-spec-declaration :added "4.1"}
(fact "emits spec declarations"
  [(emit-spec-declaration {:ns "sample.user"
                           :name "User"
                           :type sample-record-type})
   (emit-spec-declaration {:ns "sample.user"
                           :name "UserMap"
                           :type {:kind :dict
                                  :key {:kind :primitive :name :xt/str}
                                  :value sample-user-type}})]
  => [(str "class User {\n"
           "  final String id;\n"
           "  final String? displayName;\n"
           "  const User({required this.id, this.displayName});\n"
           "}")
      "typedef UserMap = Map<String, User>;"])

^{:refer std.lang.model.spec-dart.typed/emit-function-declaration :added "4.1"}
(fact "emits function declarations"
  (emit-function-declaration {:ns "sample.user"
                              :name "find-user"
                              :inputs [{:name 'users
                                        :type {:kind :named :name 'sample.user/UserMap}}
                                       {:name 'id
                                        :type {:kind :primitive :name :xt/str}}]
                              :output {:kind :maybe :item sample-user-type}})
  => "typedef FindUser = User? Function(UserMap arg0, String arg1);")

^{:refer std.lang.model.spec-dart.typed/emit-value-declaration :added "4.1"}
(fact "emits value declarations"
  (emit-value-declaration {:ns "sample.user"
                           :name "default-id"
                           :type {:kind :primitive :name :xt/str}})
  => "late final String defaultId;")

^{:refer std.lang.model.spec-dart.typed/emit-analysis-declarations :added "4.1"}
(fact "emits analysis declarations"
  (emit-analysis-declarations sample-analysis)
  => (str "class User {\n"
          "  final String id;\n"
          "  final String? displayName;\n"
          "  const User({required this.id, this.displayName});\n"
          "}\n\n"
          "typedef UserMap = Map<String, User>;\n\n"
          "typedef FindUser = User? Function(UserMap arg0, String arg1);\n\n"
          "late final String defaultId;"))

^{:refer std.lang.model.spec-dart.typed/emit-namespace-declarations :added "4.1"}
(fact "emits namespace declarations"
  (let [out (emit-namespace-declarations 'std.lang.model.spec-xtalk-typed-fixture)]
    [(str/includes? out "class User")
     (str/includes? out "typedef UserMap = Map<String, User>")
     (str/includes? out "typedef FindUser = User? Function")])
  => [true true true])
