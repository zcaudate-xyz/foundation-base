(ns code.tool.measure.common-test
  (:use code.test)
  (:require [code.tool.measure.common :as sut]))

^{:refer code.tool.measure.common/calculate-surface :added "4.1"}
(fact "calculate-surface"
  (sut/calculate-surface 10 5) => number?
  (sut/calculate-surface 0 5) => 0.0)

^{:refer code.tool.measure.common/sh-git :added "4.1"}
(fact "sh-git"
  (with-redefs [std.lib/sh (constantly {:exit 0 :out " ok "})]
    (sut/sh-git ["status"] ".")) => "ok")

^{:refer code.tool.measure.common/list-commits :added "4.1"}
(fact "list-commits"
  (with-redefs [sut/sh-git (constantly "sha1|2023-01-01\nsha2|2023-01-02")]
    (sut/list-commits "."))
  => [{:sha "sha1" :date "2023-01-01"} {:sha "sha2" :date "2023-01-02"}])

^{:refer code.tool.measure.common/list-files :added "4.1"}
(fact "list-files"
  (with-redefs [sut/sh-git (constantly "file1\nfile2")]
    (sut/list-files "." "sha"))
  => ["file1" "file2"])

^{:refer code.tool.measure.common/get-file-content :added "4.1"}
(fact "get-file-content"
  (with-redefs [sut/sh-git (constantly "content")]
    (sut/get-file-content "." "sha" "file"))
  => "content")
