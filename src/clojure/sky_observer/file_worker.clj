(ns sky-observer.file-worker
  (:require [cheshire.core :refer [parse-string]]
            [clojure.java.io :as io]))

(defn get-endpoint [name]
  (name (parse-string (slurp (io/resource "apis.json")) true)))

(defn get-orekit-data []
  (io/resource "orekit-data"))

(defn load-satellites []
  (parse-string (slurp (io/resource "satellites.json")) true))