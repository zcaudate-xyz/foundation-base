(ns code.tool.translate.c-ast-test
  (:require [code.tool.translate.c-ast :refer :all]
            [std.fs :as fs]
            [std.lib.os :as os]
            [std.make :as make])
  (:use code.test))

^{:refer code.tool.translate.c-ast/initialise :added "4.1"}
(fact "checks that clang is available"
  (with-redefs [os/sh identity]
    (initialise))
  => {:args ["clang" "--version"]})

^{:refer code.tool.translate.c-ast/translate-ast :added "4.1"}
(fact "builds the translator and shells out to clang"
  ^:hidden
  
  (let [input (fs/create-tmpfile "example.c" "int main(){return 0;}")
        output (str input ".json")]
    (with-redefs [make/build-all (fn [target] target)
                  os/sh (fn [_]
                          "{\"type\":\"TranslationUnitDecl\"}")]
      [(translate-ast input)
       (do (translate-ast input output)
           (slurp output))]))
  => ["{\"type\":\"TranslationUnitDecl\"}"
      "{\"type\":\"TranslationUnitDecl\"}"])
