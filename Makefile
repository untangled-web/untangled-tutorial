LEIN_RUN = rlwrap lein run -m clojure.main ./script/figwheel.clj

devguide:
	JVM_OPTS="-server -Ddevguide" ${LEIN_RUN}

dev:
	JVM_OPTS="-server -Dtest -Ddevguide" ${LEIN_RUN}

tests:
	npm install
	lein doo chrome automated-tests once

help:
	@ make -rpn | sed -n -e '/^$$/ { n ; /^[^ ]*:/p; }' | sort | egrep --color '^[^ ]*:'

pages:
	lein cljsbuild once pages

.PHONY: dev devguide tests help pages
