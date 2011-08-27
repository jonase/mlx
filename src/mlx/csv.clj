(ns mlx.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]))

(defn percentage-change [old new]
  (* (/ (- new old) old) 100))

(def fifth #(nth % 4))

(def extract (juxt first second fifth))

(defn parse [stock-data-row]
  (let [[date open close] (extract stock-data-row)]
    [date (percentage-change (Double/parseDouble open)
                             (Double/parseDouble close))]))

(defn max-change [csv-file]
  (with-open [reader (io/reader csv-file)]
    (->> (rest (csv/read-csv reader))
         (map parse)
         (apply max-key second))))

(comment
  (max-change "/home/jonas/Downloads/table.csv"))