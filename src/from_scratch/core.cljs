(ns from-scratch.core
  (:require-macros [from-scratch.macros :as m]
                    [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <! >! timeout close! alts! pub sub dropping-buffer]]))

(enable-console-print!)

(println (m/m 4))

(def c-pump (chan))
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

(def num-blocks 300)
(def img-width 300)
(def img-height 300)
(def num-pix (* img-width img-height))
(def px-coll (range num-pix))
(def mixed (shuffle px-coll))

(def block-size (/ img-width num-blocks))

(println block-size "is the block size")

;(defn sort-step)

; (println (take 5 px-coll))
(println (take 5 mixed))
(loop [idx 0]
  (if (< idx num-pix)
    (do
      (let [dx (mod (nth mixed idx) num-blocks)
            dy (- img-height (Math/floor (/ (- num-pix (+ 1 (nth mixed idx))) num-blocks)))
            sx (mod idx num-blocks)
            sy (- img-height (Math/floor (/ (- num-pix (+ 1 idx)) num-blocks)))
            ]
        ; (println "s: (" sx "," sy  ")   d:"  "(" dx "," dy  ")   ")
        ;(.drawImage context image sx sy 1 1 (* block-size dx) (- dy 1) block-size block-size)
        )
        
     
      (recur (+ 1 idx)))))

(loop [idx 0
       lines []]
  (if (< idx num-pix)
    (do
      (let [dx (mod (nth mixed idx) num-blocks)
            dy (- img-height (Math/floor (/ (- num-pix (+ 1 (nth mixed idx))) num-blocks)))
            sx (mod idx num-blocks)
            sy (- img-height (Math/floor (/ (- num-pix (+ 1 idx)) num-blocks)))
            ]
        
        (recur (+ 1 idx)
               (conj lines [sx sy dx dy]))))
    (go
      (println (take 5 lines))
      (dotimes [n  5]
        (dotimes [i (- (count lines) 1)]
          (let [[sx sy dx dy] (nth lines i)
                scaler (* (- 4 n)  (/ 1 4))]
            (.drawImage
             context image
             sx
             sy
             1 1
             ; (* (* scaler (- sx dx)) 4)
             ; (* (* scaler (- sy dy)) 4)
             (- sx (* scaler (- sx dx)))
             (- sy (* scaler (- sy dy)))
             1
             1)))
        (<! (timeout 2000))
        
        (println (* (- 4 n)  (/ 1 4)))
        ))))

;(go-loop [t (timeout 1000)])


(.drawImage context  image  0 0 300 227 300 0  300 227)

;; (go
;;   (while true
;;     (let [])))

(defn pump []
  (js/requestAnimationFrame #(go (>! c-pump true)))
  (go-loop [p (<! c-pump)]
    (println "wack")
    (js/requestAnimationFrame #(go (>! c-pump true)))
    (recur (<! c-pump))))


;(pump)


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


(def c-keypress (chan (dropping-buffer 1)))
(.addEventListener js/document  "keydown" #(go (>! c-keypress {:action :keypress :data %})))

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

(go (while true
      (let [[v ch] (alts! [c-keypress s-move])]
        (println "Read " v "From" ch))))

;; (let [c1 (chan)
;;       c2 (chan)]
;;   (go (while true
;;         (let [[v ch] (alts! [c1 c2])]
;;           (println "Read" v "from" ch))))
;;   (go (>! c1 "hi"))
;;   (go (>! c2 "there")))


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

