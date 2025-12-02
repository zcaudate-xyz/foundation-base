(ns std.vm.llvm-interpreter-test
  (:use code.test)
  (:require [std.vm.llvm-interpreter :as llvm]
            [std.block.parse :as parse]
            [std.lib.zip :as zip]
            [std.lib :as h]
            [std.string :as str]))

^{:refer std.vm.llvm-interpreter/block-zip :added "4.0"}
(fact "creates a zipper for the block"
  (llvm/block-zip (parse/parse-string "(a b c)"))
  => zip/zipper?)

^{:refer std.vm.llvm-interpreter/clear-screen :added "4.0"}
(fact "clears the screen"
  (h/with-out-str
    (llvm/clear-screen))
  => "\u001b[2J\u001b[H")

^{:refer std.vm.llvm-interpreter/highlight :added "4.0"}
(fact "highlights the current node in the zipper"
  (let [z (llvm/block-zip (parse/parse-string "(a b c)"))
        z (llvm/highlight z)]
    (pr-str (zip/right-element z)) => (fn [s] (str/includes? s "a"))))

^{:refer std.vm.llvm-interpreter/block-val :added "4.0"}
(fact "gets the value of a block"
  (llvm/block-val (parse/parse-string "a"))
  => 'a)

^{:refer std.vm.llvm-interpreter/instruction? :added "4.0"}
(fact "checks if a block is an instruction"
  (llvm/instruction? (parse/parse-string "[add i32 1 2]"))
  => true

  (llvm/instruction? (parse/parse-string "(add i32 1 2)"))
  => true

  (llvm/instruction? (parse/parse-string "add"))
  => false)

^{:refer std.vm.llvm-interpreter/get-instructions :added "4.0"}
(fact "gets instructions from a block"
  (count (llvm/get-instructions (parse/parse-string "(label [add i32 1 2])")))
  => 1)

^{:refer std.vm.llvm-interpreter/make-vm :added "4.0"}
(fact "creates a VM state from root code"
  (let [code "(define @main (i32)
                (entry
                  [ret i32 0]))"
        vm (llvm/make-vm (parse/parse-string code))]
    (:ip vm) => {:block 'entry :idx 0}
    (keys (:block-map vm)) => '(entry)))

^{:refer std.vm.llvm-interpreter/resolve-val :added "4.0"}
(fact "resolves a value from the VM"
  (let [vm {:registers {'%a 10}}]
    (llvm/resolve-val vm '%a) => 10
    (llvm/resolve-val vm 5) => 5
    (llvm/resolve-val vm :key) => :key))

^{:refer std.vm.llvm-interpreter/update-reg :added "4.0"}
(fact "updates a register in the VM"
  (let [vm {}
        vm (llvm/update-reg vm '%a 10)]
    (get-in vm [:registers '%a]) => 10))

^{:refer std.vm.llvm-interpreter/jump :added "4.0"}
(fact "jumps to a label"
  (let [vm {:ip {:block 'entry :idx 0}}
        vm (llvm/jump vm 'loop)]
    (:ip vm) => {:block 'loop :idx 0}
    (:prev-block vm) => 'entry))

^{:refer std.vm.llvm-interpreter/next-inst :added "4.0"}
(fact "advances to the next instruction"
  (let [vm {:ip {:block 'entry :idx 0}}
        vm (llvm/next-inst vm)]
    (:ip vm) => {:block 'entry :idx 1}))

^{:refer std.vm.llvm-interpreter/exec-inst :added "4.0"}
(fact "executes an instruction"
  (let [vm {:registers {'%a 10 '%b 20} :ip {:block 'entry :idx 0}}
        inst (parse/parse-string "[%res = add i32 %a %b]")
        vm (llvm/exec-inst vm inst)]
    (get-in vm [:registers '%res]) => 30
    (:ip vm) => {:block 'entry :idx 1})

  (let [vm {:registers {'%a 10 '%b 20} :ip {:block 'entry :idx 0}}
        inst (parse/parse-string "[%res = sub i32 %b %a]")
        vm (llvm/exec-inst vm inst)]
    (get-in vm [:registers '%res]) => 10)

  (let [vm {:registers {'%a 10 '%b 20} :ip {:block 'entry :idx 0}}
        inst (parse/parse-string "[%res = mul i32 %a %b]")
        vm (llvm/exec-inst vm inst)]
    (get-in vm [:registers '%res]) => 200)

  (let [vm {:registers {'%a 10 '%b 10} :ip {:block 'entry :idx 0}}
        inst (parse/parse-string "[%res = icmp eq i32 %a %b]")
        vm (llvm/exec-inst vm inst)]
    (get-in vm [:registers '%res]) => true)

  (let [vm {:registers {'%cond true} :ip {:block 'entry :idx 0}}
        inst (parse/parse-string "[br i1 %cond label1 label2]")
        vm (llvm/exec-inst vm inst)]
    (:ip vm) => {:block 'label1 :idx 0})
    
  (let [vm {:registers {'%cond false} :ip {:block 'entry :idx 0}}
        inst (parse/parse-string "[br i1 %cond label1 label2]")
        vm (llvm/exec-inst vm inst)]
    (:ip vm) => {:block 'label2 :idx 0})

  (let [vm {:ip {:block 'entry :idx 0}}
        inst (parse/parse-string "[br label1]")
        vm (llvm/exec-inst vm inst)]
    (:ip vm) => {:block 'label1 :idx 0})
    
  (let [vm {:ip {:block 'entry :idx 0}}
        inst (parse/parse-string "[ret i32 0]")
        vm (llvm/exec-inst vm inst)]
    (:return-val vm) => 0
    (:ip vm) => nil))

^{:refer std.vm.llvm-interpreter/get-current-inst-block :added "4.0"}
(fact "gets the current instruction block"
  (let [vm {:ip {:block 'entry :idx 0}
            :block-map {'entry [(parse/parse-string "[ret i32 0]")]}}]
    (str (llvm/get-current-inst-block vm)) => "[ret i32 0]"))

^{:refer std.vm.llvm-interpreter/highlight-ip :added "4.0"}
(fact "highlights the current instruction pointer"
  (let [code "(define @main (i32)
                (entry
                  [ret i32 0]))"
        vm (llvm/make-vm (parse/parse-string code))
        z (llvm/highlight-ip vm)]
    (pr-str (zip/right-element z)) => (fn [s] (str/includes? s "ret"))))

^{:refer std.vm.llvm-interpreter/visualize :added "4.0"}
(fact "visualizes the VM state"
  (let [code "(define @main (i32)
                (entry
                  [ret i32 0]))"
        vm (llvm/make-vm (parse/parse-string code))]
    (h/with-out-str (llvm/visualize vm))
    => (fn [s] (str/includes? s "Registers"))))

^{:refer std.vm.llvm-interpreter/animate :added "4.0"}
(fact "animates the execution"
  (let [code "(define @main (i32)
                (entry
                  [ret i32 0]))"]
    (h/with-out-str (llvm/animate code 0))
    => (contains "Finished")))


(comment

  
(defn -main []
  (println "Starting LLVM Demo...")
  (Thread/sleep 1000)

  (llvm/animate "
(define @main (i32)
  (entry
    [%cnt = add i32 0 5]
    [br loop])

  (loop
    [%cond = icmp eq i32 %cnt 0]
    [br i1 %cond exit body])

  (body
    [%cnt = sub i32 %cnt 1]
    [br loop])

  (exit
    [ret i32 %cnt]))
" 500))
)


^{:refer std.vm.llvm-interpreter/filter-valid :added "4.1"}
(fact "TODO")
