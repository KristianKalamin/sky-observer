(ns sky-observer.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all])
  (:import (java.time LocalDateTime)))

;(def uri "mongodb://localhost:27017")
(def ^:private db-name "sky-observer-db")
(def ^:private collection-name "search-collection")

(defn save-search [location-name lat lon date time]
  (mc/insert-and-return (mg/get-db (mg/connect) db-name) collection-name {:location-name    location-name
                                                                           :date             date
                                                                           :time             time
                                                                           :search-timestamp (.toString (LocalDateTime/now))
                                                                           :loc              {:coordinates (vector lon lat)
                                                                                              :type        "Point"}}))

(defn get-within-radius [lat lon]
  (mc/find-maps (mg/get-db (mg/connect) db-name) collection-name {:loc {$geoWithin {$center [[lon lat] 15]}}}))