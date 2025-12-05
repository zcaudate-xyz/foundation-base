(ns std.pipe.monitor
  (:require [std.lib :as h]
            [std.lib.stream :as s]
            [std.print :as print]
            [std.string :as str]))

(def ^:dynamic *monitor* nil)

(defn- format-time
  [ms]
  (let [seconds (long (/ ms 1000))
        minutes (long (/ seconds 60))
        hours   (long (/ minutes 60))]
    (format "%02d:%02d:%02d" hours (rem minutes 60) (rem seconds 60))))

(defn- clear-screen
  []
  (print/print "\033[2J\033[H"))

(defn- move-cursor-up
  [n]
  (print/print (str "\033[" n "A")))

(defn- clear-line
  []
  (print/print "\033[2K\r"))

(defrecord Monitor [total pending running completed failed results start-time display-fn]
  Object
  (toString [this]
    (str "#Monitor{:total " @total
         " :pending " (count @pending)
         " :running " (count @running)
         " :completed " (count @completed)
         " :failed " (count @failed) "}")))

(defn create-monitor
  [inputs display-fn]
  (map->Monitor {:total (atom (count inputs))
                 :pending (atom (set inputs))
                 :running (atom #{})
                 :completed (atom #{})
                 :failed (atom #{})
                 :results (atom [])
                 :start-time (System/currentTimeMillis)
                 :display-fn display-fn}))

(defn update-monitor
  [monitor key status & [data]]
  (let [{:keys [pending running completed failed results]} monitor]
    (condp = status
      :start (do (swap! pending disj key)
                 (swap! running conj key))
      :complete (do (swap! running disj key)
                    (swap! completed conj key)
                    (when data
                      (swap! results conj data)))
      :fail (do (swap! running disj key)
                (swap! failed conj key)
                (when data
                    (swap! results conj data))))))

(defn render-monitor
  [monitor]
  (let [{:keys [total pending running completed failed start-time]} monitor
        t @total
        p (count @pending)
        r (count @running)
        c (count @completed)
        f (count @failed)
        elapsed (- (System/currentTimeMillis) start-time)
        percent (if (pos? t) (int (* 100 (/ (+ c f) t))) 0)]
    (clear-line)
    (print/print (format "Progress: [%3d%%] Total: %d | Pending: %d | Running: %d | Completed: %d | Failed: %d | Time: %s"
                     percent t p r c f (format-time elapsed)))))

(defn monitor-loop
  [monitor interval]
  (future
    (loop []
      (render-monitor monitor)
      (Thread/sleep ^long interval)
      (when (< (+ (count @(:completed monitor)) (count @(:failed monitor))) @(:total monitor))
        (recur)))
    (render-monitor monitor) ;; Final render
    (print/println)))
