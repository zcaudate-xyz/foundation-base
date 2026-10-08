# Build a Topic editor from scratch

Build one complete list / detail / modify screen using `xt.substrate`,
`js.react.ext-model`, and the Melbourne components already in foundation-base.
Start with an empty feature directory and create each file below in order.
Every source block is a complete file; every REPL block is executable.

The finished reference is [the example README](README.md). Your new feature uses
`lang-demos.topic-from-scratch` and port **28013**, so it can coexist with the
reference example on port 28012.

## 1. Check out the prerequisites

Start from a fresh clone of the PR, which includes the two `ext-model` fixes:

```bash
git clone https://github.com/zcaudate-xyz/foundation-base.git
cd foundation-base
git fetch origin pull/467/head:learn-substrate
git switch learn-substrate
java -version
node --version
lein version
npm ci --legacy-peer-deps
mkdir -p src-build/lang_demos/topic_from_scratch
```

Use Java 21, Node 24, and Leiningen. Root npm dependencies are needed by the
React-backed REPL checks. The emitted browser app will have its own package.
On an existing checkout, fetch and switch to the PR on a clean working tree
instead of cloning again.

Keep a terminal at the repository root. Open a second terminal and run
`lein repl` from the same root. Evaluate the REPL blocks below there, in order.
Write the source files in your editor; do not paste their namespace forms into
that REPL. `src-build` is already on the repository classpath.

Useful syntax: `defn.js` emits a JavaScript function, `def.js` emits a value,
`var` declares a local JavaScript variable, `:=` assigns it, and `-/name` refers
to a definition in the same JavaScript namespace. `:%` describes a React element.
These are foundation-base's JavaScript DSL forms, rather than ordinary Clojure
functions executing inside the JVM.

## 2. Define the record and route contract

The server will own two records with `id`, `title`, and `revision` fields.
For this exercise the table is an array in the server space's state.

| Route | Arguments | Result | Change event |
| --- | --- | --- | --- |
| `topic/list` | `[]` | Array of complete records | None |
| `topic/detail` | `[id]` | One record for a known id | None |
| `topic/modify` | `[id, title]` | Updated record, with incremented revision | `topic/changed` with `{id}` |

A blank title must reject without modifying the row or publishing an event.
The space will be `learn/topics`. The client will reach the server using a
transport registered on the client as `server`.

The screen's three declaration sections connect to this contract:

| Section | Responsibility | Relationship to the table |
| --- | --- | --- |
| `:views` | Define list and detail reads | Call the read routes; results live in reactive view models |
| `:actions` | Define commands such as modify | Call the mutation route when submitted |
| `:entries` | Define display/edit fields | Select fields for this screen's UI; they do not create columns or restrict query results |

The selected id is action context, even though only the title is editable.
The list route returns revision too; the list entries can choose to hide it.

## 3. Create the request and action adapter

`link.clj` has three jobs: make requests, turn declarations into callable
functions, and fan incoming change events out to independent listeners.

Create `src-build/lang_demos/topic_from_scratch/link.clj` with this complete content:

<!-- file: src-build/lang_demos/topic_from_scratch/link.clj -->
```clojure
(ns lang-demos.topic-from-scratch.link
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.substrate :as substrate]
             [xt.event.base-listener :as listener]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.spec-promise :as promise]]})

(defn.js call
  "the statslink request boundary, using a substrate context"
  [context route args]
  (return (. (substrate/request (. context node) (. context space) route args
                                {:transport_id (. context transport_id)})
             (catch (fn [err]
                      (when (. err error)
                        (throw (new Error (. err error message))))
                      (throw err))))))

(defn.js action-fn
  "preserves statslink's input -> wrapped function -> output contract"
  [action context]
  (var f ((or (. action wrap) k/identity) (. action fn)))
  (var input-fn (or (. action input) k/identity))
  (var output-fn (or (. action output) k/identity))
  (return
   (fn [...args]
     (return (. (promise/x:promise
                 (fn []
                   (return (xt/x:apply f (input-fn [...args] context)))))
                (then output-fn))))))

(defn.js make-actions
  [impl context]
  (return (Object.fromEntries
           (. (Object.entries (. impl actions))
              (map (fn [[key action]]
                     (return [key (-/action-fn action context)])))))))

(defn.js event-key
  [space signal]
  (return (JSON.stringify [space signal])))

(defn.js install-event-bridge
  "one substrate trigger fans out to any number of listeners in each space"
  [node signal]
  (return
   (substrate/register-trigger
    node signal
    (fn [space frame local-node]
      (return (listener/trigger-keyed-listeners
               local-node (-/event-key (. space id) signal) frame)))
    {})))

(defn.js addRawListener
  "returns a cleanup function; transport subscription belongs to bootstrap"
  [event-sync callback context listener-id]
  (var #{node space} context)
  (var key (-/event-key space (. event-sync signal)))
  (listener/add-keyed-listener
   node key listener-id "raw"
   (fn [_ frame]
     (return (callback frame)))
   {} (. event-sync check))
  (return (fn [] (listener/remove-keyed-listener node key listener-id))))
```

