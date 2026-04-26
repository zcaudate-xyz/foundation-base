(ns std.lang.model.spec-xtalk.fn-python-file-test
  (:require [std.lang :as l]
            [std.lang.model.spec-xtalk.fn-python :refer [python-tf-x-file-slurp
                                                         python-tf-x-file-spit]])
  (:use code.test))

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-file-slurp :added "4.1"}
(fact "slurp file resolves relative paths from PWD"
  (l/emit-as :python [(python-tf-x-file-slurp '[_ filename opts cb])])
  => #"environ\.get\(\"PWD\"\)"

  (l/emit-as :python [(python-tf-x-file-slurp '[_ filename opts cb])])
  => #"getcwd"

  (l/emit-as :python [(python-tf-x-file-slurp '[_ filename opts cb])])
  => #"path\.abspath"

  (l/emit-as :python [(python-tf-x-file-slurp '[_ filename opts cb])])
  => #"path\.join"

  (l/emit-as :python [(python-tf-x-file-slurp '[_ filename opts cb])])
  => #"open\(resolved,\"r\"\)")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-file-spit :added "4.1"}
(fact "spit file resolves relative paths from PWD"
  (l/emit-as :python [(python-tf-x-file-spit '[_ filename s opts cb])])
  => #"environ\.get\(\"PWD\"\)"

  (l/emit-as :python [(python-tf-x-file-spit '[_ filename s opts cb])])
  => #"getcwd"

  (l/emit-as :python [(python-tf-x-file-spit '[_ filename s opts cb])])
  => #"path\.abspath"

  (l/emit-as :python [(python-tf-x-file-spit '[_ filename s opts cb])])
  => #"path\.join"

  (l/emit-as :python [(python-tf-x-file-spit '[_ filename s opts cb])])
  => #"open\(resolved,\"w\"\)")
