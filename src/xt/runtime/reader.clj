(ns xt.runtime.reader
  (:require [std.lang :as l])
  (:refer-clojure :exclude [slurp]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})

(defn.xt impl-char-at
  "returns a one-character string at the current reader offset"
  {:added "4.1"}
  [s idx]
  (if (or (< idx 0)
          (>= idx (xt/x:len s)))
    (return nil)
    (return (xt/x:str-substring s
                                (xt/x:offset idx)
                                (+ idx 1)))))

(defn.xt reader?
  "checks if the object is a runtime reader"
  {:added "4.1"}
  [x]
  (return (and (xt/x:is-object? x)
               (== "reader" (. x ["::"])))))

(defn.xt create
  "creates a string-backed runtime reader"
  {:added "4.1"}
  [input]
  (return {"::" "reader"
           "input" (or input "")
           "idx" 0
           "line" 1
           "col" 1
           "pushback" []
           "history" []}))

(defn.xt reader-position
  "returns the current [line column] position"
  {:added "4.1"}
  [reader]
  (return [(. reader line) (. reader col)]))

(defn.xt throw-reader
  "throws a reader error with position context"
  {:added "4.1"}
  [reader message]
  (var pos (-/reader-position reader))
  (var line (xt/x:first pos))
  (var col  (xt/x:second pos))
  (xt/x:err
   (xt/x:cat message
             " [line "
             (xt/x:to-string line)
             ", column "
             (xt/x:to-string col)
             "]")))

(defn.xt peek-char
  "returns the current char without advancing"
  {:added "4.1"}
  [reader]
  (var pushback (. reader pushback))
  (if (< 0 (xt/x:len pushback))
    (return (xt/x:last pushback))
    (return (-/impl-char-at (. reader input)
                            (. reader idx)))))

(defn.xt read-char
  "reads a single char and advances the reader"
  {:added "4.1"}
  [reader]
  (var pushback (. reader pushback))
  (if (< 0 (xt/x:len pushback))
    (do (var ch (xt/x:arr-pop pushback))
        (xt/x:arr-push (. reader history)
                       {"idx" (. reader idx)
                        "line" (. reader line)
                        "col" (. reader col)
                        "ch" ch
                        "advance" false})
        (if (== "\n" ch)
          (do (xt/x:set-key reader "line" (+ (. reader line) 1))
              (xt/x:set-key reader "col" 1))
          (xt/x:set-key reader "col" (+ (. reader col) 1)))
        (return ch))
    (do (var ch (-/impl-char-at (. reader input)
                                (. reader idx)))
        (if (xt/x:nil? ch)
          (return nil)
          (do (xt/x:arr-push (. reader history)
                             {"idx" (. reader idx)
                              "line" (. reader line)
                              "col" (. reader col)
                              "ch" ch
                              "advance" true})
              (xt/x:set-key reader "idx" (+ (. reader idx) 1))
              (if (== "\n" ch)
                (do (xt/x:set-key reader "line" (+ (. reader line) 1))
                    (xt/x:set-key reader "col" 1))
                (xt/x:set-key reader "col" (+ (. reader col) 1)))
              (return ch))))))

(defn.xt unread-char
  "pushes a char back onto the reader"
  {:added "4.1"}
  [reader ch]
  (var history (. reader history))
  (when (== 0 (xt/x:len history))
    (xt/x:err "Cannot unread before reading"))
  (var entry (xt/x:arr-pop history))
  (xt/x:set-key reader "idx" (. entry idx))
  (xt/x:set-key reader "line" (. entry line))
  (xt/x:set-key reader "col" (. entry col))
  (when (or (not (. entry advance))
            (not= ch (. entry ch)))
    (xt/x:arr-push (. reader pushback) ch))
  (return reader))

(defn.xt step-char
  "moves the reader one char forward"
  {:added "4.1"}
  [reader]
  (-/read-char reader)
  (return reader))

(defn.xt ignore-char
  "reads and ignores the next char"
  {:added "4.1"}
  [reader]
  (-/read-char reader)
  (return nil))

(defn.xt read-while
  "reads chars while the predicate matches"
  {:added "4.1"}
  [reader pred eof?]
  (var out "")
  (while true
    (var ch (-/read-char reader))
    (cond (xt/x:nil? ch)
          (do (when (not eof?)
                (-/throw-reader reader "Unexpected EOF."))
              (return out))

          (pred ch)
          (:= out (xt/x:cat out ch))

          :else
          (do (-/unread-char reader ch)
              (return out)))))

(defn.xt read-until
  "reads chars until the predicate matches"
  {:added "4.1"}
  [reader pred eof?]
  (return (-/read-while reader
                        (fn [ch]
                          (return (not (pred ch))))
                        eof?)))

(defn.xt slurp
  "reads the remaining input from the reader"
  {:added "4.1"}
  [reader]
  (var out "")
  (while true
    (var ch (-/read-char reader))
    (if (xt/x:nil? ch)
      (return out)
      (:= out (xt/x:cat out ch)))))
