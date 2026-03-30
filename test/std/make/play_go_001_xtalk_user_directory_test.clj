(ns std.make.play-go-001-xtalk-user-directory-test
  (:use code.test)
  (:require [clojure.string :as str]
            [std.fs :as fs]
            [std.make :as make]
            [std.make.common :as common]))

(load-file "src-build/play/go_001_xtalk_user_directory/build.clj")

(fact "the Go xtalk example writes Go declarations from canonical xtalk source"
  (let [project (common/make-config
                 (assoc @(:instance play.go-001-xtalk-user-directory.build/PROJECT)
                        :root "src-build/play/go_001_xtalk_user_directory"
                        :build ".build/test-go-xtalk-user-directory"))
        out-dir (common/make-dir project)]
    (try
      (make/build-all project)
      (let [go-output    (slurp (str out-dir "/user_directory.go"))
            source-input (slurp "src-build/play/go_001_xtalk_user_directory/main.clj")]
        {:files        (every? true?
                               (map (fn [path]
                                      (fs/exists? (str out-dir "/" path)))
                                    play.go-001-xtalk-user-directory.build/+expected-files+))
          :source-xtalk (boolean
                         (re-find #"l/script :xtalk"
                                  source-input))
          :source-spec  (str/includes? source-input "defspec.xt")
          :source-op    (str/includes? source-input "x:get-key")
          :module       (boolean
                         (re-find #"module example\.com/go-001-xtalk-user-directory"
                                  (slurp (str out-dir "/go.mod"))))
          :package      (boolean
                         (re-find #"package userdirectory"
                                  go-output))
          :user-type    (boolean
                         (re-find #"type User map\[string\]any"
                                  go-output))
          :user-map     (boolean
                         (re-find #"type UserMap map\[any\]any"
                                  go-output))
          :lookup-user  (boolean
                         (re-find #"type lookupUser func\(arg0 UserMap, arg1 UserId\) \*User"
                                  go-output))
          :user-ids     (boolean
                         (re-find #"type userIds func\(arg0 UserMap\) \[\]UserId"
                                  go-output))
          :page-size    (boolean
                         (re-find #"var DEFAULT_PAGE_SIZE int"
                                  go-output))
          :lookup-once  (= 1 (count (re-seq #"type lookupUser func" go-output)))
          :page-once    (= 1 (count (re-seq #"DEFAULT_PAGE_SIZE" go-output)))})
      (finally
        (common/make-dir-teardown project))))
  => {:files true
      :source-xtalk true
      :source-spec true
      :source-op true
      :module true
      :package true
      :user-type true
      :user-map true
      :lookup-user true
      :user-ids true
      :page-size true
      :lookup-once true
      :page-once true})
