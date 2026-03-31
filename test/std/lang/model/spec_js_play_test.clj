(ns std.lang.model.spec-js-play-test
  (:use code.test)
  (:require [clojure.string :as str]
             [std.lang.model.spec-xtalk.mixer :as mixer]
             [std.lang.model.spec-js.ts :as ts]))

(def +typescript-model-fixture+
  "test/std/lang/model/typescript_model_fixture.clj")

(fact "emits TypeScript declarations for the playground model"
  (let [analysis (mixer/mix-file +typescript-model-fixture+)
        out (ts/emit-analysis-declarations analysis)]
    [(str/includes? out "export type UserId = string;")
     (str/includes? out "export type LookupKey = UserId | number;")
     (str/includes? out "export type UserRow = [UserId, string];")
     (str/includes? out "export interface User")
     (str/includes? out "display_name?: string;")
     (str/includes? out "roles: Array<string>;")
     (str/includes? out "meta: Record<string, string>;")
     (str/includes? out "export type UserMap = Record<UserId, User>;")
     (str/includes? out "export interface SearchResult")
     (str/includes? out "user?: User;")
     (str/includes? out "next_cursor?: string;")
     (str/includes? out "export type lookup_user = (arg0: UserMap, arg1: UserId) => User | null;")
     (str/includes? out "export declare const default_page_size: number;")])
  => [true true true true true true true true true true true true true])

(fact "keeps function declarations separate from defspec.xt data declarations"
  (let [analysis (mixer/mix-file +typescript-model-fixture+)]
    [(mapv :name (:specs analysis))
     (mapv :name (:functions analysis))])
  => [["UserId" "LookupKey" "UserRow" "User" "UserMap" "SearchResult"]
      ["lookup-user"]])
