# User Inputs

This document summarizes all the inputs provided by the user.

1.  **Initial Request:** Design a dev environment in `src/indigo/*` that will make use of `code.test`, `code.doc`, `code.manage`, `code.doc` to be able to be able to run a server and be able to look at the clojure vars as well as tests generated for it. please do analysis first

2.  **Add API to map tests to vars, REPL, and file system watcher:** I would like to be able to expose an api in order to look at the tests and map them to vars in the environment I would also like to be able to pipe the repl output to the frontend as well as to be able to be notified of changes in the filesystem

3.  **Use `std.lang` for frontend and add code templating:** the frontend will be developed using std.lang. I would also like to be able to define templates for working with various namespaces so that instead of editing the code directly, code templates can be used to generate the necessary definitions to be written to file.

4.  **Investigate eventing system:** I would like to investigate an eventing routing system so that different tools can stream their outputs to the ui through a redesign of std.task

5.  **Use `std.concurrent` and `std.lib.future`:** I don't want to use core.async. it should be done through std.concurrent and std.lib.future primitives.

6.  **Explore resources:** what would be good as well is if there is a way to explore resources on the system. this is defined here: std.lib.resource

7.  **`std.lang` clarification:** please check out files in code.dev.app/* to see how to specify std.lang modules. this is not clojurescript. it is a lisp that is much closer to the js language than clojure.

8.  **Use `http-kit` and `net.http.router`:** use http-kit and net.http.router instead of ring.
