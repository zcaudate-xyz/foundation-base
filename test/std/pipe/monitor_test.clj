(ns std.pipe.monitor-test
  (:require [std.pipe.monitor :refer :all]
            [std.print :as print])
  (:use code.test))

^{:refer std.pipe.monitor/create-monitor :added "4.1"}
(fact "creates the monitor state"
  (let [monitor (create-monitor [1 2 3] str)]
    [@(:total monitor)
     @(:pending monitor)
     @(:running monitor)
     (fn? (:display-fn monitor))])
  => [3 #{1 2 3} #{} true])

^{:refer std.pipe.monitor/update-monitor :added "4.1"}
(fact "updates pending, running, completed and failed state"
  (let [monitor (create-monitor [1 2] identity)]
    (update-monitor monitor 1 :start)
    (update-monitor monitor 1 :complete {:value 10})
    (update-monitor monitor 2 :start)
    (update-monitor monitor 2 :fail {:value 20})
    [(count @(:pending monitor))
     @(:completed monitor)
     @(:failed monitor)
     (count @(:results monitor))])
  => [0 #{1} #{2} 2])

^{:refer std.pipe.monitor/render-monitor :added "4.1"}
(fact "renders the current progress line"
  (let [monitor (create-monitor [1 2] identity)
        output  (atom [])]
    (update-monitor monitor 1 :start)
    (update-monitor monitor 1 :complete)
    (with-redefs [print/print (fn [& args]
                                (swap! output conj (apply str args)))]
      (render-monitor monitor))
    (boolean (some #(re-find #"Progress: \[\s*50%\]" %)
                   @output)))
  => true)

^{:refer std.pipe.monitor/monitor-loop :added "4.1"}
(fact "loops until the monitor has finished"
  (let [monitor (create-monitor [1] identity)
        calls   (atom [])]
    (update-monitor monitor 1 :complete)
    (with-redefs [render-monitor (fn [_]
                                   (swap! calls conj :render))
                  print/println (fn [& _]
                                  (swap! calls conj :println)
                                  nil)]
      (let [fut (monitor-loop monitor 10)]
        [(deref fut 1000 :timeout)
         @calls])))
  => [nil [:render :render :println]])
