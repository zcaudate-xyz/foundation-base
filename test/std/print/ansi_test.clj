(ns std.print.ansi-test
  (:require [std.print.ansi :refer :all])
  (:use code.test))

^{:refer std.print.ansi/encode-raw :added "3.0"}
(fact "encodes the raw ansi modifier codes to string"

  (encode-raw [30 20])
  => "[30;20m")

^{:refer std.print.ansi/encode :added "3.0"}
(fact "encodes the ansi characters for modifiers"
  (encode :bold)
  => "[1m"

  (encode :red)
  => "[31m")

^{:refer std.print.ansi/style :added "3.0"}
(fact "styles the text according to the modifiers"

  (style "hello" [:bold :red])
  => "[1;31mhello[0m")

^{:refer std.print.ansi/style:remove :added "3.0"}
(fact "removes ansi formatting"

  (style:remove "[22;47;34m Hello [0m")
  => string?)

^{:refer std.print.ansi/define-ansi-forms :added "3.0"}
(fact "defines ansi forms given by the lookups"

  ;; Text:
  ;; [blue cyan green grey magenta red white yellow]

  (blue "hello")
  => "[34mhello[0m"

  ;; Background:
  ;; [on-blue on-cyan on-green on-grey
  ;;  on-magenta on-red on-white on-yellow]

  (on-white "hello")
  => "[47mhello[0m"

  ;; Attributes:
  ;; [blink bold concealed dark reverse-color underline]

  (blink "hello")
  => "[5mhello[0m")
