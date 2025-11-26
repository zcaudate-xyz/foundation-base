(ns std.block.heal.legacy
  (:require [std.block.heal.parse :as parse]
            [std.block.heal.indent :as indent]
            [std.block.heal.edit :as edit]
            [std.lib :as h]
            [std.string :as str]))

(defn heal-mismatch
  "heals a style mismatch for paired delimiters"
  {:added "4.0"}
  [content & [{:keys [ensure
                      debug]}]]
  (let [delimiters (parse/parse content)
        edits  (edit/create-mismatch-edits delimiters)]
    (edit/update-content content edits)))

;;
;; Balance for Top Heavy code
;;

(defn heal-append
  "appends at the"
  {:added "4.0"}
  [content & [{:keys [debug]}]]
  (let [delimiters (parse/parse content)
        enabled    (edit/check-append-edits delimiters)]
    (cond (parse/is-close-heavy delimiters)
          content
          
          enabled 
          (edit/update-content content (edit/create-append-edits
                                        delimiters))

          #_#_ensure
          (h/error "Not supported" {})

          :else content)))


;;
;; Balance for Bottom Heavy Code
;;

(defn heal-remove
  "removed unmatched parens"
  {:added "4.0"}
  [content & [{:keys [debug]}]]
  (let [delimiters (parse/parse content)
        edits  (edit/create-remove-edits
                delimiters)
        #_#__ (when debug
            (h/prn :REMOVE   (count edits)))]
    (edit/update-content content edits)))

;;
;; Heal generated code that is indented correctly but parens has not be placed right
;;

(defn heal-close-heavy-single-pass
  "creates deletion for multiple early closes on first pass"
  {:added "4.0"}
  [content & [{:keys [debug]
               :as opts}]]
  (let [delimiters (parse/parse content)
        candidates (indent/flag-close-heavy delimiters)
        
        #_#__          (when debug
                         (h/prn :ALL      (map (comp count second) candidates)))
        edits      (indent/build-remove-edits delimiters
                                              candidates)]
    (edit/update-content content edits)))

(defn heal-close-heavy
  "multiple close deletions"
  {:added "4.0"}
  [content & [{:keys [limit debug]
               :or {limit 50}
               :as opts}]]
  (loop [old-content content
         pass 0]
    (when debug
      (h/prn "Heal Close Heavy:" pass))
    (let [new-content (heal-close-heavy-single-pass old-content opts)]
      (if (or (= new-content old-content)
              (> pass limit))
        new-content
        (recur new-content (inc pass))))))

(defn heal-open-heavy-single-pass
  "heals content that has been wrongly 
 
   (read-string
    (core/heal-open-heavy-single-pass \"
 (defn
  (do
    (it)
  (this)
  (this))\"))
   => '(defn (do (it)) (this) (this))"
  {:added "4.0"}
  [content & [{:keys [debug]
               :as opts}]]
  (let [delimiters (parse/parse content)
        candidates (indent/flag-open-heavy
                    delimiters)
        selected   (indent/flagged-candidates-filter-run candidates opts)
        #_#__          (when debug
                         (h/prn :ALL      (map (comp count second) candidates))
                         (h/prn :SELECTED (map (comp count second) selected)))
        
        edits      (indent/build-insert-edits delimiters
                                              selected
                                              content)]
    (edit/update-content content edits)))

(defn heal-open-heavy
  "fixes indentation parens"
  {:added "4.0"}
  [content & [{:keys [limit minimum debug]
               :or {limit 50}
               :as opts}]]
  (loop [old-content content
         pass 0]
    (when debug
      (h/prn "Heal Open Heavy:" pass))
    (let [new-content (heal-open-heavy-single-pass old-content opts)]
      (if (or (= new-content old-content)
              (> pass limit))
        new-content
        (recur new-content (inc pass))))))

(defn heal-legacy-raw
  "combining all strategies for code heal"
  {:added "4.0"}
  [content & [{:keys [limit minimum debug]
               :as opts}]]
  (-> content
      (heal-close-heavy opts)
      (heal-open-heavy opts)
      (heal-append opts)
      (heal-mismatch opts)
      (heal-remove opts)
      #_(heal-open-heavy opts)))

(defn heal-legacy
  "heals the content"
  {:added "4.0"}
  [s & [{:keys [write]
         :as opts}]]
  (let [[path content] (cond (std.fs/path? s)
                             [(str s)
                              (slurp s)]

                             (string? s)
                             [nil s]

                             :else
                             [nil s])
        healed (heal-legacy-raw content opts)]
    (if (and write path)
      (spit path healed))
    healed))

