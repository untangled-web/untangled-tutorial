(ns untangled-tutorial.tutorial
  (:require
    untangled-tutorial.A-Introduction
    untangled-tutorial.A-Quick-Tour
    untangled-tutorial.B-UI
    untangled-tutorial.B-UI-Exercises
    untangled-tutorial.C-App-Database
    untangled-tutorial.C-App-Database-Exercises
    untangled-tutorial.D-Queries
    untangled-tutorial.E-UI-Queries-and-State
    untangled-tutorial.E-UI-Queries-and-State-Exercises
    untangled-tutorial.F-Untangled-Client
    untangled-tutorial.F-Untangled-DevEnv
    untangled-tutorial.F-Untangled-Initial-App-State
    untangled-tutorial.G-Mutation
    untangled-tutorial.G-Mutation-Exercises
    untangled-tutorial.G-Mutation-Solutions
    untangled-tutorial.H-Server-Interactions
    untangled-tutorial.I-Building-A-Server
    untangled-tutorial.I-Building-A-Server-Exercises
    untangled-tutorial.J-Putting-It-Together
    untangled-tutorial.K-Testing
    untangled-tutorial.M10-Advanced-UI
    untangled-tutorial.M30-Advanced-Mutation
    untangled-tutorial.M40-Advanced-Server-Topics
    untangled-tutorial.Z-Further-Reading
    untangled-tutorial.Z-Glossary
    untangled-tutorial.Z-Query-Quoting
    [devtools.core :as devtools]))

(defonce devtools-installed (devtools/install!))
