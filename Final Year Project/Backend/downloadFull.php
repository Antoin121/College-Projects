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


$sql = "SELECT longitude,latitude,num_potholes FROM RMTData";

$result = $conn->query($sql);
if ($result->num_rows > 0) {
	while($row = $result->fetch_assoc()) {
		if($row["latitude"]!=0.00000000000000){
			$latitude[] = $row["latitude"];
		}
		if($row["longitude"]!=0.00000000000000){
			$longitude[] = $row["longitude"];
		}

		if($row["longitude"]!=0.00000000000000){
			$num_potholes[] = $row["num_potholes"];
		}

	}
	// echo $result->num_rows;
}
else {
	echo "Error: " . $sql . "<br>" . $conn->error;
}

		for($i=0; $i<sizeof($latitude)-1; $i++){ 
				echo $latitude[$i]." ".$longitude[$i]." ".$num_potholes[$i]." ";
		}

		$end = sizeof($latitude)-1;
		echo $latitude[$end]." ".$longitude[$end]." ".$num_potholes[$end];

		$conn->close();

		?>