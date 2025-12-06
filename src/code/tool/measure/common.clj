(ns code.tool.measure.common
  (:require [std.lib :as h]
            [std.string :as str]))

;; Metric Configuration
(def ^:dynamic *config*
  {:base-score 1
   :depth-factor 0.1
   :control-flow-bonus 2})

;; Common Surface Calculation
(defn calculate-surface
  "Calculates the 'surface area' of the code using a Code City metaphor.
   Base Area (Width) = Total Node Count (Size)
   Height = Structural Complexity Score
   Surface = Base + 4 * (sqrt(Base) * Height)
   (Represents top surface + 4 side walls of a square tower)"
  ([base height]
   (if (zero? base)
     0.0
     (+ base (* 4 (Math/sqrt base) height)))))

;; Git Interop

(defn sh-git
  [args repo-path]
  (let [res (h/sh {:args (into ["git"] args)
                   :root (str repo-path)})]
    (if (zero? (:exit res))
      (str/trim (:out res))
      (if (nil? (:exit res))
        (throw (ex-info "Git command failed to run (exit code nil)" {:args args :res res}))
        (throw (ex-info "Git command failed" {:args args :res res}))))))

(defn list-commits
  [repo-path]
  (let [out (sh-git ["log" "--pretty=format:%H|%ad" "--date=iso"] repo-path)]
    (->> (str/split-lines out)
         (map (fn [line]
                (let [[sha date] (str/split line #"\|")]
                  {:sha sha :date date}))))))

(defn list-files
  [repo-path sha]
  (let [out (sh-git ["ls-tree" "-r" "--name-only" sha] repo-path)]
    (str/split-lines out)))

(defn get-file-content
  [repo-path sha file-path]
  (sh-git ["show" (str sha ":" file-path)] repo-path))
