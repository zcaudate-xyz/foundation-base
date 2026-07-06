# Examples

Examples are a primary entry point into Foundation Base. Each example should keep the following links together:

- authored Clojure source;
- project-generation or build definition;
- tests where available;
- generated output repository;
- command used to reproduce or push the project.

## Language walkthroughs

| Walkthrough | Published page | Source |
|---|---|---|
| Basic authoring | [Walkthrough: basic](https://zcaudate.xyz/foundation-base/hara/walkthrough-basic.html) | [`std_lang_00_basic.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/walkthrough/std_lang_00_basic.clj) |
| Multiple languages | [Walkthrough: multi](https://zcaudate.xyz/foundation-base/hara/walkthrough-multi.html) | [`std_lang_01_multi.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/walkthrough/std_lang_01_multi.clj) |
| Live evaluation | [Walkthrough: live](https://zcaudate.xyz/foundation-base/hara/walkthrough-live.html) | [`std_lang_02_live.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/walkthrough/std_lang_02_live.clj) |

## Generated project examples

### C pthreads hello

- Generated project: [`hoebat/play.c-000-pthreads-hello`](https://github.com/hoebat/play.c-000-pthreads-hello)
- Authored source: [`main.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/c_000_pthreads_hello/main.clj)
- Build definition: [`build.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/c_000_pthreads_hello/build.clj)
- Reproduce: `lein push-c-000-pthreads`

### OpenResty hello

- Generated project: [`hoebat/play.ngx-000-hello`](https://github.com/hoebat/play.ngx-000-hello)
- Authored source: [`main.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_000_hello/main.clj)
- Build definition: [`build.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_000_hello/build.clj)
- Reproduce: `lein push-ngx-000-hello`

### OpenResty live evaluation

- Generated project: [`hoebat/play.ngx-001-eval`](https://github.com/hoebat/play.ngx-001-eval)
- Authored source: [`main.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_001_eval/main.clj)
- Build definition: [`build.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_001_eval/build.clj)
- Reproduce: `lein push-ngx-001-eval`

### TUI counter

- Generated project: [`hoebat/play.tui-000-counter`](https://github.com/hoebat/play.tui-000-counter)
- Authored source: [`main.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_000_counter/main.clj)
- Build definition: [`build.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_000_counter/build.clj)
- Reproduce: `lein push-tui-000-counter`

### TUI fetch

- Generated project: [`hoebat/play.tui-001-fetch`](https://github.com/hoebat/play.tui-001-fetch)
- Authored source: [`main.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_001_fetch/main.clj)
- Build definition: [`build.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_001_fetch/build.clj)
- Reproduce: `lein push-tui-001-fetch`

### TUI Game of Life

- Generated project: [`zcaudate/play.tui-002-game-of-life`](https://github.com/zcaudate/play.tui-002-game-of-life)
- Authored source: [`main.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_002_game_of_life/main.clj)
- Build definition: [`build.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_002_game_of_life/build.clj)
- Reproduce: `lein push-tui-002-game-of-life`

### React Native components

- Generated project: [`zcaudate/foundation.react-native`](https://github.com/zcaudate/foundation.react-native)
- Authored index: [`web_native_index.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/component/web_native_index.clj)
- Build definition: [`build_native_index.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/component/build_native_index.clj)
- Reproduce: `lein push-native-code`

## Adding an example

Add a new entry here and on the generated [Examples page](https://zcaudate.xyz/foundation-base/examples.html). Include prerequisites and maturity status when the example depends on an external runtime or incomplete target.
