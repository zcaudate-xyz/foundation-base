(ns rt.postgres.base.compile.ts-schema-test
  (:require [rt.postgres.base.compile.ts-schema :as compile.ts]
            [rt.postgres.base.typed.typed-common :as types])
  (:use code.test))

^{:refer rt.postgres.base.compile.ts-schema/shape->ts-interface :added "0.1"}
(fact "shape->ts-interface generates TypeScript interface"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}
                                       :name {:type :text :nullable? true}}
                                      :User)
        result (compile.ts/shape->ts-interface shape "IUser")]
    (clojure.string/includes? result "interface IUser") => true
    (clojure.string/includes? result "id: string") => true
    (clojure.string/includes? result "name?: string") => true))

^{:refer rt.postgres.base.compile.ts-schema/shape->ts-interface :added "0.1"}
(fact "shape->ts-interface handles various types"
  (let [shape (types/make-jsonb-shape {:active {:type :boolean}
                                       :count {:type :integer}
                                       :amount {:type :numeric}
                                       :data {:type :jsonb}}
                                      :Test)
        result (compile.ts/shape->ts-interface shape "ITest")]
    (clojure.string/includes? result "active: boolean") => true
    (clojure.string/includes? result "count: number") => true
    (clojure.string/includes? result "amount: number") => true
    (clojure.string/includes? result "data: Record<string, any>") => true))

^{:refer rt.postgres.base.compile.ts-schema/shape->ts-interface :added "0.1"}
(fact "shape->ts-interface uses snake_case keys"
  (let [shape (types/make-jsonb-shape {:time-created {:type :bigint :nullable? true}
                                       :op-created {:type :uuid :nullable? true}}
                                      :User)
        result (compile.ts/shape->ts-interface shape "IUser")]
    (clojure.string/includes? result "time_created") => true
    (clojure.string/includes? result "op_created") => true
    (clojure.string/includes? result "time-created") => false))

^{:refer rt.postgres.base.compile.ts-schema/shape->ts-interface :added "0.1"}
(fact "shape->ts-interface preserves raw string keys"
  (let [shape (types/make-jsonb-shape {"db/sync" {:type :jsonb
                                                  :shape (types/make-jsonb-shape {"UserProfile" {:type :array
                                                                                                 :items {:type :jsonb}}}
                                                                                 nil :high false)}}
                                      nil :high false)
        result (compile.ts/shape->ts-interface shape "IUserEvent")]
    (clojure.string/includes? result "\"db/sync\"") => true
    (clojure.string/includes? result "UserProfile") => true
    (clojure.string/includes? result "db_sync") => false
    (clojure.string/includes? result "user_profile") => false))

^{:refer rt.postgres.base.compile.ts-schema/generate-ts-schema :added "0.1"}
(fact "generate-ts-schema creates TypeScript interfaces for all types"
  (let [ts-code (compile.ts/generate-ts-schema)]
    (string? ts-code) => true))


^{:refer rt.postgres.base.compile.ts-schema/type->ts :added "4.1"}
(fact "converts type descriptor to TypeScript type string"
  (compile.ts/type->ts {:type :uuid}) => "string"

  (compile.ts/type->ts {:type :text}) => "string"

  (compile.ts/type->ts {:type :integer}) => "number"

  (compile.ts/type->ts {:type :boolean}) => "boolean"

  (compile.ts/type->ts {:type :numeric}) => "number"

  (compile.ts/type->ts {:type :jsonb}) => "Record<string, any>"

  (compile.ts/type->ts {:is-ref? true}) => "string"

  (compile.ts/type->ts {:type :array :items {:type :text}}) => "string[]"

  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} :Test)]
    (compile.ts/type->ts {:type :jsonb :shape shape}) => string?))

^{:refer rt.postgres.base.compile.ts-schema/field->ts :added "4.1"}
(fact "converts field to TypeScript property declaration"
  (compile.ts/field->ts [:id {:type :uuid :nullable? false}]) => "  id: string;"

  (compile.ts/field->ts [:name {:type :text :nullable? true}]) => "  name?: string;"

  (compile.ts/field->ts [:count {:type :integer :nullable? false}]) => "  count: number;"

  (compile.ts/field->ts [:active {:type :boolean :nullable? true}]) => "  active?: boolean;")