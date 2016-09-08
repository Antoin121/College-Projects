<?php

$username="xxxxxx";
$password="xxxxxx";
$database="xxxxxx";
$url = "xxxxxx";

  // Create connection
$conn = new mysqli($url, $username, $password, $database);
// Check connection
if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
} 


$sql = "SELECT commentLat,commentLng,comment FROM RMTData";

$result = $conn->query($sql);
if ($result->num_rows > 0) {
	while($row = $result->fetch_assoc()) {

		if($row["commentLat"]!=0.00000000000000){
			$commentLat[] = $row["commentLat"];
		}
		if($row["commentLng"]!=0.00000000000000){
			$commentLng[] = $row["commentLng"];
		}

		if($row["commentLat"]!=0.00000000000000){
			$comment[] = $row["comment"];
		}
	}
	// echo $result->num_rows;
}
else {
	echo "Error: " . $sql . "<br>" . $conn->error;
}


		for($i=0; $i<sizeof($commentLat)-1; $i++){ 
				echo $commentLat[$i]." ".$commentLng[$i]." ".$comment[$i]." ";
		}

		$end = sizeof($commentLat)-1;
		echo $commentLat[$end]." ".$commentLng[$end]." ".$comment[$end];

		$conn->close();

		?>