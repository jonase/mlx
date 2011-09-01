(ns weft.canvas)

;; From http://paulirish.com/2011/requestanimationframe-for-smart-animating/
(def request-animation-frame ;js/webkitRequestAnimationFrame)
  (js* "(function(){
      return  window.requestAnimationFrame       || 
              window.webkitRequestAnimationFrame || 
              window.mozRequestAnimationFrame    || 
              window.oRequestAnimationFrame      || 
              window.msRequestAnimationFrame     || 
              function(/* function */ callback, /* DOMElement */ element){
                window.setTimeout(callback, 1000 / 60);
              };
    })();"))

;; Functions on canvas

(defn context [canvas]
  (.getContext canvas "2d"))

(defn width [canvas]
  (.width canvas))

(defn height [canvas]
  (.height canvas))

(defn clear [canvas]
  (set! (.width canvas) (.width canvas)))

;; Functions on context
(defn save [ctx]
  (. ctx (save)))

(defn restore [ctx]
  (. ctx (restore)))

(defn set-fill-style [ctx style]
  (set! (.fillStyle ctx) style))

(defn set-stroke-style [ctx style]
  (set! (.strokeStyle ctx) style))

(defn set-line-width [ctx w]
  (set! (.lineWidth ctx) w))

(defn fill-rect [ctx x y w h]
  (.fillRect ctx x y w h))

(defn clear-rect
  ([ctx] (clear-rect ctx
                     [0 0]
                     [(width (.canvas ctx))
                      (height (.canvas ctx))]))
  ([ctx [x y] [w h]] (.clearRect ctx x y w h)))

;; Paths
(defn move-to [ctx [x y]]
  (.moveTo ctx x y))

(defn line-to [ctx [x y]]
  (.lineTo ctx x y))

(defn arc
  ([ctx p radius start-angle end-angle]
     (arc ctx p radius start-angle end-angle true)) 
  ([ctx [x y] radius start-angle end-angle anticlockwise?]
     (.arc ctx x y radius start-angle end-angle anticlockwise?)))
  
(defn arc-to [ctx [x1 y1] [x2 y2] r]
  (.arcTo ctx x1 y1 x2 y2 r))

(defn quadratic-curve-to [ctx [cx cy] [px py]]
  (.quadraticCurveTo ctx cx cy px py))

(defn bezier-curve-to [ctx [c1x c1y] [c2x c2y] [px py]]
  (.bezierCurveTo ctx c1x c1y c2x c2y px py))

(defn begin-path [ctx]
  (. ctx (beginPath)))

(defn stroke [ctx]
  (. ctx (stroke)))

(defn fill [ctx]
  (. ctx (fill)))

(defn close-path [ctx]
  (. ctx (closePath)))

;; Text
(defn set-text-align [ctx value]
  (set! (.textAlign ctx) value))

(defn set-text-baseline [ctx value]
    (set! (.textBaseline ctx) value))

(defn set-font [ctx value]
  (set! (.font ctx) value))

(defn stroke-text
  ([ctx text [x y]] (.strokeText ctx text x y))
  ([ctx text [x y] max-width] (.strokeText ctx text x y max-width)))

(defn fill-text
  ([ctx text [x y]] (.fillText ctx text x y))
  ([ctx text [x y] max-width] (.fillText ctx text x y max-width)))

;; Gradients
(defn create-linear-gradient [ctx [x1 y1] [x2 y2]]
  (.createLinearGradient ctx x1 y1 x2 y2))

(defn add-color-stop [gradient position color]
  (.addColorStop gradient position color))

;; Custom
(defn- rounded-rect [ctx [x y] w h r]
  (let [x1 (+ x r)
        x2 (+ x w) 
        x3 (- x2 r)
        y1 (+ y r)
        y2 (+ y h)
        y3 (- y2 r)]
    (doto ctx
      (move-to [x1 y])
      (line-to [x3 y])
      (arc-to [x2 y] [x2 y1] r)
      (line-to [x2 y3])
      (arc-to [x2 y2] [x3 y2] r)
      (line-to [x1 y2])
      (arc-to [x y2] [x y3] r)
      (line-to [x y1])
      (arc-to [x y] [x1 y] r))))

(defn stroke-rounded-rect [ctx p w h r]
  (doto ctx
    begin-path
    (rounded-rect p w h r)
    stroke
    close-path))

(defn fill-rounded-rect [ctx p w h r]
  (doto ctx
    begin-path
    (rounded-rect p w h r)
    fill
    close-path))

(defn circle
  "A circle with midpoint p and radius r"
  [ctx p r]
  (arc ctx p r 0 (* 2 (. js/Math PI))))

(defn lerp
  "Linear interpolation between the points p and q interpolated by
  a. a = 0 is at point p, a = 1 is at point q, a = 0.5 is half way
  between p and q"
  [[px py] [qx qy] a]
  [(+ px (* (- qx px) a))
   (+ py (* (- qy py) a))])


    