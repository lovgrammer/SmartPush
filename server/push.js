var FCM = require('fcm-node');
var fcm = new FCM("AAAAiuDvfV8:APA91bFyntevpz6jWWaB0RBP3b2htaJqjA5f4xuAHssn2T39IBGSnKo5txu4lsf96kR6ZwEVurzDNd3KprpjiahUnKcE6ba68xLqp-c_RJI1ty-pI0UDpcXl5UfRzM7XGtO4CpBetOng");

var clients = ["eb-KO62Pdu4:APA91bEV57lxlpq1JX01lmA-nt9HUddg7ZTc0GFdwCGNnk1lK-Bxs6EbQVje60A5jy8i_Z9uPzX3-tXub-_0POAG_gSpkhOhd6KD2x7bsEK8KKL7om7tsn_kACjgmJhXAamd3z-mV7Ok", "ccE-XOTIeas:APA91bEAltSGKqvymkFQSwLuF8DqSFfCidXiZmphuYTTMriumoUEEm-g6W-xCKWCgc7zLGbK4tCITc9J3rDb-fK-nuB-9rrY7q7SyXY5imsYgXcn_Diu9xH9gl8gUmw5weeE7gRdqNYD", "e68ibDMaW2Y:APA91bGY131KQVoE7mYYASs1iqRtI8TlgoCSkbfxOPVjNPBCnfw75HZmqWokxXWUPCY1xoYPiyHQVhg1dIJMVfBS1NLHgFXArIw0rAxkJnhaxlGdB07GMf-HxQPOvWD-EuJKEscOXGUo", "fC4CmfDiLDM:APA91bETZpEZc9vx90anfM4G5xZP-KrO7Ote8e89JLj4zkOdr-nAp_Nq2HOgOhxPZUrQHJmkVq6Nmp93bcF_4k3D9zyI4vBrMZmeODsmiVeOkrtKljALAlT5LsoWLyA7KfmXBor85Ohf"];

// var clients = [
        
// ]

for (var i=0; i<clients.length; i++) {
    fcm.send({
	"to": clients[i],
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
}

