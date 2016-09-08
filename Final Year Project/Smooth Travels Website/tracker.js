var db;
var currentRow;
var trackID = new Array();
function onBodyLoad() {

	$.ajax({
		type: 'GET',
		url: 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/downloadAdmin.php',
		success: function (data) {
    // document.getElementById("tblDiv").innerHTML = data;
    var trackID=data.split(" ");
     //alert(data);
     printDatabase(trackID);
 }
});

}

function printDatabase(trackID){
	var tblText='<table style="width:100%"><tr><th style="text-align:center">Journeys</th></tr>';
	var len = trackID.length;
	for (var i = 0; i < len; i++) {
		tblText +='<tr id="\''+trackID[i]+'\'"><td style="text-align:center">' 
		+ trackID[i] +'</td></tr>';

	}
	tblText +="</table>";
	document.getElementById("t01").innerHTML =tblText;
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

$(document).ready(function() {
	$(document).on("dblclick","#t01 tr",function() {
		var $this = $(this);
		var row = $this.closest("tr");
		mapLat = new Array();
		mapLong = new Array();
		mapPot = new Array();
		mapComment = new Array();
		commentLong = new Array();
		commentLat = new Array();
		//alert(row.find('td:first').text());
		get(row.find('td:first').text());
		getComment(row.find('td:first').text());


	});
});
function get(trackID){
	var datab = {
		"trackID": trackID
	};

			$.ajax({
				type: 'POST',
				data: datab,
               
                url: 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/download.php',
                success: function(data){
                    //console.log(data);
                     //alert(data);
                     var res = data.split(" ");
                     for (var i = 0; i < res.length; i++){
                     	splitting(i,res);
                     }
                     
                     init_map();
                 },
                 error: function(e){
                 	alert('There was an error uploading your journey');
                 }
             });
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

		function init_map(){
			var center = (mapLat.length/2).toFixed();
			//alert(center);

			var potHoleMarkers = [];
				for(i=0; i<mapPot.length; i++){
					if(mapPot[i]>0){
						
						potHoleMarkers.push(new google.maps.LatLng(mapLat[i],mapLong[i]));
						
					}
				}

				var commentMarkers = [];
				//alert("Comment lng size = " + commentLong.length);
				for(i=0; i<commentLong.length; i++){
				
						commentMarkers.push(new google.maps.LatLng(commentLat[i],commentLong[i]));
				}

			var myOptions = {
				zoom:15,
				center:new google.maps.LatLng(mapLat[center],mapLong[center]),
				mapTypeId: google.maps.MapTypeId.ROADMAP
			};

				map = new google.maps.Map(document.getElementById('gmap_canvas'), myOptions);
			
			var infoWindow = new google.maps.InfoWindow();
			
			for (var i = 0; i < potHoleMarkers.length; i++) {
           
            var marker = new google.maps.Marker({
                position: potHoleMarkers[i],
                map: map
            });
 
            //Attach click event to the marker.
            (function (marker) {
                google.maps.event.addListener(marker, "click", function (e) {
                    //Wrap the content inside an HTML DIV in order to set height and width of InfoWindow.
                    infoWindow.setContent("<div>pothole</div>");
                    infoWindow.open(map, marker);
                });
            })(marker);


        }

for (var i = 0; i < commentMarkers.length; i++) {
           
            var image = 'http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/commentMap.PNG';
            var marker = new google.maps.Marker({
                position: commentMarkers[i],
                map: map,
                icon: image
            });
  
  
            //Attach click event to the marker.
            (function (marker,i) {
                google.maps.event.addListener(marker, "click", function (e) {
                    
                    infoWindow.setContent("<div>'"+mapComment[i]+"'</div>");
                    infoWindow.open(map, marker);
                });
            })(marker,i);
        }

				var trackCoords = [];

// Add each GPS entry to an array
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

						