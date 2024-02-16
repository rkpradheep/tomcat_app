<%@page contentType="text/html; charset=UTF-8" %>
<%@page session="false"%>
<html>
<head>
<meta name="viewport"  content="initial-scale=0.8,user-scalable=no"/>
<link rel="stylesheet" href="css/navbar.css">
<style>
  body {
            margin: 0;
            padding: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
            flex-direction: column;
        }
</style>
</head>
<body>
<nav id="sidebar" class="sidebar close">
    <header>
        <span id="toggle">☰</span>
    </header>
    <br>
    <a href="javascript:void(0)" id="closebtn" style="display:none" class="closebtn" onclick="closeNav()">×</a>
    <br>
    <div class="menu-bar">
        <div class="menu">
            <ul class="menu-links">
                <li class="nav-link">
                    <a href="/app">
                        <i class='bx bx--home-alt icon'></i>
                        <span class="text nav-text">Home</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/webrtc.html">
                        <i class='bx icon-park-solid--phone-video-call icon'></i>
                        <span class="text nav-text">Video Chat</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/jobs.html">
                        <i class='bx eos-icons--cronjob icon'></i>
                        <span class="text nav-text">Scheduler</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/stream.html">
                        <i class='bx ri--broadcast-line icon'></i>
                        <span class="text nav-text">Broadcast</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/live.html">
                        <i class='bx icon-park-outline--receiver icon'></i>
                        <span class="text nav-text">Subscribe</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/tokenGen.jsp">
                        <i class='bx logos--oauth icon'></i>
                        <span class="text nav-text">Oauth Tool</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/network">
                        <i class='bx ant-design--api-outlined icon'></i>
                        <span class="text nav-text">API Tool</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/hotswap">
                        <i class='bx devicon--java icon'></i>
                        <span class="text nav-text">JVM HotSwap</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/csv">
                        <i class='bx iwwa--csv icon'></i>
                        <span class="text nav-text">CSV Parser</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/commandExecutor.jsp">
                        <i class='bx mdi--powershell icon'></i>
                        <span class="text nav-text">Shell</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/admin/dbtool.jsp">
                        <i class='bx clarity--administrator-line icon'></i>
                        <span class="text nav-text">Admin</span>
                    </a>
                </li>
            </ul>
        </div>
        <div class="bottom-content">
            <li class="">
                <a href="#">
                    <i class='bx bx--log-out icon'></i>
                    <span class="text nav-text">Logout</span>
                </a>
            </li>
        </div>
    </div>
</nav>

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
<script src="js/navbar.js"></script>
<h1><b>Terminal Command Executor</b></h1>
<br/>
<h3><b>Command</b></h3>
<textarea id="command" name="command" rows="5" cols="50"></textarea><br><br>
<button onclick="sendCommand()"> RUN </button> <br><br><br><br>
<h3><b>Output</b></h3>
<textarea id="output" name="output" rows="20" cols="50"></textarea>
</body>
</html>
