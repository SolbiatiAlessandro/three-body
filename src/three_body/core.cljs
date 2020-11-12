(ns three-body.core
    (:require ))

;; DATASTRUCTURES

(defrecord Body [id x y vx vy ax ay])
(defn rand-x [] (+ 3 (rand-int 94)))
(defn body [id] (Body. id (rand-x) (rand-x) 0 0 0 0))
(def bodies (map (fn [id] (body (+ 1 id))) (range 3)))
(def comb [[1 2] [0 2] [0 1]])

;; PHYSICS

(def G 0.00000000006674)

;; https://en.wikipedia.org/wiki/Verlet_integration#Velocity_Verlet
;; http://nbabel.org/equations
;; https://physics.stackexchange.com/a/404826/279554

(defn g-force [first_body second_body]
  "Compute gravitational attraction between two bodies
  ref: https://en.wikipedia.org/wiki/Newton%27s_law_of_universal_gravitation"
  (let [dy (- (:y first_body) (:y second_body)) 
        dx (- (:x first_body) (:x second_body))  
        angle (js/Math.atan2 dy dx)
        distance (js/Math.sqrt (+ (* dy dy) (* dx dx)))
        magnitude (/ G (* distance distance))]
    [(* magnitude (js/Math.cos angle)) (* magnitude (js/Math.sin angle))]))

(defn g-accell 
  
 ([i bodies dt] 
  "Compute accelleration for i-th body"

  (let [force-components (map 
                           (fn [j] (g-force (nth bodies i) (nth bodies j)))
                           (nth comb i))])

  ;; TODO: add force compoonents with reduce
  ;; (reduce + [[1 2] [3 4] [4 5]]) --> [8 11]
  
  ) 
  ([bodies dt]
   "Compute accelleration for all the bodies"

   (
    ))
  
  )

;; RENDERING

(defn render 
  ([] (doseq [body bodies] (render @body)))
  ([body] (let [style (.-style (.getElementById js/document (str "body" (:id body))))]
    (set! (.-bottom style) (str (:y body) "%"))
    (set! (.-left style) (str (:x body) "%")))))

(defn main []
  (render))

(main)
