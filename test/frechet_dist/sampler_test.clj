(ns frechet-dist.sampler-test
  (:require [frechet-dist.core :refer [frechet-dist]]
            [frechet-dist.sampler :refer [refine]]
            [clojure.core.matrix :refer [distance]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(def dimension (gen/sample (gen/choose 2 5) 1))
; a point is a collection of n-dimensional numbers
(def point     (gen/vector (gen/double* {:infinite? false :NaN? false :min -100 :max 100})
                           (first dimension)))
; a curve is a collection of 2 or more points
(def curve     (gen/vector point 2 20))

(comment
  As stated by Eiter and Manilla in "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.90.937&rep=rep1&type=pdf"
  the discrete frechet distance is bounded from the bottom by the continous
  Frechet distance and from the top by the sum of the continous Frechet distance
  with the longest segment of the poligonal curves.)


; -------------------------------------------------------------------
; The simple discrete Frechet distance is always greater than or equal to the
; discrete frechet distance of the refined curves due to the smaller upper limit
(defspec refinement-property
  1000; tries
  (prop/for-all [P curve
                 Q curve]
    (let [distPij     (apply max (map distance P (rest P)))
          distQij     (apply max (map distance Q (rest Q)))
          D-max       (max distPij distQij)]
      (>= (first (frechet-dist P Q))
          (first (frechet-dist (refine P (/ D-max 3))
                               (refine Q (/ D-max 3))))))))
;(tc/quick-check 1000 refinement-property)