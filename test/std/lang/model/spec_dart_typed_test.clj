(ns std.lang.model.spec-dart-typed-test
  (:require [clojure.string :as str]
            [std.lang.model.spec-dart.typed :as dart-typed])
  (:use code.test))

^{:refer std.lang.model.spec-dart.typed/emit-dart-type :added "4.1"}
(fact "maps xtalk primitive and container types to dart"
  [(dart-typed/emit-dart-type {:kind :primitive :name :xt/str} nil)
   (dart-typed/emit-dart-type {:kind :array
                               :item {:kind :primitive :name :xt/int}} nil)
   (dart-typed/emit-dart-type {:kind :dict
                               :key {:kind :primitive :name :xt/str}
                               :value {:kind :primitive :name :xt/num}} nil)
   (dart-typed/emit-dart-type {:kind :maybe
                               :item {:kind :named :name 'sample.user/User}} 'sample.user)]
  => ["String" "List<int>" "Map<String, double>" "User?"])

^{:refer std.lang.model.spec-dart.typed/emit-analysis-declarations :added "4.1"}
(fact "emits spec/function/value declarations in dart syntax"
  (dart-typed/emit-analysis-declarations
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
  => (str "class User {\n"
          "  final String id;\n"
          "  const User({required this.id});\n"
          "}\n\n"
          "typedef UserMap = Map<String, User>;\n\n"
          "typedef FindUser = User? Function(UserMap arg0, String arg1);\n\n"
          "late final String defaultId;"))

(fact "can emit declarations from typed fixture namespace"
  (let [out (dart-typed/emit-namespace-declarations
             'std.lang.model.spec-xtalk-typed-fixture)]
    [(str/includes? out "class User")
     (str/includes? out "typedef UserMap = Map<String, User>")
     (str/includes? out "typedef FindUser = User? Function")])
  => [true true true])

(fact "supports strict mode for lossy type conversions"
  [(dart-typed/emit-dart-type {:kind :union
                               :types [{:kind :primitive :name :xt/str}
                                       {:kind :primitive :name :xt/int}]}
                              nil)
   (try
     (dart-typed/emit-analysis-declarations
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
  => ["Object?" true])
