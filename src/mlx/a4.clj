(ns mlx.a4)

(def w0 (/ 1000 (Math/sqrt (Math/sqrt 2))))
(def h0 (* w0 (Math/sqrt 2)))
(def A0 [w0 h0])

(defn halve [[w h]] [(/ h 2) w])

(defn A [n]
  (if (zero? n)
    A0
    (halve (A (dec n)))))

(defn A [n i size]
  (if (= n i)
    size
    (recur n (inc i) (halve size))))

(defn A
  ([n] (A n 0 A0))
  ([n i size]
     (if (= i n)
       size
       (recur n (inc i) (halve size)))))

(defn A [n]
  ((apply comp (repeat n halve)) A0))

(defn A [n]
  (nth (iterate halve A0) n))


