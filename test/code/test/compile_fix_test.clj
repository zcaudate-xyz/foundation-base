(ns code.test.compile-fix-test
  (:use code.test))

;;
;; Merged from test-play/demo test files
;; Tests for verifying checker output formatting
;;

(fact "Verify contains output on failure"

  {:a 1 :b 2 :c 3 :d 4}
  => (contains {:a 1 :b 3 :c 3}))

(fact "Verify contains output on failure with nested structure"
  
  {:a {:b 1 :c 2} :d 3}
  => (contains {:a {:b 2}}))

(fact "Verify contains-in output on failure"
  
  {:a {:b 2 :c 3} :d 4}
  => (contains-in {:a {:b 3}}))

(fact "Verify contains-in output on failure with nested structure"
  
  {:a {:b 1 :c 2} :d 3}
  => (contains-in {:a {:b 2}}))

(fact "Verify just-map diff output"

  {:a 1 :b 2}
  => (just {:a 1 :b 3}))

(fact "Verify just output on failure"

  {:a 1 :b 2 :c 3}
  => (just {:a 1 :b 3 :c 3}))

(fact "Verify just output on failure"

  (throw (ex-info "Errored"))

  1 => 2)

(comment
  (s/run '[code.test.compile-fix-test])
  )
