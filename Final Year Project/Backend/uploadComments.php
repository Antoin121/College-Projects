<?php
$username="xxxxxx";
$password="xxxxxx";
$database="xxxxxx";
$url = "xxxxxx";
 
$trackID = $_POST['trackID'];
$commentLat  = $_POST['commentLat'];
$commentLng  = $_POST['commentLng'];
$comment  = $_POST['comment'];
 
mysql_connect($url,$username,$password);
 
@mysql_select_db($database) or die( "Unable to select database");
 
$query = "INSERT INTO RMTData(trackID,commentLat,commentLng,comment) VALUES ('$trackID','$commentLat','$commentLng','$comment')";
 
mysql_query($query);
 
mysql_close();
echo "You successfully added your Coupon";  

?>