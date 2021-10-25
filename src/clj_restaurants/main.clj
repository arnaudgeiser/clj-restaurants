(ns clj-restaurants.main
  (:require [clojure.java.io :as io]
            [clojure.edn :refer [read-string]]
            [clj-restaurants.system :as system])
  (:gen-class))

(defn read-config []
  (->> (io/resource "config.edn")
       slurp
       read-string))

(defn -main
  [& _]
  (let [config (read-config)]
    (.start (system/build-system config))))

(comment
  (-main))


(comment
  (use 'com.stuartsierra.component)
  (def system (start (system/build-server (read-config))))
  (stop system))
