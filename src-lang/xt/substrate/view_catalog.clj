(ns xt.substrate.view-catalog
  "Portable view component catalog for xt.substrate.view maps.

   The catalog is the shared grammar consumed by view validation and by the
   React (@xtalk/figma-ui) and Wind platform backends. Styling is expressed
   exclusively through Tailwind class strings in the \"class\" prop; palette,
   spacing and layout do not appear as grammar props. `variant` exists only
   where the target kit API is variant-based (button/badge/alert) and maps to
   the shared class bundles in each entry's \"variants\" table.

   Bands: \"core\" components are implemented on both platforms, \"extended\"
   components are admitted once a Wind equivalent exists, and \"platform\"
   components (`fg/` prefixed ids) lower directly to @xtalk/figma-ui on web
   and are rejected by portable validation and the Wind backend."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as xts]]})

(def$.xt PLATFORM_PREFIX "fg/")

(def$.xt COMMON_PROPS
  {"id"         {"type" "string"}
   "class"      {"type" "string"}
   "hidden"     {"type" "boolean"}
   "aria_label" {"type" "string"}})

(def$.xt COMPONENTS
  {"ui/fragment"         {"band" "core" "kind" "layout"}
   "ui/row"              {"band" "core" "kind" "layout"}
   "ui/column"           {"band" "core" "kind" "layout"}
   "ui/scroll"           {"band" "core" "kind" "layout"}
   "ui/separator"        {"band" "core" "kind" "layout"}
   "ui/text"             {"band" "core" "kind" "text"
                          "props" {"value" {"type" "string"}}}
   "ui/title"            {"band" "core" "kind" "text"
                          "props" {"value" {"type" "string"}}}
   "ui/label"            {"band" "core" "kind" "text"
                          "props" {"value" {"type" "string"}
                                   "for"   {"type" "string"}}}
   "ui/description"      {"band" "core" "kind" "text"
                          "props" {"value" {"type" "string"}}}
   "ui/icon"             {"band" "core" "kind" "display"
                          "props" {"value" {"type" "string"}}}
   "ui/input"            {"band" "core" "kind" "input"
                          "props" {"value"       {"type" "string"}
                                   "placeholder" {"type" "string"}
                                   "type"        {"type" "string"}
                                   "disabled"    {"type" "boolean"}
                                   "read_only"   {"type" "boolean"}}
                          "events" {"on_change" {"path" ["value"]}}}
   "ui/textarea"         {"band" "core" "kind" "input"
                          "props" {"value"       {"type" "string"}
                                   "placeholder" {"type" "string"}
                                   "rows"        {"type" "number"}
                                   "disabled"    {"type" "boolean"}
                                   "read_only"   {"type" "boolean"}}
                          "events" {"on_change" {"path" ["value"]}}}
   "ui/button"           {"band" "core" "kind" "action"
                          "props" {"value"    {"type" "string"}
                                   "variant"  {"type" "string"}
                                   "size"     {"type" "string"}
                                   "disabled" {"type" "boolean"}
                                   "pending"  {"type" "boolean"}}
                          "events" {"on_press" {"path" nil}}
                          "variants" {"default"     "bg-slate-900 text-white"
                                      "secondary"   "bg-slate-100 text-slate-900"
                                      "outline"     "border border-slate-300 bg-white text-slate-900"
                                      "ghost"       "text-slate-900"
                                      "destructive" "bg-red-600 text-white"
                                      "link"        "text-blue-600 underline underline-offset-4"}}
   "ui/alert"            {"band" "core" "kind" "display"
                          "props" {"variant" {"type" "string"}}
                          "variants" {"default"     "border-slate-200 bg-white text-slate-900"
                                      "destructive" "border-red-200 bg-red-50 text-red-700"}}
   "ui/spinner"          {"band" "core" "kind" "display"
                          "props" {"value" {"type" "string"}}}
   "ui/badge"            {"band" "core" "kind" "display"
                          "props" {"value"   {"type" "string"}
                                   "variant" {"type" "string"}}
                          "variants" {"default"     "bg-slate-900 text-white"
                                      "secondary"   "bg-slate-100 text-slate-900"
                                      "outline"     "border border-slate-300 text-slate-900"
                                      "destructive" "bg-red-600 text-white"}}
   "ui/image"            {"band" "core" "kind" "display"
                          "props" {"src" {"type" "string" "required" true}
                                   "alt" {"type" "string"}}}
   "ui/card"             {"band" "core" "kind" "container"}
   "ui/card-header"      {"band" "core" "kind" "container"}
   "ui/card-content"     {"band" "core" "kind" "container"}
   "ui/card-title"       {"band" "core" "kind" "text"
                          "props" {"value" {"type" "string"}}}
   "ui/card-description" {"band" "core" "kind" "text"
                          "props" {"value" {"type" "string"}}}
   "ui/card-footer"      {"band" "core" "kind" "container"}
   "ui/table"            {"band" "core" "kind" "container"}
   "ui/table-header"     {"band" "core" "kind" "container"}
   "ui/table-body"       {"band" "core" "kind" "container"}
   "ui/table-row"        {"band" "core" "kind" "container"}
   "ui/table-head"       {"band" "core" "kind" "text"
                          "props" {"value" {"type" "string"}}}
   "ui/table-cell"       {"band" "core" "kind" "text"
                          "props" {"value" {"type" "string"}}}})

(defn.xt platform-id?
  "checks whether a component id is a platform (figma) escape id"
  [component-id]
  (return (xts/starts-with? component-id -/PLATFORM_PREFIX)))

(defn.xt entry
  "returns the catalog entry for a portable component id"
  [component-id]
  (return (xt/x:get-key -/COMPONENTS component-id)))

(defn.xt has-component?
  "checks whether a component id is in the portable catalog"
  [component-id]
  (return (xt/x:not-nil? (xt/x:get-key -/COMPONENTS component-id))))

(defn.xt band
  "returns the band of a component id: core, extended or platform"
  [component-id]
  (when (-/platform-id? component-id)
    (return "platform"))
  (var e (-/entry component-id))
  (when (xt/x:nil? e)
    (return nil))
  (return (xt/x:get-key e "band")))

(defn.xt portable?
  "checks whether a component id is portable across platforms"
  [component-id]
  (if (-/platform-id? component-id)
    (return false)
    (return (xt/x:not-nil? (-/entry component-id)))))

(defn.xt variant-classes
  "returns the shared Tailwind class bundle for a component variant"
  [component-id variant]
  (var e (-/entry component-id))
  (when (xt/x:nil? e)
    (return nil))
  (var variants (xt/x:get-key e "variants"))
  (when (xt/x:nil? variants)
    (return nil))
  (return (xt/x:get-key variants variant)))

(defn.xt validate-action
  "validates an event action descriptor against the grammar"
  [component-id prop value]
  (when (not (xt/x:is-object? value))
    (xt/x:err (xt/x:cat "view event requires an action descriptor - "
                        component-id " - " prop)))
  (when (not (xt/x:is-string? (xt/x:get-key value "action")))
    (xt/x:err (xt/x:cat "view event requires an action id - "
                        component-id " - " prop)))
  (var payload (xt/x:get-key value "payload"))
  (when (and (xt/x:is-object? payload)
             (xt/x:has-key? payload "$"))
    (when (not= "event" (xt/x:get-key payload "$"))
      (xt/x:err (xt/x:cat "invalid view event projection - "
                          component-id " - " prop)))
    (when (not (xt/x:is-array? (xt/x:get-key payload "path")))
      (xt/x:err (xt/x:cat "view event projection requires a path - "
                          component-id " - " prop))))
  (return true))

(defn.xt validate-prop-type
  [component-id prop spec value]
  (var type (xt/x:get-key spec "type"))
  (var ok true)
  (cond (== type "string")
        (:= ok (xt/x:is-string? value))

        (== type "number")
        (:= ok (xt/x:is-number? value))

        (== type "boolean")
        (:= ok (xt/x:is-boolean? value))

        :else (:= ok true))
  (when (not ok)
    (xt/x:err (xt/x:cat "invalid view prop type - "
                        component-id " - " prop " - " type)))
  (return true))

(defn.xt validate-props
  "validates the props of a portable component node against the catalog"
  [component-id props]
  (var e (-/entry component-id))
  (var eprops (xt/x:get-key e "props"))
  (var events (xt/x:get-key e "events"))
  (xt/for:object [[prop value] (or props {})]
    (cond (xt/x:has-key? (or events {}) prop)
          (-/validate-action component-id prop value)

          (xt/x:has-key? -/COMMON_PROPS prop)
          (-/validate-prop-type component-id prop
                                (xt/x:get-key -/COMMON_PROPS prop) value)

          (xt/x:has-key? (or eprops {}) prop)
          (do (-/validate-prop-type component-id prop
                                    (xt/x:get-key eprops prop) value)
              (when (== prop "variant")
                (var variants (xt/x:get-key e "variants"))
                (when (and (xt/x:not-nil? variants)
                           (not (xt/x:has-key? variants value)))
                  (xt/x:err (xt/x:cat "unknown view variant - "
                                      component-id " - " value)))))

          :else (xt/x:err (xt/x:cat "unknown view prop - "
                                    component-id " - " prop))))
  (xt/for:object [[prop spec] (or eprops {})]
    (when (and (== true (xt/x:get-key spec "required"))
               (not (xt/x:has-key? (or props {}) prop)))
      (xt/x:err (xt/x:cat "missing required view prop - "
                          component-id " - " prop))))
  (return true))