The request context contains `{node, space, transport_id}`. `call` passes those
values to substrate and turns an error response frame into an `Error` that a
form can display.

`action-fn` wraps the declared function once when constructing the action.
On each invocation, it transforms the supplied arguments with `:input`, applies
the wrapped function to those arguments, and transforms its resolved result
with `:output`. The Promise thunk also turns a synchronous throw into rejection.
`make-actions` only constructs functions; it does not run a mutation.

The event bridge registers one substrate trigger for a signal. The keyed
listeners beneath it can coexist for different spaces, list/detail models, and
raw-event consumers. A listener's cleanup removes only that listener. The
transport subscription itself will belong to the fixture.

## 4. Create the table, handlers, and transport

`fixture.clj` owns the data. First create the records and route handlers, then
attach the two transport endpoints and subscribe the client to change events.

Create `src-build/lang_demos/topic_from_scratch/fixture.clj` with this complete content:

<!-- file: src-build/lang_demos/topic_from_scratch/fixture.clj -->
```clojure
(ns lang-demos.topic-from-scratch.fixture
  (:require [lang.core :as l]))

(l/script :js
  {:require [[lang-demos.topic-from-scratch.link :as link]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as memory]
             [xt.lang.spec-promise :as promise]]})

(def.js SPACE "learn/topics")

(defn.js seedTopics
  []
  (return [{:id "topic-1" :title "Will turnout increase?" :revision 1}
           {:id "topic-2" :title "Will the bill pass?" :revision 1}]))

(defn.js install
  [server]
  (substrate/register-handler
   server "topic/list"
   (fn [space args request node] (return (. space state topics))) {})
  (substrate/register-handler
   server "topic/detail"
   (fn [space args request node]
     (return (. (. space state topics)
                (find (fn [row] (return (== (. row id) (. args [0])))))))) {})
  (substrate/register-handler
   server "topic/modify"
   (fn [space args request node]
     (var [id title] args)
     (var row (. (. space state topics)
                 (find (fn [entry] (return (== (. entry id) id))))))
     (when (not row) (throw (new Error "Unknown topic")))
     (when (or (not= "string" (typeof title)) (== "" (. title (trim))))
       (throw (new Error "Title is required")))
     (:= (. row title) (. title (trim)))
     (:= (. row revision) (+ (. row revision) 1))
     (return (. (substrate/publish node (. space id) "topic/changed" {:id id} {})
                (then (fn [] (return row)))))) {})
  (return server))

(defn.js close
  [state]
  (var #{client server context} state)
  (return
   (. (substrate/unsubscribe client (. context space) "topic/changed" "topic-editor"
                            {:transport_id "server"})
      (then (fn []
              (substrate/unregister-trigger client "topic/changed")
              (return (promise/x:promise-all
                       [(substrate/detach-transport client "server")
                        (substrate/detach-transport server "client")])))))))

(defn.js bootstrap
  []
  (var server (-/install
               (substrate/node-create
                {:id "topic-server"
                 :spaces {"learn/topics" {:state {:topics (-/seedTopics)}}}})))
  (var client (substrate/node-create {:id "topic-client"}))
  (link/install-event-bridge client "topic/changed")
  (var wire (memory/memory-pair {:left_id "client" :right_id "server"}))
  (var context {:node client :space -/SPACE :transport_id "server"})
  (return
   (. (promise/x:promise-all
       [(substrate/attach-transport client "server" (memory/text-endpoint (. wire left)))
        (substrate/attach-transport server "client" (memory/text-endpoint (. wire right)))])
      (then (fn []
              (return (substrate/subscribe client -/SPACE "topic/changed" "topic-editor"
                                          {:transport_id "server"}))))
      (then (fn [] (return #{client server context}))))))
```

