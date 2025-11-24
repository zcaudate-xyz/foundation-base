(ns std.text.diff-test
  (:use code.test)
  (:require [std.text.diff :refer :all])
  (:import (difflib DiffUtils
                    Chunk Patch
                    Delta Delta$TYPE
                    ChangeDelta InsertDelta DeleteDelta)))

^{:refer std.text.diff/create-patch :added "3.0"}
(fact "creates a Patch object"

  (create-patch [{:type "CHANGE"
                  :original {:position 1
                             :lines ["hello"]}
                  :revised {:position 1
                            :lines ["world"]}}
                 {:type "DELETE"
                  :original {:position 1
                             :lines ["again"]}
                  :revised {:position 1
                            :lines []}}
                 {:type "INSERT"
                  :original {:position 1
                             :lines []}
                  :revised {:position 1
                            :lines ["again"]}}])
  => Patch)

^{:refer std.text.diff/create-delta :added "3.0"}
(fact "creates a Delta object from map"

  (create-delta {:type "CHANGE"
                 :original {:position 1
                            :lines ["hello"]}
                 :revised {:position 1
                           :lines []}})
  => Delta)

^{:refer std.text.diff/create-chunk :added "3.0"}
(fact "creates a Chunk object from map"

  (create-chunk {:position 1
                 :lines ["hello"]})
  => Chunk)

(def -a- (create-chunk {:position 1
                        :lines ["hello"]}))

^{:refer std.text.diff/diff :added "3.0"}
(fact "returns a set of diff results"

  (diff "10\n11\n\12\n13\n14"
        "10\n18\n\12\n19\n14")
  => [{:type "CHANGE",
       :original {:count 1,
                  :position 1,
                  :lines ["11"]},
       :revised {:count 1,
                 :position 1,
                 :lines ["18"]}}
      {:type "CHANGE",
       :original {:count 1,
                  :position 4,
                  :lines ["13"]},
       :revised {:count 1,
                 :position 4,
                 :lines ["19"]}}])

^{:refer std.text.diff/patch :added "3.0"}
(fact "takes a series of deltas and returns the revised result"

  (patch "10\n11\n\12\n13\n14"
         [{:type "CHANGE",
           :original {:count 1,
                      :position 1,
                      :lines ["11"]},
           :revised {:count 1,
                     :position 1,
                     :lines ["18"]}}
          {:type "CHANGE",
           :original {:count 1,
                      :position 4,
                      :lines ["13"]},
           :revised {:count 1,
                     :position 4,
                     :lines ["19"]}}])
  => "10\n18\n\n\n19\n14")

^{:refer std.text.diff/unpatch :added "3.0"}
(fact "takes a series of deltas and restores the original result"

  (unpatch "10\n18\n\n\n19\n14"
           [{:type "CHANGE",
             :original {:count 1,
                        :position 1,
                        :lines ["11"]},
             :revised {:count 1,
                       :position 1,
                       :lines ["18"]}}
            {:type "CHANGE",
             :original {:count 1,
                        :position 4,
                        :lines ["13"]},
             :revised {:count 1,
                       :position 4,
                       :lines ["19"]}}])
  => "10\n11\n\12\n13\n14")

^{:refer std.text.diff/->string :added "3.0"}
(fact "returns a human readable output of the diffs"

  (println (-> (diff "10\n11\n\12\n13\n14"
                     "10\n18\n\12\n19\n14")
               (->string))))

^{:refer std.text.diff/summary :added "3.0"}
(fact "creates a summary of the various diffs"

  (summary (diff "10\n11\n\12\n13\n14"
                 "10\n18\n\12\n19\n14"))
  => {:deletes 2, :inserts 2})

(comment
  (code.manage/import {:write true}))


^{:refer std.text.diff/get-lines :added "4.0"}
(fact "gets the lines that have changed")