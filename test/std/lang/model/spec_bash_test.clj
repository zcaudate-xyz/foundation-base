(ns std.lang.model.spec-bash-test
  (:use code.test)
  (:require [std.lang.model.spec-bash :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

(l/script- :bash
  {:runtime :oneshot
   :layout :flat})

^{:refer std.lang.model-spec-bash/CANARY :guard true :adopt true :added "3.0"}
(fact "PRELIM CHECKS"

  (!.sh [echo #{"'hello'"}])
  => [0 ["'hello'"]]
  
  (!.sh [echo "1+1" | bc])
  => [0 ["2"]]

  (!.sh [pwd])
  => (contains-in [0 [string?]])
  
  (!.sh [ls "-a" "-l"])
  => (contains-in [0 [string?]]))

^{:refer std.lang.model.spec-bash/PLAYGROUND :adopt true :added "4.0"
  :setup [(std.lang.interface.type-notify/clear-sink (l/default-notify)
                                                      "test")]}
(fact "various features"
  ^:hidden
  
  ;; NOTIFY 
  (do (def +p+ (std.lang.interface.type-notify/watch-oneshot (l/default-notify)
                                                             100
                                                             "test"))
      
      (!.sh
       (do (exec (:% 3 <> "/dev/tcp/127.0.0.1/" (@! (:socket-port (l/default-notify)))))
           (echo "'{\"id\": \"test\"}'"
                 (:% 1 >&3))
           (exec (:% 3 <&-)))))
  => [0 []]
  
  @(second +p+)
  => {"id" "test"}
  
  ;; IF
  (!.sh (:= name #{"George"})
        (if [(== (:$ name)
                 #{"George"})]
          (echo "Bonjour," (:$ name))))
  => [0 ["Bonjour, George"]]
  
  ;; Case statements
  (!.sh (defn greet [name]
          (case (:$ name)
            Ge* (echo "Hello,"   (:$ name))
            *   (echo "Bonjour," (:$ name))))
        (greet "George")
        (greet "Frank"))
  => [0 ["Hello, George"
         "Bonjour, Frank"]]

  ;; Brace Expansion
  (!.sh
   (echo '['[A..K] '[1 3]]))
  => [0 ["A B C D E F G H I J K 1 3"]]
  

  (!.sh
   (echo (:% '[A..K] '[1 3])))
  => [0 ["A1 A3 B1 B3 C1 C3 D1 D3 E1 E3 F1 F3 G1 G3 H1 H3 I1 I3 J1 J3 K1 K3"]]

  (!.sh
   (echo (:% '[1 3]
             '[1 3]
             '[1 3])))
  => [0 ["111 113 131 133 311 313 331 333"]]

  ;; For LOOP
  (!.sh
    (for [file :in ($ (ls "*.clj"))]
      (echo (:$ file))))
  => [0 ["project.clj"]]
  
  (!.sh
   (defn countdown []
     (local -i (:= start $1))  
     (while (:test [0 :lt $start])
       (:= start (- $start 1))
       (echo $start)))
   (echo (countdown 10)))
  => [0 ["9 8 7 6 5 4 3 2 1 0"]]

  ;; AND FLOW
  (!.sh
   (&& (echo #{"hello"})
       (echo #{"world"})
       (echo #{"again"})))
  => [0 ["hello"
         "world"
         "again"]]

  ;; AND FLOW
  (!.sh
   (|| (echo #{"hello"})
       (echo #{"world"})
       (echo #{"again"})))
  => [0 ["hello"]]

  (!.sh
   (|| (:test [1 :ne 1])
       (echo #{"world"})
       (echo #{"again"})))
  => [0 ["world"]])

^{:refer std.lang.model.spec-bash/bash-quote-item :added "4.0"}
(fact "quotes an item"
  ^:hidden
  
  (bash-quote-item "hello" +grammar+ {})
  => "\"\\\"hello\\\"\"")

^{:refer std.lang.model.spec-bash/bash-set :added "4.0"}
(fact "quotes a string value"
  ^:hidden
  
  (bash-set #{"hello"} +grammar+ {})
  => "\"\\\"hello\\\"\""
  
  (!.sh (echo #{"hello"}))
  => [0 ["hello"]])

^{:refer std.lang.model.spec-bash/bash-quote :added "4.0"}
(fact "quotes a string value"
  ^:hidden
  
  (bash-quote '(quote "hello") +grammar+ {})
  => "\"\\\"hello\\\"\""
  
  (!.sh (echo '"hello"))
  => [0 ["hello"]])

^{:refer std.lang.model.spec-bash/bash-dash-param :added "4.0"}
(fact "if keyword, replace `:` with `-`"
  ^:hidden
  
  (bash-dash-param :hello)
  => "-hello")

^{:refer std.lang.model.spec-bash/bash-map :added "4.0"}
(fact "outputs a map"
  ^:hidden
  
  (bash-map {:hello 1
             :world "hello"}
            +grammar+ {})
  => "\"-hello\" 1 \"-world\" \"hello\""
  
  (l/emit-as
   :bash '[[(echo {:e true} #{"ibase=16;FFFF"})
            | (bc)]])
  => "echo -e \"ibase=16;FFFF\" | bc"
  
  (!.sh [(echo {:e true} #{"ibase=16;FFFF"})
         | (bc)])
  => [0 ["65535"]])

^{:refer std.lang.model.spec-bash/bash-invoke :added "4.0"}
(fact "outputs an invocation (same as vector)"
  ^:hidden
  
  (bash-invoke '(ls {:l true}) +grammar+ {})
  => "ls {:l true}"

  (bash-invoke '(echo (ls)) +grammar+ {})
  => "echo ($ (ls))"
  
  (l/emit-as
   :bash '[(expr (- 1 3))])
  => "expr 1 - 3"

  (l/emit-as
   :bash '[(echo (expr 1 + 2)
                 (expr (+ 1 2)))])
  => "echo $(expr 1 + 2) $(expr 1 + 2)"
  
  (!.sh (echo (expr 1 + 2)
              (expr (+ 1 2))))
  => [0 ["3 3"]]
  
  (!.sh (expr (- 1 3)))
  => [0 ["-2"]])

^{:refer std.lang.model.spec-bash/bash-assign :added "4.0"}
(fact "outputs an assignment"
  ^:hidden
  
  (bash-assign '(:= a 1) +grammar+ {})
  => "a=1")

^{:refer std.lang.model.spec-bash/bash-var :added "4.0"}
(fact "transforms to var"
  ^:hidden
  
  (bash-var '(var a 1))
  => '(:- :local (:= a 1))
  
  (l/emit-as
   :bash '[(defn hello []
             (var a 1)
             (echo (:$ a)))
           (hello)])
  => (std.string/|
      "function hello(){"
      "  local a=1"
      "  echo ${a}"
      "}"
      ""
      "hello")

  (!.sh (defn hello []
          (var a 1)
          (echo (:$ a)))
        (hello))
  => [0 ["1"]])

^{:refer std.lang.model.spec-bash/bash-defn :added "4.0"}
(fact "transforms a function to allow for inputs"
  ^:hidden

  (bash-defn '(defn hello [a b]
                (echo (:$ a) (:$ b))))
  => '(defn- hello []
        (var a (:$ 1))
        (var b (:$ 2))
        (echo (:$ a) (:$ b)))

  (l/emit-as
   :bash '[(defn hello [a b]
             (echo (:$ a) (:$ b)))
           (hello (expr (+ 1 2))
                  2)])
  (std.string/|
   "function hello(){"
   "  local a=${1}"
   "  local b=${2}"
   "  echo ${a} ${b}"
   "}"
   ""
   "hello $(expr 1 + 2) 2")
  
  (!.sh (defn hello [a b]
          (echo (:$ a) (:$ b)))
        (hello (expr (+ 1 2))
               2))
  => [0 ["3 2"]])

^{:refer std.lang.model.spec-bash/bash-defn- :added "4.0"}
(fact "emits a non paramerised version")

^{:refer std.lang.model.spec-bash/bash-expand :added "4.0"}
(fact "brace expansion syntax"
  ^:hidden
  
  (bash-expand '(:$ hello))
  => '(:% "${" (% hello) "}")

  (!.sh (echo (:$ BASH_VERSION)))
  => (contains-in [0 [string?]]))

^{:refer std.lang.model.spec-bash/bash-subshell :added "4.0"}
(fact "brace subshell syntax"
  ^:hidden
  
  (bash-subshell '($ hello))
  => '(:% "$(" (% hello) ")")

  (!.sh (echo ($ (expr (+ 1 2 3)))))
  => [0 ["6"]])

^{:refer std.lang.model.spec-bash/bash-test :added "4.0"}
(fact "constructs test syntax"
  ^:hidden
  
  (bash-test '(:test (1 -ne 10)))
  => '(:% "[[ " (% [1 -ne 10]) " ]]")

  (l/emit-as
   :bash '[(:test (1 :ne 10))])
  => "[[ 1 -ne 10 ]]"
  
  (!.sh (if (:test (1 :ne 1))
          (echo "hello")
          (echo "world")))
  => [0 ["world"]])

^{:refer std.lang.model.spec-bash/bash-pipeleft :added "4.0"}
(fact "constructs pipeleft syntax"
  ^:hidden
  
  (bash-pipeleft '(echo (<$ (echo "hello"))))
  => '(:% "<(" (% (<$ (echo "hello"))) ")")
  
  (l/emit-as
   :bash '[(echo (<$ (echo #{"hello"})))])
  => "echo <(echo \"hello\")"

  (!.sh
   (echo (<$ (echo #{"hello"}))))
  => (contains-in [0 [#"/dev/fd"]]))

^{:refer std.lang.model.spec-bash/bash-piperight :added "4.0"}
(fact "constructs pipeleft syntax"
  ^:hidden
  
  (bash-piperight '($> (echo "hello")))
  => '(:% ">(" (% (echo "hello")) ")")
  
  (l/emit-as
   :bash '[($> "3&")])
  => ">(3&)"
  
  (l/emit-as
   :bash '[[(echo #{"hello"}) $> "/dev/null"]])
  => "echo \"hello\" > /dev/null"
  
  (!.sh
   [(echo #{"hello"}) $> "/dev/null"])
  => [0 []])

^{:refer std.lang.model.spec-bash/bash-here :added "4.0"}
(fact "construct here block and here string"
  ^:hidden
  
  (l/emit-as
   :bash '[(cat (<< :EOF
                    ["input"
                     "on multiple lines"]))])
  => (std.string/|
      "cat <<EOF"
      "input"
      "on multiple lines"
      "EOF")
  
  ;; HERE BLOCK
  (!.sh (cat (<< :EOF
                 ["input"
                  "on multiple lines"])))
  => [0 ["input" "on multiple lines"]]

  ;; HERE STRING
  (!.sh (cat (<< "HERE IS INPUT")))
  => [0 ["HERE IS INPUT"]])

^{:refer std.lang.model.spec-bash/bash-check-fn :added "4.0"}
(fact "custom check for vectors"
  ^:hidden
  
  (bash-check-fn [1 :ne 10])
  => '(:test [1 :ne 10])
  
  (bash-check-fn '(hello world))
  => '(hello world))

^{:refer std.lang.model.spec-bash/bash-when :added "4.0"}
(fact "when support for custom vectors"
  ^:hidden
  
  (l/emit-as
   :bash '[(when true
             (echo "hello"))])
  => "if true; then\n  echo hello\nfi"

  (!.sh (when true
          (echo "hello")))
  => [0 ["hello"]]
  
  (l/emit-as
   :bash '[(when [true]
             (echo "hello"))])
  => "if [[ true ]]; then\n  echo hello\nfi"
  
  (!.sh (when [true]
          (echo "hello")
          (echo "hello")))
  => [0 ["hello" "hello"]])

^{:refer std.lang.model.spec-bash/bash-if :added "4.0"}
(fact "if support for custom vectors")

^{:refer std.lang.model.spec-bash/bash-cond :added "4.0"}
(fact "cond support for custom vectors"
  ^:hidden
  
  (l/emit-as
   :bash '[(cond [1 :ne 1]
                 (echo "hello")

                 [2 :ne 2]
                 (echo "world")

                 :else
                 (echo "again"))])
  => (std.string/|
      "if [[ 1 -ne 1 ]]; then"
      "  echo hello"
      "elif [[ 2 -ne 2 ]]; then"
      "  echo world"
      "else"
      "  echo again"
      "fi")

  (!.sh (cond [1 :ne 1]
                 (echo "hello")

                 [2 :ne 2]
                 (echo "world")

                 :else
                 (echo "again")))
  => [0 ["again"]])
