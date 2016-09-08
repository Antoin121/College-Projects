var db;
var currentRow;
function onBodyLoad() {
    document.addEventListener("deviceready", onDeviceReady, false);
}
         // Cordova is ready
        //
        function onDeviceReady() {
            if(navigator.network.connection.type == Connection.NONE){
                $("#home_network_button").text('No Internet Access')
                .attr("data-icon", "delete")
                .button('refresh');
            }
            $.support.cors = true;

            db = window.openDatabase("Database", "1.0", "Cordova Demo", 200000);
            db.transaction(populateDB, errorCB, successCB);
        }


        // Populate the database

        function populateDB(tx) {
            //alert("populateDB");
            tx.executeSql('DROP TABLE DEMO');
            tx.executeSql('CREATE TABLE IF NOT EXISTS DEMO (trackID,uniqueID INTEGER PRIMARY KEY AUTOINCREMENT, longitude, latitude, speed, time, accel_x,accel_y,accel_z,direction,avgSpeed,distance)');
            tx.executeSql('CREATE TABLE IF NOT EXISTS DEMO2 (trackID)');

        }

        function errorCB(err) {
            alert("Error processing SQL: "+err.code);
        }

        // Transaction success callback
        //
        function successCB() {
            //alert("table created");
        }

//===================================================================================================================================================
//tracking journey


var track_id = '';      // Name/ID of the exercise
var watch_ACC = null;
var watch_GPS = null;   // ID of the geolocation
var oldlongitude;
var oldlatitude;
var newlongitude;
var newlatitude;
var totalSpeed=0;
var avgSpeed=0;
var newSpeed;
var oldDir;
var newDir;
var disReg=0;
var straight=true;
var j=0;
var total_km;
var total_km_rounded;
var calculatePost = false;
var row=0;

$("#startTracking_start").live('click', function(){

    $('#startTracking_start').attr("disabled", true); //disabling the
    $('#stopTracking').attr("disabled", false);
    track_id = $("#track_id").val();


    watch_ACC  = navigator.accelerometer.watchAcceleration(

        function (accel){
            insertDB_ACC(track_id, accel.x.toFixed(7),accel.y.toFixed(7), accel.z.toFixed(7)); 

        }, 

        function (){
            alert("Accelerometer Error");
        },
        {frequency: 25});


        setupWatch(3500); //geolocation watch
                

    // Tidy up the UI     
    $("#track_id").hide();
    $("#startTracking_status").show();
    $("#startTracking_status").html("Tracking workout: <strong>" + track_id + "</strong>");
});

var activeWatch=null;
// sets up the interval at the specified frequency
function setupWatch(freq) {
    activeWatch = setInterval(watchLocation, freq);

}

// stop watching

function logout() {
    clearInterval(activeWatch);
}
// this is what gets called on the interval.
function watchLocation() {
    var gcp = navigator.geolocation.getCurrentPosition(
           // Success
           function(position){
            newlongitude = position.coords.longitude;
            
            newlatitude = position.coords.latitude;
            newSpeed = position.coords.speed;

            
            var timestamp = new Date().toISOString().slice(0, 19).replace('T', ' ');

            if(disReg>=1){
                total_km=distanceCal(oldlatitude, oldlongitude, newlatitude, newlongitude).toFixed(2);
            }
            

            oldlongitude=newlongitude;
            oldlatitude=newlatitude;
            if(newSpeed>=0){
                totalSpeed+=newSpeed;
            }
            disReg++;
            

            insertDB(track_id, newlongitude,newlatitude,newSpeed,timestamp,row);
            row+=120;
            
        },

        // Error
        function(error){
            alert(error.message);
        }, {
            enableHighAccuracy: true
        });


    // console.log(gcp);

}

function insertDB(trackID, longi,lat,speed, time,arow){

    navigator.compass.getCurrentHeading(onSuccess, onError);

    function onSuccess(heading) {
        newDir = heading.magneticHeading;
                    
                    if(arow===0){
                        arow=1;
                    }

                    db.transaction(function(tx){
                        tx.executeSql('UPDATE DEMO SET longitude = '+'\''+longi+'\''+', latitude = '+'\''+lat+'\''+
                            ', speed = '+'\''+speed+'\''+ ', time = '+'\''+time+'\''+', direction = '+'\''+newDir+'\''+
                            ' WHERE uniqueID = ' + '\''+arow+'\'');
                
                }, errorCB);

                };

                function onError(error) {
                    alert('CompassError: ' + error.code);
                };      

            }

            function insertDB_ACC(trackID, x,y, z){


            db.transaction(function(tx){

                tx.executeSql('INSERT INTO DEMO (trackID,accel_x,accel_y,accel_z) VALUES (?,?,?,?)',[trackID,x,y,z]);
            }, errorCB);

        }


