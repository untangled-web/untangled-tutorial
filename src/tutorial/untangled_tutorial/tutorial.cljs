(ns untangled-tutorial.tutorial
  (:require
    untangled-tutorial.A-Introduction
    untangled-tutorial.B-UI
    untangled-tutorial.B-UI-Exercises
    untangled-tutorial.C-App-Database
    untangled-tutorial.C-App-Database-Exercises
    untangled-tutorial.D-Queries
    untangled-tutorial.E-UI-Queries-and-State
    untangled-tutorial.E-UI-Queries-and-State-Exercises
    untangled-tutorial.F-Untangled-Client
    untangled-tutorial.G-Mutation
    untangled-tutorial.G-Mutation-Exercises
    untangled-tutorial.H-Server-Interactions
    untangled-tutorial.Z-Glossary
    untangled-tutorial.Z-Query-Quoting
    [devtools.core :as devtools]))

(defonce devtools-installed
         (do (devtools/enable-feature! :sanity-hints)
             (devtools/install!)
             true))
