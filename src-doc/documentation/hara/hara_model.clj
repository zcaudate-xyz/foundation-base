(ns documentation.hara-model
  (:require [hara.lang :as l]
            [hara.model.spec-js :as js]
            [hara.model.spec-lua :as lua])
  (:use code.test))

[[:hero {:title "hara.model"
         :subtitle "Target language specifications and xtalk function libraries."
         :lead "`hara.model` contains target specs for JS, Lua, Python, Go, Dart, SQL, Solidity, xtalk, and annex languages. These models are the bridge between structured hara.lang forms and emitted target code."}]]

[[:chapter {:title "Motivation"}]]
"A language model owns target syntax, helper functions, type declarations, and runtime-specific emission rules. Generated projects in `src-build/play` use these models when producing Go, TypeScript, Lua, C, and JS artifacts."

[[:chapter {:title "API"}]]

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Emitting code to a target"}]]

"`hara.lang/emit-as` is the main entry point for turning a hara form into target source code. The same expression can be rendered for different language models just by changing the target keyword."

(fact "arithmetic emits to multiple targets"
  (l/emit-as :js '[(+ 1 2)])
  => "1 + 2"

  (l/emit-as :lua '[(+ 1 2)])
  => "1 + 2"

  (l/emit-as :python '[(+ 1 2)])
  => "1 + 2")

[[:section {:title "Data literals"}]]

"Maps and vectors are lowered into each target's native data syntax. The JS model uses JSON-like object notation, Lua uses table syntax, and Python uses dict/list literals."

(fact "maps and vectors render in target syntax"
  (l/emit-as :js '[{:a 1 :b 2}])
  => "{\"a\":1,\"b\":2}"

  (l/emit-as :lua '[{:a 1 :b 2}])
  => "{a=1,b=2}"

  (l/emit-as :js '[[1 2 3]])
  => "[1,2,3]"

  (l/emit-as :lua '[[1 2 3]])
  => "setmetatable({1,2,3}, cjson.array_mt)")

[[:section {:title "Functions and control flow"}]]

"Function definitions and conditionals are expanded into the target language's statements. The body is emitted with the target's block and return conventions."

(fact "define functions in JS and Lua"
  (l/emit-as :js '[(defn add [a b] (return (+ a b)))])
  => "function add(a,b){\n  return a + b;\n}"

  (l/emit-as :lua '[(defn add [a b] (return (+ a b)))])
  => "local function add(a,b)\n  return a + b\nend")

(fact "if/else becomes target conditionals"
  (l/emit-as :js '[(if (> x 0) (return x) (return 0))])
  => "if(x > 0){\n  return x;\n}\nelse{\n  return 0;\n}"

  (l/emit-as :lua '[(if (> x 0) (return x) (return 0))])
  => "if x > 0 then\n  return x\nelse\n  return 0\nend")

[[:section {:title "Target-specific helpers"}]]

"Each model exposes small helper functions for target-specific edge cases. The JS model has `js-regex` and `js-map-key`; the Lua model has `lua-map-key`."

(fact "JS helpers"
  (js/js-regex #"abc")
  => "/abc/"

  (js/js-map-key :hello js/+grammar+ {})
  => "\"hello\""

  (js/js-map-key 'x js/+grammar+ {})
  => "[x]")

(fact "Lua helpers"
  (lua/lua-map-key :abc lua/+grammar+ {})
  => "abc"

  (lua/lua-map-key 123 lua/+grammar+ {})
  => "[123]")

;; BEGIN merged documentation: guides/MANAGE_XTALK.md
;; sha256: 5d3bf37e18ce83b1dabca7c89e88b976714977d3c3d01f16cbabcd21c9a4ac1d
[[:chapter {:title "Xtalk Management Guide" :link "merged-guides-manage-xtalk-md"}]]

"`xtalk` (cross-talk) is a polymorphic template language that transpiles lisp to multiple runtime targets (JavaScript, Lua, Python, R, Ruby, Dart, Go, and others). The xtalk management system provides auditing, scaffolding, and maintenance tools for tracking language support, test coverage, and runtime compatibility across the codebase."

[[:section {:title "Core Concepts" :link "merged-guides-manage-xtalk-md-core-concepts"}]]

[[:subsection {:title "Xtalk Model Layers" :link "merged-guides-manage-xtalk-md-xtalk-model-layers"}]]

"The xtalk system is organized into distinct layers:"

"| Layer | Purpose | Location |\n|-------|---------|----------|\n| **Grammar** | Core syntax and operators for xtalk transpilation | `src/std/lang/base/grammar_xtalk.clj` |\n| **Model** | Language-specific transpilation rules (fn_*, com_*) | `src/std/lang/model/spec_xtalk/` |\n| **Typed** | Type analysis and declaration generation | `src/std/lang/typed/xtalk_*.clj` |\n| **Runtime** | Language runtime implementations and live eval | `src/rt/` |\n| **Tests** | Grammar tests and runtime integration tests | `test/std/lang/model/spec_xtalk/` |"

[[:subsection {:title "Key Concepts" :link "merged-guides-manage-xtalk-md-key-concepts"}]]

"- **Function Model** (`fn_<lang>.clj`): Defines how xtalk functions transpile to a specific language\n- **Component Model** (`com_<lang>.clj`): Defines xtalk data structure transpilation (records, dicts, etc.)\n- **Support Matrix**: A semantic grid tracking which xtalk features (operators, constructs) are implemented vs. abstract vs. missing for each language\n- **Inventory**: Cataloging of model definitions, test coverage, and runtime availability"

[[:section {:title "Common Tasks" :link "merged-guides-manage-xtalk-md-common-tasks"}]]

[[:subsection {:title "1. Status and Inventory" :link "merged-guides-manage-xtalk-md-1-status-and-inventory"}]]

"**Get overall xtalk status**"

[[:code {:lang "bash"} "lein manage status :with xtalk"]]

"From REPL:"

[[:code {:lang "clojure"} "(require '[code.manage.xtalk :as manage])\n\n;; Get model file inventory by language\n(manage/xtalk-model-inventory)\n;; => {:js {:model-files [...], :model-forms #{:fn :com}, :model-count 2}\n;;     :lua {:model-files [...], ...}\n;;     :python {...}}\n\n;; Get test coverage by language\n(manage/xtalk-test-inventory)\n;; => {:js {:test-files [...], :test-forms #{:fn :com}, :test-count 2}\n;;     ...}\n\n;; Get runtime installation and spec support\n(manage/xtalk-runtime-inventory)\n;; => {:js {:runtime-installed? true, :runtime-executable? true, \n;;          :spec-implemented 42, :spec-abstract 3, :spec-missing 0}\n;;     :lua {:runtime-installed? false, ...}\n;;     :python {...}}\n\n;; Get spec implementation status\n(manage/xtalk-spec-inventory)\n;; => {:js {:spec-tracked? true, :spec-feature-count 15,\n;;          :spec-implemented 12, :spec-abstract 2, :spec-missing 1}\n;;     ...}\n\n;; Unified language status (model + runtime + spec + test)\n(manage/xtalk-language-status)\n;; => {:js {:model-count 2, :test-count 2, :coverage 1.0,\n;;          :runtime-installed? true, :spec-implemented 42, ...}\n;;     ...}"]]

[[:subsection {:title "2. Spec and Feature Auditing" :link "merged-guides-manage-xtalk-md-2-spec-and-feature-auditing"}]]

"**Audit xtalk feature support across languages**"

[[:code {:lang "bash"} "# Full audit of all supported languages\nlein manage audit-languages\n\n# View feature status matrix\nlein manage support-matrix"]]

"From REPL:"

[[:code {:lang "clojure"} ";; Get list of xtalk categories (feature groups)\n(manage/xtalk-categories)\n;; => [:xtalk-conditional :xtalk-loop :xtalk-error ...]\n\n;; Get all xtalk operations\n(manage/xtalk-op-map)\n;; => {:x:if {...}, :x:do {...}, :x:try {...}, ...}\n\n;; Get all x:* symbols (xtalk operators)\n(manage/xtalk-symbols)\n;; => [:x:if :x:do :x:try :x:throw :x:let ...]\n\n;; Check feature implementation for specific language\n(manage/feature-status :js :x:if)\n;; => :implemented\n\n(manage/feature-status :lua :x:async)\n;; => :missing\n\n;; Get full support matrix (all languages + features)\n(manage/support-matrix)\n;; => {:languages [:js :lua :python :r :ruby]\n;;     :features [:x:if :x:do :x:try ...]\n;;     :status {:js {...}, :lua {...}, ...}\n;;     :summary {:js {:implemented 40, :abstract 2, :missing 1}, ...}}\n\n;; Get support matrix for specific languages\n(manage/support-matrix [:js :python])\n\n;; Get support matrix for specific features\n(manage/support-matrix nil [:x:if :x:do :x:let])\n\n;; Audit installed language runtimes\n(manage/installed-languages)\n;; => [:js :lua :python :r]\n\n;; Find what's missing for each language\n(manage/missing-by-language)\n;; => {:lua [...], :python [...], :r [...]}\n\n;; Find which languages are missing specific features\n(manage/missing-by-feature)\n;; => {:x:async [:lua :r], :x:spread [:python], ...}"]]

[[:subsection {:title "3. Xtalk Operations Inventory" :link "merged-guides-manage-xtalk-md-3-xtalk-operations-inventory"}]]

"**Manage and document xtalk operator definitions**"

[[:code {:lang "clojure"} ";; Generate operation inventory (reads grammar-xtalk)\n(manage/generate-xtalk-ops)\n;; Generates config/xtalk/xtalk_ops.edn with all operator details\n\n;; The result includes:\n;; {:op :x:if\n;;  :category :xtalk-conditional\n;;  :canonical-symbol hara.lang.base.grammar-xtalk/x:if\n;;  :symbols [x:if]\n;;  :macro hara.lang.base.grammar-xtalk/x:if\n;;  :doc \"Conditional expression\"\n;;  :cases [...]}"]]

[[:subsection {:title "4. Test Scaffolding and Generation" :link "merged-guides-manage-xtalk-md-4-test-scaffolding-and-generation"}]]

"**Generate missing tests for grammar operations**"

[[:code {:lang "bash"} "# Scaffold grammar tests from operations inventory\nlein manage scaffold-xtalk-grammar-tests"]]

"From REPL:"

[[:code {:lang "clojure"} ";; Scaffold grammar operation tests\n(manage/scaffold-xtalk-grammar-tests {:write true})\n;; Generates test/std/lang/base/grammar_xtalk_ops_test.clj\n\n;; Split grammar tests by runtime language\n(manage/separate-runtime-tests {:lang :js :write true})\n;; Generates: test/std/lang/base/grammar_xtalk_js_test.clj"]]

[[:subsection {:title "5. Language Support and Diagnostics" :link "merged-guides-manage-xtalk-md-5-language-support-and-diagnostics"}]]

"**Check which language runtimes are installed and available**"

[[:code {:lang "clojure"} ";; Get installed language runtimes\n(manage/installed-languages)\n;; => [:js :lua :python]\n\n;; Get languages that use xtalk as parent grammar\n(manage/audit-languages)\n;; => [:js :lua :python :r :rb :dart]\n\n;; Filter to only installed languages with xtalk parent\n(manage/audit-languages [:js :python :go])\n;; => [:js :python]  (go removed if not installed)\n\n;; Visualize support matrix\n(manage/visualize-support)\n;; Formatted ASCII table showing language vs. feature support"]]

[[:subsection {:title "6. Code Generation and Operations" :link "merged-guides-manage-xtalk-md-6-code-generation-and-operations"}]]

"**Generate xtalk operator implementations**"

[[:code {:lang "clojure"} ";; Generate default implementations for missing operators\n(manage/generate-xtalk-ops {:write true})\n;; Creates/updates operator configuration\n\n;; List all management operations\n(manage/xtalk-op-map)\n;; => All available management commands and their details"]]

[[:section {:title "Workflow Examples" :link "merged-guides-manage-xtalk-md-workflow-examples"}]]

[[:subsection {:title "Workflow 1: Add Support for a New Language" :link "merged-guides-manage-xtalk-md-workflow-1-add-support-for-a-new-language"}]]

"Steps to add a new target language (e.g., Go):"

"1. **Create grammar specification**\n   - Add `:go` to runtime languages in `src/std/lang/manage/xtalk_scaffold.clj`\n   - Define `+runtime-lang-config+` entry for Go\n\n2. **Create function model**\n   - Create `src/std/lang/model/spec_xtalk/fn_go.clj`\n   - Define how xtalk functions transpile to Go\n\n3. **Create component model**\n   - Create `src/std/lang/model/spec_xtalk/com_go.clj`\n   - Define how xtalk data structures transpile to Go\n\n4. **Create tests**\n   - Create `test/std/lang/model/spec_xtalk/fn_go_test.clj`\n   - Create `test/std/lang/model/spec_xtalk/com_go_test.clj`\n\n5. **Audit coverage**\n   ```clojure\n   (manage/xtalk-language-status {:langs [:go]})\n   ;; Check model-count, test-count, spec coverage\n   ```\n\n6. **Generate operator tests**\n   ```clojure\n   (manage/scaffold-xtalk-grammar-tests {:write true})\n   ;; Generates grammar tests for Go\n   ```"

[[:subsection {:title "Workflow 2: Improve Language Coverage" :link "merged-guides-manage-xtalk-md-workflow-2-improve-language-coverage"}]]

"When you have missing features (`:spec-missing > 0`):"

"1. **Identify missing features**\n   ```clojure\n   (manage/missing-by-language)\n   ;; Shows which operators are not yet implemented\n   ```\n\n2. **Update language model**\n   - Edit `src/std/lang/model/spec_xtalk/fn_<lang>.clj`\n   - Add implementation for missing operator (change from `:abstract` to `:emit`)\n\n3. **Verify coverage**\n   ```clojure\n   (manage/support-matrix [:your-lang])\n   ;; Check that missing count decreased\n   ```\n\n4. **Add tests**\n   - Create appropriate test cases in language-specific test file\n   - Run: `lein test :only hara.lang.model.spec-xtalk.<lang>-test`"

[[:subsection {:title "Workflow 3: Audit Test Coverage" :link "merged-guides-manage-xtalk-md-workflow-3-audit-test-coverage"}]]

"Ensure every model definition has corresponding tests:"

[[:code {:lang "clojure"} ";; Get model and test inventory\n(let [model (manage/xtalk-model-inventory)\n      tests (manage/xtalk-test-inventory)]\n  (doseq [[lang model-entry] model]\n    (let [test-entry (get tests lang)\n          model-count (:model-count model-entry)\n          test-count (:test-count test-entry)\n          coverage (if (pos? model-count) \n                     (double (/ test-count model-count)) \n                     0.0)]\n      (println (format \"%s: %d models, %d tests (%.1f%%)\"\n                       lang model-count test-count (* 100 coverage))))))"]]

[[:section {:title "Command Reference" :link "merged-guides-manage-xtalk-md-command-reference"}]]

[[:subsection {:title "CLI Commands" :link "merged-guides-manage-xtalk-md-cli-commands"}]]

"All commands are invoked via `lein manage` with the pattern:"

[[:code {:lang "bash"} "lein manage <task> [selectors] [options]"]]

"**Xtalk-specific tasks:**"

"| Task | Description | Example |\n|------|-------------|---------|\n| `status :with xtalk` | Show overall xtalk status | `lein manage status :with xtalk` |\n| `audit-languages` | Check installed language runtimes | `lein manage audit-languages` |\n| `support-matrix` | Display feature support matrix | `lein manage support-matrix` |\n| `missing-by-language` | Show missing operators per language | `lein manage missing-by-language` |\n| `missing-by-feature` | Show which languages lack features | `lein manage missing-by-feature` |\n| `generate-xtalk-ops` | Generate operation inventory | `lein manage generate-xtalk-ops` |\n| `scaffold-xtalk-grammar-tests` | Create test skeletons for operators | `lein manage scaffold-xtalk-grammar-tests` |\n| `separate-runtime-tests` | Split grammar tests by language | `lein manage separate-runtime-tests :lang js` |"

[[:subsection {:title "API Reference" :link "merged-guides-manage-xtalk-md-api-reference"}]]

"**From `code.manage.xtalk` namespace:**"

[[:subsubsection {:title "Inventory Functions" :link "merged-guides-manage-xtalk-md-inventory-functions"}]]

[[:code {:lang "clojure"} "(xtalk-model-inventory)        ; => {:js {...}, :lua {...}, ...}\n(xtalk-test-inventory)         ; => {:js {...}, :lua {...}, ...}\n(xtalk-runtime-inventory)      ; => {:js {...}, :lua {...}, ...}\n(xtalk-spec-inventory)         ; => {:js {...}, :lua {...}, ...}\n(xtalk-language-status)        ; => {:js {...}, :lua {...}, ...}"]]

[[:subsubsection {:title "Audit Functions" :link "merged-guides-manage-xtalk-md-audit-functions"}]]

[[:code {:lang "clojure"} "(installed-languages)          ; => [:js :lua :python :r]\n(audit-languages)              ; => [:js :lua :python]\n(audit-languages [:js :go])    ; => [:js] (filtered to installed)\n(support-matrix)               ; => {:languages [...] :features [...] :status {...}}\n(support-matrix [:js :lua])    ; => Support matrix for specific languages\n(support-matrix nil [:x:if])   ; => Support matrix for specific features\n(missing-by-language)          ; => {:lua [...] :go [...] :r [...]}\n(missing-by-feature)           ; => {:x:async [:lua] :x:spread [...] ...}"]]

[[:subsubsection {:title "Metadata Functions" :link "merged-guides-manage-xtalk-md-metadata-functions"}]]

[[:code {:lang "clojure"} "(xtalk-categories)             ; => [:xtalk-conditional :xtalk-loop ...]\n(xtalk-op-map)                 ; => {:x:if {...} :x:do {...} ...}\n(xtalk-symbols)                ; => [:x:if :x:do :x:try ...]"]]

[[:subsubsection {:title "Generation Functions" :link "merged-guides-manage-xtalk-md-generation-functions"}]]

[[:code {:lang "clojure"} "(generate-xtalk-ops)           ; Generate/update operator inventory\n(scaffold-xtalk-grammar-tests) ; Generate grammar test skeleton\n(separate-runtime-tests)       ; Split tests by runtime language\n(visualize-support)            ; Display formatted support matrix"]]

[[:section {:title "File Structure" :link "merged-guides-manage-xtalk-md-file-structure"}]]

[[:subsection {:title "Source Organization" :link "merged-guides-manage-xtalk-md-source-organization"}]]

[[:code {:lang "text"} "src/std/lang/\n├── manage/\n│   ├── xtalk_audit.clj        # Audit and feature tracking\n│   ├── xtalk_ops.clj          # Operation inventory management\n│   ├── xtalk_scaffold.clj     # Test generation and scaffolding\n│   └── manage.clj             # Main coordination\n├── model/\n│   └── spec_xtalk/\n│       ├── fn_js.clj          # JavaScript function transpilation\n│       ├── fn_lua.clj         # Lua function transpilation\n│       ├── fn_python.clj      # Python function transpilation\n│       ├── com_js.clj         # JavaScript component transpilation\n│       └── ...\n├── typed/\n│   ├── xtalk.clj              # Core typed system\n│   ├── xtalk_analysis.clj     # Type analysis\n│   ├── xtalk_check.clj        # Type checking\n│   ├── xtalk_parse.clj        # Parsing typed definitions\n│   └── xtalk_ops.clj          # Typed operations\n└── base/\n    └── grammar_xtalk.clj      # Core xtalk grammar definitions"]]

[[:subsection {:title "Configuration" :link "merged-guides-manage-xtalk-md-configuration"}]]

[[:code {:lang "text"} "config/xtalk/\n└── xtalk_ops.edn             # Operation inventory (managed)"]]

[[:subsection {:title "Tests" :link "merged-guides-manage-xtalk-md-tests"}]]

[[:code {:lang "text"} "test/std/lang/\n└── model/\n    └── spec_xtalk/\n        ├── fn_js_test.clj     # JavaScript function tests\n        ├── fn_lua_test.clj    # Lua function tests\n        ├── com_js_test.clj    # JavaScript component tests\n        └── ..."]]

[[:section {:title "Common Issues and Solutions" :link "merged-guides-manage-xtalk-md-common-issues-and-solutions"}]]

[[:subsection {:title "Issue 1: Missing Runtime for Language" :link "merged-guides-manage-xtalk-md-issue-1-missing-runtime-for-language"}]]

"**Problem:** `(manage/xtalk-runtime-inventory)` shows `:runtime-installed? false` for a language you need."

"**Solution:**"

"- Install the runtime: `apt-get install nodejs` (for JavaScript), etc.\n- Or, if it's already installed, ensure it's on your PATH: `which node`\n- Verify with `(manage/installed-languages)`"

[[:subsection {:title "Issue 2: Features Showing as Missing" :link "merged-guides-manage-xtalk-md-issue-2-features-showing-as-missing"}]]

"**Problem:** Language has `:spec-missing > 0` when adding a new language."

"**Solution:**"

"1. Check which features are missing:\n   ```clojure\n   (manage/missing-by-language)\n   ```\n2. For each missing feature, update the grammar in the language's model files:\n   - Edit `src/std/lang/model/spec_xtalk/fn_<lang>.clj`\n   - Change operator status from `:abstract` to `:emit` with implementation\n3. Optionally provide a partial implementation with `:abstract :emit` to indicate \n   work-in-progress"

[[:subsection {:title "Issue 3: Test Coverage is Low" :link "merged-guides-manage-xtalk-md-issue-3-test-coverage-is-low"}]]

"**Problem:** A language has models but few tests (`:coverage < 1.0`)."

"**Solution:**"

"1. Identify uncovered models:\n   ```clojure\n   (let [model (manage/xtalk-model-inventory)\n         tests (manage/xtalk-test-inventory)]\n     (doseq [[lang entry] model]\n       (when (> (:model-count entry) (get-in tests [lang :test-count] 0))\n         (println \"Coverage gap for\" lang))))\n   ```\n2. Create corresponding test files in `test/std/lang/model/spec_xtalk/`\n3. Run: `lein test :only hara.lang.model.spec_xtalk.<lang>-test`"

[[:subsection {:title "Issue 4: Support Matrix Shows Incomplete Data" :link "merged-guides-manage-xtalk-md-issue-4-support-matrix-shows-incomplete-data"}]]

"**Problem:** Some languages or features aren't appearing in `(manage/support-matrix)`."

"**Solution:**"

"- Ensure the language has been required/loaded: `(manage/audit-languages)`\n- Check that grammar definitions are properly tagged with `x:` prefix\n- Verify the language grammar is properly registered in \n  `src/std/lang/base/registry.clj`"

[[:subsection {:title "Issue 5: Operator Inventory is Stale" :link "merged-guides-manage-xtalk-md-issue-5-operator-inventory-is-stale"}]]

"**Problem:** New operators aren't appearing in `(manage/xtalk-op-map)`."

"**Solution:**"

"1. Regenerate the inventory:\n   ```clojure\n   (manage/generate-xtalk-ops {:write true})\n   ```\n2. This reads the latest grammar definitions and updates `config/xtalk/xtalk_ops.edn`\n3. Verify with: `(keys (manage/xtalk-op-map))`"

[[:section {:title "Advanced Tasks" :link "merged-guides-manage-xtalk-md-advanced-tasks"}]]

[[:subsection {:title "Analyzing Operator Metadata" :link "merged-guides-manage-xtalk-md-analyzing-operator-metadata"}]]

[[:code {:lang "clojure"} ";; Get details for a specific operator\n(get (manage/xtalk-op-map) :x:if)\n;; => {:op :x:if\n;;     :category :xtalk-conditional\n;;     :canonical-symbol ...\n;;     :symbols [x:if]\n;;     :class :infix\n;;     :requires [...]\n;;     :emit :code\n;;     :macro ...\n;;     :doc \"...\"\n;;     :cases [...]}"]]

[[:subsection {:title "Tracking Language Feature Gaps" :link "merged-guides-manage-xtalk-md-tracking-language-feature-gaps"}]]

[[:code {:lang "clojure"} ";; For each language, see which features block full implementation\n(let [matrix (manage/support-matrix)]\n  (doseq [[lang summary] (:summary matrix)]\n    (let [missing (:missing summary)]\n      (when (pos? missing)\n        (println (format \"%s needs %d features\" lang missing))))))"]]

[[:subsection {:title "Custom Coverage Analysis" :link "merged-guides-manage-xtalk-md-custom-coverage-analysis"}]]

[[:code {:lang "clojure"} ";; Calculate detailed metrics\n(defn coverage-report []\n  (let [langs (manage/audit-languages)\n        model (manage/xtalk-model-inventory)\n        tests (manage/xtalk-test-inventory)\n        specs (manage/xtalk-spec-inventory)]\n    (doseq [lang langs\n            :let [m (get model lang) t (get tests lang) s (get specs lang)]]\n      (when m\n        (printf \"%-8s | Models: %3d | Tests: %3d | Cov: %5.1f%% | \"\n                lang (:model-count m) (:test-count t)\n                (* 100.0 (/ (:test-count t 0) (:model-count m 1))))\n        (printf \"Features: %2d impl, %2d missing\\n\"\n                (:spec-implemented s 0) (:spec-missing s 0))))))\n\n(coverage-report)"]]

[[:section {:title "Related Guides" :link "merged-guides-manage-xtalk-md-related-guides"}]]

"- [code.manage](code.manage.md) - General code management and maintenance tasks\n- [code.test](code.test.md) - Testing framework used for xtalk tests\n- [std.task](std.task.md) - Task execution engine underlying management operations\n- [README.md](../README.md) - Overview of `hara.lang` and xtalk system"
;; END merged documentation: guides/MANAGE_XTALK.md

;; BEGIN merged documentation: plans/slop/deftype_pg_usage.md
;; sha256: be13cdb6bcc968686910fa64fdc3f24061d4ca59cc861cd8cb19691dd41cbebd
[[:chapter {:title "Comprehensive Usage Guide for deftype.pg" :link "merged-plans-slop-deftype-pg-usage-md"}]]
"# Comprehensive Usage Guide for `deftype.pg`\n\nThis guide provides a detailed overview of `deftype.pg`, the macro used within the `hara.lang` ecosystem to define PostgreSQL table schemas. It covers basic syntax, column configuration, relationships, and advanced settings.\n\n## 1. Introduction\n\n`deftype.pg` serves as the primary mechanism for defining data models in the `rt.postgres` runtime. It declaratively defines tables, columns, constraints, and relationships, which are then compiled into standard PostgreSQL `CREATE TABLE` statements. It integrates with the project's registry for type checking and reference resolution.\n\n## 2. Basic Syntax\n\nThe basic structure of a `deftype.pg` form is:\n\n```clojure\n(deftype.pg TableName\n  [column-1 {:key val ...}\n   column-2 {:key val ...}]\n  {table-options})\n```\n\n-   **TableName**: A symbol representing the table name.\n-   **Column Vector**: A vector of alternating column names (symbols/keywords) and property maps.\n-   **Table Options**: A map for table-level configuration (e.g., constraints, partitioning).\n\n## 3. Column Definitions\n\nEach column is defined by a property map.\n\n### Core Properties\n\n| Key | Description | Example |\n| :--- | :--- | :--- |\n| `:type` | The PostgreSQL type (e.g., `:text`, `:int`, `:uuid`, `:boolean`, `:jsonb`). | `{:type :text}` |\n| `:primary` | Marks the column as a primary key. | `{:primary true}` |\n| `:required` | Adds a `NOT NULL` constraint. | `{:required true}` |\n| `:unique` | Adds a `UNIQUE` constraint. | `{:unique true}` |\n| `:default` | Sets the default value. | `{:default \"now()\"}` |\n| `:enum` | Specifies an enum type definition. | `{:type :enum :enum {:ns 'my.ns/MyEnum}}` |\n\n### Example\n\n```clojure\n(deftype.pg User\n  [:id    {:type :uuid :primary true}\n   :name  {:type :text :required true}\n   :role  {:type :enum :enum {:ns 'my.app/UserRole}}])\n```\n\n## 4. SQL Customization (`:sql`)\n\nThe `:sql` sub-map allows fine-grained control over the generated SQL for a specific column.\n\n-   **`:cascade`**: Adds `ON DELETE CASCADE` (often used with foreign keys).\n-   **`:check`**: Adds a column-level `CHECK` constraint.\n-   **`:unique`**: If a string is provided, groups columns into a composite unique constraint.\n-   **`:index`**: Creates an index for the column.\n    -   `true`: Simple index.\n    -   `{:using :gin :where ...}`: Advanced index configuration.\n\n```clojure\n(deftype.pg Item\n  [:data {:type :jsonb :sql {:index {:using :gin}}}\n   :age  {:type :int   :sql {:check (> age 0)}}])\n```\n\n## 5. Relationships (Foreign Keys)\n\nForeign keys are defined using the `:ref` type. The system automatically handles naming (e.g., appending `_id`) and validation.\n\n### Syntax\n\n```clojure\n:owner {:type :ref\n        :ref  {:ns 'target.namespace/TargetTable}}\n```\n\n### Features\n-   **Automatic Naming**: A column named `:user` referencing `User` becomes `user_id` in SQL.\n-   **Hydration**: Validates that the referenced namespace and table exist in the project registry.\n-   **Cross-Module References**: Supports referencing tables in different schemas/modules.\n\n## 6. Table Options\n\nThe third argument to `deftype.pg` handles table-wide settings.\n\n### Constraints\nDefine named `CHECK` constraints.\n\n```clojure\n{:constraints {:check_positive_balance (> balance 0)}}\n```\n\n### Partitioning\nDefine table partitioning strategies.\n\n```clojure\n{:partition-by [:range :created_at]}\n```\n\n### Custom SQL\nAppend raw SQL fragments to the table definition.\n\n```clojure\n{:custom [\"CONSTRAINT custom_rule ...\"]}\n```\n\n## 7. Metadata Configuration\n\nBehavior can be controlled via metadata on the table symbol.\n\n-   **Schema**: `^{:static/schema \"public\"}` defines the PostgreSQL schema.\n-   **Tracking**: `^{:track [...]}` adds audit columns (e.g., `created_at`, `updated_at`) via a tracking strategy (e.g., `rt.postgres.grammar.common-tracker/TrackingMin`).\n-   **Lifecycle**: `^{:final true}` prevents `DROP TABLE IF EXISTS` generation (useful for production).\n-   **Composition**: `^{:prepend [...] :append [...]}` mixes in column definitions from fragments.\n\n## 8. Advanced Features\n\n### Composite Keys\nDefining multiple columns with `:primary true` creates a composite primary key.\n\n### Composite Uniques\nUse the `:sql {:unique \"group_name\"}` property on multiple columns to create a composite unique constraint.\n\n### Indexes\nIndexes are automatically collected and generated as separate `CREATE INDEX` statements.\n\n## 9. Comprehensive Example\n\n```clojure\n(deftype.pg ^{:static/schema \"app\"\n              :track [rt.postgres.grammar.common-tracker/TrackingMin]}\n  Order\n  [:id          {:type :uuid :primary true}\n   :user        {:type :ref  :ref {:ns 'app/User} :sql {:cascade true}}\n   :status      {:type :text :default \"pending\"}\n   :items       {:type :jsonb :sql {:index {:using :gin}}}\n   :total       {:type :numeric :required true}]\n  {:constraints {:positive_total (> total 0)}})\n```\n"
;; END merged documentation: plans/slop/deftype_pg_usage.md

;; BEGIN merged documentation: plans/slop/doc_pg.md
;; sha256: 66e39f1bb53a69968af1a8bb10f2023db70b3ac77165483ce8e8e9266f363d59
[[:chapter {:title "Introduction to hara.lang" :link "merged-plans-slop-doc-pg-md"}]]
"Here is the content recreated in Markdown format:\n\n---\n\n# **Introduction to hara.lang**\n\n## What is `hara.lang`?\n\n`std.lang` is a Domain-Specific Language (DSL) designed to simplify the process of defining database operations and structures programmatically. It bridges the gap between high-level programming concepts (e.g., functions and data structures) and database-specific logic like SQL/PLpgSQL.\n\nWith `hara.lang`, developers can:\n- Write database functions, types, and procedures in a declarative, high-level syntax.\n- Automatically generate PostgreSQL-compatible SQL scripts for execution.\n- Leverage features like validations, inline helper calls, and error handling seamlessly.\n\n### Why Use `hara.lang`?\n\n- **Consistency**: Define database operations in a structured, reusable way.\n- **Ease of Use**: Abstracts SQL intricacies while giving access to advanced features.\n- **Integration**: Embeds naturally into larger projects using functional programming languages like Clojure.\n- **Maintainability**: Ensures clean and organized database-related code.\n\n---\n\n## Key Features of `hara.lang`\n\n### 1. **Function Definitions**\n`hara.lang` allows you to define database functions easily:\n\n```clojure\n(defn.pg example-function\n  \"A simple example function\"\n  {:added \"1.0\"}\n  ([:uuid i_input :text i_message]\n   (let [output (str \"Processed: \" i_message)]\n     (return output))))\n```\n\nGenerates:\n\n```sql\nCREATE OR REPLACE FUNCTION example_function(\n  i_input UUID,\n  i_message TEXT\n) RETURNS TEXT AS $$\nDECLARE\n  output TEXT;\nBEGIN\n  output := 'Processed: ' || i_message;\n  RETURN output;\nEND;\n$$ LANGUAGE plpgsql;\n```\n\n### 2. **Type Definitions**\nCreate custom types and schemas programmatically:\n\n```clojure\n(deftype.pg example-type\n  [:id {:type :uuid :primary true}\n   :name {:type :text :required true}\n   :created_at {:type :timestamp :default \"now()\"}])\n```\n\nGenerates:\n\n```sql\nCREATE TABLE example_type (\n  id UUID PRIMARY KEY,\n  name TEXT NOT NULL,\n  created_at TIMESTAMP DEFAULT now()\n);\n```\n\n### 3. **Validations and Assertions**\nInline checks for input validation:\n\n```clojure\n(pg/assert [value :is-not-null] [:error/tag \"Value cannot be null\"])\n```\n\n### 4. **Dynamic SQL Construction**\nEmbed SQL logic dynamically:\n\n```clojure\n(pg/t:select app/UserAccount {:where {:email i_email}})\n```\n\nGenerates:\n\n```sql\nSELECT * FROM app.UserAccount WHERE email = i_email;\n```\n\n---\n\n## **Getting Started with Tutorials**\n\n### Tutorial 1: **Setting Up Your Environment**\n\n**Goal**: Install and configure `hara.lang` in your project.\n\n1. **Prerequisites**:\n   - PostgreSQL installed locally or accessible via a database connection.\n   - Familiarity with SQL and basic programming concepts.\n\n2. **Setup**:\n   - Install `hara.lang` dependencies in your project.\n   - Connect to your PostgreSQL database.\n\n3. **Example Setup Script**:\n\n```clojure\n(ns myproject.core\n  (:require [hara.lang :as l]\n            [rt.postgres :as pg]))\n\n;; Example database connection\n(pg/setup {:dbname \"my_database\"\n           :user \"user\"\n           :password \"password\"\n           :host \"localhost\"})\n```\n\n---\n\n### Tutorial 2: **Defining Your First Function**\n\n**Goal**: Create a simple function to greet users.\n\n1. **Write Your Function in `hara.lang`**:\n\n```clojure\n(defn.pg greet-user\n  \"Greets a user by name\"\n  {:added \"1.0\"}\n  ([:text i_name]\n   (let [greeting (str \"Hello, \" i_name \"!\")]\n     (return greeting))))\n```\n\n2. **Generated SQL**:\n\n```sql\nCREATE OR REPLACE FUNCTION greet_user(\n  i_name TEXT\n) RETURNS TEXT AS $$\nDECLARE\n  greeting TEXT;\nBEGIN\n  greeting := 'Hello, ' || i_name || '!';\n  RETURN greeting;\nEND;\n$$ LANGUAGE plpgsql;\n```\n\n3. **Test Your Function**:\n\n```sql\nSELECT greet_user('Alice');\n-- Output: \"Hello, Alice!\"\n```\n\n---\n\n### Tutorial 3: **Using Validations and Error Handling**\n\n**Goal**: Extend the `greet-user` function with input validation.\n\n1. **Add Validation Logic**:\n\n```clojure\n(defn.pg greet-user\n  \"Greets a user by name\"\n  {:added \"1.1\"}\n  ([:text i_name]\n   (pg/assert [i_name :is-not-null] [:error/invalid-input \"Name cannot be null\"])\n   (let [greeting (str \"Hello, \" i_name \"!\")]\n     (return greeting))))\n```\n\n2. **Generated SQL**:\n\n```sql\nCREATE OR REPLACE FUNCTION greet_user(\n  i_name TEXT\n) RETURNS TEXT AS $$\nDECLARE\n  greeting TEXT;\nBEGIN\n  IF i_name IS NULL THEN\n    RAISE EXCEPTION 'Name cannot be null';\n  END IF;\n  greeting := 'Hello, ' || i_name || '!';\n  RETURN greeting;\nEND;\n$$ LANGUAGE plpgsql;\n```\n\n---\n\n### Tutorial 4: **Creating Custom Types**\n\n**Goal**: Define a custom PostgreSQL table for users.\n\n1. **Define Your Table**:\n\n```clojure\n(deftype.pg user\n  [:id {:type :uuid :primary true}\n   :name {:type :text :required true}\n   :email {:type :citext :unique true}\n   :created_at {:type :timestamp :default \"now()\"}])\n```\n\n2. **Generated SQL**:\n\n```sql\nCREATE TABLE user (\n  id UUID PRIMARY KEY,\n  name TEXT NOT NULL,\n  email CITEXT UNIQUE,\n  created_at TIMESTAMP DEFAULT now()\n);\n```\n\n---\n\n### Tutorial 5: **Advanced: Writing Procedures**\n\n**Goal**: Combine multiple operations into a single procedure.\n\n1. **Create a Registration Procedure**:\n\n```clojure\n(defn.pg register-user\n  \"Registers a new user\"\n  {:added \"1.0\"}\n  ([:uuid i_user_id :jsonb i_user_data]\n   (let [v_name (i_user_data ->> \"name\")\n         v_email (i_user_data ->> \"email\")]\n     (pg/assert [v_email :is-not-null] [:error/invalid-input \"Email cannot be null\"])\n     (pg/t:insert user {:id i_user_id :name v_name :email v_email})\n     (return (str \"User registered: \" v_name)))))\n```\n\n2. **Generated SQL**:\n\n```sql\nCREATE OR REPLACE FUNCTION register_user(\n  i_user_id UUID,\n  i_user_data JSONB\n) RETURNS TEXT AS $$\nDECLARE\n  v_name TEXT;\n  v_email TEXT;\nBEGIN\n  v_name := i_user_data ->> 'name';\n  v_email := i_user_data ->> 'email';\n\n  IF v_email IS NULL THEN\n    RAISE EXCEPTION 'Email cannot be null';\n  END IF;\n\n  INSERT INTO user (id, name, email)\n  VALUES (i_user_id, v_name, v_email);\n\n  RETURN 'User registered: ' || v_name;\nEND;\n$$ LANGUAGE plpgsql;\n```\n\n---\n\nLet me know if you'd like to refine or expand this Markdown-based guide! 😊"
;; END merged documentation: plans/slop/doc_pg.md
