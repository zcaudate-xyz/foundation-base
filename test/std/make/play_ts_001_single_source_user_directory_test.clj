(ns std.make.play-ts-001-single-source-user-directory-test
  (:use code.test)
  (:require [std.fs :as fs]
            [std.make :as make]
            [std.make.common :as common]))

(load-file "src-build/play/ts_001_single_source_user_directory/build.clj")

^{:refer play.ts-001-single-source-user-directory.build/PROJECT :added "4.1"}
(fact "the single-source typescript def.make example writes js and d.ts from one file"
  (let [project (common/make-config
                 (assoc @(:instance play.ts-001-single-source-user-directory.build/PROJECT)
                        :root "src-build/play/ts_001_single_source_user_directory"
                        :build ".build/test-single-source-user-directory"))
        out-dir (common/make-dir project)]
    (try
      (make/build-all project)
      (let [js-output  (slurp (str out-dir "/src/index.js"))
            dts-output (slurp (str out-dir "/src/index.d.ts"))]
        {:files          (every? true?
                                 (map (fn [path]
                                        (fs/exists? (str out-dir "/" path)))
                                      play.ts-001-single-source-user-directory.build/+expected-files+))
         :js-function    (boolean (re-find #"export function lookupUser" js-output))
         :js-value       (boolean (re-find #"export var DEFAULT_PAGE_SIZE = 20;" js-output))
         :dts-interface  (boolean (re-find #"export interface User" dts-output))
         :dts-function   (boolean (re-find #"export type lookupUser = \(arg0: UserMap, arg1: UserId\) => User \| null;" dts-output))
         :dts-value      (boolean (re-find #"export declare const DEFAULT_PAGE_SIZE: number;" dts-output))})
      (finally
        (common/make-dir-teardown project))))
  => {:files true
      :js-function true
      :js-value true
      :dts-interface true
      :dts-function true
      :dts-value true})
