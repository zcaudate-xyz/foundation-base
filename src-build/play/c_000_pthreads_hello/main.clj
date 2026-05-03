(ns play.c-000-pthreads-hello.main
  (:require [hara.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :c
  {:import [["<stdio.h>"]
            ["<stdlib.h>"]
            ["<pthread.h>"]]})

(defn.c ^{:- [:void :*]}
  print-message-function
  ([:void :* ptr]
   (var :char :* message (:char :* ptr))
   (printf "%s \n" message)))

(defn.c ^{:- [:int]}
  main
  ([]
   (var :pthread_t thread1 thread2)
   (var :char :* message1 := "Thread 1")
   (var :char :* message2 := "Thread 2")
   (var :int iret1 iret2)

   (:= iret1 (pthread-create (:& thread1) NULL print-message-function (:void :* message1)))
   (:= iret2 (pthread-create (:& thread2) NULL print-message-function (:void :* message2)))

   (pthread-join thread1 NULL)
   (pthread-join thread2 NULL)
   
   (printf "Thread 1 returns: %d\n", iret1)
   (printf "Thread 2 returns: %d\n", iret2)

   (exit 0)))
