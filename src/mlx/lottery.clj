(ns mlx.lottery
  (:require [clojure [set :as set]]))

(defn rand-lottery-number [] 
  (inc (rand-int 39)))

(defn rand-lottery-row []
  (->> (range 1 40)
       shuffle
       (take 7)
       set))

(defn rand-lottery-row' []
  (->> (repeatedly rand-lottery-number)
       distinct
       (take 7)
       set))

(defn rand-lottery-row'' []
  (loop [lottery-row #{}]
    (if (= (count lottery-row) 7)
      lottery-row
      (recur (conj lottery-row (rand-lottery-number))))))

(defn bench [f]
  (dotimes [i 10]
    (printf "Run %d: " i)
    (time (dotimes [_ 100000] (f)))))

(defn lottery-numbers [] (repeatedly rand-lottery-row''))

(defn correct [row-1 row-2]
  (count (set/intersection row-1 row-2)))

(defn run-lottery [my-nums n]
  (time
   (->> (lottery-numbers)
	(map #(correct my-nums %))
	(some #(= % n)))))

