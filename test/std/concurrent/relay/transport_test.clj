(ns std.concurrent.relay.transport-test
  (:require [std.concurrent.relay.transport :as t]
            [std.lib.foundation]
            [std.lib.future])
  (:use code.test))

^{:refer std.concurrent.relay.transport/bytes-output :added "4.0"}
(fact "creates a byte output stream"
  ^:hidden
  
  (t/bytes-output)
  => java.io.ByteArrayOutputStream)

^{:refer std.concurrent.relay.transport/mock-input-stream :added "4.0"}
(fact "creates a mock input stream")

^{:refer std.concurrent.relay.transport/read-bytes-limit :added "4.0"
  :setup [(def +stream+ (t/mock-input-stream
                         (atom ["OK "
                                "Hello\n"
                                "World\n"])))
          (def +state+  (volatile! []))]}
(fact "reads a limited number of bytes from stream"
  ^:hidden
  
  (dotimes [i 2]
    (t/read-bytes-limit {:raw +stream+}
                       (fn [v]
                         (vswap! +state+ conj (std.lib.foundation/string v)))
                       identity
                       2
                       10
                       10))
  @+state+
  => ["OK" " H"])

^{:refer std.concurrent.relay.transport/read-bytes-line :added "4.0"
  :setup [(def +stream+ (t/mock-input-stream
                         (atom ["OK "
                                "Hello\n"
                                "World\n"])))
          (def +state+  (volatile! []))]}
(fact "reads individual lines from stream"
  ^:hidden
  
  (dotimes [i 2]
    (t/read-bytes-line {:raw +stream+}
                       (fn [v]
                         (vswap! +state+ conj (std.lib.foundation/string v)))
                       identity
                       10
                       10))
  
  @+state+
  => ["OK Hello\n" "World\n"])

^{:refer std.concurrent.relay.transport/read-bytes-some :added "4.0"
  :setup [(def +stream+ (t/mock-input-stream
                         (atom ["OK "
                                "Hello\n"
                                "World\n"])))
          (def +state+  (volatile! []))]}
(fact "reads until timeout"
  ^:hidden
  
  (t/read-bytes-some {:raw +stream+}
                     (fn [v]
                       (vswap! +state+ conj (std.lib.foundation/string v)))
                     10
                     10)
  @+state+
  => ["OK "
      "Hello\n"
      "World\n"]

  (do (def +state+  (volatile! []))
      (def +input+ (atom ["OK " "Hello "]))
      (def +p+ (std.lib.future/future (Thread/sleep 500)
                         (swap! +input+ conj "World ")))
      (t/read-bytes-some {:raw (t/mock-input-stream +input+)}
                         (fn [v]
                           (vswap! +state+ conj (std.lib.foundation/string v)))
                         10
                         10)
      (Thread/sleep 200)
      @+state+)
  => ["OK " "Hello "]
  @+p+
  
  (do (def +state+  (volatile! []))
      (def +input+ (atom ["OK " "Hello "]))
      (def +p+ (std.lib.future/future (Thread/sleep 50)
                         (swap! +input+ conj "World ")
                         (Thread/sleep 50)
                         (swap! +input+ conj "Again ")))
      (t/read-bytes-some {:raw (t/mock-input-stream +input+)}
                         (fn [v]
                           (vswap! +state+ conj (std.lib.foundation/string v)))
                         80
                         20)
      @+state+)
  => ["OK " "Hello " "World " "Again "]
  @+p+)

^{:refer std.concurrent.relay.transport/op-count :added "4.0"}
(fact "outputs the count in the stream"
  ^:hidden
  
  (-> (t/op-count {:raw (t/mock-input-stream
                         (atom ["OK "
                                "OK "]))})
      :count)
  => 3)

^{:refer std.concurrent.relay.transport/op-clean :added "4.0"}
(fact "cleans the current input stream"
  ^:hidden
  
  (do (def +input+ (atom ["OK " "Hello "]))
      
      (-> (t/op-clean {:raw (t/mock-input-stream
                             +input+)})
          :dropped))
  => 3

  @+input+
  => ["Hello "])

^{:refer std.concurrent.relay.transport/op-clean-some :added "4.0"}
(fact "cleans until timeout"
  ^:hidden

  (do (def +input+ (atom ["OK " "Hello "]))
      
      (-> (t/op-clean-some {:raw (t/mock-input-stream
                                  +input+)}
                           10
                           01)
          :dropped))
  => number?

  @+input+
  => [])

^{:refer std.concurrent.relay.transport/op-read-all-bytes :added "4.0"}
(fact "reads all bytes from the stream"
  ^:hidden
  
  (-> (t/op-read-all-bytes {:raw (t/mock-input-stream (atom ["OK " "Hello " "World "])
                                                      )})
      :output
      std.lib.foundation/string)
  => "OK Hello World ")

