(ns std.vm.jvm-interpreter-test
  (:use code.test)
  (:require [std.vm.jvm-interpreter :as jvm]
            [std.block.parse :as parse]
            [std.block.construct :as construct]
            [std.block.base :as base]
            [std.lib.zip :as zip]))

(defn demo []
  (println "Starting JVM Demo...")
  (Thread/sleep 1000)

  (jvm/animate "
(class Factorial
  (method main
    [
     [iconst_5]
     [istore_1]  ;; n = 5
     [iconst_1]
     [istore_2]  ;; res = 1

     ;; Loop Unrolled for Toy Demo (no labels/goto yet)
     [iload_2]
     [iload_1]
     [imul]
     [istore_2]  ;; res = res * n

     [iload_1]
     [iconst_1]
     [isub]
     [istore_1]  ;; n = n - 1

     [iload_2]
     [iload_1]
     [imul]
     [istore_2]  ;; res = res * n

     [iload_2]
     [ireturn]
    ]))" 500))


^{:refer std.vm.jvm-interpreter/block-zip :added "4.0"}
(fact "creates a zipper for JVM structure"
  (let [root (parse/parse-string "[ 1 2 ]")]
    (jvm/block-zip root) => map?))

^{:refer std.vm.jvm-interpreter/clear-screen :added "4.0"}
(fact "clears the screen"
  (jvm/clear-screen) => any)

^{:refer std.vm.jvm-interpreter/highlight :added "4.0"}
(fact "highlights the current instruction"
  (let [root (parse/parse-string "[ 1 2 ]")
        z (jvm/block-zip root)
        z (if (zip/can-step-inside? z) (zip/step-inside z) z)]
    (jvm/highlight z) => map?))

^{:refer std.vm.jvm-interpreter/block-val :added "4.0"}
(fact "extracts value from a block"
  (jvm/block-val (construct/token 10)) => 10
  (jvm/block-val 10) => 10)

^{:refer std.vm.jvm-interpreter/filter-valid :added "4.0"}
(fact "filters out whitespace and comments"
  (let [block (construct/container :vector
                                   [(construct/token 1)
                                    (construct/space)
                                    (construct/token 2)])]
    (count (jvm/filter-valid block)) => 2))

^{:refer std.vm.jvm-interpreter/make-jvm :added "4.0"}
(fact "creates a JVM instance"
  (let [code "(class Test (method main []))"
        root (parse/parse-string code)
        vm (jvm/make-jvm root)]
    (:classes vm) => (contains {'main base/block?})
    (:frames vm) => empty?))

^{:refer std.vm.jvm-interpreter/push-frame :added "4.0"}
(fact "pushes a new stack frame"
  (let [code "(class Test (method main [[iconst_1]]))"
        root (parse/parse-string code)
        vm (jvm/make-jvm root)
        vm (jvm/push-frame vm 'main [])]
    (count (:frames vm)) => 1
    (:method-name (jvm/current-frame vm)) => 'main))

^{:refer std.vm.jvm-interpreter/pop-frame :added "4.0"}
(fact "pops the current stack frame"
  (let [code "(class Test (method main []))"
        root (parse/parse-string code)
        vm (jvm/make-jvm root)
        vm (-> vm (jvm/push-frame 'main []) jvm/pop-frame)]
    (:frames vm) => empty?))

^{:refer std.vm.jvm-interpreter/current-frame :added "4.0"}
(fact "gets the current frame"
  (let [code "(class Test (method main []))"
        root (parse/parse-string code)
        vm (-> (jvm/make-jvm root)
               (jvm/push-frame 'main []))]
    (jvm/current-frame vm) => map?))

^{:refer std.vm.jvm-interpreter/update-current-frame :added "4.0"}
(fact "updates the current frame"
  (let [code "(class Test (method main []))"
        root (parse/parse-string code)
        vm (-> (jvm/make-jvm root)
               (jvm/push-frame 'main []))
        vm (jvm/update-current-frame vm (fn [f] (assoc f :stack [1])))]
    (:stack (jvm/current-frame vm)) => [1]))

^{:refer std.vm.jvm-interpreter/exec-inst :added "4.0"}
(fact "executes an instruction"
  (let [code "(class Test (method main [[iconst_5]]))"
        root (parse/parse-string code)
        vm (-> (jvm/make-jvm root)
               (jvm/push-frame 'main []))
        ;; Use parse-string to ensure structure matches what VM expects
        inst (parse/parse-string "[iconst_5]")
        vm (jvm/exec-inst vm inst)]
    (:stack (jvm/current-frame vm)) => [5]))

^{:refer std.vm.jvm-interpreter/visualize :added "4.0"}
(fact "visualizes the VM state"
  (let [code "(class Test (method main []))"
        root (parse/parse-string code)
        vm (-> (jvm/make-jvm root)
               (jvm/push-frame 'main []))]
    (jvm/visualize vm) => any))

^{:refer std.vm.jvm-interpreter/animate :added "4.0"}
(fact "animates execution"
  (jvm/animate "(class T (method main [[iconst_1]]))" 0) => any)
