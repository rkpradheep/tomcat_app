<%@page contentType="text/html" %>
<html>
<head>
<meta  name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
<script>
function callAPI()
{
var isPK = false;

if(document.getElementById("pk").value.length != 0)
isPK=true;


if(document.getElementById("spaceID").value.length == 0 && !isPK)
{
alert("Enter a valid space id");
return;
}
var pathValue = isPK? document.getElementById("pk").value : document.getElementById("spaceID").value;
fetch("/api/v1/sas/limits/" +  pathValue + "?pk=" + isPK, {
  method: "GET"
})
.then(response =>
{
return response.text();
}
).then(data=> {
document.getElementById("output").value = data;
}).catch(error => {
   document.getElementById("output").value = error;
});
}
</script>
<h1><b>Get SAS limits</b></h1>
<br/>
<p>
Execute the below query in jboss by replacing zsid placeholder with your zsid to get space id. <br> <br>

   <b>SELECT SASAccounts.ID AS "Space ID" FROM SASAccounts INNER JOIN UserDomains on SASAccounts.ID = UserDomains.ID
   INNER JOIN CustomerDatabase on UserDomains.CUSTOMERID = CustomerDatabase.CustomerID
   where SASAccounts.LOGINNAME = {zsid};</b>
</p>

<!--<h3><b>Space ID</b></h3>-->
<textarea id="spaceID" name="spaceID" rows="3" cols="30" placeholder="Space ID"></textarea><br><br>
<button onclick="callAPI()"> GET SAS LIMITS FROM SPACE ID</button> <br><br><br><br>
<textarea id="pk" name="pk" rows="3" cols="30" placeholder="PK Value"></textarea><br><br>
<button onclick="callAPI()"> GET SPACE ID FROM PK</button> <br><br><br><br>
<h3><b>Output</b></h3>
<textarea id="output" name="output" rows="5" cols="50"></textarea>
</form>

</form>
</body>
</html>
