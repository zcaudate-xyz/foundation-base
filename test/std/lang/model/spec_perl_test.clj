(ns std.lang.model.spec-perl-test
  (:require [std.lang.model.spec-perl :as spec-perl]
            [std.lang :as l]
            [std.lib :as h]
            [code.test :as t]))

(l/script :perl)

(t/fact "test perl emission"
  (l/emit-as :perl
   '[(do
      (var a 10)
      (var b 20)
      (if (> a b)
        (return a)
        (return b)))])
  => "my $a = 10;\nmy $b = 20;\nif($a > $b){\n  return $a;\n}\nelse{\n  return $b;\n}")

(t/fact "test perl functions"
  (l/emit-as :perl
   '[(defn add [a b]
      (return (+ a b)))])
  => "sub add {\nmy $a = shift;\nmy $b = shift;\nreturn $a + $b;\n}")

(t/fact "test perl arrays"
  (l/emit-as :perl
   '[(var arr [1 2 3])])
  => "my $arr = [1, 2, 3]")

(t/fact "test perl maps"
  (l/emit-as :perl
   '[(var m {"a" 1 "b" 2})])
  => "my $m = {\"a\" => 1, \"b\" => 2}")

(t/fact "test perl invoke"
  (l/emit-as :perl
   '[(do
      (print "Hello")
      (add 1 2))])
  => "print(\"Hello\");\nadd(1, 2);")

(t/fact "test perl concat"
  (l/emit-as :perl
   '[(var s (concat "a" "b"))])
  => "my $s = \"a\" . \"b\"")

(t/fact "test perl xtalk core"
  (l/emit-as :perl
   '[(do
      (x:len arr)
      (x:cat "a" "b")
      (x:print "hello"))])
  => "scalar($arr);\n\"a\" . \"b\";\nprint(\"hello\", \"\\n\");")

(t/fact "test perl xtalk math"
  (l/emit-as :perl
   '[(do
      (x:m-abs -1)
      (x:m-pow 2 3)
      (x:m-sqrt 9))])
  => "abs(-1);\n2 ** 3;\nsqrt(9);")

(t/fact "test perl xtalk array"
  (l/emit-as :perl
   '[(do
      (x:arr-push arr 1)
      (x:arr-pop arr))])
  => "push($arr, 1);\npop($arr);")

(t/fact "test perl xtalk string"
  (l/emit-as :perl
   '[(do
      (x:str-split "a,b" ",")
      (x:str-to-upper "a")
      (x:str-join "," arr))])
  => "split(\",\", \"a,b\");\nuc(\"a\");\njoin(\",\", $arr);")


^{:refer std.lang.model.spec-perl/perl-var :added "4.1"}
(t/fact "emit perl variable declaration"
  (spec-perl/perl-var '(var a 10))
  => '(:- "my $a = 10"))

^{:refer std.lang.model.spec-perl/perl-symbol :added "4.1"}
(t/fact "emit perl symbol with $ prefix if it's a variable"
  (spec-perl/perl-symbol 'a spec-perl/+grammar+ {})
  => "$a"
  (spec-perl/perl-symbol 'a spec-perl/+grammar+ {:perl/func true})
  => "a")

^{:refer std.lang.model.spec-perl/perl-invoke-args :added "4.1"}
(t/fact "emit perl invoke args"
  (spec-perl/perl-invoke-args '[1 2] spec-perl/+grammar+ {})
  => "1, 2")

^{:refer std.lang.model.spec-perl/perl-invoke :added "4.1"}
(t/fact "emit perl function call"
  (spec-perl/perl-invoke '(f 1 2) spec-perl/+grammar+ {})
  => "f(1, 2)")

^{:refer std.lang.model.spec-perl/perl-defn :added "4.1"}
(t/fact "emit perl subroutine definition"
  (spec-perl/perl-defn '(defn add [a b] (return (+ a b))))
  => (list :- "sub add {\nmy $a = shift;\nmy $b = shift;\n(do (return (+ a b)))\n}"))

^{:refer std.lang.model.spec-perl/perl-array :added "4.1"}
(t/fact "emit perl array reference"
  (spec-perl/perl-array [1 2 3] spec-perl/+grammar+ {})
  => "[1, 2, 3]")

^{:refer std.lang.model.spec-perl/perl-map :added "4.1"}
(t/fact "emit perl hash reference"
  (spec-perl/perl-map {"a" 1 "b" 2} spec-perl/+grammar+ {})
  => "{\"a\" => 1, \"b\" => 2}")


^{:refer std.lang.model.spec-perl/perl-var :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-perl/perl-symbol :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-perl/perl-invoke-args :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-perl/perl-invoke :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-perl/perl-defn :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-perl/perl-array :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-perl/perl-map :added "4.1"}
(fact "TODO")