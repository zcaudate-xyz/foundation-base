(ns app.manage.workspace (:require [lang.core :as l]))
(l/script :js {:import [["react" :as React] ["@monaco-editor/react" :as Monaco]]
               :static {:export [Workspace]}})
(defn.js ^{:- [:async]} getJSON [path]
  (var response (await (fetch path)))
  (var value (await (response.json)))
  (when (not response.ok) (throw (new Error (or value.error "Request failed"))))
  (return value))
(defn.js syncChecks [frame]
  (var checkbox (document.querySelector "#include-checks"))
  (when (and frame frame.contentWindow checkbox)
    (frame.contentWindow.postMessage {"appManageChecks" checkbox.checked} window.location.origin)))
(defn.js matches [value query]
  (return (. (. (String (or value "")) (toLowerCase)) (includes (. query (toLowerCase))))))
(defn.js Code [props]
  (var editorRef (React.useRef nil))
  (React.useEffect (fn [] (when editorRef.current (editorRef.current.revealLineInCenter (or props.file.row 1)))) [props.file.row props.file.path])
  (return (React.createElement Monaco
    {"height" "100%" "language" "clojure" "theme" props.theme
     "value" (or props.file.content "") "path" props.file.path
     "onMount" (fn [editor] (:= editorRef.current editor) (editor.revealLineInCenter (or props.file.row 1)))
     "options" {"readOnly" true "automaticLayout" true "fontSize" 13 "minimap" {"enabled" false}
                  "scrollBeyondLastLine" false}})))
(defn.js fileTree [files]
  (var root {})
  (. files (forEach (fn [file]
    (var node root)
    (var parts (. file.path (split "/")))
    (. parts (forEach (fn [part index]
      (when (not (. node [part])) (:= (. node [part]) {"name" part "path" (. (. parts (slice 0 (+ index 1))) (join "/")) "children" {}}))
      (when (== index (- parts.length 1)) (:= (. node [part] file) file))
      (:= node (. node [part] children))))))))
  (return root))
(defn.js FileTree [props]
  (return [:div (. (Object.values props.nodes) (map (fn [node]
    (return (:? node.file
      [:button {:key node.path :onClick (fn [] (props.open node.file))} node.name]
      [:details {:key node.path :open (. props.expanded (includes node.path))
                 :onToggle (fn [event] (when (== event.target event.currentTarget) (props.toggle node.path event.currentTarget.open)))} [:summary node.name]
       (React.createElement -/FileTree {"nodes" node.children "open" props.open "expanded" props.expanded "toggle" props.toggle})]))))) ]))
(defn.js liveOutput [run]
  (when (not run) (return ""))
  (when run.result (return (+ (or run.result.stdout run.stdout "") "\n" (or run.result.stderr ""))))
  (var chunks (. (or run.events []) (filter (fn [event] (return (== event.type "test/output"))))))
  (return (. (. chunks (map (fn [event] (return event.text)))) (join ""))))
(defn.js groupAssertions [assertions]
  (var groups (Object.create nil))
  (. assertions (forEach (fn [assertion]
    (var key (or (. assertion ["fact-id"]) assertion.desc assertion.name "Unassigned fact"))
    (when (not (. groups [key])) (:= (. groups [key]) {"id" key "name" (or assertion.desc assertion.name key) "assertions" []}))
    (. (. groups [key] assertions) (push assertion)))))
  (return (Object.values groups)))
(defn.js Results [props]
  (return [:div
    (. props.namespaces (map (fn [ns]
      (return [:details {:key ns.namespace :open true}
        [:summary (+ ns.namespace " · " ns.status)] [:p ns.message]
        (. (-/groupAssertions (or ns.assertions [])) (map (fn [group]
          (return [:details {:key group.id "data-fact" group.id}
            [:summary group.name]
            (. group.assertions (map (fn [a index]
              (return [:details {:key index "data-assertion" index :className a.status}
                [:summary {:onClick (fn [] (when a.path (props.open a.path (or a.line 1))))} (+ a.status " · L" (or a.line "?"))]
                [:h4 "Expected / checker"] [:pre (JSON.stringify a.check nil 2)]
                [:h4 "Actual"] [:pre (JSON.stringify a.data nil 2)]]))))]))))]))))]))