$("#stopTracking").live('click', function(){
    $('#startTracking_start').attr("disabled", false);
    $('#stopTracking').attr("disabled", true);

            
    navigator.accelerometer.clearWatch(watch_ACC);
            
    
    logout();

    //alert("Last row inserted= "+row);
    avgSpeed=totalSpeed/disReg;

    insertSpeed_Dis(track_id, avgSpeed,total_km);
    
    watch_ACC = null;

    disReg=0;
    totalSpeed=0;
    total_km=0;

    $('#startTracking_status').hide();
    $("#track_id").val('').show();
  
    $.mobile.changePage("#home", {reverse: false, transition: "slide"});
});


function insertSpeed_Dis(trackID, avgSpeed,total_km){
//alert("speed = "+avgSpeed+" distance= "+total_km);
            db.transaction(function(tx){

                tx.executeSql('INSERT INTO DEMO (trackID,avgSpeed,distance) VALUES (?,?,?)',[trackID,avgSpeed,total_km]);
                db.transaction(queryDB, errorCB);
            }, errorCB);

        }

//===========================================================================================================================================
//printing recent journeys
function goQuery() {
    db.transaction(queryDB, errorCB);
}

function queryDB(tx){
    //alert("query");
    tx.executeSql('SELECT DISTINCT trackID FROM DEMO',[], printDatabase, errorCB);
    
}

function printDatabase(tx, results){

    
    var len = results.rows.length;
    if(len<1){
        document.getElementById("tblDiv").innerHTML =" ";
    }else{
    var tblText='<table id="t01"><tr><th style="text-align:center">Recent Journeys</th></tr>';
    for (var i = 0; i < len; i++) {
        tblText +='<tr onclick="goPopup(\''+results.rows.item(i).trackID+'\');"><td style="text-align:center">' 
        + results.rows.item(i).trackID +'</td></tr>';

    }
    tblText +="</table>";
    document.getElementById("tblDiv").innerHTML =tblText;
}
}

function goQueryHistory() {
    db.transaction(queryHistoryDB, errorCB);
}

function queryHistoryDB(tx){
    //alert("query");
    tx.executeSql('SELECT DISTINCT trackID FROM DEMO2',[], printHistoryDatabase, errorCB);
    
}

function printHistoryDatabase(tx, results){
    var tblText='<table id="t01"><tr><th style="text-align:center">History</th></tr>';
    var len = results.rows.length;
    for (var i = 0; i < len; i++) {
        tblText +='<tr onclick="goPopupHistory(\''+results.rows.item(i).trackID+'\');"><td style="text-align:center">' 
        + results.rows.item(i).trackID +'</td></tr>';

    }
    tblText +="</table>";
    document.getElementById("tblDiv2").innerHTML =tblText;
}

function goPopup(row) {
    currentRow=row;
    document.getElementById("qrpopup").style.display="block";
    document.getElementById("editNameBox").value = row;
}

function editRow(tx) {
    tx.executeSql('UPDATE DEMO SET trackID ="'+document.getElementById("editNameBox").value+
        '" WHERE trackID = ' + '\''+currentRow+'\'', [], queryDB, errorCB);
}

function goEdit() {
    db.transaction(editRow, errorCB);
    document.getElementById('qrpopup').style.display='none';
}

function deleteRow(tx) {
    tx.executeSql('DELETE FROM DEMO WHERE trackID = ' + '\''+currentRow+'\'', [], queryDB, errorCB);
}

function goDelete() {

    db.transaction(deleteRow, errorCB);
    document.getElementById('qrpopup').style.display='none';
}

function goPopupHistory(row) {
    currentRow=row;
    document.getElementById("qrpopup3").style.display="block";
    document.getElementById("editNameBox2").value = row;
}



function deleteRowHistory(tx) {
    tx.executeSql('DELETE FROM DEMO2 WHERE trackID = ' + '\''+currentRow+'\'', [], queryHistoryDB, errorCB);
}

function goDeleteHistory() {

    db.transaction(deleteRowHistory, errorCB);
    document.getElementById('qrpopup3').style.display='none';
}

function goQueryAcc() {
    db.transaction(queryDB_Acc, errorCB);
}

