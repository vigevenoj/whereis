(ns whereis.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [whereis.core-test]))

(doo-tests 'whereis.core-test)

