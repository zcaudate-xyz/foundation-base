(ns code.test.diff
  (:require [std.lib.collection :as coll]
            [std.lib.diff.seq :as seq]
            [code.test.checker.common :as common]))

(defn checker-equal?
  "custom equality check for diffing that handles checkers"
  [v1 v2]
  (let [ck (common/->checker v1)]
    (common/succeeded? (common/verify ck v2))))

(defn- unflatten
  [m]
  (reduce-kv (fn [out k v]
               (assoc-in out k v))
             {}
             m))

(defn diff-map
  "diffs two maps, respecting checkers"
  [expect actual]
  (let [diff (coll/diff expect actual)
        ;; The standard diff checks for equality using =.
        ;; We need to re-verify changes (key :>) using checker-equal?
        ;; to avoid false positives where the value matches the checker but is not equal to it.
        changed (:> diff)

        ;; If something is marked as changed, we check if it actually satisfies the checker.
        ;; If it does, we remove it from changed.
        real-changes (reduce-kv (fn [out k v]
                                  (let [v-actual (get-in actual k)]
                                    (if (checker-equal? v v-actual)
                                      out
                                      (assoc-in out k v))))
                                {}
                                changed)

        ;; Unwrap keys for + and - as well
        missing (unflatten (:+ diff))
        extra   (unflatten (:- diff))

        final-diff (cond-> {}
                     (not-empty missing) (assoc :+ missing)
                     (not-empty extra)   (assoc :- extra)
                     (not-empty real-changes) (assoc :> real-changes))]

    (if (empty? final-diff)
      nil
      final-diff)))

(defn diff-seq
  "diffs two sequences"
  [expect actual]
  (let [expect (vec expect)
        actual (vec actual)
        [dist edits] (seq/diff expect actual)]
    (if (zero? dist)
      nil
      edits)))

(defn diff
  "calculates the diff between expected and actual values"
  [expect actual]
  (cond (common/checker? expect)
        (let [tag (:tag expect)]
          (cond (= tag :contains)
                (let [d (diff (:expect expect) actual)]
                  (if d
                    (if (map? d)
                      (let [clean (dissoc d :-)]
                        (if (empty? clean) nil clean))
                      d)
                    nil))

                (= tag :just)
                (diff (:expect expect) actual)

                (= tag :satisfies)
                (diff (:expect expect) actual)

                (= tag :exactly)
                (diff (:expect expect) actual)

                :else
                nil))

        (and (map? expect) (map? actual))
        (diff-map expect actual)

        (and (sequential? expect) (sequential? actual))
        (diff-seq expect actual)

        :else
        nil))