function queryDB_Acc(tx){
    //alert("query");
    tx.executeSql('SELECT trackID,uniqueID,longitude,latitude FROM DEMO',[], printAcc, errorCB);
    
}

function printAcc(tx, results){
    var tblText='<table id="t01"><tr><th>ID</th> <th>X</th> <th>Y</th> <th>Z</th></tr>';
    var len = results.rows.length;
    //alert(len);
    for (var i = 0; i < len; i++) {
        (function(i) {
            if(results.rows.item(i).longitude!=null){
                tblText +='<td>'+ results.rows.item(i).trackID +'</td><td>'
                + results.rows.item(i).uniqueID +'</td><td>'
                + results.rows.item(i).longitude +'</td><td>'
                + results.rows.item(i).latitude +'</td></tr>';
            }})(i);       
        }
        tblText +="</table>";
        document.getElementById("tblDiv_Acc").innerHTML =tblText;
    }


//===================================================================================================================================================
//distance

function distanceCal(lat1, lon1, lat2, lon2)
{

    var R = 6371; // km
    var dLat = (lat2-lat1) * (Math.PI / 180);
    var dLon = (lon2-lon1) * (Math.PI / 180);
    var lat1 = lat1 * (Math.PI / 180);
    var lat2 = lat2 * (Math.PI / 180);

    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    var d = R * c;

    return d;
}


//======================================================================================================================================
//Mapping

function goMapping() {
    db.transaction(mapQuery, errorCB);
}

function mapQuery(tx){
    //alert('\''+currentRow+'\'');
    tx.executeSql('SELECT longitude,latitude,avgSpeed,distance FROM DEMO WHERE trackID = ' + '\''+currentRow+'\'',[], mapping, errorCB);
    document.getElementById('qrpopup').style.display='none';
}


function mapping(tx, results){
    
    
document.getElementById('avgSpeedBox').style.display='block';
document.getElementById('distanceBox').style.display='block';
    var len = results.rows.length/120;
    var center;
    if(len<=4){
        center = 0;
    }
    else{
     center = ((len/2).toFixed()*120)-1;
        }   //alert(center);

//alert(results.rows.item(center).latitude+" "+results.rows.item(center).longitude);
var myOptions = {
    zoom:15,
    center:new google.maps.LatLng(results.rows.item(center).latitude,results.rows.item(center).longitude),
    mapTypeId: google.maps.MapTypeId.ROADMAP
};

map = new google.maps.Map(document.getElementById('map_canvas'), myOptions);


        var trackCoords = [];

len=results.rows.length;
for(i=0; i<len; i++){
    if(results.rows.item(i).latitude!=null){
        //alert('\''+results.rows.item(i).latitude+'\''+" "+'\''+results.rows.item(i).longitude+'\'');
    trackCoords.push(new google.maps.LatLng(results.rows.item(i).latitude,results.rows.item(i).longitude));
    }
    if(results.rows.item(i).avgSpeed!=null){
        document.getElementById('avgSpeed').innerHTML="Average Speed= "+results.rows.item(i).avgSpeed.toFixed(2)+"m/s";
        document.getElementById('distanceTextbox').innerHTML="Distance= "+results.rows.item(i).distance+"km";
    }
}

                // Plot the GPS entries as a line on the Google Map
                var trackPath = new google.maps.Polyline({
                    path: trackCoords,
                    strokeColor: "hsl(120, 100%, 73%)",
                    strokeOpacity: 1.0,
                    strokeWeight: 5
                });

                // Apply the line to the map
                trackPath.setMap(map);
                    }


                


                var map;
                function init_map(){
                    var center = (mapLat.length/2).toFixed();
            var potHoleMarkers = [];
            for(i=0; i<mapPot.length; i++){
                if(mapPot[i]>0){
                        potHoleMarkers.push(new google.maps.LatLng(mapLat[i],mapLong[i]));

}
}

var commentMarkers = [];

                for(i=0; i<commentLong.length; i++){
                        commentMarkers.push(new google.maps.LatLng(commentLat[i],commentLong[i]));
                }

var myOptions = {
    zoom:15,
    center:new google.maps.LatLng(mapLat[center],mapLong[center]),
    mapTypeId: google.maps.MapTypeId.ROADMAP
};

map = new google.maps.Map(document.getElementById('map_canvas2'), myOptions);

            var infoWindow = new google.maps.InfoWindow();
            for (var i = 0; i < potHoleMarkers.length; i++) {
            var marker = new google.maps.Marker({
                position: potHoleMarkers[i],
                map: map
            });

            //Attach click event to the marker.
            (function (marker) {
                google.maps.event.addListener(marker, "click", function (e) {
                    
                    infoWindow.setContent("<div>Pothole</div>");
                    infoWindow.open(map, marker);
                });
            })(marker);
        }

for (var i = 0; i < commentMarkers.length; i++) {
            var image = 'commentMap.PNG';
            var marker = new google.maps.Marker({
                position: commentMarkers[i],
                map: map,
                icon: image
            });
  
  
           
            (function (marker,i) {
                google.maps.event.addListener(marker, "click", function (e) {
                    infoWindow.setContent("<div>'"+mapComment[i]+"'</div>");
                    infoWindow.open(map, marker);
                });
            })(marker,i);
        }

        var trackCoords = [];


for(i=0; i<mapLat.length; i++){
    trackCoords.push(new google.maps.LatLng(mapLat[i],mapLong[i]));
} 
                var trackPath = new google.maps.Polyline({
                    path: trackCoords,
                    strokeColor: "hsl(120, 100%, 73%)",
                    strokeOpacity: 1.0,
                    strokeWeight: 5
                }); 
                trackPath.setMap(map);             
            }

