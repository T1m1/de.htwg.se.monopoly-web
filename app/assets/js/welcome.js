var startPage = angular.module('monopolyStartPage', [ 'ngAnimate' ]);

startPage
		.controller(
				'Controller',
				function($scope, $timeout, $http, $interval) {

					$scope.isSignedIn = false;
					$scope.showNetwork = false;
					$scope.showLocal = false;
					$scope.chooseType = true;

					// basic button functions
					$scope.localGame = function() {
						$scope.showLocal = true;
						$scope.chooseType = false;
					}

					$scope.networkGame = function() {
						$scope.getGames()
						if (!$scope.isSignedIn) {
							$('#authModal').modal('show');
							$scope.renderSignIn();
						} else {
							$scope.showNetwork = true;
							$scope.chooseType = false;
						}
						// check if already created and joined
						$http.get('/created')
							.error(function() {
								$scope.alreadyCreated = false;
							})
							.success(function() {
								$scope.alreadyCreated = true;
							});
						
						$http.get('/joined')
							.error(function() {
								$scope.alreadyJoined = false;
							})
							.success(function() {
								$scope.alreadyJoined = true;
							});
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
					
					$scope.joinIcons = [];
					
					// help flags for checking a created or joined game, TODO:
					// maybe backend
					$scope.alreadyCreated = false;
					$scope.alreadyJoined = false;

					$scope.joiningGame;

					// pending game instances
					$scope.gameInstances = [];

					$scope.createGame = function() {
						if ($scope.alreadyCreated) {
							$scope.displayError("Du hast bereits ein Spiel gestartet.");
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
							$scope.updateIcons(game);
							$('#joinGameModal').modal('show');

						}

					}
					
					// edit the array with player icons for joining a game.
					$scope.updateIcons = function(game) {
						var tempArray = $scope.icons;
						angular.forEach(game.players, function(value, key) {
							// delete icon because it is already selected by another user
							var index = tempArray.indexOf(value.figure);
							if (index != -1) {
								tempArray.splice(index, 1);
							}
						});
						$scope.joinIcons = tempArray;
					}

					// create a new game in modal
					$scope.create = function() {
						var exit = false;
						angular.forEach($scope.gameInstances, function(value, key) {
							if (value.name === $scope.myGame.name) {
								$scope.displayModalError("Spielname existiert schon.");
								exit = !exit;
								return;
							}
						});
						
						if (exit)
							return;

						if ($scope.myGame.name === "") {
							$scope
									.displayModalError("Bitte Namen für Spiel eintragen")
							return;
						} else if ($scope.myGame.playerName == "") {
							$scope.displayModalError("Bitte Namen eintragen.")
							return;
						} else if ($scope.myGame.playerIcon == "") {
							$scope.displayModalError("Bitte Figur auswählen.")
							return;
						}

						// local variable
						var myNewGame = {
							name : $scope.myGame.name,
							players : [ {
								name : $scope.myGame.playerName,
								figure : $scope.myGame.playerIcon
							} ],
							numberOfPlayer : $scope.myGame.numberOfPlayer
						};

						for (var index = 2; index <= 6; ++index) {
							if (index <= $scope.myGame.numberOfPlayer) {
								myNewGame.players.push({
									name : "offen",
									figure : ""
								});
							} else {
								myNewGame.players.push({
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
						
						$scope.pendingMessage = "Auf Mitspieler warten";
						$scope.showPendingStatus = true;
						
						$scope.pollWaitForOponents(myNewGame.name);
					}

					// join a game in modal
					$scope.join = function() {
						
						if ($scope.joiner.playerName === "") {
							$scope.displayModalError("Bitte Namen eintragen!")
							return;
						} else if ($scope.joiner.playerIcon === "") {
							$scope.displayModalError("Bitte Figur auswählen.")
							return;
						}

						for (var index = 0; index < $scope.joiningGame.players.length; ++index) {

							if ($scope.joiningGame.players[index].name === $scope.joiner.playerName) {
								$scope
										.displayModalError("Name existiert schon, bitte anderen Namen eintragen.")
								return;
							}
						}
						
						var playerToJoin = {
							"gameName" : $scope.joiningGame.name, 
							"player": {
								"name" : $scope.joiner.playerName,
								"figure": $scope.joiner.playerIcon
							}
						};
						
						$http.post('/addPlayerToGame', playerToJoin).then(function () {
							$scope.getGames();
							$('#joinGameModal').modal('hide');
						})

						$scope.alreadyJoined = true;
						
						$scope.pendingMessage = "Auf Mitspieler warten";
						$scope.showPendingStatus = true;
						
						$scope.pollWaitForOponents(playerToJoin.gameName);
						
					}

					// request for pending games
					$scope.getGames = function() {
						return $http.get('/games').then(function(res) {
							$scope.gameInstances = res.data;
						})
					}
					
					// poll function for waiting on oponent
					$scope.pollWaitForOponents = function(gameName) {
						
						$interval(function() {
							$scope.getGames().then(function() {
								$http.get('/isFull/'+gameName)
									.error(function() {
										
									})
									.success(function() {
										
										$('.bodyblue').addClass('blur');
										$('body').prepend(
												'<div class="absolute"><div class="spinner"> <div  class="double-bounce1"></div><div  class="double-bounce2"></div></div></div>');

										$http.get('/startGame/' + gameName).then(function() {
											$timeout(function() {
												var loc = location.origin + "/go"
												location.href = loc;
											}, 1600);
										});
									});
							});
						}, 1600);
					}
					
					

					// sign in functionallity
					$scope.processAuth = function(authResult) {
						if ($scope.isSignedIn || authResult['access_token']) {
							$scope.isSignedIn = true;

							$scope.showNetwork = true;
							$scope.chooseType = false;
							$('#authModal').modal('hide');

						} else if (authResult['error']) {
							$scope.isSignedIn = false;
							console.log('Error:' + authResult['error']);
						}
					}

					
					// this will be called when authenication is done!
					$scope.signIn = function(authResult) {
						$scope.$apply(function() {
							$scope.processAuth(authResult);
						});
					}

					$scope.renderSignIn = function() {
						gapi.signin.render(
						'HTWGsignin',
						{
							'callback' : $scope.signIn,
							'clientid' : "1094692145630-vh5jho9nkha2hfmt5kmc455k2v06fakk.apps.googleusercontent.com",
							'requestvisibleactions' : "http://schemas.google.com/AddActivity",
							'scope' : 'https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/userinfo.email',
							'cookiepolicy' : "single_host_origin"
						});
					}

				});