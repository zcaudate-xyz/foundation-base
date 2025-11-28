# foundation


### Getting Started

For a comprehensive guide on installation, usage, and development setup, please refer to [GETTING_STARTED.md](GETTING_STARTED.md).

#### Quick Install (Users)

```bash
git clone git@github.com:zcaudate-xyz/foundation-base.git
cd foundation-base
lein install
```

#### Quick Setup (Developers)

1.  **Prerequisites**: Java 21, Leiningen.
2.  **Setup**: `git clone ...` then `lein deps`.
3.  **Run Tests**: `./lein test :only std.lib.collection-test` (Do not run full suite initially).

### std.lang - A Playground for Languages

There are so many languages currently out there is the world. Every single one of them has their own quirks but each are inspired by one another. 

`std.lang` creates an environment where multiple languages and multiple runtimes can be mixed, matched and integrated to get the basic minimum piece of code working. 

When the benefits of repl driven development can be made available
to any language, programming then becomes about the code itself and not everything around making the code work.

An example working with Javascript in emacs:

![captured](https://www.github.com/zcaudate-xyz/foundation-base/assets/1455572/a57f2ad2-23f9-4d39-917b-490bae8bd70b)

### Template Base Transpile
The best way to think about `std.lang` is that it is a convertor from lisp to algol. Algol languages syntactically is about 95% as another algol language. C is not that different from Js, is not that different from Python, is not that different from Solidity. There are differences in terms of types and keywords and whitespace/braces, but in general, the conventions are always present. `std.lang` provides a lisp dsl for these conventions as well as a method to write one's own grammer to target any language.

Furthermore, robust programs require more than just writing the function. Testing is paramount and there is so much pain when jumping from one language to another due to the fact that one has to relearn all the tooling of a language's eco system to be effective. The advantage of `std.lang` is to provide a common testing/maintainance strategy across all code → from C to bash to solidity. If a new language needs to be targeted, a grammar and a runtime specific to that language would be suffice to integrate that language into the existing clojure toolchain. 

The generated code can be run independently of `std.lang`, using a language's native toolchain. `std.lang` only takes care of transpiling lisp to a target language, it doesn't try to do anything more than that. If a client asked you to do a C project. In the past you would probably say ‘no thanks’. But with `std.lang`, you write in lisp, test the functions in lisp and generate the c files. The client is happy because they get what they want, the lisper is happy because they get to write in lisp, test in lisp and have the same dynamic eval that a lisper expects.

### Solidity examples

A runtime/workflow has been developed for live evaluation of solidity code. The tests are more important than the source files and can be walked through form by form. `nodejs` as well and the `ganache`, `solc`, `ethers` packages will need to be installed. Please see setup for [the testing environment](https://github.com/zcaudate/infra-testing/blob/main/infra/Dockerfile_foundation).

- [bookstore](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/web3/lib/example_bookstore.clj) and [test](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/test/web3/lib/example_bookstore_test.clj)
- [erc20 token](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/web3/lib/example_erc20.clj) and [tests](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/test/web3/lib/example_erc20_test.clj)
- [basic counter](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/test/web3/lib/example_counter_test.clj) and [tests](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/test/web3/lib/example_counter_test.clj)


### Dev prerequisites

There are a number of programs needing to be preinstalled for the java environment to shell out to. Not all of them will be needed on your own projects but they will be necessary for running tests in dev.

Please see [GETTING_STARTED.md](GETTING_STARTED.md) for detailed development instructions.

### std.lang - overview

[std.lang](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang.clj) started off as an experimental snippet generator to run bits of lua code on openresty. I was looking around for a lightweight alternative to clojure servers and made the decision because of [this epic rant](https://www.github.com/zcaudate-xyz/foundation-base/discussions/4) on Quora. 

As features crept into the library, it slowly evolved into what it is now. More features were added to the project and it has evolved into a tool for exploration into multi-language/multi-runtime environment, allowing unprecedented control in code manipulation and testing:

- A standard set of [symbols](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang/base/grammar_spec.clj) for transpile
- grammar spec for transpile to the following target languages:
  - [lua](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang/model/spec_lua.clj)       (production ready)
  - [js](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang/model/spec_js.clj)        (production ready)
  - [python](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang/model/spec_python.clj)    (somewhat production ready)
  - [r](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang/model/spec_r.clj)         (experimetal)
  - [c](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang/model/spec_c.clj)         (gpu kernel code)
  - [solidity](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/solidity/grammar.clj)  (production ready)
  - [bash](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang/model/spec_bash.clj)      (experimental)
  - go        (seeking implementations)
  - ocaml     (seeking implemenations)
  - haskell   (seeking implementations)
- Live eval through pluggable runtimes
  - os      ([js](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/basic/impl/process_js.clj), [python](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/basic/impl/process_python.clj), [R](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/basic/impl/process_r.clj), [lua](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/basic/impl/process_lua.clj), [c](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/basic/impl/process_c.clj))  
  - [graal](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/graal.clj)   (js, python)
  - [nginx](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/nginx.clj)   (lua)
  - [evm](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/rt/solidity/client.clj)     (solidity)
  - jocl        (c, gpu kernel)  
  - websockets  (js, lua, python)
  - browser     (js, through chrome driver)
  - blender     (python, seeking implementations)
- [xtalk](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src/std/lang/base/grammar_xtalk.clj) (crosstalk), a template language transpiling across dynamic language targets (lua, js, python)
- various helpers across different environments
- [js](https://www.github.com/zcaudate-xyz/foundation-base/tree/main/src/js), [lua](https://www.github.com/zcaudate-xyz/foundation-base/tree/main/src/lua) and [xtalk](https://www.github.com/zcaudate-xyz/foundation-base/tree/main/src/xt) utility libaries.

### std.lang - walkthroughs

Guided walkthroughs are provided for 
- [00 basics](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/walkthrough/std_lang_00_basic.clj)
- [01 multi-lang](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/walkthrough/std_lang_01_multi.clj)
- [02 live eval](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/walkthrough/std_lang_02_live.clj)
- 03 under the hood
- 04 creating a language
- 05 creating a runtime

### std.lang - examples

- [react native components](https://github.com/zcaudate/foundation.react-native), generated from [build namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/component/build_native_index.clj) and [index namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/component/web_native_index.clj).
- [c pthreads](https://github.com/zcaudate/play.c-000-pthreads-hello), generated from [build namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/c_000_pthreads_hello/build.clj) and [main namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/c_000_pthreads_hello/main.clj)
- [ngx hello](https://github.com/zcaudate/play.ngx-000-hello), generated from [build namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_000_hello/build.clj) and [main namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_000_hello/main.clj)
- [ngx server](https://github.com/zcaudate/play.ngx-000-hello), generated from [build namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_001_eval/build.clj) and [main namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_001_eval/main.clj)
- [tui counter](https://github.com/zcaudate/play.tui-000-counter), generated from [build namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_000_counter/build.clj) and [main namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_000_counter/main.clj)
- [tui fetch](https://github.com/zcaudate/play.tui-001-fetch), generated from [build namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_001_fetch/build.clj) and [main namespace](https://www.github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/tui_001_fetch/main.clj)
- [tui game of life](https://github.com/zcaudate/play.tui-002-game-of-life), generated from [build namespace](https://www.github.com/zcaudate-xyz/foundation-base/tree/main/src-build/play/tui_002_game_of_life/build.clj) and [main namespace](https://www.github.com/zcaudate-xyz/foundation-base/tree/main/src-build/play/tui_002_game_of_life/main.clj)

## License

Copyright © 2023 Chris Zheng, MIT License
