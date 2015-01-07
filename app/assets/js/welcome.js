var startPage = angular.module('monopolyStartPage', [ 'ngAnimate' ]);

startPage
		.controller(
				'Controller',
				function($scope, $timeout, $http) {

					$scope.showNetwork = false;
					$scope.showLocal = false;
					$scope.chooseType = true;

					// basic button functions
					$scope.localGame = function() {
						$scope.showLocal = true;
						$scope.chooseType = false;
					}

					$scope.networkGame = function() {
						$scope.getGames();
						$scope.showNetwork = true;
						$scope.chooseType = false;
					}

					$scope.back = function() {
						$scope.showLocal = false;
						$scope.showNetwork = false;
						$scope.chooseType = true;
					}

					/** functionallity for local game * */
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

					$scope.icons = [ 'maechtel', 'neuschwander', 'schoppa',
							'boger', 'bittel', 'eck' ];

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
						// delete from available icons
						$scope.icons.splice($scope.icons.indexOf(select), 1);
					}

					$scope.startGame = function() {

						for (var index = 0; index < $scope.players.length; ++index) {

							if ($scope.players[index].name === "") {
								$scope.displayError("Bitte Namen für Spieler "
										+ (index + 1) + " eintragen.")
								return;
							} else if ($scope.players[index].figure === "") {
								$scope.displayError("Bitte Figur für "
										+ $scope.players[index].name
										+ " auswaehlen.")
								return;
							}

						}

						$('.bodyblue').addClass('blur');

						$('body')
								.prepend(
										'<div class="absolute"><div class="spinner"> <div  class="double-bounce1"></div><div  class="double-bounce2"></div></div></div>');

						$http.post('/start', $scope.players).then(function() {

							$timeout(function() {
								var loc = location.origin + "/go"
								location.href = loc;
							}, 1600);

						});

					};

					$scope.displayError = function(error) {
						$scope.alertMessage = error
						$scope.showAlert = true;

						$timeout(function() {
							$scope.showAlert = false;
						}, 3000);
					}

					$scope.displayModalError = function(error) {
						$scope.alertMessage = error
						$scope.showModalAlert = true;

						$timeout(function() {
							$scope.showModalAlert = false;
						}, 3000);
					}

					/** functionallity for network game * */

					// game which is going to be created
					$scope.myGame = {
						name : "",
						playerName : "",
						playerIcon : "",
						numberOfPlayer : "2"
					}
					// info for joining a game
					$scope.joiner = {
						playerName : "",
						playerIcon : ""
					};
					// help flags for checking a created or joined game, TODO:
					// maybe backend
					$scope.alreadyCreated = false;
					$scope.alreadyJoined = false;

					$scope.joiningGame;

					// dummy gameinstances, will be removed eventually
					$scope.gameInstances = [];

					$scope.createGame = function() {
						if ($scope.alreadyCreated) {
							$scope
									.displayError("Du hast bereits ein Spiel gestartet.");
						} else {
							$('#createGameModal').modal('show');
						}
					}

					$scope.joinGame = function(game) {
						if ($scope.alreadyJoined) {
							$scope
									.displayError("Du bist bereits einem Spiel beigetreten.");
						} else {
							$scope.joiningGame = game;
							$('#joinGameModal').modal('show');

						}

					}

					$scope.create = function() {

						// TODO check if game name already exist (optional..)

						if ($scope.myGame.name === "") {
							$scope.displayModalError("Bitte Namen für Spiel eintragen")
							return;
						} else if ($scope.myGame.playerIcon == "") {
							$scope.displayModalError("Bitte Figur auswählen.")
							return;
						} else if ($scope.myGame.playerName == "") {
							$scope.displayModalError("Bitte Namen eintragen.")
							return;
						}
						
						// local variable
						var myNewGame = {
							name : $scope.myGame.name,
							players : [ {
								name : $scope.myGame.playerName,
								figure : $scope.myGame.playerIcon
							}],
							numberOfPlayer : $scope.myGame.numberOfPlayer
						};
						
						for (var index = 2; index <= 6; ++index) {
							if (index <= $scope.myGame.numberOfPlayer){
								myNewGame.players.push( {
									name : "offen",
									figure : ""
								});
							} else {
								myNewGame.players.push( {
									name : "X",
									figure : ""
								});
								
							}
						}
						
						
						if ($scope.gameInstances == null) {
							$scope.gameInstances = myNewGame;
						} else {
							$scope.gameInstances.push(myNewGame);
						}
						
						$http.post('/createGame', myNewGame)
						
						// add game to game instances
						$scope.alreadyCreated = true;

						$('#createGameModal').modal('hide');
					}

					$scope.join = function() {

						// check if name or icon already exist
						if ($scope.joiner.playerName === "") {
							$scope.displayError("Bitte Namen eintragen!")
							return;
						} else if ($scope.joiner.playerIcon == "") {
							$scope.displayModalError("Bitte Figur auswählen.")
							return;
						}

						for (var index = 0; index < $scope.joiningGame.players.length; ++index) {

							if ($scope.joiningGame.players[index].name === $scope.joiner.playerName) {
								$scope
										.displayModalError("Name existiert schon, bitte anderen Namen eintragen.")
								return;
							}
							// TODO check icon by not showing it in the dropdown
						}

						$scope.alreadyJoined = true;
						$('#joinGameModal').modal('hide');

					}

					$scope.getGames = function() {
						$http.get('/games').then(function(res) {
							console.log(res)
							$scope.gameInstances = res.data;
						})
					}

				});
