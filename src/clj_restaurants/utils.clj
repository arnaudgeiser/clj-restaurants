(ns clj-restaurants.utils
  (:require [clojure.string :refer [includes?]]))

(defn now [] (java.time.LocalDate/now))

(defn ip-address []
  (->> (java.net.Inet4Address/getLocalHost)
       (.getHostAddress)))

(defn count-likes [likes]
  (->> likes
       (filter :likes/appreciation)
       count))

(defn count-dislikes [likes]
  (->> likes
       (filter (complement :likes/appreciation))
       count))

(defn get-restaurant-id [name restaurants]
  (->> restaurants
       (filter #(includes? (:restaurants/nom %) name))
       first
       :restaurants/numero))
