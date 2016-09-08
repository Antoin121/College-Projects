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
$sql = "SELECT DISTINCT trackID FROM RMTData";

$result = $conn->query($sql);
if ($result->num_rows > 0) {
	while($row = $result->fetch_assoc()) {
		
			echo $row["trackID"]." ";		
	}
	// echo $result->num_rows;
}
else {
	echo "Error: " . $sql . "<br>" . $conn->error;
}
		$conn->close();
?>