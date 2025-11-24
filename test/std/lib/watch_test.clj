(ns std.lib.watch-test
  (:use code.test)
  (:require [std.lib.watch :as watch :refer :all]))

^{:refer std.lib.watch/wrap-select :added "3.0"}
(fact "enables operating on a given key"

  ((watch/wrap-select (fn [_ _ p n]
                        (+ p n))
                      :a)
   nil nil {:a 2} {:a 1})
  => 3)

^{:refer std.lib.watch/wrap-diff :added "3.0"}
(fact "only functions when the inputs are different"

  ((watch/wrap-diff (fn [_ _ p n]
                      (+ p n)))
   nil nil 2 2)
  => 2

  ((watch/wrap-diff (fn [_ _ p n]
                      (+ p n)))
   nil nil 2 3)
  => 5)

^{:refer std.lib.watch/wrap-mode :added "3.0"}
(comment "changes how the function is run, :sync (same thread) and :async (new thread)")

^{:refer std.lib.watch/wrap-suppress :added "3.0"}
(comment "runs the function but if errors, does not throw exception")

^{:refer std.lib.watch/process-options :added "3.0"}
(fact "helper function for building a watch function"

  (watch/process-options {:supress true
                          :diff true
                          :mode :async
                          :args 2}
                         (fn [_ _] 1))
  (resolve 'process-options) => var?)

^{:refer std.lib.watch/watch:add :added "3.0"}
(fact "Adds a watch function through the IWatch protocol" ^:hidden

  (def subject (atom nil))
  (def observer (atom nil))

  (watch:add subject :follow
             (fn [_ _ _ n]
               (reset! observer n)))
  (reset! subject 1)
  @observer => 1

  ;; options can be given to either transform
  ;; the current input as well as to only execute
  ;; the callback if there is a difference.

  (def subject  (atom {:a 1 :b 2}))
  (def observer (atom nil))

  (watch:add subject :clone
             (fn [_ _ p n] (reset! observer n))
             {:select :b
              :diff true})

  (swap! subject assoc :a 0) ;; change in :a does not
  @observer => nil           ;; affect watch


  (swap! subject assoc :b 1) ;; change in :b does
  @observer => 1)

^{:refer std.lib.watch/watch:list :added "3.0"}
(fact "Lists watch functions through the IWatch protocol" ^:hidden

  (def subject   (atom nil))
  (do (watch:add subject :a (fn [_ _ _ n]))
      (watch:add subject :b (fn [_ _ _ n]))
      (watch:list subject))
  => (contains {:a fn? :b fn?}))

^{:refer std.lib.watch/watch:remove :added "3.0"}
(fact "Removes watch function through the IWatch protocol" ^:hidden

  (def subject   (atom nil))
  (do (watch:add subject :a (fn [_ _ _ n]))
      (watch:add subject :b (fn [_ _ _ n]))
      (watch:remove subject :b)
      (watch:list subject))
  => (contains {:a fn?}))

^{:refer std.lib.watch/watch:clear :added "3.0"}
(fact "Clears all watches form the object" ^:hidden

  (def subject   (atom nil))
  (do (watch:add subject :a (fn [_ _ _ n]))
      (watch:add subject :b (fn [_ _ _ n]))
      (watch:clear subject)
      (watch:list subject))
  => {})

^{:refer std.lib.watch/watch:set :added "3.0"}
(fact "Sets a watch in the form of a map" ^:hidden
  (def obj (atom nil))
  (do (watch:set obj {:a (fn [_ _ _ n])
                      :b (fn [_ _ _ n])})
      (watch:list obj))
  => (contains {:a fn? :b fn?}))

^{:refer std.lib.watch/watch:copy :added "3.0"}
(fact "Copies watches from one object to another" ^:hidden
  (def obj-a   (atom nil))
  (def obj-b   (atom nil))
  (do (watch:set obj-a {:a (fn [_ _ _ n])
                        :b (fn [_ _ _ n])})
      (watch:copy obj-b obj-a)
      (watch:list obj-b))
  => (contains {:a fn? :b fn?}))

(comment
  (code.manage/import))
