<%@page contentType="text/html; charset=UTF-8" %>
<%@page session="false"%>
<html>
<head>
<meta name="viewport"  content="initial-scale=0.8,user-scalable=no"/>
    <link rel='stylesheet' href='css/navbar.css'>
</head>
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
                    <a href="/snakegame" target="_blank">
                        <i class='bx fxemoji--snake icon'></i>
                        <span class="text nav-text">Snake Game</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/freessl">
                        <i class='bx material-symbols--shield-lock-outline icon'></i>
                        <span class="text nav-text">Free SSL</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/chat.html">
                        <i class='bx quill--chat icon'></i>
                        <span class="text nav-text">Text Chat</span>
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
                    <a href="/tokenGen.jsp" style="background-color:var(--primary-color);"  onclick="return false;">
                        <i class='bx logos--oauth icon' style="color:var(--sidebar-color);"></i>
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
                <a href="/logout">
                    <i class='bx bx--log-out icon'></i>
                    <span class="text nav-text">Logout</span>
                </a>
            </li>
        </div>
    </div>
</nav>
<br><br><br>
<h1><b>OAuth 2.0 Token Generator for Zoho</b></h1>
<br/>
To generate token using your own secrets <a target="_blank" href="/tokenGenCustom.jsp">click here</a>
<br/>
<br/>
<br/>
<form>
  DC :
<select id="dcList">
</select>
</form>
<br><br>
<input type="text" name="scope" id="scope" style="width:400px;height:25px" placeholder="Enter scope"><br><br>

<button id="tokenButton" name="tokenButton" onclick="redirect()"> Generate Token </button> <br>
<button onclick="refresh()"> Refresh Token</button> <br><br><br><br>

<div id="response" style="display:none;text-align: center;">
<button onclick="copyAT()"> Copy Access Token </button> <button onclick="copyRT()"> Copy Refresh Token </button> <button onclick="copyAll()"> Copy All </button> <br>
<textarea readonly id="output" name="output" rows="10" cols="100" style="font-size:18px;color:white;background-color:black;max-width: 80vw;overflow: auto; "></textarea>
</div>
<script src="js/navbar.js"></script>
<script src="js/common.js"></script>
<script>
 var dcListOptions = ""
 const dcList = ['dev', 'local', 'us', 'in', 'uk', 'eu', 'au', 'jp', 'ca', 'sa']
 for (var i in dcList) {
      var dc = dcList[i]
      var val = dc == 'dev' ? 'DEVELOPMENT' : dc.toUpperCase()
      dcListOptions += "<option id=" + dc + " value=" + dc + ">" + val + "</option>";
 }
 document.getElementById('dcList').innerHTML = dcListOptions
 document.getElementById('dcList').value='us'

          window.onload = function() {
                forceHttpsRedirect();
                if (getCookie('production') == 'true')
                {
                hideElement('dev')
                hideElement('local')
                }
           }
const code =  new URLSearchParams(window.location.search).get('code');
if(code!=null && code.length > 10 && localStorage.getItem('dc') != null)
{
const url = new URL(window.location.href);
window.history.replaceState({}, document.title, `${window.location.origin}/tokenGen.jsp`);
getTokens(code)
}
function getDomainUrl()
{
let dc = getElementValue('dcList');
if(dc == "dev")
return "https://accounts.csez.zohocorpin.com/oauth/v2";
if(dc == "local")
return "https://accounts.localzoho.com/oauth/v2";
if(dc == "in")
return "https://accounts.zoho.in/oauth/v2";
if(dc == "us")
return "https://accounts.zoho.com/oauth/v2";
if(dc == "eu")
return "https://accounts.zoho.eu/oauth/v2";
if(dc == "au")
return "https://accounts.zoho.com.au/oauth/v2";
if(dc == "jp")
return "https://accounts.zoho.jp/oauth/v2";
if(dc == "ca")
return "https://accounts.zohocloud.ca/oauth/v2";
if(dc == "sa")
return "https://accounts.zoho.sa/oauth/v2";
if(dc == "uk")
return "https://accounts.zoho.uk/oauth/v2";
}
function redirect()
{
     if(document.getElementById("scope").value == "")
     {
         alert("Scope is mandatory");
         return;
      }

      localStorage.setItem('dc', getElementValue('dcList'));
      localStorage.setItem('scope', document.getElementById("scope").value);

    const data = {
            'scope' : document.getElementById("scope").value,
            'url' : getDomainUrl() + "/auth"
    }

      fetch( "/api/v1/oauth/code", {
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
const res = JSON.parse(data)
if(handleRedirection(res))
{
   return;
}
    window.open (res["redirect_uri"], "_self");
      }).catch(error => {
         alert("Something went wrong. Server might be down");
      });

}
function getTokens(code)
{

setElementValue('dcList', localStorage.getItem('dc'))
document.getElementById("scope").value = localStorage.getItem('scope')
localStorage.clear()

document.getElementById("response").style.display="block";
document.getElementById("output").value = "Generating ........";

const data = {
'code': code,
'url': getDomainUrl() + "/token"
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
console.log(error)
alert("Something went wrong. Server might be down");
});
}

function isRefreshTokenAvailable()
{
try
{
return JSON.parse(document.getElementById("output").value).refresh_token.length > 5;
}
catch(e)
{
return false;
}
}

function refresh()
{
if(!isRefreshTokenAvailable())
{
        alert("Refresh token is unavailable. Please generate it first.");
        return;
}

const refreshToken = JSON.parse(document.getElementById("output").value).refresh_token;

document.getElementById("response").style.display="block";

if(!isRefreshTokenAvailable())
    document.getElementById("output").value = "Generating ........";

const data = {
  'refresh_token':refreshToken,
  'url': getDomainUrl() + "/token"
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
const jsonData = JSON.parse(data);
                    if(handleRedirection(jsonData))
                    {
                        return;
                    }
jsonData.refresh_token = refreshToken
document.getElementById("output").value = JSON.stringify(jsonData, null, 2);
}).catch(error => {
    console.log(error)
    if(isRefreshTokenAvailable())
        alert("Something went wrong. Server might be down");
});
}






function copyAT(){
try{
const json = JSON.parse(document.getElementById("output").value);
if(json.access_token!=undefined)
copyToClipboard(json.access_token);
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
if(json.refresh_token!=undefined)
copyToClipboard(json.refresh_token);
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
}
</script>
</body>
</html>