Notice the ownership:

- The server space contains `state.topics`; the client does not share that array.
- Client endpoint `wire.left` is registered under transport id `server`.
- Server endpoint `wire.right` is registered under transport id `client`.
- Subscription `topic-editor` targets space `learn/topics`, signal `topic/changed`.
- `close` removes that subscription, unregisters the trigger, and detaches both transports.

`topic/modify` validates before mutating, changes the server record, then
publishes the event. Reads are real substrate requests across the memory
transport; this is not a direct call from the component into the array.

### Checkpoint A: read the table without React

In the REPL, evaluate this setup once. The helper ensures that every checkpoint
closes its fixture on both success and failure. Each call starts with fresh rows.

<!-- repl: runtime-setup -->
```clojure
(require '[lang.core :as l]
         '[xt.lang.common-notify :as notify])

(l/script- :js
  {:runtime :basic
   :require [[lang-demos.topic-from-scratch.fixture :as fixture]
             [lang-demos.topic-from-scratch.link :as link]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]]})

(l/rt:restart :js)

(defn.js with-fixture
  [f]
  (return (. (fixture/bootstrap)
             (then (fn [state]
                     (return (. (promise/x:promise (fn [] (return (f state))))
                                (finally (fn [] (return (fixture/close state)))))))))))
```

Now request both the list and the second record. `notify/wait-on` lets the JVM
wait for the asynchronous JavaScript result; `repl/notify` is the result boundary.
The assertion checks actual record data, rather than transport success alone.

<!-- repl: read-table -->
```clojure
(def read-result
  (notify/wait-on [:js 10000]
      (. (-/with-fixture
          (fn [state]
            (var context (. state context))
            (return (. (promise/x:promise-all
                        [(link/call context "topic/list" [])
                         (link/call context "topic/detail" ["topic-2"])])
                       (then (fn [[rows detail]] (return {:rows rows :detail detail})))))))
         (then (fn [value] (repl/notify value))))))

read-result

(assert (= {"rows" [{"id" "topic-1" "title" "Will turnout increase?" "revision" 1}
                  {"id" "topic-2" "title" "Will the bill pass?" "revision" 1}]
         "detail" {"id" "topic-2" "title" "Will the bill pass?" "revision" 1}}
           read-result))
```

The REPL prints both rows and the second detail record. The assertion returns
`nil` when it passes. Resolve a failed read before adding UI code.

## 5. Declare views, actions, and entries

Keep transport details in the adapter and the screen contract in `model.clj`.

Create `src-build/lang_demos/topic_from_scratch/model.clj` with this complete content:

<!-- file: src-build/lang_demos/topic_from_scratch/model.clj -->
```clojure
(ns lang-demos.topic-from-scratch.model
  (:require [lang.core :as l]))

(l/script :js
  {:require [[lang-demos.topic-from-scratch.link :as link]]})

;; The familiar statsui arrangement. Request arguments and context are explicit;
;; entries describe the fields displayed/edited, independently of the transport.
(def.js TOPIC
  {:event-sync {:signal "topic/changed"}
   :views
   {:list {:fn link/call
           :input (fn:> [args context] [context "topic/list" args])}
    :detail {:fn link/call
             :input (fn:> [args context] [context "topic/detail" args])}}
   :actions
   {:modify {:fn link/call
             :input (fn:> [args context] [context "topic/modify" args])}}
   :entries
   {:list ["id" "title"]
    :detail ["id" "title" "revision"]
    :modify ["title"]}})
```

For example, `actions.modify("topic-1", "Revised question?")` starts with
arguments `["topic-1", "Revised question?"]`. Its input transformation produces
`[context, "topic/modify", args]`, which becomes the positional arguments to
`link/call`. The server receives `[id, title]` as the request arguments.

Missing `:wrap` and `:output` default to identity. Entries use string field names
so this declaration can be emitted as JavaScript. The modify entries contain
only `title`; the component supplies the selected id separately.

### Checkpoint B: submit an action without a form

Add the new model to the REPL's JavaScript dependencies. Keep the helper from
Checkpoint A; the same JavaScript namespace is still active.

<!-- repl: add-model -->
```clojure
(l/script- :js
  {:runtime :basic
   :require [[lang-demos.topic-from-scratch.fixture :as fixture]
             [lang-demos.topic-from-scratch.link :as link]
             [lang-demos.topic-from-scratch.model :as model]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]]})

(l/rt:restart :js)
```

