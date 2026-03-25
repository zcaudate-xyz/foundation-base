(ns refactor.id-005-2026-03-25
  "Babashka migration script.

   Migrates deftype/defrecord implementations of `clojure.lang.IFn` and
   `clojure.lang.IDeref` to also satisfy the babashka-compatible protocol
   alternatives introduced in std.lib.foundation:

     * IFnLike  – protocol alternative to clojure.lang.IFn
     * IDerefLike – protocol alternative to clojure.lang.IDeref

   Remaining sites after the initial manual migrations (Link, Journal, Trace,
   IFnWrapper, Mono via dimpl-dereflike-forms):

     Step 1 – java.lang.Object → Object           (grep-replace)
       std.pretty.edn/extend-protocol IEdn

     Step 2 – add IDerefLike beside clojure.lang.IDeref   (refactor-code)
       std.object.query/Delegate   (deref [self] fields)

     Step 3 – add IFnLike beside clojure.lang.IFn        (refactor-code)
       code.query.match/Matcher    (invoke [this nav] (f nav))
       code.test.checker.common/Checker (invoke [ck data] ...)
       std.object.query/Delegate   (invoke [self] ...) × 3

     Step 4 – add IDerefLike branch to extend-protocol    (transform-code)
       std.pretty.edn – clojure.lang.IDeref case

   Run each step from the REPL with :write false first (dry-run),
   review the printed diff, then set :write true to apply."
  (:require [clojure.string :as str]
            [std.lib :as h]
            [std.block :as b]
            [std.block.navigate :as e]
            [code.manage :as manage]
            [code.query :as q]))

