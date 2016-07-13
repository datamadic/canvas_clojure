(ns from-scratch.core
  (:require-macros [from-scratch.macros :as m]
                    [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <! >! timeout close! alts! pub sub dropping-buffer]]))

(enable-console-print!)

(println (m/m 4))

(def c-mousedown (chan))
(def c-mousemove (chan))
(def c-click (chan))

(def s-move (chan (dropping-buffer 1)))
(def s-move-paint (chan (dropping-buffer 1)))

(def p-mousemove (pub c-mousemove :action))

(sub p-mousemove :move s-move)
(sub p-mousemove :move s-move-paint)

(def canvas (js/document.getElementById "canvas"))
(def context (.getContext canvas  "2d" ))

(def canvas-img (js/document.getElementById "canvas-img"))
(def image (js/document.getElementById "source"))
(def context-img (.getContext canvas-img  "2d" ))

(def img-width 300)
(def img-height 227)
(def num-pix (* img-width img-height))
(def px-coll (range num-pix))
(def mixed (shuffle px-coll))

; (println (take 5 px-coll))
(println (take 5 mixed))
(loop [idx 0]
  (if (< idx num-pix)
    (do
      (let [dx (mod (nth mixed idx) img-width)
            dy (- img-height (Math/floor (/ (- num-pix (+ 1 (nth mixed idx))) img-width)))
            sx (mod idx img-width)
            sy (- img-height (Math/floor (/ (- num-pix (+ 1 idx)) img-width)))
            ]
        ; (println "s: (" sx "," sy  ")   d:"  "(" dx "," dy  ")   ")
        (.drawImage context image sx sy 1 1 dx dy 1 1))
      
      ;; (println "the idx" idx
      ;;          (nth mixed idx)
      ;;          ;"el divo" (Math/floor  (/ num-pix idx))
      ;;          "the column "
      ;;          "the row" (- img-height (Math/floor (/ (- num-pix (+ 1 idx)) img-width)) ) )
      (recur (+ 1 idx)))))

; (Math/floor (/ num-pix num-pix))
;; [0 1 2
;;  3 4 5
;;  6 7 8]

;; how many sets of rows are before?

(comment
  ; sx refers to the src image, dx refers to the dest canvas 
  void ctx.drawImage(image, dx, dy);
  void ctx.drawImage(image, dx, dy, dWidth, dHeight);
  void ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
  )

; (.drawImage context-img image 33 71 104 124 21 20 87 104)

;; (set! (.-fillStyle context) "rgba(0, 0, 200, 0.8)")

;; ;; x y width height
;; (.strokeRect context  5 5  100 100)
;; (.fillRect context 10 10 100 100)
;; (.clearRect context 60 60 100 100)

(defn rect-at [x, y, ctx]
  ;; (if (even? (rand-int 100))
  ;;   (set! (.-strokeStyle ctx) "rgba(0, 0, 200, 1)")
  ;;                                       ;(set! (.-strokeStyle ctx) "red")
  ;;   (set! (.-strokeStyle ctx) "rgba(200, 0, 0, 1)")
  ;;   )
  (.strokeRect ctx  x y  100 100))

(defn clear-at [x, y, ctx]
  (.clearRect ctx  x y  100 100))

(defn fade-out [x y width ctx]
  (loop[x x y y width width]
    (if (> width 4)
      
      (do
        (println x y width)
        (let [_x (+ x 20)
              _y (+ y 20)
              _width (- width 15)]
          (.strokeRect ctx
                     _x
                     _y
                     _width
                     _width)
          (recur _x _y _width)))
      (do
        (println "done")))))


(go-loop []
  (let [event (<! c-mousedown)]
    (println "down it was..." event))
  (recur))

(.addEventListener canvas "mousedown" #(go (>! c-mousedown %)))



(.addEventListener canvas "mousemove" #(go (>! c-mousemove {:action :move :data %})) )

(.addEventListener  canvas "click" #(clear-at
                                         (- (.-offsetX %) 50)
                                         (- (.-offsetY %) 50)
                                         context))
(.addEventListener  canvas "click" #(go (>! c-click %)))

(go
  (println (<! c-mousedown) "there it was..."))


(go-loop []
  (let [data (:data (<! s-move))]
    (println data))
  (recur))
  
;; (go-loop []
;;   (let [event (<! c-mousemove)]
;;     (println "got it " event)
;;     (rect-at
;;      (- (.-offsetX event) 50)
;;      (- (.-offsetY event) 50)
;;      context)
    
;;     ;; (fade-out
;;     ;;  (- (.-offsetX event) 50)
;;     ;;  (- (.-offsetY event) 50)
;;     ;;  100
;;     ;;  context)
;;     )
  
;;   (recur))






;; (go
;;   (println "got it")
;;   (println (<! c-click))
;;   (println "see?"))

;ctx.fillStyle = "green";
;ctx.fillRect(10, 10, 100, 100);

(println "this is the context" canvas)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

