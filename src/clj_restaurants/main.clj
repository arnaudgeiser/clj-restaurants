(ns clj-restaurants.main
  (:require [clj-restaurants.system :as system]))

(defn -main
  [& _]
  (.start system/system))

(comment
  (-main))
