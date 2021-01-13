(ns sky-observer.logic-test
  (:use midje.sweet)
  (:require [sky-observer.logic :refer :all]
            [cheshire.core :refer [parse-string]])
  (:import (java.time LocalDateTime)))

(fact "regex-check"
      (#'sky-observer.logic/is-numeric-string "40.5")
      => true?)

(fact "find historic searches"
      (find-historic-searches "40.0" "50.0")
      =not=> empty?)

(fact "find historic searches numeric input"
      (find-historic-searches 40.0 50.0)
      => (throws ClassCastException))

(fact "check coordinates invalid input coordinate-location"
      (check-coordinates "40s.0" "50.0f" coordinate-location)
      => (fn [result] (= (compare result "{}") 0)))

(fact "check coordinates find-historic-searches"
      (check-coordinates "40.0" "50.0" find-historic-searches)
      =not=> empty?)

(fact "check coordinates coordinate-location"
      (check-coordinates "44.7727174" "20.4747931" coordinate-location)
      => (fn [result] (= (compare result
                                  "{\"lat\":\"44.7727708\",\"lon\":\"20.475007857226572\",\"place\":\"Факултет организационих наука, 154, Јове Илића, МЗ Чиновничка колонија, Београд (Вождовац), Градска општина Вождовац, Београд, Град Београд, Централна Србија, 11000, Србија\"}") 0)))

(fact "locations"
      (locations "Berli")
      => (fn [result] (= (count (parse-string result)) 9)))

(fact "locations invalid input"
      (locations "qwerrt")
      => (fn [result] (= (count (parse-string result)) 0)))

(fact "search empty lat"
      (search "Location" "" "50.0" "2021-01-13" "22:15")
      => (fn [result] (= (compare result "[]") 0)))

(fact "search invalid lat lon"
      (search "Location" "100" "190" "2021-01-13" "22:15")
      => (fn [result] (= (compare result "[]") 0)))

(fact "search invalid date time"
      (search "Location" "50.0" "50.0" "2021:01-13" "25:15")
      => (fn [result] (= (compare result "[]") 0)))

(fact "search"
      (search "Location" "40.5555" "87.255" "2020-10-10" "12:10")
      => (fn [result] (= (count (parse-string result)) 24)))

(fact "visible flyby"
      (#'sky-observer.logic/get-visible-flyby (LocalDateTime/parse "2020-10-10T12:10:00") 40.5555 87.255 0)
      => (every-checker (fn [result] (= (count (parse-string result)) 24))) ; output should be only one list with 24 maps
      (every-checker (fn [result] (filter (fn [v] (> v 1) (vals (frequencies (parse-string result)))))))) ; checks for duplicates

(fact "get results from threads"
      (#'sky-observer.logic/get-results (LocalDateTime/parse "2020-10-10T12:10:00") 40.5555 87.255 0)
      => (fn [result] (>= (count result) 4)))

(fact "get coco"
      (#'sky-observer.logic/get-coco 40.5555 87.255 "2020-10-10" "12:10")
      => (fn [result] (>= result 0)))

(fact "get coco no data"
      (#'sky-observer.logic/get-coco 40.5555 87.255 "2025-10-10" "12:10")
      => 0)


