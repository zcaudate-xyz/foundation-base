(ns std.make.play-ts-000-user-directory-test
  (:use code.test)
  (:require [clojure.string :as str]
            [std.fs :as fs]
            [std.make :as make]
            [std.make.common :as common]))

(load-file "src-build/play/ts_000_user_directory/build.clj")

^{:refer play.ts-000-user-directory.build/PROJECT :added "4.1"}
(fact "the typescript def.make example writes js and d.ts sidecars"
  (let [project (common/make-config
                 (assoc @(:instance play.ts-000-user-directory.build/PROJECT)
                        :root "src-build/play/ts_000_user_directory"
                        :build ".build/test-user-directory"))
        out-dir (common/make-dir project)]
    (try
      (make/build-all project)
      (let [package-json (slurp (str out-dir "/package.json"))
            js-output    (slurp (str out-dir "/src/index.js"))
            dts-output   (slurp (str out-dir "/src/index.d.ts"))]
        {:files          (every? true?
                                 (map (fn [path]
                                        (fs/exists? (str out-dir "/" path)))
                                      play.ts-000-user-directory.build/+expected-files+))
         :package-types  (boolean
                          (re-find #"\"types\"\s*:\s*\"src/index\.d\.ts\""
                                   package-json))
         :js-function    (boolean
                          (re-find #"export function lookupUser"
                                   js-output))
         :dts-interface  (boolean
                          (re-find #"export interface User"
                                   dts-output))
         :dts-function   (boolean
                          (re-find #"export type lookupUser = \(arg0: UserMap, arg1: UserId\) => User \| null;"
                                   dts-output))
         :dts-value      (boolean
                          (re-find #"export declare const DEFAULT_PAGE_SIZE: number;"
                                   dts-output))
         :lookupUserOnce (= 1 (count (re-seq #"export type lookupUser =" dts-output)))
         :userIdsOnce    (= 1 (count (re-seq #"export type userIds =" dts-output)))} )
      (finally
        (common/make-dir-teardown project))))
  => {:files true
      :package-types true
      :js-function true
      :dts-interface true
      :dts-function true
      :dts-value true
      :lookupUserOnce true
      :userIdsOnce true})
