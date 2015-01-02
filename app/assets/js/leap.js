/**
 * Created by Timi on 01.01.2015.
 */

var options = {enableGestures: true};

var reset = 0;
var status = 1;
var gesturereset = 0;
var gesturestatus = 1;

// Main Leap Loop
function triggerGestureFunc(btn) {
    if(gesturestatus == 1) {
        gesturestatus = 0;
        gesturereset = 0;
        $(btn).trigger("click");
        console.log(btn);
    }
}

function handleSwipeGesture(gesture) {
    //Classify swipe as either horizontal or vertical
    var isHorizontal = Math.abs(gesture.direction[0]) > Math.abs(gesture.direction[1]);
    //Classify as right-left or up-down
    if(isHorizontal){
        if(gesture.direction[0] > 0){
            // left to right
            triggerGestureFunc('#endTurn');
        } else {
            // right to left
            triggerGestureFunc('#rollDice');
        }
    } else { //vertical
        // up
        if(gesture.direction[1] > 0){
            triggerGestureFunc('#drawCard');
        } else {
            triggerGestureFunc('#prisonRoll');
        }
    }
}

function checkGestures(frame) {
    if (frame.gestures.length > 0) {
        for (var i = 0; i < frame.gestures.length; i++) {
            var gesture = frame.gestures[i];
            if(gesture.type == "swipe") {
                handleSwipeGesture(gesture);
            }
        }
    } else {
        /* handle multiple call of swipe events */
        if(gesturestatus === 0) {
            gesturereset++;
        }
        if(gesturereset > 50) {
            gesturestatus = 1;
        }
    }
}

function checkPinch(frame) {
    for (var i = 0, len = frame.hands.length; i < len; i++) {
        hand = frame.hands[i];
        /* TODO evtl. mind 2 mal pinchStrength == 1 ?*/
        if (hand.pinchStrength === 1) {
            if (status == 1) {
                console.log("buy");
                $('#buy').trigger("click");
                status = 0;
                reset= 0;
            }
        } else {
            /* handle multiple call of pinch events */
            if(status == 0) {
                reset++;
            }
            if(reset > 50) {
                status = 1;
            }
        };
    };
}

/* LEAP MOTION LOOP */
Leap.loop(options, function (frame) {
    // swipe gestures check
    checkGestures(frame);
    // pinch gesture check
    checkPinch(frame);
});