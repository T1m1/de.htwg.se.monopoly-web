/**
 * Created by Timi on 29.10.2014.
 */

$(document).ready(main);

function main() {
    var $diceButton = $('.dice');
    var $endTurn = $('.endTurn');
    var $buy = $('.buy');
    $diceButton.on('click', function() {
        window.location='/rollDice'
    });

}