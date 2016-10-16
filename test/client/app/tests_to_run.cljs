(ns app.tests-to-run
  (:require
    app.sample-spec
    app.testing-devguide-solutions-spec
    app.exercises-solutions-spec
    ; TODO: Import your additional specs here
    app.testing-devguide-spec))

;********************************************************************************
; IMPORTANT:
; For cljs tests to work in CI, we want to ensure the namespaces for all tests are included/required. By placing them
; here (and depending on them in suite.cljs for dev), we ensure that the all-tests namespace (used by CI) loads
; everything as well.
;********************************************************************************

