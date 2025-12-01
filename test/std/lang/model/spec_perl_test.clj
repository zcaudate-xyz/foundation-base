(ns std.lang.model.spec-perl-test
  (:require [std.lang.model.spec-perl :as perl]
            [std.lang :as l]
            [std.lib :as h]
            [code.test :as t]))

(l/script :perl)

(t/fact "test perl emission"
  (l/emit-script
   '(do
      (var a 10)
      (var b 20)
      (if (> a b)
        (return a)
        (return b)))
   {:lang :perl})
  => "my $a = 10;\nmy $b = 20;\nif($a > $b){\n  return $a;\n}\nelse{\n  return $b;\n}")

(t/fact "test perl functions"
  (l/emit-script
   '(defn add [a b]
      (return (+ a b)))
   {:lang :perl})
  => "sub add {\nmy $a = shift;\nmy $b = shift;\nreturn $a + $b;\n}")

(t/fact "test perl arrays"
  (l/emit-script
   '(var arr [1 2 3])
   {:lang :perl})
  => "my $arr = [1, 2, 3]")

(t/fact "test perl maps"
  (l/emit-script
   '(var m {"a" 1 "b" 2})
   {:lang :perl})
  => "my $m = {'a' => 1, 'b' => 2}")

(t/fact "test perl invoke"
  (l/emit-script
   '(do
      (print "Hello")
      (add 1 2))
   {:lang :perl})
  => "print('Hello');\nadd(1, 2);")

(t/fact "test perl concat"
  (l/emit-script
   '(var s (concat "a" "b"))
   {:lang :perl})
  => "my $s = 'a' . 'b'")
