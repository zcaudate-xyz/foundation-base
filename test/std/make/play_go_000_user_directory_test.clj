(ns std.make.play-go-000-user-directory-test
  (:use code.test)
  (:require [std.fs :as fs]
            [std.make :as make]
            [std.make.common :as common]))

(load-file "src-build/play/go_000_user_directory/build.clj")

(fact "the Go example writes a small library project"
  (let [project (common/make-config
                 (assoc @(:instance play.go-000-user-directory.build/PROJECT)
                        :root "src-build/play/go_000_user_directory"
                        :build ".build/test-go-user-directory"))
        out-dir (common/make-dir project)]
    (try
      (make/build-all project)
      (let [go-output (slurp (str out-dir "/user_directory.go"))]
        {:files   (every? true?
                          (map (fn [path]
                                 (fs/exists? (str out-dir "/" path)))
                               play.go-000-user-directory.build/+expected-files+))
         :module  (boolean
                   (re-find #"module example\.com/go-000-user-directory"
                            (slurp (str out-dir "/go.mod"))))
         :package (boolean
                   (re-find #"package userdirectory"
                            go-output))
         :key-fn  (boolean
                   (re-find #"func FormatUserKey"
                            go-output))
         :page-fn (boolean
                   (re-find #"func NextOffset"
                            go-output))})
      (finally
        (common/make-dir-teardown project))))
  => {:files true
      :module true
      :package true
      :key-fn true
      :page-fn true})
