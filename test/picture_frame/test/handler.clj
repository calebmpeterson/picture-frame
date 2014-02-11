(ns picture-frame.test.handler
  (:use clojure.test
        ring.mock.request  
        picture-frame.handler))

(deftest test-app  
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))


(deftest test-scale-factor
  (testing "scale factor"
           (is (= (calculate-scale-factor 100 100 100 100) 1.00))
           (is (= (calculate-scale-factor   1 100 100 100) 1.00))
           (is (= (calculate-scale-factor 100   1 100 100) 1.00))
           (is (= (calculate-scale-factor 100 100   1 100) 0.01))
           (is (= (calculate-scale-factor 100 100 100   1) 0.01))
           (is (= (calculate-scale-factor 100 100   1   1) 0.01))))