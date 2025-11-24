(ns std.lib.component.track-test
  (:use code.test)
  (:require [std.lib.component.track :refer :all]
            [std.protocol.track :as protocol.track]))

(defrecord Store []
  protocol.track/ITrack
  (-track-path [_] [:test :store]))

^{:refer std.lib.component.track/track-path :added "3.0"}
(fact "retrieves the track path"

  (track-path (Store.))
  => [:test :store])

^{:refer std.lib.component.track/trackable? :added "3.0"}
(fact "checks if record implements `-track-path`"

  (trackable? 1)
  => false

  (trackable? (Store.))
  => true)

^{:refer std.lib.component.track/track-entry? :added "3.0"}
(fact "checks if object is a tracking entry" ^:hidden

  (track-entry? (map->TrackEntry {}))
  => true)

^{:refer std.lib.component.track/track-id :added "3.0"}
(fact "retrieves the track id and updates object if map"

  (track-id (Store.))
  => (contains [string? map?]))

^{:refer std.lib.component.track/track :added "3.0"}
(fact "tracks an object"

  (track (Store.))
  => anything)

^{:refer std.lib.component.track/tracked? :added "3.0"}
(fact "checks if object has been tracked" ^:hidden

  (-> (Store.)
      (track)
      (tracked?))
  => true)

^{:refer std.lib.component.track/untrack :added "3.0"}
(fact "untracks a given object" ^:hidden

  (-> (doto (Store.)
        (track)
        (untrack))
      tracked?)
  => false)

^{:refer std.lib.component.track/untrack-all :added "3.0"}
(fact "clears all tracked objects"

  (untrack-all [:test :store])
  => map?)

^{:refer std.lib.component.track/tracked:action:add :added "3.0"
  :setup  [(track (Store.))]
  :teardown (tracked:last [] :untrack)}
(fact "adds an action"

  (tracked:action:add :println println) ^:hidden

  (with-out-str
    (tracked:last [:test :store] :println))
  => "#std.lib.component.track_test.Store{}\n")

^{:refer std.lib.component.track/tracked:action:remove :added "3.0"}
(fact "removes an action"

  (tracked:action:remove :println)
  => coll?)

^{:refer std.lib.component.track/tracked:action:get :added "3.0"}
(fact "gets an action"

  (tracked:action:get :identity)
  => identity)

^{:refer std.lib.component.track/tracked:action:list :added "3.0"}
(fact "lists all actions"

  (tracked:action:list)
  => (contains [:identity :peek] :in-any-order :gaps-ok))

^{:refer std.lib.component.track/tracked:all :added "3.0"}
(fact "returns all tracked objects"

  (tracked:all)
  => map?

  (tracked:all [:test :store])
  => (any map? nil?))

^{:refer std.lib.component.track/tracked :added "3.0"}
(fact "returns and performs actions on tracked objects"

  (tracked [:test] :identity)
  => map?)

^{:refer std.lib.component.track/tracked:count :added "3.0"}
(fact "returns the count on the categories"

  (tracked:count)
  => (any map? number?))

^{:refer std.lib.component.track/tracked:locate :added "3.0"}
(fact "locates given entries with tag" ^:hidden

  (track:with-metadata [{:tag :executor}]
                       (track (Store.)))

  (tracked:locate {:tag :executor})
  => (comp not empty?))

^{:refer std.lib.component.track/tracked:list :added "3.0"}
(fact "outputs entries with tag as list"

  (tracked:list {:tag :executor})
  => coll?)

^{:refer std.lib.component.track/tracked:last :added "3.0"}
(fact "operates on the last tracked objects"

  (tracked:last [] :identity 3)
  => coll?)

^{:refer std.lib.component.track/track:with-metadata :added "3.0"
  :style/indent 1
  :teardown [(untrack-all [:test :store])]}
(fact "applies additional metadata to the tracking object" ^:hidden

  (track:with-metadata [{:tag :default}]
                       (track (Store.)))

  (tracked:locate {:tag :default})
  => (comp not empty?))

(tracked:action:add :untrack untrack
                    :track track)