(comment

  ;; ================================================================
  ;; STEP 1  java.lang.Object → Object
  ;;
  ;; Replaces `java.lang.Object` used as a protocol-extension head
  ;; (extend-protocol / deftype) with the unqualified `Object`.
  ;; The regex anchors to lines that contain ONLY the symbol so that
  ;; doc-strings, type-hints and Java interop calls are left untouched.
  ;; ================================================================

  ;; Dry-run – review which lines would change:
  (manage/grep-replace
   :all
   {:print {:function true}
    :query   "^  java\\.lang\\.Object$"
    :replace "  Object"})

  ;; Apply:
  (manage/grep-replace
   :all
   {:print {:function true}
    :write true
    :query   "^  java\\.lang\\.Object$"
    :replace "  Object"})


  ;; ================================================================
  ;; STEP 2  Add IDerefLike to deftype/defrecord IDeref blocks
  ;;
  ;; For every `clojure.lang.IDeref` symbol found inside a
  ;; deftype / defrecord body the script:
  ;;   1. reads the body of the `(deref [arg] ...)` sibling form
  ;;   2. inserts `std.lib.foundation/IDerefLike` immediately after
  ;;      the deref method, with a matching `(-deref-val [arg] ...)`
  ;;
  ;; Affected files:
  ;;   src/std/object/query.clj – Delegate deftype
  ;; ================================================================

  ;; Locate sites first (dry-run, no :write):
  (manage/locate-code
   :all
   {:print {:function true :item true}
    :query [(fn [form]
              (= 'clojure.lang.IDeref form))]})

  ;; Apply:
  (manage/refactor-code
   :all
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (q/modify
               nav
               ;; Selector: find the bare symbol clojure.lang.IDeref
               [(fn [form]
                  (and (symbol? form)
                       (= 'clojure.lang.IDeref form)))]
               (fn [ideref-nav]
                 ;; Guard: skip if IDerefLike already follows immediately
                 ;; (idempotent run safety)
                 (let [after-deref (h/suppress
                                    (-> ideref-nav e/right e/right))]
                   (if (and after-deref
                            (= 'std.lib.foundation/IDerefLike
                               (e/value after-deref)))
                     ideref-nav     ; already migrated, leave unchanged
                     ;; Step right to find the (deref [arg] body...) form
                     (let [deref-nav  (e/right ideref-nav)
                           deref-form (e/value deref-nav)
                           ;; deref-form looks like: (deref [arg] expr...)
                           [_ [arg] & body] deref-form
                           deref-val-form (list '-deref-val
                                               [arg]
                                               (first body))]
                       ;; Insert IDerefLike + -deref-val after (deref ...)
                       (-> deref-nav
                           ;; insert (-deref-val ...) to the right of (deref ...)
                           (e/insert-raw (b/layout deref-val-form))
                           ;; now at (-deref-val ...), step left to (deref ...)
                           e/left
                           ;; insert the protocol symbol between them
                           (e/insert-raw 'std.lib.foundation/IDerefLike)
                           ;; return to the original clojure.lang.IDeref position
                           e/left
                           e/left))))))))]})


  ;; ================================================================
  ;; STEP 3  Add IFnLike to deftype/defrecord IFn blocks
  ;;
  ;; For every `clojure.lang.IFn` symbol inside a deftype / defrecord
  ;; body the script:
  ;;   1. advances right past all `(invoke ...)` siblings
  ;;   2. inserts `std.lib.foundation/IFnLike` and
  ;;      `(-apply-fn [this args] (apply this args))` after the last one
  ;;
  ;; On JVM `(apply this args)` dispatches through IFn.applyTo, calling
  ;; the correct invoke arity.  For full babashka support the generated
  ;; `-apply-fn` stub should be replaced with a self-contained
  ;; implementation (see per-type notes below).
  ;;
  ;; Per-type notes:
  ;;   Matcher  [f]         → (-apply-fn [this args] (apply (:f this) args))
  ;;   Checker  [fn]        → (-apply-fn [ck args] (apply (:fn ck) args))
  ;;   Delegate [pointer fields] – multi-arity; use delegate-invoke helper
  ;;
  ;; Affected files:
  ;;   src/code/query/match.clj
  ;;   src/code/test/checker/common.clj
  ;;   src/std/object/query.clj  (Delegate)
  ;; ================================================================

  ;; Locate sites first:
  (manage/locate-code
   :all
   {:print {:function true :item true}
    :query [(fn [form]
              (= 'clojure.lang.IFn form))]})

  ;; Apply generic stub (JVM-safe, review before enabling babashka):
  (manage/refactor-code
   :all
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (q/modify
               nav
               [(fn [form]
                  (and (symbol? form)
                       (= 'clojure.lang.IFn form)))]
               (fn [ifn-nav]
                 ;; Guard: skip if IFnLike already follows
                 (let [after (h/suppress
                              (-> ifn-nav
                                  ;; skip past invoke siblings to find what follows
                                  (->> (iterate (fn [n]
                                                  (let [r (e/right n)]
                                                    (if (and r
                                                             (list? (e/value r))
                                                             (= 'invoke (first (e/value r))))
                                                      r
                                                      nil))))
                                       (take-while some?)
                                       last)
                                  e/right))]
                   (if (and after
                            (= 'std.lib.foundation/IFnLike (e/value after)))
                     ifn-nav  ; already migrated
                     ;; Advance past all (invoke ...) siblings
                     (let [last-invoke
                           (loop [pos ifn-nav]
                             (let [r (e/right pos)]
                               (if (and r
                                        (list? (e/value r))
                                        (= 'invoke (first (e/value r))))
                                 (recur r)
                                 pos)))]
                       ;; Insert (-apply-fn ...) then IFnLike after last-invoke
                       (-> last-invoke
                           (e/insert-raw '(-apply-fn [this args]
                                                     (apply this args)))
                           e/left
                           (e/insert-raw 'std.lib.foundation/IFnLike)
                           e/left
                           e/left))))))))]})


  ;; ================================================================
  ;; STEP 4  Add IDerefLike branch to extend-protocol (edn.clj)
  ;;
  ;; std.pretty.edn extends IEdn to clojure.lang.IDeref.  In babashka
  ;; that class does not exist, so objects satisfying IDerefLike only
  ;; would not be handled.  This step appends a matching IDerefLike
  ;; dispatch branch after the existing clojure.lang.IDeref block.
  ;;
  ;; The IDerefLike branch is simpler than the IDeref one because
  ;; IDerefLike objects are always "ready" (no IPending concept).
  ;; ================================================================

  (manage/transform-code
   '[std.pretty.edn]
   {:print {:function true}
    :write true
    :transform
    (fn [text]
      (str/replace
       text
       ;; Match the clojure.lang.IDeref block inside the extend-protocol
       #"(?m)(  clojure\.lang\.IDeref\n  \(-edn \[x\].*?\n(?:    .*\n)*?      \(tagged-object x \{:status status :val val\}\)\)\)\))"
       (fn [matched]
         (str matched
              "\n\n  std.lib.foundation/IDerefLike\n"
              "  (-edn [x]\n"
              "    (let [val (std.lib.foundation/-deref-val x)]\n"
              "      (tagged-object x {:status :ready :val val})))"))})})


  ;; ================================================================
  ;; VERIFICATION – locate any remaining unpatched sites
  ;; ================================================================

  ;; Should return no matches after all steps are applied:
  (manage/locate-code
   :all
   {:print {:function true :item true}
    :query [(fn [form]
              (or (= 'clojure.lang.IFn form)
                  (= 'clojure.lang.IDeref form)))]})

  )
