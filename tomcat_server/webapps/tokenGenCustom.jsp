<%@page contentType="text/html;charset=UTF-8" %>
<%@page session="false"%>
<html>
<head>
<meta name="viewport"  content="initial-scale=0.8,user-scalable=no"/>
    <link rel='stylesheet' href='css/navbar.css'>
</head>
    <style>
    #rootdiv
    {
    margin-left:10px;
    }
    @media (min-width:1000px)
    {
    #rootdiv
    {
    margin-left:80px;
    }
    }
        button {
            background-color: #4caf50; /* Green */
            border: none;
            color: white;
            padding: 5px 12px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
            -webkit-transition-duration: 0.4s; /* Safari */
            transition-duration: 0.4s;
        }

        button:hover {
            box-shadow: 0 12px 16px 0 rgba(0, 0, 0, 0.24), 0 17px 50px 0 rgba(0, 0, 0, 0.19);
        }
</style>
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
                <a href="/logout">
                    <i class='bx bx--log-out icon'></i>
                    <span class="text nav-text">Logout</span>
                </a>
            </li>
        </div>
    </div>
</nav>

<script>
async function copyToClipboard(textToCopy) {
    if (false && navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(textToCopy);
    } else {

        const textArea = document.createElement("textarea");
        textArea.value = textToCopy;

        textArea.style.position = "absolute";
        textArea.style.left = "-999999px";

        document.body.prepend(textArea);
        textArea.select();

        try {
            document.execCommand('copy');
        } catch (error) {
            console.error(error);
        } finally {
            textArea.remove();
        }
    }
}
function getDomain(isAuthorizeURL)
{
let dc = document.querySelector('input[name="dc"]:checked').value;
if(dc == "na")
{
    return isAuthorizeURL ? getElementValue("authorization_uri") : getElementValue("access_token_uri");
}
const endpoint = isAuthorizeURL ? "/auth" : "/token";

if(dc == "dev")
return "https://accounts.csez.zohocorpin.com/oauth/v2" + endpoint;
if(dc == "local")
return "https://accounts.localzoho.com/oauth/v2" + endpoint;
if(dc == "us")
return "https://accounts.zoho.com/oauth/v2" + endpoint;
if(dc == "in")
return "https://accounts.zoho.in/oauth/v2" + endpoint;
}
function redirect()
{
    if(document.querySelector('input[name="dc"]:checked') == null)
    {
        alert("Please choose DC");
        return;
    }

    if(getElementValue("authorization_uri") == "")
    {
        alert("Authorize URL is mandatory for custom service");
        return;
    }
    if(document.getElementById("client_id").value == "")
    {
      alert("Client ID is mandatory");
      return;
     }
     if(document.getElementById("scope").value == "")
     {
         alert("Scope is mandatory");
         return;
      }
      if(document.getElementById("redirect_uri").value == "")
      {
           alert("Redirect URI is mandatory");
           return;
      }
    window.open (getDomain(true) + "?scope=" + document.getElementById("scope").value + "&client_id=" + document.getElementById("client_id").value + "&response_type=code&access_type=offline&redirect_uri=" + document.getElementById("redirect_uri").value + "&prompt=consent");
}
function getTokens()
{
if(document.querySelector('input[name="dc"]:checked') == null)
{
    alert("Please choose DC");
    return;
}
if(getElementValue("access_token_uri") == "")
{
    alert("Access Token URL is mandatory for custom service");
    return;
}
if(document.getElementById("client_id").value == "")
{
   alert("Client ID is mandatory to get token");
   return;
}
if(document.getElementById("client_secret").value == "")
{
   alert("Client Secret is mandatory to get token");
   return;
}
if(document.getElementById("redirect_uri").value == "")
{
   alert("Redirect URI is mandatory to get token");
   return;
}
if(document.getElementById("redirected_uri").value == "")
{
   alert("Redirected URI is mandatory to get token");
   return;
}
if(! /code=([^&]+)/.test(getElementValue('redirected_uri')))
{
    alert("Redirected URI is invalid. Please paste the redirected uri without any modification.")
    return;
}
document.getElementById("response").style.display="block";
document.getElementById("output").value = "Generating ........";

const data = {
'code': getElementValue("redirected_uri").match(/code=([^&]+)/)[1],
'client_id':document.getElementById("client_id").value,
'client_secret':document.getElementById("client_secret").value,
'redirect_uri':document.getElementById("redirect_uri").value,
'grant_type':"authorization_code",
'url' :  getDomain(false)
}
fetch( "/api/v1/oauth/tokens", {
     method: "POST",
      headers: {
          'Content-Type' : 'application/json'
      },
      body: JSON.stringify(data)
})
.then(response =>
{
return response.text();
}
).then(data=> {
const res = JSON.parse(data);
                    if(handleRedirection(res))
                    {
                        return;
                    }
document.getElementById("output").value = JSON.stringify(res, null, 2);
if(res.refresh_token!=undefined){
document.getElementById("refresh_token").value = res.refresh_token
}
}).catch(error => {
   alert("Something went wrong. Server might be down");
});
}



