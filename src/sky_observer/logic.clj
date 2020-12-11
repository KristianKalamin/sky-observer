(ns sky-observer.logic
  (:require [sky-observer.api-call :refer [weather-condition find-location]]
            [sky-observer.file-worker :as file-worker]
            [clojure.string :as str]
            [clojure.set :refer [union]]
            [clojure.core.async :refer [thread <!!]])
  (:import (sky_observer.space VisibilityCheck)))

(def visibility-check (VisibilityCheck. (file-worker/get-orekit-data)))
(def satellite-list (doall (file-worker/load-satellites)))

(defn propagate [date time lat lon satellites sky-visibility]
  (thread
    (map (fn [obj]
           (dissoc obj :class))
         (filter (fn [x]
                   (some? (:startFlybyTime x)))             ; not null
                 (map (fn [satellite]
                        (bean (.propagate visibility-check
                                          (str date "T" time) ;"2020-11-20T23:00:00"
                                          sky-visibility
                                          lat
                                          lon
                                          (get satellite :satellite-name)
                                          (get satellite :line1)
                                          (get satellite :line2))))
                      satellites))
         )))

(defn get-coco [lat lon date time]
  (:coco (first (filter (fn [hour-weather]
                          (= (subs (get (str/split (get hour-weather :time) #" ") 1) 0 2) (subs time 0 2)))
                        (weather-condition lat lon date)))))

(defn get-visible-flyby [date time lat lon sky-visibility]
  (distinct (reduce concat (map (fn [thread-result]
                                  (<!! thread-result))
                                (map (fn [s]
                                       (propagate date time lat lon s sky-visibility))
                                     (partition-all (int (/ (count satellite-list) 4)) satellite-list))))))

(defn search [location lat lon date time]
  (let [weather-condition-code (get-coco lat lon date time)]
    (cond
      (< weather-condition-code 3) (get-visible-flyby date time lat lon 0)
      (= weather-condition-code 3) (get-visible-flyby date time lat lon 60)
      :default (get-visible-flyby date time lat lon 100))


    ;(if (< weather-condition-code 3)
    ;  (get-visible-flyby date time lat lon 0)
    ;  (if (= weather-condition-code 3)
    ;    (get-visible-flyby date time lat lon 60)
    ;    (get-visible-flyby date time lat lon 100)))
    ))

(defn locations [params]
  (find-location params))