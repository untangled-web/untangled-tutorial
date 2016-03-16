LEIN_RUN = rlwrap lein run -m clojure.main ./script/figwheel.clj

tutorial:
	JVM_OPTS="-server -Dtutorial" ${LEIN_RUN}

dev:
	JVM_OPTS="-server -Dclient -Dtest -Dtutorial" ${LEIN_RUN}

tests:
	npm install
	lein doo chrome automated-tests once

help:
	@ make -rpn | sed -n -e '/^$$/ { n ; /^[^ ]*:/p; }' | sort | egrep --color '^[^ ]*:'

.PHONY: dev tutorial tests help