function refresh()
{
if(document.querySelector('input[name="dc"]:checked') == null)
{
   alert("Please choose DC");
   return;
}
if(getElementValue("access_token_uri") == "")
{
    alert("Access Token URL is mandatory for custom service");
    return;
}
if(document.getElementById("client_id").value == "")
{
   alert("Client ID is mandatory to get token");
   return;
}
if(document.getElementById("client_secret").value == "")
{
   alert("Client Secret is mandatory to get token");
   return;
}
if(document.getElementById("refresh_token").value == "")
{
   alert("Refresh token is mandatory to get token");
   return;
}
document.getElementById("response").style.display="block";
document.getElementById("output").value = "Generating ........";

const data = {
'client_id' : document.getElementById("client_id").value,
'client_secret': document.getElementById("client_secret").value,
'refresh_token': document.getElementById("refresh_token").value,
'grant_type': "refresh_token",
'url':  getDomain(false)
}

fetch( "/api/v1/oauth/tokens", {
     method: "POST",
      headers: {
          'Content-Type' : 'application/json'
      },
      body: JSON.stringify(data)
})
.then(response =>
{
return response.text();
}
).then(data=> {
const res = JSON.parse(data);
                    if(handleRedirection(res))
                    {
                        return;
                    }
document.getElementById("output").value = JSON.stringify(res, null, 2);
}).catch(error => {
  alert("Something went wrong. Server might be down");
});
}






function copyAT(){
try{
const json = JSON.parse(document.getElementById("output").value);
if(json.access_token!=undefined){
copyToClipboard(json.access_token);
alert("copied");}
else
alert("Access token not found in response");
}
catch(err)
{
alert("Access token not found in response");
}
}
function copyRT(){
try{
const json = JSON.parse(document.getElementById("output").value);
if(json.refresh_token!=undefined){
copyToClipboard(json.refresh_token);
alert("copied");}
else
alert("Refresh token not found in response");
}
catch(err)
{
alert("Refresh token not found in response");
}
}
function copyAll(){
copyToClipboard(document.getElementById("output").value);
alert("copied");
}

function reset(auto)
{
    if(auto != true && !confirm("Are you sure you want to reset all values?"))
    {
    return;
    }
    document.getElementById("client_id").value="";
    document.getElementById("scope").value="";
    document.getElementById("redirect_uri").value="";
    document.getElementById("client_secret").value="";
    document.getElementById("redirected_uri").value="";
    document.getElementById("response").value="";
    document.getElementById("refresh_token").value="";

    document.getElementById("response").style.display="none";

    if(!auto){
   const chbx = document.getElementsByName("dc");
       for(let i=0; i < chbx.length; i++) {
       chbx[i].checked = false;
   }
   }
}