<!-- repl: modify-row -->
```clojure
(def modify-result
  (notify/wait-on [:js 10000]
      (. (-/with-fixture
          (fn [state]
            (var context (. state context))
            (var actions (link/make-actions model/TOPIC context))
            (return (. (link/call context "topic/detail" ["topic-1"])
                       (then (fn [before]
                               (return (. (. actions (modify "topic-1" "  Revised question?  "))
                                          (then (fn [after] (return {:before before :after after})))))))))))
         (then (fn [value] (repl/notify value))))))

modify-result

(assert (= {"before" {"id" "topic-1" "title" "Will turnout increase?" "revision" 1}
         "after" {"id" "topic-1" "title" "Revised question?" "revision" 2}}
           modify-result))
```

The before/after values show that constructing `actions` does not execute the
command. Submitting it trims the title and increments the revision once.

### Checkpoint C: reject an invalid mutation

<!-- repl: reject-blank-title -->
```clojure
(def rejection-result
  (notify/wait-on [:js 10000]
      (. (-/with-fixture
          (fn [state]
            (var context (. state context))
            (var count 0)
            (var cleanup (link/addRawListener (. model/TOPIC event-sync)
                                             (fn [] (:= count (+ count 1))) context "negative"))
            (return (. (link/call context "topic/modify" ["topic-1" " "])
                       (then (fn [] (return {:rejected false})))
                       (catch (fn [err]
                                (return (. (link/call context "topic/detail" ["topic-1"])
                                           (then (fn [row]
                                                   (cleanup)
                                                   (return {:rejected true :message (. err message)
                                                            :events count :revision (. row revision)})))))))))))
         (then (fn [value] (repl/notify value))))))

rejection-result

(assert (= {"rejected" true "message" "Title is required" "events" 0 "revision" 1}
           rejection-result))
```

Expect the error message, zero change events, and unchanged revision 1.
The rejection marker ensures that an accidentally successful request fails the
assertion. Now the mutation contract is verified before a form exists.

## 6. Turn the reads into reactive views

`hook.clj` creates `ext-model` views from the declaration and attaches the change
events. The view object holds the input, current result, and pending/error state.

Create `src-build/lang_demos/topic_from_scratch/hook.clj` with this complete content:

<!-- file: src-build/lang_demos/topic_from_scratch/hook.clj -->
```clojure
(ns lang-demos.topic-from-scratch.hook
  (:require [lang.core :as l]))

(l/script :js
  {:require [[lang-demos.topic-from-scratch.link :as link]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [xt.event.base-model :as event-model]]})

(defn.js makeListView
  [impl m context]
  (return (ext-model/makeViewRaw
           (Object.assign {:handler (link/action-fn (. impl views list) context)
                           :defaultOutput []}
                          m))))

(defn.js makeSingleView
  [impl m context]
  (return (ext-model/makeViewRaw
           (Object.assign {:handler (link/action-fn (. impl views detail) context)}
                          m))))

(defn.js attachViewEvents
  "refreshes reads, filtering detail events by the current model input"
  [view event-sync context listener-id single]
  (return
   (link/addRawListener
    event-sync
    (fn [frame]
      (var args (. (event-model/get-input view) current data))
      (when (and args
                 (or (not single)
                     (== (. args [0]) (. frame data id))))
        (:= (. view event_refresh) (ext-model/refresh-view view))))
    context listener-id)))

(defn.js listenRawEvents
  "React lifecycle equivalent of ext-cell/listenRawEvents"
  [event-sync callback context]
  (var listener-id (r/const (. (Math.random) (toString 36) (slice 2))))
  (r/watch [event-sync callback context]
    (return (link/addRawListener event-sync callback context listener-id))))

(defn.js useViewEvents
  [view impl context single]
  (var listener-id (r/const (. (Math.random) (toString 36) (slice 2))))
  (r/watch [view impl context single]
    (return (-/attachViewEvents view (. impl event-sync) context listener-id single))))

(defn.js useListView
  [impl m context]
  (var view (r/useMemo (fn [] (return (-/makeListView impl m context))) [impl context]))
  (-/useViewEvents view impl context false)
  (ext-model/useRefreshArgs view [] {:remote "none"})
  (return view))

(defn.js useSingleView
  [impl m context id]
  (var view (r/useMemo (fn [] (return (-/makeSingleView impl m context))) [impl context]))
  (-/useViewEvents view impl context true)
  (ext-model/useRefreshArgs view [id] {:remote "none"})
  (return view))

(defn.js useActions
  [impl context]
  (return (r/useMemo (fn [] (return (link/make-actions impl context))) [impl context])))
```