^{:refer std.concurrent.relay.transport/op-read-all :added "4.0"}
(fact "reads all bytes from stream as string"
  ^:hidden
  
  (-> (t/op-read-all {:raw (t/mock-input-stream (atom ["OK " "Hello " "World "])
                                                )})
      :output)
  => "OK Hello World ")

^{:refer std.concurrent.relay.transport/op-read-some-bytes :added "4.0"}
(fact "read bytes from stream until timeout"
  ^:hidden
  
  (do (def +state+  (volatile! []))
      (def +input+ (atom ["OK " "Hello "]))
      (def +p+ (std.lib.future/future (Thread/sleep 50)
                         (swap! +input+ conj "World ")
                         (Thread/sleep 50)
                        (swap! +input+ conj "Again ")))
      (-> (t/op-read-some-bytes {:raw (t/mock-input-stream +input+)}
                                80
                                20)
          :output
          std.lib.foundation/string))
  => "OK Hello World Again "
  @+p+)

^{:refer std.concurrent.relay.transport/op-read-some :added "4.0"}
(fact "read bytes from stream until timeout as string"
  ^:hidden
  
  (do (def +state+  (volatile! []))
      (def +input+ (atom ["OK " "Hello "]))
      (def +p+ (std.lib.future/future (Thread/sleep 50)
                         (swap! +input+ conj "World ")
                         (Thread/sleep 50)
                         (swap! +input+ conj "Again ")))
      (-> (t/op-read-some {:raw (t/mock-input-stream +input+)}
                          80
                          20)
          :output))
  => "OK Hello World Again "
  @+p+)

^{:refer std.concurrent.relay.transport/op-read-line :added "4.0"}
(fact "read line from stream or until timeout"
  ^:hidden

  (do (def +state+  (volatile! []))
      (def +input+ (atom ["OK " "Hello "]))
      (def +p+ (std.lib.future/future (Thread/sleep 50)
                         (swap! +input+ conj "World\n")
                         (Thread/sleep 50)
                        (swap! +input+ conj "Again ")))
      (-> (t/op-read-line {:raw (t/mock-input-stream +input+)}
                          80
                          20)
          :output))
  => "OK Hello World\n"
  @+p+)

^{:refer std.concurrent.relay.transport/op-read-limit :added "4.0"}
(fact  "reads an limited amount of characters from stream or until timeout"
  ^:hidden

  (do (def +state+  (volatile! []))
      (def +input+ (atom ["OK " "Hello "]))
      (def +p+ (std.lib.future/future (Thread/sleep 50)
                         (swap! +input+ conj "World\n")
                         (Thread/sleep 50)
                         (swap! +input+ conj "Again ")))
      (-> (t/op-read-limit {:raw (t/mock-input-stream +input+)}
                           18
                           200
                           20)
          :output))
  => "OK Hello World\nAga"
  @+p+)

^{:refer std.concurrent.relay.transport/process-by-line :added "4.0"}
(fact "process each line using a function"
  ^:hidden
  
  (def +state+ (volatile! []))
  (t/process-by-line {:raw (java.io.ByteArrayInputStream.
                            (.getBytes "Hello\n World\n"))}
                     (fn [line _]
                       (vswap! +state+ conj line))
                     {})
  @+state+
  => ["Hello" " World"])

^{:refer std.concurrent.relay.transport/process-by-handler :added "4.0"}
(fact "process the input with a function"
  ^:hidden
  
  (def +state+ (volatile! []))
  (t/process-by-handler {:raw (java.io.ByteArrayInputStream.
                               (.getBytes "Hello\n World\n"))}
                        (fn [^java.io.InputStream is _]
                          (vreset! +state+ (std.lib.foundation/string (.readAllBytes is))))
                        {})

  @+state+
  => "Hello\n World\n")

^{:refer std.concurrent.relay.transport/process-op :added "4.0"}
(fact "processes an op given a command"
  ^:hidden
  
  (-> (t/process-op {:raw (java.io.ByteArrayInputStream.
                           (.getBytes "Hello\n World\n"))}
                    :count 
                    {})
      :count)
  => 13
  
  (-> (t/process-op {:raw (java.io.ByteArrayInputStream.
                           (.getBytes "Hello\n World\n"))}
                    :clean
                    {})
      :dropped)
  => 13)

^{:refer std.concurrent.relay.transport/send-write-raw :added "4.0"}
(fact "sends a raw write command")

^{:refer std.concurrent.relay.transport/send-write-flush :added "4.0"}
(fact "sends a raw flush command")

^{:refer std.concurrent.relay.transport/send-write-line :added "4.0"}
(fact "sends a command with a newline and flush")

^{:refer std.concurrent.relay.transport/send-command :added "4.0"}
(fact "sends a command to output and then waits on the input")

(comment
  (./import)
  )
