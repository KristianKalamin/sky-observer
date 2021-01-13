(ns sky-observer.logic
  (:require [sky-observer.api-call :refer [weather-condition
                                           find-location
                                           find-location-with-coordinates]]
            [sky-observer.file-worker :as file-worker]
            [clojure.string :as str]
            [cheshire.core :refer [generate-string]]
            [clojure.set :refer [union]]
            [clojure.core.async :refer [thread <!! go]]
            [sky-observer.db :refer [save-search get-within-radius]])
  (:import (sky_observer.space VisibilityCheck)
           (java.time LocalDateTime)))

(def ^:private visibility-check (VisibilityCheck. (file-worker/get-orekit-data)))
(def ^:private satellite-list (doall (file-worker/load-satellites)))

(defn ^:private propagate [date-time lat lon satellites sky-visibility]
  (thread
    (map (fn [obj]
           (dissoc obj :class))
         (filter (fn [x] (some? (:startFlybyTime x)))       ; not nil
                 (map (fn [satellite] (bean (.propagate visibility-check
                                                        date-time ; "2020-11-20T23:00:00"
                                                        sky-visibility
                                                        lat
                                                        lon
                                                        (get satellite :satellite-name)
                                                        (get satellite :line1)
                                                        (get satellite :line2)))) satellites)))))

(defn ^:private get-coco [lat lon date time]
  (let [weather (weather-condition lat lon date)]
    (if (nil? weather)
      0                                                     ; no weather data so assuming it will be clear sky
      (:coco (first (filter (fn [hour-weather]
                              (= (subs (get (str/split (get hour-weather :time) #" ") 1) 0 2) (subs time 0 2)))
                            weather))))))

(defn ^:private get-results [date-time lat lon sky-visibility]
  (map (fn [thread-result]
         (<!! thread-result))
       (map (fn [s] (propagate date-time lat lon s sky-visibility))
            (partition-all (int (/ (count satellite-list) 4)) satellite-list))))

(defn ^:private get-visible-flyby [date-time lat lon sky-visibility]
  (generate-string (distinct (reduce concat (get-results date-time lat lon sky-visibility)))))

(defn ^:private check-date-time [date time]
  (try
    (LocalDateTime/parse (str date "T" time))
    (catch Exception _
      ; (prn e)
      nil)))

(defn ^:private validate-coordinates [lat lon]
  (cond
    (or (= lat "") (= lon "")) nil
    :else
    (let [d-lat (Double/parseDouble lat)
          d-lon (Double/parseDouble lon)]
      (cond
        (and (>= d-lat -90) (<= d-lat 90) (>= d-lon -180) (<= d-lon 180)) {:lat d-lat
                                                                           :lon d-lon}
        :default nil))))

(defn ^:private start-search [weather-condition-code date-time num-lat num-lon]
  (cond
    (< weather-condition-code 3) (get-visible-flyby date-time num-lat num-lon 0)
    (= weather-condition-code 3) (get-visible-flyby date-time num-lat num-lon 60)
    :default (get-visible-flyby date-time num-lat num-lon 100)))


(defn search [location lat lon date time]
  (let [date-time (check-date-time date time)
        coord-map (validate-coordinates lat lon)]

    (if (or (nil? date-time) (nil? coord-map))
      (generate-string [])
      (let [{num-lat :lat
             num-lon :lon} coord-map]
        (go (save-search location num-lat num-lon date time))
        (start-search (get-coco num-lat num-lon date time) date-time num-lat num-lon)))))

(defn locations [params]
  (generate-string (find-location params)))

(defn ^:private is-numeric-string [s]
  (= (re-matches #"[0-9\.]+$" s) s))

(defn coordinate-location [lat lon]
  (generate-string (find-location-with-coordinates lat lon)))

(defn ^:private get-lat-lon [data]
  {:lon ((get (get data :loc) :coordinates) 0)
   :lat ((get (get data :loc) :coordinates) 1)})

(defn check-coordinates [lat lon fun]
  (let [str-lat (str lat)
        str-lon (str lon)]
    (if (or (is-numeric-string str-lat) (is-numeric-string str-lon))
      (fun str-lat str-lon)
      (generate-string {}))))

(defn find-historic-searches [lat lon]
  (generate-string (map get-lat-lon (get-within-radius (Double/parseDouble lat)
                                                       (Double/parseDouble lon)))))