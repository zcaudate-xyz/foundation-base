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

^{:refer hara.model.annex.spec-php.rewrite/php-rewrite-form :added "4.1"
  :id test-php-rewrite-form-unbound}
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

^{:refer hara.model.annex.spec-php.rewrite/php-rewrite-stage :added "4.1"
  :id test-php-rewrite-stage-vector}
(fact "carries top-level vector bindings forward"
  (rewrite/php-rewrite-stage '[(var out []) out]
                             {})
  => '[(var $out []) $out])


^{:refer hara.model.annex.spec-php.rewrite/php-local-symbol? :added "4.1"}
(fact "classifies unqualified PHP locals without rewriting reserved markers"
  (mapv rewrite/php-local-symbol?
        '[value $value :keyword -raw !macro __global qualified/value])
  => [true false false false false false false])

^{:refer hara.model.annex.spec-php.rewrite/php-prefix-local :added "4.1"}
(fact "prefixes PHP locals exactly once"
  (mapv rewrite/php-prefix-local '[value $value qualified/value])
  => '[$value $value qualified/value])

^{:refer hara.model.annex.spec-php.rewrite/php-rewrite-form* :added "4.1"}
(fact "rewrites scoped symbols recursively while preserving call heads"
  (rewrite/php-rewrite-form*
   '(handler {:item value} [value external])
   '#{value})
  => '(handler {:item $value} [$value external])
  (rewrite/php-rewrite-form* '(quote value) '#{value})
  => '(quote value))

^{:refer hara.model.annex.spec-php.rewrite/php-rewrite-statements* :added "4.1"}
(fact "carries sequential var bindings into later statements"
  (rewrite/php-rewrite-statements*
   '((var value 1)
     (var other (+ value 2))
     (return (+ value other external)))
   #{})
  => '((var $value 1)
       (var $other (+ $value 2))
       (return (+ $value $other external))))
