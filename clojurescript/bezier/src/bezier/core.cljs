(ns bezier.core
  (:require [weft.canvas :as c]
            [goog.dom :as dom]))

(def p1 [20 200])
(def p2 [140 20])
(def p3 [280 280])
(def p4 [420 40])

(defn draw-points [ctx points r color]
  (doto ctx
    c/save
    (c/set-fill-style color))
  (doseq [p points]
    (doto ctx
      c/begin-path
      (c/move-to p)
      (c/circle p r)
      c/fill))
  (c/restore ctx))

(doto (c/context (dom/getElement "bg-quad-canvas"))
  c/begin-path
  (c/move-to p1)
  (c/line-to p2)
  (c/line-to p3)
  c/stroke
  (draw-points [p1 p2 p3] 3 "black"))

(doto (c/context (dom/getElement "bg-cubic-canvas"))
  c/begin-path
  (c/move-to p1)
  (c/line-to p2)
  (c/line-to p3)
  (c/line-to p4)
  c/stroke
  (draw-points [p1 p2 p3 p4] 3 "black"))
  
(def quad-ctx  (c/context (dom/getElement "anim-quad-canvas")))
(def cubic-ctx (c/context (dom/getElement "anim-cubic-canvas")))

(defn make-ticker [step]
  (let [t (atom 0)
        d (atom 1)]
    (fn []
      (when-not (<= 0 @t 1)
        (swap! d * -1))
      (swap! t + (* step @d)))))

(def quad-tick  (make-ticker 0.005))
(def cubic-tick (make-ticker 0.005))

(defn render-quad []
  (let [t (quad-tick)
        p (c/lerp p1 p2 t)
        q (c/lerp p2 p3 t)
        x (c/lerp p q t)]
    (doto quad-ctx
      c/save
      c/clear-rect

      (c/set-line-width 2)
      (c/set-stroke-style "red")

      c/begin-path
      (c/move-to p1)
      (c/quadratic-curve-to p x)
      c/stroke

      (c/set-line-width 1)
      (c/set-stroke-style "black")
      
      c/begin-path
      (c/move-to p)
      (c/line-to q)
      c/stroke
      
      (draw-points [p q] 3 "black")
      (draw-points [x] 4 "red")

      c/restore))
  (c/request-animation-frame render-quad))

(defn render-cubic []
  (let [t (cubic-tick)
        p (c/lerp p1 p2 t)
        q (c/lerp p2 p3 t)
        r (c/lerp p3 p4 t)
        u (c/lerp p q t)
        v (c/lerp q r t)
        x (c/lerp u v t)]
    
    (doto cubic-ctx
      c/save
      c/clear-rect
      
      (c/set-line-width 2)
      (c/set-stroke-style "red")

      c/begin-path
      (c/move-to p1)
      (c/bezier-curve-to p u x)
      c/stroke

      (c/set-line-width 1)
      (c/set-stroke-style "black")

      c/begin-path
      (c/move-to p)
      (c/line-to q)
      (c/line-to r)
      (c/move-to u)
      (c/line-to v)
      c/stroke
      
      (draw-points [p q r u v] 3 "black")
      (draw-points [x] 4 "red")
      c/restore))
  (c/request-animation-frame render-cubic))

(defn ^:export start []
  (render-quad)
  (render-cubic))