There are two related event layers:

| Event | Producer | Consumer | Effect |
| --- | --- | --- | --- |
| `topic/changed` | The server mutation handler | `attachViewEvents` on the client | Rerun reads using their current arguments |
| `model.output` / `model.pending` | The local `ext-model` view | React's `listenView` / `listenViewOutput` | Rerender the screen from the latest view state |

`useListView` reads with arguments `[]`. `useSingleView` reads with `[selectedId]`.
The detail listener compares an event id to the view's **current** input, so
switching selections does not leave it filtering against the previous id.
`{:remote "none"}` disables ext-model's secondary remote/sync pipeline; the
ordinary handler already performs the substrate request.

`listenRawEvents` is available when a component needs the change frame itself,
rather than a refreshed view. Its effect returns a cleanup function. React hook
functions must be called inside components; the following checkpoint uses the
plain view constructors to exercise the same data flow without mounting React.

### Checkpoint D: prove event refresh and cleanup

Add the view dependencies to the REPL:

<!-- repl: add-views -->
```clojure
(l/script- :js
  {:runtime :basic
   :require [[lang-demos.topic-from-scratch.fixture :as fixture]
             [lang-demos.topic-from-scratch.link :as link]
             [lang-demos.topic-from-scratch.model :as model]
             [lang-demos.topic-from-scratch.hook :as hook]
             [js.react.ext-model :as ext-model]
             [xt.event.base-model :as event-model]
             [xt.event.base-listener :as listener]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]]})

(l/rt:restart :js)
```

<!-- repl: event-refresh -->
```clojure
(def refresh-result
  (notify/wait-on [:js 10000]
      (. (-/with-fixture
          (fn [state]
            (var context (. state context))
            (var list-view (hook/makeListView model/TOPIC {} context))
            (var detail-view (hook/makeSingleView model/TOPIC {} context))
            (var clean-list (hook/attachViewEvents list-view (. model/TOPIC event-sync) context "list" false))
            (var clean-detail (hook/attachViewEvents detail-view (. model/TOPIC event-sync) context "detail" true))
            (return (. (promise/x:promise-all
                        [(ext-model/refresh-args list-view [])
                         (ext-model/refresh-args detail-view ["topic-1"])])
                       (then (fn [] (return (link/call context "topic/modify" ["topic-1" "Updated?"]))))
                       (then (fn [] (return (promise/x:promise-all
                                             [(. list-view event_refresh) (. detail-view event_refresh)]))))
                       (then (fn []
                               (clean-list)
                               (clean-detail)
                               (return {:list-title (. (event-model/get-current list-view) [0] title)
                                        :detail-title (. (event-model/get-current detail-view) title)
                                        :listeners (listener/list-keyed-listeners
                                                    (. context node)
                                                    (link/event-key (. context space) "topic/changed"))})))))))
         (then (fn [value] (repl/notify value))))))

refresh-result

(assert (= {"list_title" "Updated?" "detail_title" "Updated?" "listeners" []}
           refresh-result))
```

Both views report `Updated?` after a mutation, without calling manual refresh
after the edit. Their event listeners have been removed by the end of the
checkpoint. `event_refresh` records the refresh promise so the checkpoint can
await completion instead of depending on a delay. JavaScript emission converts
the checkpoint's keyword keys `:list-title` / `:detail-title` to underscores.

## 7. Build the screen from entries

`main.clj` connects the selected id, two views, the editable title, and the action.
It uses the existing Melbourne text, button, and input components.

Create `src-build/lang_demos/topic_from_scratch/main.clj` with this complete content:

<!-- file: src-build/lang_demos/topic_from_scratch/main.clj -->
```clojure
(ns lang-demos.topic-from-scratch.main
  (:require [lang.core :as l]))

(l/script :js
  {:config {:emit {:lang/jsx false}}
   :require [[lang-demos.topic-from-scratch.fixture :as fixture]
             [lang-demos.topic-from-scratch.model :as model]
             [lang-demos.topic-from-scratch.hook :as hook]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react-native :as n]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-input :as ui-input]]})

(def.js DESIGN {:type "light"})

(defn.js EntryFields
  [#{entry fields}]
  (return
   [:% n/View
    (. fields
       (map (fn [field]
              (return
               [:% ui-static/Text
                {:key field :design -/DESIGN :style {:marginBottom 4}}
                (+ field ": " (or (. entry [field]) ""))]))))]))

(defn.js TopicEditor
  [#{context}]
  (var [selected setSelected] (r/local "topic-1"))
  (var [title setTitle] (r/local ""))
  (var [message setMessage] (r/local ""))
  (var list-view (hook/useListView model/TOPIC {} context))
  (var detail-view (hook/useSingleView model/TOPIC {} context selected))
  (var views {:list list-view :detail detail-view})
  (var actions (hook/useActions model/TOPIC context))
  (var rows (or (ext-model/listenView (. views list) "success") []))
  (var detail (ext-model/listenViewOutput (. views detail) ["output" "pending"] {}))
  (var entry (. detail current))
  (r/watch [entry]
    (setTitle (or (and entry (. entry title)) "")))
  (var #{waiting errored result onAction}
       (r/useSubmitResult
        {:onSubmit (fn [] (return (. actions (modify selected title))))
         :onError (fn [err] (return {:status "error" :message (. err message)}))
         :onSuccess (fn [] (setMessage "Saved; the topic/changed event refreshed the views."))}))
  (return
   [:% n/View {:style {:gap 20}}
    [:% ui-static/Text {:design -/DESIGN :style {:fontSize 24 :fontWeight "700"}}
     "Topics"]
    [:% n/View {:style {:gap 8}}
     (. rows
        (map (fn [row]
               (return
                [:% n/View {:key (. row id) :style {:padding 12 :borderWidth 1 :borderColor "#ddd"}}
                 [:% -/EntryFields {:entry row :fields (. model/TOPIC entries list)}]
                 [:% ui-button/Button
                  {:design -/DESIGN :text "Select"
                   :textProps {:accessibilityLabel (+ "Select " (. row id))}
                   :onPress (fn [] (setSelected (. row id)) (setMessage ""))}]]))))]
    [:% ui-static/Text {:design -/DESIGN :style {:fontSize 20 :fontWeight "700"}}
     "Detail / modify"]
    (:? entry
        [:% n/View {:style {:gap 12}}
         [:% -/EntryFields {:entry entry :fields (. model/TOPIC entries detail)}]
         (. (. model/TOPIC entries modify)
            (map (fn [field]
                   (return
                    [:% ui-input/Input
                     {:key field :design -/DESIGN :value title :onChangeText setTitle
                      :accessibilityLabel field :editable (not waiting)}]))))
         [:% ui-button/Button
          {:design -/DESIGN :text (:? waiting "Saving..." "Save title")
           :textProps {:accessibilityLabel "Save title"}
           :disabled (or waiting (. detail pending))
           :onPress onAction}]]
        [:% ui-static/Text {:design -/DESIGN} "Loading topic..."])
    [:% ui-static/Text {:design -/DESIGN :accessibilityRole "status"}
     (:? errored (or (. result message) "Unable to save") message)]]))

(defn.js App
  []
  (var [state setState] (r/local nil))
  (var [error setError] (r/local nil))
  (r/init []
    (var active true)
    (var connected nil)
    (. (fixture/bootstrap)
       (then (fn [value]
               (:= connected value)
               (if active (setState value) (fixture/close value))))
       (catch (fn [err] (when active (setError (. err message))))))
    (return (fn []
              (:= active false)
              (when connected (fixture/close connected)))))
  (return
   [:% n/View {:style {:maxWidth 640 :width "100%" :alignSelf "center" :padding 24}}
    (:? error
        [:% n/Text error]
        state
        [:% -/TopicEditor {:context (. state context)}]
        :else
        [:% n/Text "Connecting..."])]))

(defn.js mount
  []
  (return (r/renderDOMRoot "app" -/App)))
```

Follow the data through `TopicEditor`:

1. `useListView` creates the list model; `listenView` reads its successful output.
2. `EntryFields` renders the fields named in `TOPIC.entries.list` for each row.
3. Select changes `selected`; `useSingleView` reruns the detail read for that id.
4. `listenViewOutput` supplies the detail's current record and pending flag.
5. `TOPIC.entries.detail` chooses the detail fields; `entries.modify` creates the title input.
6. `useSubmitResult` calls `actions.modify(selected, title)` only when Save is pressed.
7. The server event refreshes the views; their model events update the displayed data.

