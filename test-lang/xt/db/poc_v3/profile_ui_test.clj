(ns xt.db.poc-v3.profile-ui-test
  "Compile-level test for the `xt.db.poc-v3.profile-ui` React UI.

   The UI is built on top of the SharedWorker-backed xt.db.node adaptor.
   Rendering the React component in the headless :playground runtime is not
   currently feasible because that runtime executes JS in a worker context
   where `react-dom/client` is not available, and because the SharedWorker's
   native fetch never resolves under headless Chrome. This test verifies that
   the namespace compiles cleanly and exposes the expected public API."
  (:use code.test)
  (:require [xt.db.poc-v3.profile-ui :as profile-ui]))

^{:refer xt.db.poc-v3.profile-ui-test/profile-ui-compiles
  :added "4.1"}
(fact "profile ui namespace compiles and exposes renderProfileUI"
  (some? (resolve 'xt.db.poc-v3.profile-ui/renderProfileUI))
  => true)
