(ns demo.tictactoe-test
  (:use code.test)
  (:require [demo.tictactoe :as tt]))

^{:refer demo.tictactoe/new-game :added "4.0"}
(fact "creates a new game"
  ^:hidden
  
  (tt/new-game)
  => {:board
      {:bg #{:aa :cb :ac :bb :ba :bc :ca :cc :ab}, :p1 #{}, :p2 #{}},
      :turn :p1,
      :status :active,
      :winner nil})

^{:refer demo.tictactoe/check-win :added "4.0"}
(fact "checks if pieces are in the winning position"
  ^:hidden

  (tt/check-win #{:aa :bb})
  => false

  (tt/check-win #{:aa :bb :cc})
  => true

  (tt/check-win #{:aa :ba :ca})
  => true

  (tt/check-win #{:aa :ba :cb})
  => false)

^{:refer demo.tictactoe/next-move :added "4.0"}
(fact "transitions from one state to the next"
  ^:hidden

  (-> (tt/new-game)
      (tt/next-move [:p1 :ac])
      (tt/next-move [:p2 :ab])
      (tt/next-move [:p1 :bb])
      (tt/next-move [:p2 :ca])
      (tt/next-move [:p1 :bc])
      (tt/next-move [:p2 :ba])
      (tt/next-move [:p1 :cc]))
  => {:board
      {:bg #{:aa :cb}, :p1 #{:ac :bb :bc :cc}, :p2 #{:ba :ca :ab}},
      :turn :p2,
      :status :done,
      :winner :p1})
