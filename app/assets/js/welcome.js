var startPage = angular.module('monopolyStartPage', [ 'ngAnimate' ]);

startPage.controller('Controller', function($scope, $timeout, $http) {

	$scope.showAddButton = true;
	$scope.showMinusButton = false;
	$scope.showAlert = false;
	$scope.alertMessage;
	
	$scope.showLoading = false;

	$scope.players = [ {
		name : "",
		figure : ""
	}, {
		name : "",
		figure : ""
	} ];

	$scope.icons = [ 'Maechtel', 'Neuschwander', 'Schoppa', 'Boger', 'Bittel',
			'Eck' ];

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

			$timeout(function() {
				$scope.showAlert = false;
			}, 1500);

		} else {
			// add to available icons
			$scope.icons.push($scope.players[index].figure);
			$scope.icons.sort();

			// delete existing user
			$scope.players.splice(index, 1);

			// show the the plus button
			$scope.showAddButton = true;
		}

	};

	$scope.updatePlayerIcon = function(index, select) {
		$scope.players[index].figure = select;

		console.log($scope.players);

		// delete from available icons
		$scope.icons.splice($scope.icons.indexOf(select), 1);
	}

	$scope.startGame = function() {

		for (var index = 0; index < $scope.players.length; ++index) {

			if ($scope.players[index].name === "") {
				$scope.displayError("Bitte Namen für Spieler " + (index + 1)
						+ " eintragen.")
				return;
			} else if ($scope.players[index].figure === "") {
				$scope.displayError("Bitte Figur für "
						+ $scope.players[index].name + " auswaehlen.")
				return;
			}

		}
		
		$('.bodyblue').addClass('blur');
		
		$('body').prepend('<div class="absolute"><div class="spinner"> <div  class="double-bounce1"></div><div  class="double-bounce2"></div></div></div>');

		$http.post('/start', $scope.players).then(function() {
			
			// show "loading screen"
			// $('#myModal').modal('show');
			
			$timeout(function() {
				location.href = "http://localhost:9000/go";
			}, 3000);

		});

	};

	$scope.displayError = function(error) {
		$scope.alertMessage = error
		$scope.showAlert = true;

		$timeout(function() {
			$scope.showAlert = false;
		}, 3000);
	}

});