function showOrHideCustomServerURI()
{
    if(document.querySelector('input[name="dc"]:checked').value == "na")
    {
        unHideElement("custom_server");
    }
    else
    {
        hideElement("custom_server");
    }
}

</script>
<div id="rootdiv">
<br><br><br>
<h1><b>Token Generator</b></h1>

<br/>
<br/>

<form>
  DC : &nbsp;&nbsp;
  <input type="radio" id="dev" name="dc" value="dev" onchange="showOrHideCustomServerURI()">
  <label for="dev">Development</label>
  <input type="radio" id="local" name="dc" value="local" onchange="showOrHideCustomServerURI()">
  <label for="local">Local</label>
  <input type="radio" id="in" name="dc" value="in" onchange="showOrHideCustomServerURI()">
  <label for="in">IN</label>
  <input type="radio" id="us" name="dc" value="us" onchange="showOrHideCustomServerURI()">
  <label for="us">US</label>
  <input type="radio" id="na" name="dc" value="na" checked onchange="showOrHideCustomServerURI()">
  <label for="na">NA</label><br><br>
</form>

Client ID &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="client_id" id="client_id" style="width:400px;height:25px"><br><br>
Client Secret &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="client_secret" id="client_secret" style="width:400px;height:25px"><br><br>
Scope  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="scope" id="scope" style="width:400px;height:25px"><br><br>
Redirect URI  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="redirect_uri" id="redirect_uri" style="width:400px;height:25px"><br><br>
Refresh Token &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="refresh_token" name="refresh_token" style="width:400px;height:25px"><br><br>
Redirected URI  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="redirected_uri" name="redirected_uri" style="width:400px;height:25px"><br><br>
<div id="custom_server" style="display:block">
 <label for="authorization_uri">Authorize URL</label> &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;<input type="text" id="authorization_uri" style="width:400px;height:25px"><br><br>
 <label for="access_token_uri"> Access Token URL </label> &nbsp;<input type="text" id="access_token_uri" style="width:400px;height:25px"><br><br>
</div>
<button onclick="reset(false)"> Reset </button>

<div id="response" style="display:none;">
<button onclick="copyAT()"> Copy Access Token </button> <button onclick="copyRT()"> Copy Refresh Token </button> <button onclick="copyAll()"> Copy All </button> <br>
<textarea readonly id="output" name="output" rows="10" cols="100" style="font-size:18px;color:white;background-color:black "></textarea>
</div>
<div style="background-color:#D3D3D3">
<u><h2><b style="color:green;">Access and Refresh token generation</b></h2></u>
<h3><b>Step 1: Code Generation (<spam style="font-size:15px;color:blue;">Mandatory Fields :</spam><spam style="font-size:15px;color:red;"> Client ID, Scope, Redirect URI</spam>)</b></h3>
<button onclick="redirect()" title="You will be redirected to a consent page now. After giving consent, you will be redirected to another page and make sure to copy the the url of that page and paste it in Redirected URI field."> Get Code </button> <br>

<h3><b>Step 2: Token Generation (<spam style="font-size:15px;color:blue;">Mandatory Fields :</spam><spam style="font-size:15px;color:red;"> Client ID, Client Secret, Redirect URI, Redirected URI</spam>)</b></h3>
<button id="tokenButton" name="tokenButton" onclick="getTokens()"> Get Tokens </button> <br><br>
</div>

<div style="background-color:#D3D3D3">
<h2><b><u style="color:green;">Generate Access token from Refresh token</u> (<spam style="font-size:15px;color:blue;">Mandatory Fields : </spam><spam style="font-size:15px;color:red;"> Client ID, Client Secret, Refresh Token</spam>)</b></h2></u>
<button onclick="refresh()"> Generate Access Token </button> <br><br><br><br>
</div>
</div>
</body>
<script src="js/navbar.js"></script>
<script src="js/common.js"></script>
</html>
