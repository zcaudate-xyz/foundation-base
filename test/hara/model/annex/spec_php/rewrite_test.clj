(ns hara.model.annex.spec-php.rewrite-test
  (:use code.test)
  (:require [hara.model.annex.spec-php.rewrite :as rewrite]))

^{:refer hara.model.annex.spec-php.rewrite/php-rewrite-form :added "4.1"}
(fact "prefixes function params and their scoped references"
  (rewrite/php-rewrite-form '((fn [x]
                                (return (+ x 1)))
                              2))
  => '((fn [$x]
         (return (+ $x 1)))
       2))

^{:refer hara.model.annex.spec-php.rewrite/php-rewrite-stage :added "4.1"}
(fact "prefixes sequential var bindings for later statements"
  (rewrite/php-rewrite-stage '(do
                                (var out [])
                                (var entries [0 1 2])
                                (for:array [i entries]
                                  (xt/x:arr-push out i))
                                out)
                             {})
  => '(do
        (var $out [])
        (var $entries [0 1 2])
        (for:array [$i $entries]
          (xt/x:arr-push $out $i))
        $out))

^{:refer hara.model.annex.spec-php.rewrite/php-rewrite-form :added "4.1"}
(fact "leaves unbound call heads and global keys untouched"
  (rewrite/php-rewrite-form '(do:>
                               (try
                                 (throw "boom")
                                 (catch err
                                   (return err)))))
  => '(do:>
        (try
          (throw "boom")
          (catch $err
            (return $err)))))

^{:refer hara.model.annex.spec-php.rewrite/php-rewrite-stage :added "4.1"}
(fact "carries top-level vector bindings forward"
  (rewrite/php-rewrite-stage '[(var out []) out]
                             {})
  => '[(var $out []) $out])
