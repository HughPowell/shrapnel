(ns test
  (:require [com.mjdowney.rich-comment-tests.test-runner :as test-runner]))

(defn run-tests! [dirs]
  (let [{:keys [test fail error]} (test-runner/run-tests-in-file-tree! dirs)]
    (if (or (zero? test) (not (zero? fail)) (not (zero? error)))
      (System/exit 1)
      (System/exit 0))))
