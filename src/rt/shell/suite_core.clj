^{:no-test true}
(ns rt.shell.suite-core
  (:require [std.lang :as l :refer [defmacro.!]]
            [std.lib.env :as env]
            [std.lib.future :as future]
            [std.lib.template :as template]
            [std.string.prose :as prose]
            [xt.lang.base-notify :as notify])
  (:refer-clojure :exclude [if cat]))

(l/script :bash
  rt.shell
  {:runtime :oneshot})

(defmacro emit
  "emits as script"
  {:added "4.0"}
  [& body]
  (l/emit-script (apply list 'do body)
                 {:lang :bash}))

(defmacro.! ^{:static/lang :bash
              :static/wrap 'env/pl}
  man
  "gets the manual for a given command"
  {:added "4.0"}
  [& args]
  (apply list 'man args))

(defmacro.! ^{:static/lang :bash
              :static/wrap 'env/pl}
  apropos
  "gets commands for a given text"
  {:added "4.0"}
  [& args]
  (apply list 'apropos args))

(defmacro.! ^{:static/lang :bash}
  echo
  "echo command"
  {:added "4.0"}
  [& args]
  (apply list 'echo args))

(defmacro.! ^{:static/lang :bash}
  pwd
  "current working directory"
  {:added "4.0"}
  [& args]
  (apply list 'pwd args))

(defmacro.! ^{:static/lang :bash}
  nc
  "command for netcat"
  {:added "4.0"}
  [& args]
  (apply list 'nc args))

(defmacro.! ^{:static/lang :bash}
  >>
  "pipe operator"
  {:added "4.0"}
  [& args]
  (apply list '>> args))

(defmacro.! ^{:static/lang :bash}
  ls
  "directory listing"
  {:added "4.0"}
  [& args] (apply list 'ls args))

(defmacro.! ^{:static/lang :bash
              :style/indent 1}
  if
  "branch operator"
  {:added "4.0"}
  [& args] (apply list 'if args))

(defmacro.! ^{:static/lang :bash}
  !
  "runs a base command"
  {:added "4.0"}
  [& args] (apply list 'do args))

(defmacro.! ^{:static/lang :bash}
  cat
  "cat operator"
  {:added "4.0"}
  [& args] (apply list 'cat args))

(defmacro.sh
  nc:port-check
  "checks if port is available"
  {:added "4.0"}
  [port & [host]]
  (list 'nc '-zv (or host "127.0.0.1") port))

(defmacro.sh notify-form
  "notifiy form"
  {:added "4.0"}
  [id port form]
  (let [address (str "/dev/tcp/127.0.0.1/" port)
        fstr  (str "'{id: \"" id  "\", type: \"raw\", value:.}'")]
    (template/$ (do (exec (:% 3 <> ~address))
             [~form
              | (jq {:R true
                     :s true}
                    ~fstr)
              (:% >& 3)]
             (exec (:% 3 <&-))))))

(defmacro.! ^{:static/lang :bash
              :style/indent 1}
  notify
  "oneshots the notify server"
  {:added "4.0"}
  [form]
  (let [{:keys [runtime]} (:emit (l/macro-opts))
        id notify/*override-id*
        port (:socket-port (l/default-notify))]
    ((:template @notify-form) id port form)))

(comment

  (l/with:print
      (!
       (:= my_array (:- "(foo bar)"))
       (echo (:$ (. my_array [1])))))
  
  
   
  (!
   (:= PERSON #{"'hello'"})
   (echo $PERSON))


  
  (!.sh
   (>> (echo #{"[1,3,2]"})
       (jq #{"def inc(x): x + 1 \n; inc(inc(.[0]))"})))
  
  

  (! (echo "hello")
     (echo "hello"))
  
  (! (echo "hello")
     (echo (@.c "hello")))
  
  (>> (echo hello)
      (cat)))

(comment

  (!.sh
   (@.c "hello"))
  (echo (!:uuid))
  
  
  @(future/on:all
    [(future/future
       (>> (echo (@.c "hello"))))
     (future/future
       (>> (echo (@.c "hello"))))])
  
  (>> (echo '(@.c
              (fn main []
                (puts "Hello World")
                (return 0))))
      (tcc -run -))

  (>> (echo hello))
  (h)
  
  (>> (echo '(@.lua
              (print (+ 1 2 3))))
      (luajit -))


  (>> (echo '(@.lua
              (-%%-
               [" -- defines a factorial function"
                "    function fact (n)"
                "      if n == 0 then"
                "        return 1"
                "      else"
                "        return n * fact(n-1)"
                "      end"
                "    end"
                "print(fact(10))"])))
      (luajit -))

  (l/with:print
      (>> (echo '(@.lua
                  (do (fn fact [n]
                        (if (== n 0)
                          (return 1)
                          (return (* n (fact (- n 1))))))
                      (print (fact 10)))))
          (luajit -)))
  
  
  
  
  (l/with:print-all
   (>> (echo '(@.python
               (print (+ 1 2 3))
               (print (+ 1 2 3))))
       (python3 -)))

  (>> (echo '(@.js
              (console.log (+ 1 2 3))))
      (node))
  
  '()
  
  
  (!.sh
   (-> '(@! (prose/|
             "\\# Header"
               ".nf"
               ".mk a"
               "Sometown, Earth"
               "vs@johndoethetrueone.com.invalid"
               ".br"
               ".rt \\nau"
               ".ps +16p"
               ".ce 1"
               ".sp +10p"
               "John \"The Doe\" Doe"
               ".ps"
               ".rt \\nau"
               ".rj 2"
               "linkedin.com/in/j.doe"
               "github.com/j.doe"
               ".fi"))
       (echo)
       (>> (groff -ms #_#_:> out.ps)))
   #_(open out.ps))


  

  (>> (echo '(@! (prose/|
                  "; ----------------------------------------------------------------------------------------"
                  "; Writes \"Hello, World\" to the console using only system calls. Runs on 64-bit Linux only."
                  "; To assemble and run:"
                  ";"
                  ";     nasm -felf64 hello.asm && ld hello.o && ./a.out"
                  "; ----------------------------------------------------------------------------------------"
                  ""
                  "          global    _start"
                  ""
                  "          section   .text"
                  "_start:   mov       rax, 1                  ; system call for write"
                  "          mov       rdi, 1                  ; file handle 1 is stdout"
                  "          mov       rsi, message            ; address of string to output"
                  "          mov       rdx, 13                 ; number of bytes"
                  "          syscall                           ; invoke operating system to do the write"
                  "          mov       rax, 60                 ; system call for exit"
                  "          xor       rdi, rdi                ; exit code 0"
                  "          syscall                           ; invoke operating system to exit"
                  ""
                  "          section   .data"
                  "message:  db        \"Hello, World\", 10      ; note the newline at the end")))
      (nasm -felf64)) 
  
  
  
  (l/with:print-all
   (!.sh
    '(man)))
  
  (comment
    (l/rt:restart)
    (l/reset-annex)
    (help
     (echo hello world)
     (man:ptr 123)
     (apropos man))

    (comment (man "man"))))

#_(man:ptr)
