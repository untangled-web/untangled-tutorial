# Untangled Tutorial 

NOTE: This tutorial requires Om alpha31+ which is not yet released. Until then you can use [Om as a checkout dependency](https://github.com/technomancy/leiningen/blob/792750b7a1bdf0499081c72b197df41cee5ef648/doc/TUTORIAL.md#checkout-dependencies).

This is an interactive tutorial for developing applications with 
the Untangled web framework.

It is really meant to be cloned locally, as the 
exercises require you to extend and edit the code.

There are two primary branches in this repository:

- `main`: The branch to use to complete the tutorial
- `solution`: A branch with all of the exercises completed

## What's inside?

This tutorial covers all of the elements of the Untangled 
framework, including the necessary elements of the underlying
Om library (on which much of the system is based):

- Recommended Development Environment
    - IntelliJ/Cursive
    - Adding Run Configurations for client and server
    - Running Figwheel w/REPL Integration
    - Running Server REPL
    - Running Selected Builds with REPL
        - Switching Client REPLs
- The critical elements of Om UI
    - Application database format
        - Tables
        - Idents
        - Building out the graph
    - Queries
        - Properties
        - Joins
        - Unions
    - Constructing UI
        - Colocated queries and Idents
        - Normalization
        - Initial state
- Untangled extensions for UI
    - Built-in Om parser
    - Rationale for reductions of complexity (and flexibility) from standard Om Next
    - Built-in UI mutations
    - Making the UI dynamic
        - Basic mutations
        - (e.g. tabs, sub-tabs, modal dialogs)
    - Internationalization
    - Logging
- Server integration 
    - The Untangled server
        - Server Side Query processing
            - Parsing Queries
        - Parsing/Processing Mutations
            - Tempids
    - Initial Application Loads
        - `(load-collection)`/`(load-singleton)`
        - Choosing a query
        - Eliding portions of the query (for later lazy loading)
        - Sending additional parameters to the server
        - Post-processing the query result
    - Lazy loading additional content
        - Lazy loading a field for a component `(load-field)`
    - Network plumbing guarantees sequential processing
    - Tempid Handling
        - Support for returning :tempids from server action
        - Automatically fixed in client state (no code required)
        - Tempids rewritten in network send queue
    - Fallbacks (unhappy path for handling server errors)
        - `(tx/fallback)`
        - Clearing the network send queue
    - Support for UI/Server data separation in queries
        - Elision of :ui/... attributes in server queries
    - Advanced Merging
        - Deep merging
        - Behavior when merging to existing entities
- Preparing for Production Deployment
    - Internationalization 
        - Extracting Strings
        - Translating
        - Generating cljs translation files
        - Using modules to lazy-load translations
        
# Running It

## Figwheel

The following builds are configured in figwheel:

- `tutorial`: Devcards tutorial for the content listed above
- `client`: A full-stack application (that you extend as part of the tutorial)
- `test`: Tests for the client application (for you to see/extend)

### IntelliJ/Cursive

The project is set up for an optimal experience in IntelliJ with Cursive:

- Running selected figwheel builds using run configurations
- Integration with REPL commands (e.g. send form to REPL from editor)

To accomplish this, we chose to use figwheel sidecar and script it. The
code is organized so that if you wish to use nREPL with Emacs/vi you
can get the same features (documentation coming soon).

The `dev/server/user.clj` contains the primary code, which is in turn 
invoked by the `script/figwheel.clj` file. You select builds by specifying
`-Dbuild_name` as a JVM option (any number of times). Thus, you can
run one or many builds.

To configure this in IntelliJ:

- Edit Run Configurations
- Press +, and add a Clojure Local REPL. Name it something like "All builds"
- Choose "Use Clojure Main" (see image below)
- Add `-Dn` options to the JVM Args for each build you want to run, where `n` is a build
defined in the `:cljsbuild` section of the `project.clj` file.
- Set parameters to `script/figwheel.clj`

<img src="docs/figwheel-build.png">

You should now be able to run the builds via the IDE.

### Command line

```
JVM_OPTS="-Dtutorial" lein run -m clojure.main script/figwheel.clj
```

Then browse to the following URL:

```
http://localhost:3449
```

### Figwheel notes

Once the figwheel REPL is going, you can clean and rebuild with 

```
(reset-autobuild)
```

after which you probably want to reload the page in your browser to clear out any cruft.

Sometimes (rarely) it is necessary to just stop it all, clean everything with `lein clean` and
restart.

## Server

Running the server is pretty simple. It is set up to run just fine from nREPL or clojure main.

### IntelliJ

Add a run configuration for a Local Clojure REPL, and do NOT specify parameters (JVM args are
fine, of course).

### Command line

```
lein repl
```

or

```
lein run -m clojure.main
```

### Using the Server REPL

Once you have a server REPL going, you can start the server (and refresh/reset it at any time).
The server is written using the components library, so it is trivial to hot reload.

Starting the server:

```
user=> (go)
```

Code refresh/server restart:

```
user=> (reset)
```

If you get compile errors, you'll need to manually refresh the source:

```
user=> (refresh)
```

DO NOT do a refresh while the server is running, only IF a compile fails
(at which point the server will have been stopped). If you accidentally
refresh while the server is running you will not be able to start it
because the old server will have the port, but you will not be able to
stop it. If this happens you must kill/restart you REPL.
