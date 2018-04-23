var FCM = require('fcm-node');
var fcm = new FCM("AAAAiuDvfV8:APA91bFyntevpz6jWWaB0RBP3b2htaJqjA5f4xuAHssn2T39IBGSnKo5txu4lsf96kR6ZwEVurzDNd3KprpjiahUnKcE6ba68xLqp-c_RJI1ty-pI0UDpcXl5UfRzM7XGtO4CpBetOng");

fcm.send({
    "to": "eb-KO62Pdu4:APA91bEV57lxlpq1JX01lmA-nt9HUddg7ZTc0GFdwCGNnk1lK-Bxs6EbQVje60A5jy8i_Z9uPzX3-tXub-_0POAG_gSpkhOhd6KD2x7bsEK8KKL7om7tsn_kACjgmJhXAamd3z-mV7Ok",
    "notification": {
	"title" : "Portugal vs. Denmark",
	"body" : "5 to 1"
    }
}, function(err, response) {
    if (err) {
	console.error('ERROR');
	console.error(err);
	return;
    }

    console.log('Push Sended');
    console.log(response);
});
