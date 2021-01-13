(ns sky-observer.db-test
  (:use midje.sweet)
  (:require [sky-observer.db :refer :all]))

(defn contains-kv? [k v]
  (fn [actual]
    (if-let [kv (find actual k)]
      (= (val kv) v)
      false)))

(fact "save search test"
      (save-search "Location" 50.0 40.0 "2021-01-06" "18:00")
      => (contains-kv? :location-name "Location")
      (contains-kv? :date "2021-01-06")
      (contains-kv? :time "18:00")
      (contains-kv? :loc {:coordinates [40.5 50.0]
                          :type        "Point"}))
