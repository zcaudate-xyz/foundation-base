(ns std.block.editor
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.parse :as parse]
            [std.lib.zip :as zip]
            [std.dom.common :as dom]
            [std.dom.type :as type]))

;; 1. Register Block Types in std.dom (Mock environment)
;; This allows us to create "Visual Blocks"

(defonce _register-types
  (do
    ;; Ensure base metaclasses exist (though std.dom.type usually inits them)
    (type/metaclass-add :dom/element {:metatype :dom/element})
    (type/metaclass-add :dom/value {:metatype :dom/value})

    ;; Now add metaprops, referencing the metaclass by ID, not metatype directly
    ;; metaprops-add takes [metaclass-id props]
    (type/metaprops-add :dom/element
                        {:tag :block/container
                         :children {:key :children}})

    (type/metaprops-add :dom/value
                        {:tag :block/token})))

;; 2. Editor State

(defrecord Editor [zip])

(defn create-editor [code-str]
  (let [root (parse/parse-string code-str)
        ;; Use a standard vector zipper context for block children
        ;; But wait, block children are stored in block objects.
        ;; We need a block-specific zipper context.
        context {:create-container    construct/block
                 :create-element      construct/block
                 :is-container?       base/container?
                 :is-empty-container? (fn [b] (empty? (base/block-children b)))
                 :is-element?         (constantly true)
                 :list-elements       base/block-children
                 :update-elements     base/replace-children
                 :add-element         construct/add-child

                 :cursor              '|
                 :at-left-most?       zip/at-left-most?
                 :at-right-most?      zip/at-right-most?
                 :at-inside-most?     zip/at-inside-most?
                 :at-inside-most-left? zip/at-inside-most-left?
                 :at-outside-most?    zip/at-outside-most?

                 :update-step-inside  (fn [b c] b)
                 :update-step-right   (fn [b c] b)
                 :update-step-left    (fn [b c] b)
                 :update-step-outside (fn [b c] b)}
        z (zip/zipper root context)]
    (Editor. z)))

;; 3. Actions (Controller)

(defn move-next [editor]
  (update editor :zip #(if (zip/can-step-right? %) (zip/step-right %) %)))

(defn move-prev [editor]
  (update editor :zip #(if (zip/can-step-left? %) (zip/step-left %) %)))

(defn move-in [editor]
  (update editor :zip #(if (zip/can-step-inside? %) (zip/step-inside %) %)))

(defn move-out [editor]
  (update editor :zip #(if (zip/can-step-outside? %) (zip/step-outside %) %)))

(defn wrap [editor tag]
  (update editor :zip
          (fn [z]
            (let [curr (zip/right-element z)]
              (if curr
                (let [wrapped (construct/block [curr]) ;; Wrap in vector initially? No, use construct/container if tag specific
                      ;; But constructing specific containers is hard without knowing props.
                      ;; Let's just wrap in a generic list for now.
                      wrapped-block (construct/container tag [curr])]
                  (zip/replace-right z wrapped-block))
                z)))))

(defn insert-right [editor val]
  (update editor :zip
          (fn [z]
            (zip/insert-right z (construct/block val)))))

;; 4. View (Block -> DOM)

(defn block->dom [block current-path target-path]
  ;; Convert a std.block node to a std.dom node
  ;; highlight if paths match
  (let [is-focused (= current-path target-path)
        props (cond-> {:focused is-focused
                       :tag (base/block-tag block)}
                (base/expression? block) (assoc :value (base/block-value-string block)))]

    (cond
      (base/container? block)
      (let [children (base/block-children block)
            child-doms (map-indexed (fn [i child]
                                      (block->dom child (conj current-path i) target-path))
                                    children)]
        (dom/dom-create :block/container props child-doms))

      (not (base/container? block)) ;; Assume token or void
      (dom/dom-create :block/token (assoc props :text (base/block-string block)))

      :else
      (dom/dom-create :block/token (assoc props :text (base/block-string block) :type :void)))))

(defn get-zipper-path [z]
  ;; Compute path from root to current location indices
  ;; This requires zipper path support or we assume a simple depth/index tracking
  ;; std.lib.zip does not expose path directly easily without walking back up.
  ;; For this toy, we can just mark the *object* that is currently focused.
  ;; But objects might be identical (e.g. two '1's).
  ;; So we need object identity or path.
  ;; Let's rely on object identity for the toy if possible, or just pass the zipper to the renderer
  ;; and let the renderer traverse the zipper to build the DOM?

  ;; Better approach: Render from the ROOT of the zipper, but maintain a "pointer" to the focused node.
  ;; During traversal, check if (identical? node focus-node).
  (let [focus-node (zip/right-element z)]
    focus-node))

(defn render-view [editor]
  (let [z (:zip editor)
        root (zip/root-element z)
        focus (zip/right-element z)]

    ;; Recursive function to build DOM
    (letfn [(build [node]
              (let [is-focused (identical? node focus)
                    props {:focused is-focused
                           :tag (base/block-tag node)}]
                (cond
                  (base/container? node)
                  (dom/dom-create :block/container props (map build (base/block-children node)))

                  :else
                  (dom/dom-create :block/token (assoc props :text (base/block-string node))))))]
      (build root))))
