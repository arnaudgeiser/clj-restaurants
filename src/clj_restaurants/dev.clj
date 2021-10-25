(ns clj-restaurants.dev
  (:require [reloaded.repl :refer [reset-all go]]
            [clj-restaurants.system :refer [build-server]]))

(comment
  (reset-all)
  (go)
  (reloaded.repl/clear)
  (let [config (clj-restaurants.main/read-config)]
    (reloaded.repl/set-init! (partial #'build-server config)))

  (build-server {})
  (reloaded.repl/initializer))
