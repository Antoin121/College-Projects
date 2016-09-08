<?php

$username="xxxxxx";
$password="xxxxxx";
$database="xxxxxx";
$url = "xxxxxx";

 
$trackID = $_POST['trackID'];
$longitude =$_POST['longitude'];
$latitude  = $_POST['latitude'];
$time  = $_POST['time'];
$accel_x  = $_POST['accel_x'];
$accel_y  = $_POST['accel_y'];
$accel_z  = $_POST['accel_z'];
$speed  = $_POST['speed'];
$direction  = $_POST['direction'];
$id  = $_POST['id'];
 
mysql_connect($url,$username,$password);
 
@mysql_select_db($database) or die( "Unable to select database");
 
$query = "INSERT INTO RMTData(trackID,ordering,longitude,latitude,time,accel_x,accel_y,accel_z,speed,direction) VALUES ('$trackID','$id','$longitude','$latitude','$time','$accel_x','$accel_y','$accel_z','$speed','$direction')";
 
mysql_query($query);
 
mysql_close();
echo "You successfully added your Coupon";  

?>