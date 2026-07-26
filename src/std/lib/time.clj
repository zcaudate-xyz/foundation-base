(ns std.lib.time
  (:import (hara.lib.foundation Clock)))

(def ^:dynamic *format* nil)

(def ^:dynamic *no-gc* false)

(defn system-ns
  "returns the system nano time
 
   (system-ns)
   ;; 8158606456270
   => number?"
  {:added "3.0"}
  ([]
   (System/nanoTime)))

(defn system-ms
  "returns the system milli time
 
   (system-ms)
   => number?"
  {:added "3.0"}
  ([]
   (System/currentTimeMillis)))

(defn time-ns
  "returns current time in nano seconds
 
   (time-ns)
   ;; 1593922814991449423
   => number?"
  {:added "3.0"}
  ([]
   (Clock/currentTimeNanos)))

(defn time-us
  "returns current time in micro seconds
 
   (time-us)
   ;; 1593922882412533
   => number?"
  {:added "3.0"}
  ([]
   (Clock/currentTimeMicros)))

(defn time-ms
  "returns current time in milli seconds
 
   (time-ms)
   ;; 1593922917603
   => number?"
  {:added "3.0"}
  ([]
   (Clock/currentTimeMillis)))

(defn format-ms
  "returns ms time is human readable format
 
   (format-ms 10000000)
   => \"02h 46m 40s\""
  {:added "3.0"}
  ([time]
   (let [millis  (mod time  1000)
         seconds (mod (quot time 1000) 60)
         mins    (mod (quot time (* 60 1000)) 60)
         hours   (mod (quot time (* 60 60 1000)) 24)
         days    (quot time (* 24 60 60 1000))
         level   (count (drop-while zero? [days hours mins seconds millis]))]
     (case level
       0 "0ms"
       1 (format "%03dms" millis)
       2 (format "%02ds %03dms" seconds millis)
       3 (format "%02dm %02ds" mins seconds)
       4 (format "%02dh %02dm %02ds" hours mins seconds)
       5 (format "%02dd %02dh %02dm" days mins seconds)))))

(defn parse-ms
  "parses the string representation of time in ms
 
   (parse-ms \"1s\")
   => 1000
 
   (parse-ms \"0.5h\")
   => 1800000"
  {:added "3.0"}
  ([^String s]
   (let [len (count s)
         [s unit] (cond (.endsWith s "ms")
                        [(subs s 0 (- len 2)) 1]

                        :else
                        (let [unit (case (last s)
                                     \d (* 24 60 60 1000)
                                     \h (* 60 60 1000)
                                     \m (* 60 1000)
                                     \s 1000)]
                          [(subs s 0 (dec len)) unit]))
         v (Double/parseDouble s)]
     (long (* v unit)))))

(defn format-ns
  "returns ns time in seconds
 
   (format-ns 1000000)
   => \"1.000ms\""
  {:added "3.0"}
  ([time]
   (format-ns time 4))
  ([time digits]
   (let [full   (str time)
         len    (count full)
         suffix (cond (<= 1 len 3)
                      "ns"
                      (<= 4 len 6)
                      "us"
                      (<= 7 len 9)
                      "ms"
                      (<= 10 len 12)
                      "s"
                      (<= 13 len 15)
                      "ks"
                      (<= 16 len 18)
                      "Ms"
                      (<= 19 len 21)
                      "Gs")
         decimal (inc (rem (dec len) 3))]
     (if (<= len digits)
       (str full "ns")
       (let [out (subs full 0 digits)]
         (str (subs out 0 decimal)
              "."
              (subs out decimal)
              suffix))))))

(defn elapsed-ms
  "determines the time in ms that has elapsed
 
   (elapsed-ms (- (time-ms) 10) true)
   => string?"
  {:added "3.0"}
  ([ms] (elapsed-ms ms *format*))
  ([ms format]
   (cond-> (- (time-ms) ms)
     format format-ms)))

(defn elapsed-ns
  "determines the time in ns that has elapsed
 
   (elapsed-ns (time-ns) true)
   ;; \"35.42us\"
   => string?"
  {:added "3.0"}
  ([ns] (elapsed-ns ns *format*))
  ([ns format]
   (cond-> (- (time-ns) ns)
     format (format-ns))))

(defn- bench-form
  [time-fn elapsed-fn opts body]
  (let [[opts body] (cond (map? opts) [opts body]
                          :else [{} (cons opts body)])
        {:keys [format no-gc runs]
         :or {runs 1}} opts]
    `(binding [*format* ~format]
       (if-not (or ~no-gc *no-gc*)
         (System/gc))
       (let [start# (~time-fn)]
         (dotimes [_# ~runs]
           (do ~@body))
         (quot (~elapsed-fn start#) ~runs)))))

(defmacro bench-ns
  "measures a block in nanoseconds
 
   (bench-ns (Thread/sleep 1))
   => integer?"
  {:added "3.0" :style/indent 1}
  ([opts & body]
   (bench-form `time-ns `elapsed-ns opts body)))

(defmacro bench-ms
  "measures a block in milliseconds
 
   (bench-ms (Thread/sleep 10))
   => integer?"
  {:added "3.0" :style/indent 1}
  ([opts & body]
   (bench-form `time-ms `elapsed-ms opts body)))

(defn parse-ns
  "parses the string repesentation of time in ns
 
   (parse-ns \"2ns\")
   => 2
 
   (parse-ns \"0.3s\")
   => 300000000"
  {:added "3.0"}
  ([^String s]
   (if (.endsWith s "s")
     (let [len  (count s)
           nchar (nth s (- len 2))
           [s unit] (cond (<= (int \0) (int nchar) (int \9))
                          [(subs s 0 (dec len)) 1000000000]

                          :else
                          (let [unit (case nchar
                                       \n 1
                                       \u 1000
                                       \m 1000000
                                       \k 1000000000000
                                       \M 1000000000000000
                                       \G 1000000000000000000)]
                            [(subs s 0 (- len 2)) unit]))
           v (Double/parseDouble s)]
       (long (* v unit))))))

