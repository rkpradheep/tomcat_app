<%@page contentType="text/html" %>
<html>
<head>
<meta  name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
<script>
function sendCommand()
{
const params = new URLSearchParams();
if(document.getElementById("command").value.length == 0)
{
alert("Enter a valid command");
return;
}
params.append('command',document.getElementById("command").value);
params.append('password', prompt("Enter password to execute"));
fetch("/api/v1/run", {
  body: params,
  method: "POST",
  headers : {
  'Content-Type' : 'application/x-www-form-urlencoded'
  }
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
<h1><b>Terminal Command Executor</b></h1>
<br/>
<h3><b>Command</b></h3>
<textarea id="command" name="command" rows="5" cols="50"></textarea><br><br>
<button onclick="sendCommand()"> RUN </button> <br><br><br><br>
<h3><b>Output</b></h3>
<textarea id="output" name="output" rows="20" cols="50"></textarea>
</form>

</form>
</body>
</html>
