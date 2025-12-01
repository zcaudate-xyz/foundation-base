(ns std.vm.forth-interpreter-test
  (:use code.test)
  (:require [std.vm.forth-interpreter :as forth]
            [std.block.parse :as parse]
            [std.block.construct :as construct]
            [std.lib.zip :as zip]))


^{:refer std.vm.forth-interpreter/block-zip :added "4.0"}
(fact "creates a zipper for the block"
  (let [root (parse/parse-string "[ 1 2 ]")]
    (forth/block-zip root) => map?))

^{:refer std.vm.forth-interpreter/make-vm :added "4.0"}
(fact "creates a VM from a root block"
  (let [root (parse/parse-string "[ 1 2 ]")
        vm (forth/make-vm root)]
    (:stack vm) => []
    (:code-zip vm) => map?))

^{:refer std.vm.forth-interpreter/clear-screen :added "4.0"}
(fact "clears the screen"
  (forth/clear-screen) => any)

^{:refer std.vm.forth-interpreter/visualize :added "4.0"}
(fact "visualizes the current state"
  (let [root (parse/parse-string "[ 1 2 ]")
        vm (forth/make-vm root)]
    (forth/visualize vm) => any))

^{:refer std.vm.forth-interpreter/push :added "4.0"}
(fact "pushes a value onto the stack"
  (let [root (parse/parse-string "[]")
        vm (forth/make-vm root)]
    (:stack (forth/push vm 10)) => [10]))

^{:refer std.vm.forth-interpreter/pop-stack :added "4.0"}
(fact "pops a value from the stack"
  (let [root (parse/parse-string "[]")
        vm (-> (forth/make-vm root)
               (forth/push 10)
               (forth/push 20))]
    (first (forth/pop-stack vm)) => 20
    (:stack (second (forth/pop-stack vm))) => [10]))

^{:refer std.vm.forth-interpreter/binary-op :added "4.0"}
(fact "performs a binary operation"
  (let [root (parse/parse-string "[]")
        vm (-> (forth/make-vm root)
               (forth/push 10)
               (forth/push 20))]
    (:stack (forth/binary-op vm +)) => [30]))

^{:refer std.vm.forth-interpreter/unary-op :added "4.0"}
(fact "performs a unary operation"
  (let [root (parse/parse-string "[]")
        vm (-> (forth/make-vm root)
               (forth/push 10))]
    (:stack (forth/unary-op vm inc)) => [11]))

^{:refer std.vm.forth-interpreter/run-block :added "4.0"}
(fact "executes a block"
  (let [root (parse/parse-string "[]")
        vm (-> (forth/make-vm root)
               (forth/push 10)
               (forth/push 20))
        block (parse/parse-string "[ + ]")]
    (:stack (forth/run-block vm block)) => [30]))

^{:refer std.vm.forth-interpreter/exec-word :added "4.0"}
(fact "executes a word"
  (let [root (parse/parse-string "[]")
        vm (-> (forth/make-vm root)
               (forth/push 10)
               (forth/push 20))]
    (:stack (forth/exec-word vm '+)) => [30]))

^{:refer std.vm.forth-interpreter/step :added "4.0"}
(fact "steps through the code"
  (let [root (construct/block (construct/container :vector
                                                   [(construct/token 10)
                                                    (construct/token 20)
                                                    (construct/token '+)]))
        vm (forth/make-vm root)
        vm1 (forth/step vm)  ;; push 10
        vm2 (forth/step vm1) ;; push 20
        vm3 (forth/step vm2)] ;; +
    (:stack vm1) => [10]
    (:stack vm2) => [10 20]
    (:stack vm3) => [30]))

^{:refer std.vm.forth-interpreter/animate :added "4.0"}
(fact "animates execution"
  (forth/animate "[ 1 2 + ]" 0) => any)

(defn demo []
  (println "Starting Forth Demo...")
  (Thread/sleep 1000)
  
  ;; Arithmetic
  (forth/animate "[ 10 20 + 5 * . ]" 500)
  (Thread/sleep 1000)

  ;; Condition
  (forth/animate "[ 10 5 > [ \"Yes\" . ] [ \"No\" . ] if ]" 500))
