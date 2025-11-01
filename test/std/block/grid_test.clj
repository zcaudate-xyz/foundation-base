(ns std.block.grid-test
  (:use code.test)
  (:require [std.block.grid :refer :all]
            [std.block.construct :as construct]
            [std.block.parse :as parse]))

;; if scope is empty, then grid follows the first indent rule for
;; if scope is zero, then forms follow indentation without first indent rule
;; scope is passed onto children - if zero, then don't pass , if {}, only pass to selected children and decrement the rule
;;   [0 1 [0 2]] ->  (1, [0 2])
;;      - [0] for all children except binding 0
;;      - [0 1], for binding 0
;;   [0 {1 [0 1]]}] -> [0, [0 1]] for element 1
;; only for lists and fns.
;;   - basic data is {:indent 0, :bind 0, scope [0]}
;;   - basic list is {:indent 0, :bind 0, scope []}

^{:refer std.block.grid/trim-left :added "3.0"}
(fact "removes whitespace nodes on the left"

  (->> (trim-left [(construct/space)
                   :a
                   (construct/space)])
       (mapv str))
  => [":a" "␣"])

^{:refer std.block.grid/trim-right :added "3.0"}
(fact "removes whitespace nodes on the right"

  (->> (trim-right [(construct/space)
                    :a
                    (construct/space)])
       (mapv str))
  => ["␣" ":a"])

^{:refer std.block.grid/split-lines :added "3.0"}
(fact "splits the nodes, retaining the linebreak nodes"

  (split-lines [:a :b (construct/newline) :c :d])
  => [[:a :b]
      [(construct/newline) :c :d]])

^{:refer std.block.grid/remove-starting-spaces :added "3.0"}
(fact "removes extra spaces from the start of the line"

  (remove-starting-spaces [[(construct/newline)
                            (construct/space)
                            (construct/space) :a :b]
                           [(construct/newline) (construct/space) :c :d]])
  => [[(construct/newline) :a :b]
      [(construct/newline) :c :d]])

^{:refer std.block.grid/adjust-comments :added "3.0"}
(fact "adds additional newlines after comments"

  (->> (adjust-comments [(construct/comment ";hello") :a])
       (mapv str))
  => [";hello" "\\n" ":a"])

^{:refer std.block.grid/remove-extra-linebreaks :added "3.0"}
(fact "removes extra linebreaks"

  (remove-extra-linebreaks [[:a]
                            [(construct/newline)]
                            [(construct/newline)]
                            [(construct/newline)]
                            [:b]])
  => [[:a]
      [(construct/newline)]
      [:b]])

^{:refer std.block.grid/grid-scope :added "3.0"}
(fact "calculates the grid scope for child nodes"

  (grid-scope [{0 1} 1])
  => [{0 1} 0])

^{:refer std.block.grid/grid-rules :added "3.0"}
(fact "creates indentation rules for current block"

  (grid-rules :list nil nil nil)
  => {:indent 0, :bind 0, :scope []}

  (grid-rules :vector nil nil nil)
  => {:indent 0, :bind 0, :scope [0]}

  (grid-rules :list 'add [1] nil)
  => {:indent 1, :bind 0, :scope [0]}

  (grid-rules :list 'if nil '{if {:indent 1}})
  => {:indent 1, :bind 0, :scope []})

^{:refer std.block.grid/indent-bind :added "3.0"}
(fact "returns the number of lines indentation"

  (indent-bind [[(construct/token 'if-let)]
                [(construct/newline)]
                [(construct/newline) (construct/block '[i (pos? 0)])]
                [(construct/newline) (construct/block '(+ i 1))]]
               1)
  => 2 ^:hidden

  (indent-bind [[(construct/token 'if-let)]
                [(construct/newline)]
                [(construct/newline) (construct/block '[i (pos? 0)])]
                [(construct/newline) (construct/block '(+ i 1))]]
               0)
  => 0)

^{:refer std.block.grid/indent-lines :added "3.0"}
(fact "indent lines given an anchor and rule"

  (-> (indent-lines [[(construct/token 'if-let)]
                     [(construct/newline)]
                     [(construct/newline) (construct/block '[i (pos? 0)])]
                     [(construct/newline) (construct/block '(+ i 1))]]
                    1
                    {:indent 1
                     :bind 1})
      (construct/contents)) ^:hidden
  => '([if-let]
       (\n ␣ ␣ ␣ ␣)
       (\n ␣ ␣ ␣ ␣ [i (pos? 0)])
       (\n ␣ ␣ (+ i 1))))

^{:refer std.block.grid/grid :added "3.0"}
(fact "formats lines given indentation rules and scope"

  (-> (construct/block ^:list ['if-let
                               (construct/newline)
                               (construct/newline) (construct/block '[i (pos? 0)])
                               (construct/newline) (construct/block '(+ i 1))])
      (grid 1 {:rules {'if-let {:indent 1
                                :bind 1}}})
      (construct/contents)) ^:hidden
  => '(if-let
       \n ␣ ␣ ␣ ␣ ␣
       \n ␣ ␣ ␣ ␣ ␣ [i (pos? 0)]
       \n ␣ ␣ ␣ (+ i 1)))

(comment

  (def +rules+
    '{do    {:indent 1
             :bind 0
             :scope [0]}
      let   {:indent 1
             :bind 1
             :scope [0]}
      letfn {:indent 1
             :bind 0
             :scope [0 [0 2]]}
      doseq {:indent 1
             :bind 1
             :scope [0]}})

  (def let-str
    "#(let #{a
              a1}
      #(let #{b
          b1}
        (let [d
             d1])))")

  (prn (grid (parse/parse-string let-str)
             0
             {:rules +rules+})))

(comment
  (def letfn-str
    "(letfn [(double [x]
                     (* x 2))]   ;; special indentation here
    (let [y (double 2)
          z (double 3)]
            (println y     ;; but not here
                     z)))")

  (prn (grid (parse/parse-string letfn-str)
             0
             {:rules +rules+})))

(comment

  (def hello-str
    "#{hello  #{a b
    c d}}")

  (prn (grid (parse/parse-string hello-str)
             0
             {:rules +rules+})))

(comment

  (./arrange)

  (def a (grid (construct/construct ^:list ['list :b (construct/newline) :c])
               0
               {:rules +rules+}))
  (prn a)

  (prn (grid (construct/construct ^:list ['doseq
                                          (construct/newline)
                                          (construct/newline)
                                          (construct/newline) :c
                                          (construct/newline) :d
                                          (construct/newline) :e
                                          ;(construct/newline) :e
                                          ;:e
                                          ])
             0
             {:rules +rules+}))

  (prn (grid (construct/construct ^:list ['doseq
                                          (construct/newline) ^:list ['doseq (construct/newline) :a
                                                                      ;:a
                                                                      (construct/newline) ^:list ['doseq (construct/newline) :a
                                                                                                  (construct/newline) :a
                                                                                                  (construct/newline) :a
                                                                      ;:a
                                                                                                  ]]
                                          (construct/newline) :c])
             0
             {:rules +rules+}))

  (prn (grid (construct/construct ^:list ['doseq
                                          :b
                                          (construct/newline) :c])
             0
             {:rules +rules+}))

  (grid (construct/construct ^:list ['other :b (construct/newline) :c])
        0
        {:rules +rules+})

  (grid (construct/construct ^:list [:a :b (construct/newline) :c])
        0
        {:rules +rules+}))
