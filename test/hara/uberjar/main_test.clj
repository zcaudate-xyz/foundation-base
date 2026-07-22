(ns hara.uberjar.main-test
  (:require [clojure.string :as str]
            [hara.uberjar.main :as cli])
  (:use code.test))

^{:refer hara.uberjar.main/run :added "4.1"}
(fact "runs help, discovery, emission, stdin, and error flows"
  (cli/run ["--help"])
  => {:exit 0 :out cli/usage}

  (cli/run ["languages"])
  => {:exit 0
      :out "bash\nc\ndart\nelisp\nglsl\njs\nlua\noracle\npython\nscheme\nsql\nxtalk"}

  (cli/run ["emit" "js" "[(+ 1 2 3)]"])
  => {:exit 0 :out "1 + 2 + 3"}

  (cli/run ["emit" "lua" "-"] (constantly "[(* 6 7)]"))
  => {:exit 0 :out "6 * 7"}

  (str/includes? (:err (cli/run ["emit" "unknown" "[(+ 1 2)]"]))
                 "Unsupported language")
  => true

  (str/includes? (:err (cli/run ["emit" "js" "42"]))
                 "sequential collection")
  => true

  (:exit (cli/run ["emit"]))
  => 2)
