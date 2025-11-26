(ns std.lib.future-start-test
  (:use code.test)
  (:require [std.lib.future-start :as st]
            [std.lib.future :as f]))

(fact "startable future can be created and started"
  (let [a (atom 0)
        s (st/startable (fn [] (swap! a inc)))]
    @(st/-start s)
    @a => 1
    @(st/-start s)
    @a => 2))

(fact "calling start multiple times returns different futures"
  (let [s (st/startable (fn [] 1))]
    (not= (st/-start s) (st/-start s)) => true))

(fact "future completes with the correct value"
  (let [s (st/startable (fn [] (+ 1 2)))]
    @(st/-start s) => 3))

(fact "composition for a retry mechanism"
  (let [a (atom 0)
        s (st/startable (fn []
                          (swap! a inc)
                          (if (< @a 3)
                            (throw (ex-info "fail" {}))
                            :success)))
        p (f/incomplete)]
    (letfn [(run []
              (-> (st/-start s)
                  (f/on:complete (fn [v e]
                                   (if e
                                     (run)
                                     (f/future:force p v))))))]
      (run))
    @p => :success
    @a => 3))

(fact "composition of S1 -> (S2, S3)"
  (let [a (atom [])
        s1 (st/startable (fn [] (swap! a conj :s1) :s1-done))
        s2 (st/startable (fn [] (swap! a conj :s2) :s2-done))
        s3 (st/startable (fn [] (swap! a conj :s3) :s3-done))
        p (f/incomplete)]
    (-> (st/-start s1)
        (f/on:success
         (fn [_]
           (-> (f/on:all [(st/-start s2) (st/-start s3)])
               (f/on:success (fn [v] (f/future:force p v)))))))
    @p => [:s2-done :s3-done]
    (let [log @a]
      (and (= (first log) :s1)
           (contains? (set (rest log)) :s2)
           (contains? (set (rest log)) :s3))) => true))
