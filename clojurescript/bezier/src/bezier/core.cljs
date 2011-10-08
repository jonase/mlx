(ns bezier.core
  (:require [weft.canvas :as c])
  (:use [clojure.browser.dom :only [get-element]]
        [weft.canvas :only [lerp
                            save restore
                            set-fill-style set-stroke-style set-line-width
                            begin-path fill stroke
                            move-to line-to quadratic-curve-to bezier-curve-to
                            circle clear-rect
                            context
                            request-animation-frame]]))

(def p1 [20 200])
(def p2 [140 20])
(def p3 [280 280])
(def p4 [420 40])

(defn draw-points [ctx points r color]
  (doto ctx
    save
    (set-fill-style color))
  (doseq [p points]
    (doto ctx
      begin-path
      (move-to p)
      (circle p r)
      fill))
  (restore ctx))

(doto (context (get-element "bg-quad-canvas"))
  begin-path
  (move-to p1)
  (line-to p2)
  (line-to p3)
  stroke
  (draw-points [p1 p2 p3] 3 "black"))

(doto (context (get-element "bg-cubic-canvas"))
  begin-path
  (move-to p1)
  (line-to p2)
  (line-to p3)
  (line-to p4)
  stroke
  (draw-points [p1 p2 p3 p4] 3 "black"))
  
(def quad-ctx  (context (get-element "anim-quad-canvas")))
(def cubic-ctx (context (get-element "anim-cubic-canvas")))

(defn make-ticker [step]
  (let [t (atom 0)
        d (atom 1)]
    (fn []
      (when (or (> @t 1)
                (< @t 0))
        (swap! d * -1))
      (swap! t + (* step @d)))))

(def quad-tick  (make-ticker 0.005))
(def cubic-tick (make-ticker 0.005))

(defn render-quad []
  (let [t (quad-tick)
        p (lerp p1 p2 t)
        q (lerp p2 p3 t)
        x (lerp p q t)]
    (doto quad-ctx
      save
      clear-rect

      (set-line-width 2)
      (set-stroke-style "red")

      begin-path
      (move-to p1)
      (quadratic-curve-to p x)
      stroke

      (set-line-width 1)
      (set-stroke-style "black")
      
      begin-path
      (move-to p)
      (line-to q)
      stroke
      
      (draw-points [p q] 3 "black")
      (draw-points [x] 4 "red")

      restore))
  (request-animation-frame render-quad))

(defn render-cubic []
  (let [t (cubic-tick)
        p (lerp p1 p2 t)
        q (lerp p2 p3 t)
        r (lerp p3 p4 t)
        u (lerp p q t)
        v (lerp q r t)
        x (lerp u v t)]
    
    (doto cubic-ctx
      save
      clear-rect
      
      (set-line-width 2)
      (set-stroke-style "red")

      begin-path
      (move-to p1)
      (bezier-curve-to p u x)
      stroke

      (set-line-width 1)
      (set-stroke-style "black")

      begin-path
      (move-to p)
      (line-to q)
      (line-to r)
      (move-to u)
      (line-to v)
      stroke
      
      (draw-points [p q r u v] 3 "black")
      (draw-points [x] 4 "red")
      restore))
  (request-animation-frame render-cubic))

(defn ^:export start []
  (render-quad)
  (render-cubic))
