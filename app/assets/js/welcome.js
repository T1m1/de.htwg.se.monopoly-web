var startPage = angular.module('monopolyStartPage', ['ngAnimate']);

startPage.controller('Controller',  function($scope, $timeout, $http) {

	$scope.showAddButton = true;
	$scope.showMinusButton = false;
	$scope.showAlert = false;
	$scope.alertMessage;

	$scope.players = [ {
		name : "",
		figure : ""
	}, {
		name : "",
		figure : ""
	} ];

	$scope.addPlayer = function() {

		$scope.players.push({
			name : "",
			figure : ""
		});

		if ($scope.players.length > 5) {
			$scope.showAddButton = false;

		}
	};
	
	$scope.rmPlayer = function(index) {
		if ($scope.players.length < 3) {

			$scope.alertMessage = "Mindestens 2 Spieler";
			$scope.showAlert = true;

			$timeout(function () { $scope.showAlert = false; }, 1500);

		} else {
			$scope.players.splice(index, 1);
			$scope.showAddButton = true;
		}
		
	};


	$scope.startGame = function() {

        var exampleCall = [ {
            name : "Timi",
            figure : "BOGER"
        }, {
            name : "Steffen",
            figure : "ECK"
        } ];

      /*  $http({
            method: 'POST',
            url: '/start',
            data: exampleCall,
            headers: {'Content-Type': 'application/json'}
        });
*/
        /*
            $http.post('/start', exampleCall).

            success(function(data) {
                console.log(data);
            }).
            error(function(data) {
                console.log(data);
            });
        */

        $http.get('/go').
            success(function(data) {
                console.log(data);
            }).
            error(function(data) {
                console.log(data);
            });


		//$scope.alertMessage = "BLAAAA";
		//$scope.showAlert = true;
		//$timeout(function () { $scope.showAlert = false; }, 3000);
		
	};


	// variables

});