(defn.js flagText [flag]
  (return (or (. {"missing-test" "No test evidence found"
                  "missing-test-link" "Calls found; source/test reference needs review"
                  "pointer-only-tests" "Only pointer checks; no behavioural invocation"
                  "type-only-tests" "Only broad type predicates are asserted"
                  "missing-script-runtime" "No l/script- runtime declared"
                  "indirect-test-review" "Indirect calls: inspect helper assertions"
                  "todo" "TODO placeholder" "no-assertions" "Linked fact has no assertions"
                  "duplicate-fact-id" "Duplicate fact ID can prevent earlier facts from running"
                  "incomplete" "code.manage marks this unit incomplete"
                  "orphaned-reference" "Source reference no longer resolves"
                  "external-reference" "Reference is outside indexed namespaces"} [flag]) flag "Analysis error")))
(defn.js Diagnostics [props]
  (return [:div (. (or props.items []) (map (fn [d index]
    (return [:button {:key index :onClick (fn [] (props.open d))}
              (+ d.path " · " (or d.error (-/flagText d.code)))]))))]))
(defn.js paneSize [element dimension value]
  (var maximum (:? (== dimension "height") (* window.innerHeight 0.7)
                    (. element (matches ".explorer")) (Math.min 600 (* window.innerWidth 0.45))
                    :else (- element.parentElement.clientWidth 200)))
  (:= (. element.style [dimension]) (+ (Math.max (:? (== dimension "height") 80 160) (Math.min maximum value)) "px")))
(defn.js finishPane [element]
  (document.dispatchEvent (new Event "pointerup"))
  (when (. element (matches ".editors > section:first-child")) (. element.style (removeProperty "width"))))
(defn.js resizePane [event selector dimension direction]
  (var element (document.querySelector selector))
  (when (not element) (return nil))
  (event.preventDefault)
  (var initial (. (element.getBoundingClientRect) [dimension]))
  (var origin (:? (== dimension "width") event.clientX event.clientY))
  (var move (fn [next]
    (-/paneSize element dimension (+ initial (* direction (- (:? (== dimension "width") next.clientX next.clientY) origin))))))
  (var stop (fn []
    (document.removeEventListener "pointermove" move)
    (document.removeEventListener "pointerup" stop)
    (document.removeEventListener "pointercancel" stop)
    (when (. element (matches ".editors > section:first-child")) (. element.style (removeProperty "width")))))
  (document.addEventListener "pointermove" move)
  (document.addEventListener "pointerup" stop)
  (document.addEventListener "pointercancel" stop))
(defn.js resizeKey [event selector dimension direction]
  (when (not (. ["ArrowUp" "ArrowDown" "ArrowLeft" "ArrowRight"] (includes event.key))) (return nil))
  (event.preventDefault)
  (var element (document.querySelector selector))
  (when element
    (-/paneSize element dimension (+ (. (element.getBoundingClientRect) [dimension])
                                     (* direction (:? (. ["ArrowUp" "ArrowLeft"] (includes event.key)) -16 16))))
    (-/finishPane element)))
