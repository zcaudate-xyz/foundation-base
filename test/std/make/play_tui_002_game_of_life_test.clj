(ns std.make.play-tui-002-game-of-life-test
  (:use code.test)
  (:require [std.fs :as fs]
            [std.make :as make]
            [std.make.common :as common]))

(load-file "src-build/play/tui_002_game_of_life/build.clj")

^{:refer play.tui-002-game-of-life.build/PROJECT :added "4.1"}
(fact "the game of life example writes an actual def.make build tree"
  (let [project (common/make-config
                 (assoc @(:instance play.tui-002-game-of-life.build/PROJECT)
                        :root "src-build/play/tui_002_game_of_life"
                        :build ".build/test-game-of-life"))
        out-dir (common/make-dir project)]
    (try
      (make/build-all project)
      {:files        (every? true?
                             (map (fn [path]
                                    (fs/exists? (str out-dir "/" path)))
                                  play.tui-002-game-of-life.build/+expected-files+))
       :package-main (boolean
                      (re-find #"\"main\"\s*:\s*\"dist/main\.js\""
                               (slurp (str out-dir "/package.json"))))
       :make-start   (boolean
                      (re-find #"(?m)^start:"
                               (slurp (str out-dir "/Makefile"))))
       :webpack-entry (boolean
                       (re-find #"\"entry\"\s*:\s*\"\.\/src\/main\.js\""
                                (slurp (str out-dir "/webpack.config.js"))))
       :module-body  (boolean
                      (re-find #"import ReactBlessed"
                               (slurp (str out-dir "/src/main.js"))))}
      (finally
        (common/make-dir-teardown project))))
  => {:files true
      :package-main true
      :make-start true
      :webpack-entry true
      :module-body true})
