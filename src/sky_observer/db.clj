(ns sky-observer.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all])
  (:import (java.time LocalDateTime)))

;(def uri "mongodb://localhost:27017")
(def db-name "sky-observer-db")
(def collection-name "search-collection")

(def get-db (mg/get-db (mg/connect) db-name))


(defmacro ^{:private true} defoperator
  [operator]
  `(def ^{:const true} ~(symbol (str operator)) ~(str operator)))

(defoperator $center)

(defn save-search [location-name lat lon date time]

  (mc/insert-and-return get-db collection-name (hash-map
                                                 :location-name location-name
                                                 :date date
                                                 :time time
                                                 :search-timestamp (-> (LocalDateTime/now)
                                                                       (.toString))
                                                 :loc (hash-map
                                                        :coordinates (vector lon lat)
                                                        :type "Point"
                                                        ))))

(defn get-within-radius [lat lon]
  (mc/find-maps get-db collection-name {:loc {$geoWithin {$center [[lon lat] 15]}}}))