(defn.js ExplorerPanel [props]
  (var #{section setSection report setReport query setQuery unit setUnit source setSource test setTest profiles setProfiles session setSession snapshot setSnapshot panel setPanel error setError theme setTheme tabs setTabs navigationSequence sourceSequence testSequence problemPosition mobilePane setMobilePane history closed expanded setExpanded sidebarOpen setSidebarOpen panelOpen setPanelOpen layout setLayout palette setPalette preview setPreview legacy setLegacy openFile openUnit command runTests latest testRun rows} props)
  (return [:aside {:className "explorer" :style {"width" layout.explorer}} [:h3 section] [:button {:className "mobile-switch" :onClick (fn [] (setSidebarOpen false))} "Close explorer"]
       (:? source.path [:details [:summary "Outline"]
         (. report.units (filter (fn [u] (return (== u.source.path source.path))))
            (map (fn [u] (return [:button {:key u.id :onClick (fn [] (openUnit u.id))} u.name]))))] nil)
       (:? (or (== section "Application") (== section "Build"))
         (. (:? (== section "Application") ["pages" "slices"] ["pipeline" "previews" "outputs" "checks" "runs"])
            (map (fn [view] (return [:button {:key view :onClick (fn [] (setLegacy view))} view]))))
         (:? (== section "Testing")
           [:div (. profiles (map (fn [profile] (return [:details {:key profile.id :open true}
             [:summary profile.label]
             (. profile.namespaces (map (fn [ns] (return [:details {:key ns}
               [:summary ns] [:button {:onClick (fn [] (runTests ns))} "Run namespace"]
               (. (or (. report ["test-inventory"]) [])
                 (filter (fn [f] (return (== f.namespace ns))))
                 (map (fn [f] (return [:button {:key (+ f.path f.id) :onClick (fn [] (setUnit nil) (openFile f.path f.line.row setTest))} (or f.intro f.id)]))))]))))]))))]
         [:div
          (:? (== section "Explorer")
            (React.createElement -/FileTree {"nodes" (-/fileTree (or report.files [])) "expanded" expanded
              "toggle" (fn [path open] (setExpanded (fn [current]
                        (return (:? open (:? (. current (includes path)) current (. current (concat [path])))
                                    (. current (filter (fn [value] (return (not= value path))))))))))
              "open" (fn [file] (setUnit nil) (when (not= file.kind "test") (setTest {}))
                        (openFile file.path 1 (:? (== file.kind "test") setTest setSource)))}) nil)
          (:? (and (== section "Search") query.length)
            [:details {:open true} [:summary "Files and tests"]
             (. (or report.files []) (filter (fn [file] (return (-/matches file.path query))))
                (map (fn [file] (return [:button {:key file.path :onClick (fn [] (setUnit nil) (openFile file.path 1 (:? (== file.kind "test") setTest setSource)))} file.path]))))
             (. (or (. report ["test-inventory"]) []) (filter (fn [f] (return (-/matches f.intro query))))
                (map (fn [f] (return [:button {:key (+ f.path f.id) :onClick (fn [] (setUnit nil) (openFile f.path f.line.row setTest))} (+ f.path " · " (or f.intro f.id))]))))] nil)
          (. (:? query.length rows []) (map (fn [u] (return [:details {:key u.id}
            [:summary {:onClick (fn [] (openUnit u.id)) :onDoubleClick (fn [] (setTabs (fn [current] (return (:? (. current (includes u.id)) current (. current (concat [u.id])))))))} u.name [:small (+ " " u.namespace)]]
            [:button {:onClick (fn [] (openUnit u.id))} "Compare source and tests"]
            (. u.tests (map (fn [t] (return [:button {:key (+ t.path t.id) :onClick (fn [] (openFile t.path t.line.row setTest))} (+ t.id " · " t.assertions)]))))
            [:small (. (. u.flags (map -/flagText)) (join ", "))]]))))]))]))
(defn.js WorkspaceView [props]
  (var #{section setSection report setReport query setQuery unit setUnit source setSource test setTest profiles setProfiles session setSession snapshot setSnapshot panel setPanel error setError theme setTheme tabs setTabs navigationSequence sourceSequence testSequence problemPosition mobilePane setMobilePane history closed expanded setExpanded sidebarOpen setSidebarOpen panelOpen setPanelOpen layout setLayout palette setPalette preview setPreview legacy setLegacy openFile openUnit command runTests latest testRun rows} props)
  (return
    [:div {:className (+ "ide " (:? (== theme "vs-dark") "dark" "light") " show-" mobilePane (:? sidebarOpen " sidebar-open" " sidebar-closed"))}
     [:header [:strong "app.manage"] [:input {"aria-label" "Quick open" :placeholder "Search files and symbols" :value query :onChange (fn [e] (setQuery e.target.value))}]
      [:button {:className "mobile-switch" :onClick (fn [] (setMobilePane (:? (== mobilePane "source") "test" "source")))} (:? (== mobilePane "source") "Show tests" "Show source")]
      [:label [:input {:id "include-checks" :type "checkbox" :defaultChecked true
                       :onChange (fn [] (-/syncChecks (document.querySelector ".legacy")))}] "Build checks"]
      [:button {:onClick (fn [] (command "build"))} "Build"]
      [:button {:onClick (fn [] (command "check"))} "Check"]
      [:button {:onClick (fn [] (command (:? (and snapshot snapshot.watching) "watch-stop" "watch-start")))} (:? (and snapshot snapshot.watching) "Stop watch" "Watch")]
      [:button {:onClick (fn [] (setTheme (:? (== theme "vs-dark") "light" "vs-dark")))} "Theme"]]
     (:? palette [:div {:className "command-palette" :role "dialog" "aria-label" "Command palette"}
       (. ["Explorer" "Search" "Testing" "Application" "Build"] (map (fn [name] (return [:button {:key name :onClick (fn [] (setSection name) (setSidebarOpen true) (setPalette false))} (+ "Open " name)]))))
       [:button {:onClick (fn [] (. (-/getJSON "/api/units") (then setReport)) (setPalette false))} "Rescan source and tests"]] nil)
     [:div {:className "ide-body"}
      [:nav {"aria-label" "Activity bar"}
       (. ["Explorer" "Search" "Testing" "Application" "Build"] (map (fn [item]
         (return [:button {:key item :title item "aria-label" item "aria-pressed" (== section item) :onClick (fn [] (setSection item) (setSidebarOpen (:? (== section item) (not sidebarOpen) true)))} (. item (slice 0 1))]))))]
      (React.createElement -/ExplorerPanel props)
      [:div {:className "splitter explorer-splitter" :role "separator" "aria-label" "Resize explorer" "aria-orientation" "vertical" :tabIndex 0
             :onPointerDown (fn [event] (-/resizePane event ".explorer" "width" 1))
             :onKeyDown (fn [event] (-/resizeKey event ".explorer" "width" 1))}]
      [:main
       [:div {:className "tabs"}
        [:button {"aria-label" "Back" :onClick (fn [] (when (> history.current.position 0) (:= history.current.position (- history.current.position 1)) (openUnit (. history.current.items [history.current.position]) false)))} "←"]
        [:button {"aria-label" "Forward" :onClick (fn [] (when (< history.current.position (- history.current.items.length 1)) (:= history.current.position (+ history.current.position 1)) (openUnit (. history.current.items [history.current.position]) false)))} "→"]
        (:? (and preview (not (. tabs (includes preview)))) [:button {:onClick (fn [] (openUnit preview)) :onDoubleClick (fn [] (setTabs (. tabs (concat [preview]))))} (+ preview " · preview")] nil)
        (. tabs (map (fn [id] (return [:button {:key id :onClick (fn [] (openUnit id))} id
          [:span {:role "button" "aria-label" "Close tab" :onClick (fn [e] (e.stopPropagation) (. closed.current (push id)) (setTabs (. tabs (filter (fn [x] (return (not= x id)))))))} " ×"]]))))]
       (:? (or (== section "Application") (== section "Build"))
         [:iframe {:className "legacy" :title legacy :onLoad (fn [event] (-/syncChecks event.currentTarget))
                   :src (+ "/legacy?embedded=true&view="
                           (:? (and (== section "Build") (not (. ["pipeline" "previews" "outputs" "checks" "runs"] (includes legacy)))) "pipeline"
                               (and (== section "Application") (not (. ["pages" "slices"] (includes legacy)))) "pages" :else legacy))}]
         [:div {:className "editors" :style {"--source-width" (:? layout.source (+ layout.source "px") "50%")}}
          [:section [:div {:className "breadcrumb"} (or source.path "Select a functional unit")]
           [:div {:className "code"} (React.createElement -/Code {"file" source "theme" theme})]]
          [:div {:className "splitter editor-splitter" :role "separator" "aria-label" "Resize code split" "aria-orientation" "vertical" :tabIndex 0
                 :onPointerDown (fn [event] (-/resizePane event ".editors > section:first-child" "width" 1))
                 :onKeyDown (fn [event] (-/resizeKey event ".editors > section:first-child" "width" 1))}]
          [:section [:div {:className "breadcrumb"} (or test.path (:? unit "No linked test" "Linked tests"))]
           [:div {:className "test-actions"}
            (:? unit (. unit.tests (map (fn [t] (return [:button {:key (+ t.namespace t.id) :onClick (fn [] (runTests t.namespace))} (+ "Run " t.namespace (:? (== (. t ["link-kind"]) "inferred") " (inferred link)" ""))])))) nil)
            [:button {:onClick (fn []
              (var linked (and unit (. unit.tests (find (fn [f] (return (== f.path test.path)))))))
              (var script (and linked (. linked ["runtime-evidence"] scripts [0])))
              (setTest (Object.assign {} test {"row" (:? script script.line.row 1)})))} "Reveal runtime"]
            [:button {:onClick (fn []
              (var linked (and unit (. unit.tests (find (fn [f] (return (== f.path test.path)))))))
              (var setup (and linked (. linked ["setup-line"])))
              (if setup (setTest (Object.assign {} test {"row" setup.row})) (setError "No namespace setup declaration was found")))} "Reveal setup"]
            [:select {"aria-label" "Open helper source" :value "" :onChange (fn [e] (when e.target.value (openUnit e.target.value)))}
              [:option {:value ""} "Open helper…"]
              (. report.units (filter (fn [candidate]
                (return (and unit (. unit.tests (some (fn [f] (return (. (. f ["runtime-evidence"] calls) (includes candidate.id))))))))))
                (map (fn [candidate] (return [:option {:key candidate.id :value candidate.id} candidate.id]))))]]
           [:div {:className "code"} (React.createElement -/Code {"file" test "theme" theme})]]])
       [:section {:className "bottom-panel" :style {"height" (:? panelOpen layout.panel 32) "minHeight" (:? panelOpen 80 32) "resize" "none"}}
        [:div {:className "panel-splitter" :role "separator" "aria-label" "Resize results panel" "aria-orientation" "horizontal" :tabIndex 0
               :style {"display" (:? panelOpen "block" "none")}
               :onPointerDown (fn [event] (-/resizePane event ".bottom-panel" "height" -1))
               :onKeyDown (fn [event] (-/resizeKey event ".bottom-panel" "height" -1))}]
        [:div {:className "panel-tabs"} (. ["Problems" "Test results" "Output" "Runs"] (map (fn [p] (return [:button {:key p :onClick (fn [] (setPanelOpen (:? (== p panel) (not panelOpen) true)) (setPanel p))} p]))))]
        [:div {:className "panel-content" :style {"display" (:? panelOpen "block" "none")}} [:p {:role "status"} error]
         (:? (== panel "Problems")
           [:div
            (React.createElement -/Diagnostics {"items" report.diagnostics "open" (fn [d]
              (setUnit nil)
              (var file (. (or report.files []) (find (fn [file] (return (== file.path d.path))))))
              (openFile d.path (or (and d.line d.line.row) 1) (:? (and file (== file.kind "test")) setTest setSource)))})
           (. rows (filter (fn [u] (return u.flags.length))) (map (fn [u] (return [:button {:key u.id :onClick (fn [] (openUnit u.id))} (+ u.id " · " (. (. u.flags (map -/flagText)) (join ", ")))]))))]
           (:? (== panel "Test results")
             [:div
              [:p (or (and testRun testRun.status) "No test namespace has run")
                (:? (and testRun testRun.result (or testRun.result.stale (and report.fingerprint (not= report.fingerprint testRun.result.fingerprint)))) " · stale; rescan and rerun" "") ]
              [:p (and testRun testRun.result testRun.result.report testRun.result.report.message)]
              (React.createElement -/Results {"namespaces" (or (and testRun testRun.result testRun.result.report testRun.result.report.namespaces) [])
                                             "open" (fn [path row] (openFile path row setTest))})]
             [:pre (:? (== panel "Output") (-/liveOutput latest) (JSON.stringify (and snapshot snapshot.runs) nil 2))]))]]]]
     [:footer "Read-only · Structural findings and execution results are independent"]])
)
(defn.js Workspace []
  (var [section setSection] (React.useState "Explorer"))
  (var [report setReport] (React.useState {"units" [] "diagnostics" []}))
  (var [query setQuery] (React.useState ""))
  (var [unit setUnit] (React.useState nil))
  (var [source setSource] (React.useState {}))
  (var [test setTest] (React.useState {}))
  (var [profiles setProfiles] (React.useState []))
  (var [session setSession] (React.useState {}))
  (var [snapshot setSnapshot] (React.useState nil))
  (var [panel setPanel] (React.useState "Problems"))
  (var [error setError] (React.useState ""))
  (var [theme setTheme] (React.useState "vs-dark"))
  (var [tabs setTabs] (React.useState []))
  (var navigationSequence (React.useRef 0))
  (var sourceSequence (React.useRef 0))
  (var testSequence (React.useRef 0))
  (var problemPosition (React.useRef -1))
  (var [mobilePane setMobilePane] (React.useState "source"))
  (var history (React.useRef {"items" [] "position" -1}))
  (var closed (React.useRef []))
  (var [expanded setExpanded] (React.useState []))
  (var [sidebarOpen setSidebarOpen] (React.useState (fn [] (return (>= window.innerWidth 620)))))
  (var [panelOpen setPanelOpen] (React.useState true))
  (var [layout setLayout] (React.useState {}))
  (var [palette setPalette] (React.useState false))
  (var [preview setPreview] (React.useState nil))
  (var [legacy setLegacy] (React.useState "pages"))
  (React.useEffect (fn []
    (. (-/getJSON "/api/units") (then setReport) (catch (fn [e] (setError e.message))))
    (. (-/getJSON "/api/test-profiles") (then setProfiles) (catch (fn [e] (setError e.message))))
    (. (-/getJSON "/api/session") (then setSession))
    (var timer (setInterval (fn [] (. (-/getJSON "/api/state") (then setSnapshot))) 1500))
    (return (fn [] (clearInterval timer)))) [])
  (var openFile (fn [path row setter]
    (var sequence (:? (== setter setSource) sourceSequence testSequence))
    (:= sequence.current (+ sequence.current 1))
    (var current sequence.current)
    (. (-/getJSON (+ "/api/source?path=" (encodeURIComponent path)))
       (then (fn [file] (when (== sequence.current current) (:= file.row (or row 1)) (setter file))))
       (catch (fn [e] (setError e.message))))))
  (var openUnit (fn [id record]
    (:= navigationSequence.current (+ navigationSequence.current 1))
    (var sequence navigationSequence.current)
    (. (-/getJSON (+ "/api/units?unit=" (encodeURIComponent id)))
       (then (fn [value]
         (when (not= navigationSequence.current sequence) (return nil))
         (setUnit value)
         (when (and (not= record false) (not= (. history.current.items [history.current.position]) id))
           (:= history.current.items (. (. history.current.items (slice 0 (+ history.current.position 1))) (concat [id])))
           (:= history.current.position (- history.current.items.length 1)))
         (setPreview id)
         (openFile value.source.path value.source.line.row setSource)
         (if value.tests.length (openFile (. value.tests [0] path) (. value.tests [0] line row) setTest)
           (do (:= testSequence.current (+ testSequence.current 1)) (setTest {})))))
       (catch (fn [e] (setError e.message))))))
  (var command (fn [operation]
    (var checkbox (document.querySelector "#include-checks"))
    (setPanel "Runs") (setPanelOpen true)
    (. (fetch "/api/actions" {"method" "POST" "headers" {"Content-Type" "application/json" "X-App-Manage-Token" session.token}
             "body" (JSON.stringify {"operation" operation "tests" (:? checkbox checkbox.checked true)})})
       (then (fn [r] (return (r.json))))
       (then (fn [r] (when r.error (setError r.error))))
       (catch (fn [e] (setError e.message))))))
  (var runTests (fn [namespace]
    (var profile (. profiles (find (fn [p] (return (. p.namespaces (includes namespace)))))))
    (if (not profile) (setError "No execution profile is registered for this namespace")
      (do (setPanel "Test results") (setPanelOpen true)
          (. (fetch "/api/actions" {"method" "POST" "headers" {"Content-Type" "application/json" "X-App-Manage-Token" session.token}
                  "body" (JSON.stringify {"operation" "run-test-namespaces" "request" {"profile" profile.id "namespaces" [namespace]}})})
             (then (fn [r] (return (r.json)))) (then (fn [r] (when r.error (setError r.error)))))))))
  (React.useEffect (fn []
    (when session.project
      (try (var saved (JSON.parse (or (localStorage.getItem (+ "app.manage:" session.project)) "{}")))
           (setTabs (or saved.tabs [])) (setLayout (or saved.layout {})) (setExpanded (or saved.expanded [])) (setTheme (or saved.theme "vs-dark"))
           (catch e (setError "Saved workspace layout could not be restored"))))) [session.project])
  (React.useEffect (fn []
    (when session.project (localStorage.setItem (+ "app.manage:" session.project) (JSON.stringify {"tabs" tabs "theme" theme "layout" layout "expanded" expanded})))) [tabs theme layout expanded session.project])
  (React.useEffect (fn []
    (var handler (fn [e]
      (when (and (or e.metaKey e.ctrlKey) (== (. e.key (toLowerCase)) "p"))
        (e.preventDefault)
        (if e.shiftKey (setPalette true) (do (setSidebarOpen true) (setSection "Search") (. (document.querySelector "[aria-label='Quick open']") (focus)))))
      (when (and (or e.metaKey e.ctrlKey) (== (. e.key (toLowerCase)) "b")) (e.preventDefault) (setSidebarOpen (fn [value] (return (not value)))))
      (when (and (or e.metaKey e.ctrlKey) (== (. e.key (toLowerCase)) "j")) (e.preventDefault) (setPanelOpen (fn [value] (return (not value)))))
      (when (and (or e.metaKey e.ctrlKey) e.shiftKey (== (. e.key (toLowerCase)) "t") closed.current.length)
        (e.preventDefault) (var id (. closed.current (pop)))
        (setTabs (fn [current] (return (:? (. current (includes id)) current (. current (concat [id]))))))
        (openUnit id))
      (when (and (or e.metaKey e.ctrlKey) (== (. e.key (toLowerCase)) "w") preview)
        (e.preventDefault) (. closed.current (push preview))
        (setTabs (fn [current] (return (. current (filter (fn [id] (return (not= id preview))))))))
        (setPreview nil) (setUnit nil) (setSource {}) (setTest {}))
      (when (and e.altKey (or (== e.key "ArrowLeft") (== e.key "ArrowRight")))
        (e.preventDefault)
        (var position (+ history.current.position (:? (== e.key "ArrowLeft") -1 1)))
        (when (and (>= position 0) (< position history.current.items.length))
          (:= history.current.position position) (openUnit (. history.current.items [position]) false)))
      (when (== e.key "F8")
        (var problems (. report.units (filter (fn [u] (return u.flags.length)))))
        (when problems.length
          (e.preventDefault)
          (:= problemPosition.current (mod (+ problemPosition.current (:? e.shiftKey -1 1) problems.length) problems.length))
          (openUnit (. problems [problemPosition.current] id))))
      (when (and (. e.target (closest ".explorer")) (. ["ArrowUp" "ArrowDown" "ArrowLeft" "ArrowRight"] (includes e.key)))
        (e.preventDefault)
        (var nodes (. (Array.from (document.querySelectorAll ".explorer summary,.explorer button"))
                     (filter (fn [node] (return (. (node.getClientRects) length))))))
        (var index (. nodes (indexOf e.target)))
        (when (or (== e.key "ArrowDown") (== e.key "ArrowUp"))
          (var next (. nodes [ (+ index (:? (== e.key "ArrowDown") 1 -1)) ]))
          (when next (next.focus)))
        (var details (. e.target (closest "details")))
        (when (and details (or (== e.key "ArrowLeft") (== e.key "ArrowRight"))) (:= details.open (not= e.key "ArrowLeft"))))
      (when (== e.key "Escape") (setPalette false))))
    (document.addEventListener "keydown" handler)
    (return (fn [] (document.removeEventListener "keydown" handler)))) [preview report.units])
  (React.useEffect (fn []
    (when report.fingerprint
      (setTabs (fn [current] (return (. current (filter (fn [id] (return (. report.units (some (fn [u] (return (== u.id id)))))))))))))) [report.fingerprint])
  (React.useEffect (fn []
    (var resized (fn []
      (var explorer (document.querySelector ".explorer"))
      (var panel (document.querySelector ".bottom-panel"))
      (var codePane (document.querySelector ".editors > section:first-child"))
      (when (and explorer panel) (setLayout (fn [current] (return {"explorer" (or explorer.offsetWidth current.explorer 280) "panel" (:? (> panel.offsetHeight 80) panel.offsetHeight (or current.panel 230))
                                               "source" (:? (and codePane codePane.offsetWidth) codePane.offsetWidth current.source)}))))))
    (document.addEventListener "pointerup" resized)
    (return (fn [] (document.removeEventListener "pointerup" resized)))) [])
  (var latest (and snapshot (. snapshot.runs [0])))
  (var testRun (and snapshot (. snapshot.runs (find (fn [run] (return (== run.operation "run-test-namespaces")))))))
  (var rows (. report.units (filter (fn [u] (return (or (-/matches u.id query) (-/matches u.source.path query)
                                                  (. u.tests (some (fn [f] (return (-/matches f.intro query)))))))))))
  (return (React.createElement -/WorkspaceView {"section" section "setSection" setSection "report" report "setReport" setReport "query" query "setQuery" setQuery "unit" unit "setUnit" setUnit "source" source "setSource" setSource "test" test "setTest" setTest "profiles" profiles "setProfiles" setProfiles "session" session "setSession" setSession "snapshot" snapshot "setSnapshot" setSnapshot "panel" panel "setPanel" setPanel "error" error "setError" setError "theme" theme "setTheme" setTheme "tabs" tabs "setTabs" setTabs "navigationSequence" navigationSequence "sourceSequence" sourceSequence "testSequence" testSequence "problemPosition" problemPosition "mobilePane" mobilePane "setMobilePane" setMobilePane "history" history "closed" closed "expanded" expanded "setExpanded" setExpanded "sidebarOpen" sidebarOpen "setSidebarOpen" setSidebarOpen "panelOpen" panelOpen "setPanelOpen" setPanelOpen "layout" layout "setLayout" setLayout "palette" palette "setPalette" setPalette "preview" preview "setPreview" setPreview "legacy" legacy "setLegacy" setLegacy "openFile" openFile "openUnit" openUnit "command" command "runTests" runTests "latest" latest "testRun" testRun "rows" rows})))
