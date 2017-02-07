(ns app.i18n.es (:require untangled.i18n.core) (:import goog.module.ModuleManager))

;; This file was generated by untangled's i18n leiningen plugin.

(def
 translations
 {"|This is a test" "Spanish",
  "|Hi, {name}" "Ola, {name}",
  "|N: {n, number} ({m, date, long})"
  "N: {n, number} ({m, date, long})"})

(swap!
 untangled.i18n.core/*loaded-translations*
 (fn [x] (assoc x "es" translations)))

(try
 (-> goog.module.ModuleManager .getInstance (.setLoaded locale))
 (catch js/Object e))