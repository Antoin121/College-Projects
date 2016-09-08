<?php

$username="xxxxxx";
$password="xxxxxx";
$database="xxxxxx";
$url = "xxxxxx";
 
$trackID = $_POST['trackID'];
$accel_x  = $_POST['accel_x'];
$accel_y  = $_POST['accel_y'];
$accel_z  = $_POST['accel_z'];
$id  = $_POST['id'];
 
mysql_connect($url,$username,$password);
 
@mysql_select_db($database) or die( "Unable to select database");
 
$query = "INSERT INTO RMTData(trackID,ordering,accel_x,accel_y,accel_z) VALUES ('$trackID','$id','$accel_x','$accel_y','$accel_z')";
 
mysql_query($query);
 
mysql_close();
echo "You successfully added your Coupon";  

?>