The form keeps the editable title in local state. Loading another detail record
copies that record's title into the input. Pending state disables submission,
and the rejected Promise becomes the visible error message.

`App` boots the fixture on mount and closes it on unmount. It also closes a
fixture that finishes booting after the component has already unmounted.

`EntryFields` is a small renderer, not a schema-driven form generator. If you
add another editable field, also add its local state, action arguments, and
handler validation; extending `entries.modify` alone does not implement those.

## 8. Emit and run your new app

The build reaches `main/mount`, emits its dependencies into one JavaScript app,
and maps React Native imports to `react-native-web`. Vite's `globalThis` mapping
supports the existing native animation dependencies in a browser.

Create `src-build/lang_demos/topic_from_scratch/build.clj` with this complete content:

<!-- file: src-build/lang_demos/topic_from_scratch/build.clj -->
```clojure
(ns lang-demos.topic-from-scratch.build
  (:require [lang.core :as l]
            [std.make :as make :refer [def.make]]
            [lang-demos.topic-from-scratch.main]))

(def +package+
  {"name" "topic-from-scratch-example"
   "private" true
   "type" "module"
   "scripts" {"dev" "vite --host 0.0.0.0 --port 28013"
              "build" "vite build"
              "test" "npm run build && node smoke.cjs"}
   "dependencies" {"react" "19.2.3"
                   "react-dom" "19.2.3"
                   "react-native-web" "~0.21.0"}
   "devDependencies" {"vite" "^7.1.7"
                      "jsdom" "26.1.0"}})

(def +index+
  ["<!doctype html>"
   "<html lang=\"en\"><head><meta charset=\"utf-8\">"
   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
   "<title>Topic / xt.substrate</title></head>"
   "<body style=\"margin:0;background:#f7f8fa\"><main id=\"app\"></main>"
   "<script type=\"module\" src=\"./app.js\"></script></body></html>"])

(def.make TOPIC-FROM-SCRATCH
  {:tag "lang-demos.topic-from-scratch"
   :build ".build/demo/topic-from-scratch"
   :triggers '#{lang-demos.topic-from-scratch.main
                lang-demos.topic-from-scratch.hook
                lang-demos.topic-from-scratch.model
                lang-demos.topic-from-scratch.link
                lang-demos.topic-from-scratch.fixture}
   :sections
   {:setup [{:type :package.json :main +package+}
            {:type :raw :file "vite.config.js"
             :main ["export default {define: {global: 'globalThis'}};"]}
            {:type :raw :file "index.html" :main +index+}]}
   :default
   [{:type :raw
     :file "smoke.cjs"
     :main (fn [] [(slurp "src-build/lang_demos/topic_from_scratch/smoke.cjs")])}
    {:type :raw
     :file "app.js"
     :main (fn []
             [(l/emit-script
               '(do (lang-demos.topic-from-scratch.main/mount))
               {:lang :js :layout :full
                :emit {:lang/jsx false
                       :override {"react-native" "react-native-web"}}})])}]})

(defn -main
  []
  (make/build-all TOPIC-FROM-SCRATCH)
  (shutdown-agents)
  (System/exit 0))
```

Reuse the reference example's UI smoke harness; it operates on the emitted
bundle and does not depend on either feature namespace:

```bash
cp src-build/lang_demos/js_007_statstrade_substrate/smoke.cjs src-build/lang_demos/topic_from_scratch/smoke.cjs
lein run -m lang-demos.topic-from-scratch.build
cd .build/demo/topic-from-scratch
npm install
npm run dev
```

Open **http://localhost:28013**. The list should contain two topics and the first
one should initially be selected. These are now the files you created, rather
than the reference app. Generated output lives under `.build/demo/topic-from-scratch`;
make edits in your authored `.clj` files, not in the emitted `app.js`.

After a source edit, run the `lein run -m ...` build again from the repository
root. If a stopped dev server is needed, restart it from the emitted directory.

### Checkpoint E: exercise the complete screen

| What to do | Expected result |
| --- | --- |
| Select the second topic | Detail and input show `Will the bill pass?`, revision 1 |
| Enter `Updated in the UI?` and save | List and detail both show the new title; detail shows revision 2 |
| Enter a blank title and save | `Title is required`; saved title and revision 2 remain unchanged |
| Reload | Fixture resets to the original two records, each with revision 1 |

From the emitted app directory, run:

```bash
npm test
```