var comment=new Array();
var commentLatLng=new Array();
var numComments=-1;
            function goCommenting(){
                event.preventDefault();
        document.getElementById('popupComment').style.display='none';
        
        numComments++;
        var infoWindow = new google.maps.InfoWindow();
        var listener1 = map.addListener('click', function(e) {

                    comment.push(document.getElementById("comment").value);
                    commentLatLng.push(e.latLng);
                    placeMarkerAndPanTo(e.latLng, map,comment);
                });


        function placeMarkerAndPanTo(latLng, map) {
            var commentMarker = new google.maps.Marker({
                position: latLng,
                map: map,
                draggable:true
            });
            (function (commentMarker) {
                google.maps.event.addListener(commentMarker, "click", function (e) {
                    
                    infoWindow.setContent("<div>'"+document.getElementById("comment").value+"'</div>");
                    infoWindow.open(map, commentMarker);
                });
            })(commentMarker);

                google.maps.event.addListener(
                    commentMarker,
                    'drag',
                    function() {
                        
                        commentLatLng[numComments]=new google.maps.LatLng(commentMarker.position.lat(),commentMarker.position.lng());
                       
                    });

            google.maps.event.removeListener(listener1);
        }
        
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//posting
$("#upload").live('click', function(){

    event.preventDefault();
     db.transaction(uploadQuery, errorCB);
    
})



function uploadQuery(tx){
    tx.executeSql('SELECT * FROM DEMO',[], posting, errorCB);
}

function analyzeQuery(){
    
    db.transaction(function(tx){
        tx.executeSql('SELECT DISTINCT trackID FROM DEMO',[], analyze,errorCB);
    }, errorCB);
}


function analyze(tx, results){
    var len = results.rows.length;
    for (var i = 0; i < len; i++){

        callAnalyze(results.rows.item(i).trackID);            
    }
}

function callAnalyze(trackID){

    var datab = {
        "trackID": trackID,
    };

            $.ajax({
                type: 'POST',
                data: datab,
                
                url: 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/analyze.php',
                success: function(data){
                    alert(data);
                },
                error: function(e){
                    //console.log(data);
                    alert('There was an error analyzing');
                }
            });

        }

function insertHistoryDB(trackID){


            db.transaction(function(tx){

                tx.executeSql('INSERT INTO DEMO2 (trackID) VALUES (?)',[trackID]);
            }, errorCB);

        }
        
        function posting(tx, results){
            var len = results.rows.length;
            var i=0;
            insertHistoryDB(results.rows.item(i).trackID);
         for (i = 0; i < len; i++){
            if(results.rows.item(i).latitude!=null){
                
                sendAll(results.rows.item(i).trackID,results.rows.item(i).longitude,results.rows.item(i).latitude,results.rows.item(i).time,results.rows.item(i).accel_x,results.rows.item(i).accel_y,results.rows.item(i).accel_z,results.rows.item(i).speed,results.rows.item(i).direction,results.rows.item(i).uniqueID);
            }
            else{
                
            sendAcc(results.rows.item(i).trackID,results.rows.item(i).accel_x,results.rows.item(i).accel_y,results.rows.item(i).accel_z,results.rows.item(i).uniqueID);
        }
      }
     
      var commentLen= commentLatLng.length;
      for (i = 0; i < commentLen; i++){
        
            sendComments(results.rows.item(i).trackID,commentLatLng[i],comment[i]);
      }
      
     if(i===(len-1)){
        analyzeQuery();
     }
     db.transaction(populateDB, errorCB, successCB);
      row=0;
     
     alert("Your Journey was successfully Uploaded");


 }

 function sendAll(trackID,longitude,latitude,time,accel_x,accel_y,accel_z,speed,direction,id){
    var datab = {
        "trackID": trackID,
        "longitude": longitude,
        "latitude": latitude,
        "time": time,
        "accel_x": accel_x,
        "accel_y": accel_y,
        "accel_z": accel_z,
        "speed": speed,
        "direction": direction,
        "id": id
    };
            //data = $.param(data);

            $.ajax({
                type: 'POST',
                data: datab,
                
                url: 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/upload.php',
                success: function(data){
                    //console.log(data);
                },
                error: function(e){
                    //console.log(data);
                    alert('There was an error uploading your journey');
                }
            });
        }

function sendAcc(trackID,accel_x,accel_y,accel_z,id){
    var datab = {
        "trackID": trackID,
        "accel_x": accel_x,
        "accel_y": accel_y,
        "accel_z": accel_z,
        "id": id
    };

            $.ajax({
                type: 'POST',
                data: datab,
                
                url: 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/uploadAcc.php',
                success: function(data){
                    //console.log(data);
                },
                error: function(e){
                    //console.log(data);
                    alert('There was an error uploading your journey');
                }
            });
        }

        function sendComments(trackID,commentLatLng,comment){
            
    var datab = {
        "trackID": trackID,
        "commentLat": commentLatLng.lat(),
        "commentLng": commentLatLng.lng(),
        "comment": comment
    };
           
            $.ajax({
                type: 'POST',
                data: datab,
                
                url: 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/uploadComments.php',
                success: function(data){
            
                },
                error: function(e){
                    //console.log(data);
                    alert('There was an error uploading your journey');
                }
            });
            //alert("comments posted");
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
$("#download").live('click', function(){
    event.preventDefault();
    mapLat = new Array();
    mapLong = new Array();
    mapPot = new Array();
    db.transaction(downloadQuery, errorCB);
    
})

$("#commentButton").live('click', function(){
    event.preventDefault();
    document.getElementById('popupComment').style.display="block";

})

function downloadQuery(tx){
    tx.executeSql('SELECT DISTINCT trackID FROM DEMO',[], retrieving, errorCB);
    
}


function retrieving(tx, results){
    var len = results.rows.length;
    for (var i = 0; i < len; i++){
        get(results.rows.item(i).trackID);  
    }
}
function goDownload(){
    mapLat = new Array();
    mapLong = new Array();
    mapPot = new Array();
    mapComment = new Array();
    commentLong = new Array();
    commentLat = new Array();
    
    get(""+document.getElementById("editNameBox2").value+"");
    getComment(""+document.getElementById("editNameBox2").value+"");
document.getElementById('grpopup3').style.display='none';

    

}

function get(trackID){
    var datab = {
        "trackID": trackID,
    };
            

            $.ajax({
                type: 'POST',
                data: datab,
               
                url: 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/download.php',
                success: function(data){
                    
                     var res = data.split(" ");
                     for (var i = 0; i < res.length; i++){
                        splitting(i,res);
                     }
init_map();

$.mobile.changePage("#viewJourneyHistory", {reverse: false, transition: "slide"});
},
error: function(e){
                    alert('There was an error uploading your journey');
                }
            });
            //alert("finished");
        }

            function getComment(trackID){
    var datab = {
        "trackID": trackID
    };
            $.ajax({
                type: 'POST',
                data: datab,
                url: 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/downloadComment.php',
                success: function(data){
                    //console.log(data);
                     //alert(data);
                     var res = data.split("/");
                     for (var i = 0; i < res.length; i++){
                        splittingComment(i,res);
                     }
                     init_map();
                     
                 },
                 error: function(e){
                    alert('There was an error uploading your journey');
                 }
             });
        }

        var mapLat;
        var mapLong;
        var mapPot;
        var mapComment;
        var commentLong;
        var commentLat;

        function splitting(i,results){
            if(i%3===0){
                mapLat.push(results[i]);
            }
            else if(i%3===1){
                mapLong.push(results[i]);
            }
            else if(i%3===2){
                mapPot.push(results[i]);
            }
        }

        function splittingComment(i,results){
            if(i%3===0){
                commentLat.push(results[i]);
            }
            else if(i%3===1){
                commentLong.push(results[i]);
            }
            else if(i%3===2){
                mapComment.push(results[i]);
            }
        }