It builds the production bundle and mounts the real React/Melbourne components
in jsdom. It checks initial reads, selection, editing, event-driven updates, and
blank-title rejection. Success ends with:

```text
UI smoke passed: list, selection, edit/event refresh, and validation.
```

This is a DOM integration check; it does not replace checking browser layout.

### Exercise: change the entries without changing the table

In your `model.clj`, change `:entries :list` from `["id" "title"]` to
`["id" "title" "revision"]`. Re-emit the app from the repository root and
refresh the browser. Each list row now displays its revision: the existing
list view already returned that field. No handler or table change was needed.
Save a title and watch the revision update in both list and detail. Restore the
two-field list when you are done.

## 9. Turn the checkpoints into repeatable tests

The reference [example tests](../../../test-lang/lang_demos/js_007_statstrade_substrate/example_test.clj)
use the same expressions as the REPL checkpoints. Make your own test namespace:

```bash
# Run from the repository root.
mkdir -p test-lang/lang_demos/topic_from_scratch
python3 - <<'PYTEST'
from pathlib import Path
source = Path('test-lang/lang_demos/js_007_statstrade_substrate/example_test.clj')
target = Path('test-lang/lang_demos/topic_from_scratch/example_test.clj')
target.write_text(source.read_text().replace('js-007-statstrade-substrate', 'topic-from-scratch'))
PYTEST
lein test :only lang-demos.topic-from-scratch.example-test
lein test :only js.react.ext-model-test
```

Expect 8 checks for your feature and 19 for `ext-model`. This repository uses
`code.test` facts and `=>`, rather than `clojure.test`. The extra checks cover
transformations, synchronous throws, independent listeners, and unrelated
record events. Give multiple facts for the same `:refer` unique `:id` metadata
so none overwrite one another in the test registry.

Temporarily change the expected updated title in one of your new facts to a
wrong value. Confirm the runner reports `FAILED`, then restore the expectation.
That proves the assertion executes, rather than trusting an exit code alone.

When you are finished with the REPL, evaluate:

<!-- repl: stop-runtime -->
```clojure
(l/rt:stop :js)
```

## 10. Connect a real database table

Keep the declaration, action adapter, view hooks, and screen. Change the server
handler implementation in `fixture/install` to use your database access layer:

| Handler | Database operation | Preserve this result contract |
| --- | --- | --- |
| `topic/list` | Select the screen's topic rows | Array of `{id, title, revision}` records |
| `topic/detail` | Select one topic by id | `{id, title, revision}` for a known id |
| `topic/modify` | Validate and update by id; increment revision | Return the updated record; publish `{id}` after the transaction commits |

The query can read a SQL table, a database view, or an RPC that joins several
tables. None of those choices is made by `entries`. In a deployed system, the
server handler also owns authorization; hiding an entry does not prevent a
caller from requesting or mutating data.

For a different table, first specify the route arguments and result fields.
Then update the handlers and declaration together. Choose the entries your
screen needs, decide which inputs come from selection/context, and make the
change signal carry the identity used to filter detail refreshes. A table
schema or a `:source` symbol does not automatically generate the handler in
this example.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| New namespace cannot be found | Run from the repository root; use `topic_from_scratch` in paths and `topic-from-scratch` in namespaces |
| `Cannot find module 'react'` in a REPL check | Install root JS dependencies before starting the runtime; restart it after installation |
| Substrate request cannot reach a handler | Match the exact route string, space id, and client transport id `server` |
| Edit works but displayed records stay old | Confirm transport subscription and `topic/changed` bridge; use the PR's `model.*` listener fixes |
| Selection produces a React cleanup error | Use the PR's `useRefreshArgs` fix; effects return cleanup functions rather than refresh Promises |
| Title error does not appear | Convert response frames to `Error` in `call`; return `{status: "error", message}` from `onError` |
| Source edits do not affect the app | Re-emit with the `lein run` build command; editing `.build` output is temporary |
| REPL checkpoint times out | Keep the documented 10-second timeout for graph emission and async runtime startup; inspect the actual result rather than increasing it indefinitely |
| `uv_interface_addresses` when starting Vite in a restricted environment | Use `npm run dev -- --host 127.0.0.1` |

The original `ext-model` fixes and completed reference source remain on the PR
branch. Once this walkthrough passes, your new feature consists of the five
implementation files, one build file, the smoke harness, and the copied test
namespace. Add your next table operation only after defining and checking its
request, result, and event contracts.

