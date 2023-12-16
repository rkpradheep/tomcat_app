<%@page contentType="text/html" %>
<%@page session="false"%>
<html>
<head>
<meta  name="viewport" content="width=device-width, initial-scale=1.0">
</head>
    <style>
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
<h1><b>Token Generator</b></h1>
<br/>
To generate token using custom secrets <a target="_blank" href="/tokenGenCustom.jsp">click here</a>
<br/>
<br/>
<br/>
<form>
  DC&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <input type="radio" id="dev" name="dc" value="dev">
  <label for="dev">Development</label>
  <input type="radio" id="local" name="dc" value="local">
  <label for="local">Local</label>
  <input type="radio" id="in" name="dc" value="in">
  <label for="in">IN</label>
  <input type="radio" id="us" name="dc" value="us">
  <label for="us">US</label><br><br>
</form>

Scope   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="scope" id="scope" style="width:400px;height:25px"><br><br>

<div id="response" style="display:none;">
<button onclick="copyAT()"> Copy Access Token </button> <button onclick="copyRT()"> Copy Refresh Token </button> <button onclick="copyAll()"> Copy All </button> <br>
<textarea readonly id="output" name="output" rows="10" cols="100" style="font-size:18px;color:white;background-color:black "></textarea>
</div>
<div style="background-color:#D3D3D3">
<u><h2><b style="color:green;">Access and Refresh token generation</b></h2></u>

<button id="tokenButton" name="tokenButton" onclick="redirect()"> Generate Tokens </button> <br><br>
</div>

<div style="background-color:#D3D3D3">
<h2><b><u style="color:green;">Generate Access token from Refresh token</u></b></h2></u>
<button onclick="refresh()"> Generate Access Token </button> <br><br><br><br>
</div>

<script>
if(!window.location.href.includes("pradheep-14225"))
{
window.open(new URL(window.location.href).origin + "/tokenGenCustom.jsp" , "_self");
}
const code =  new URLSearchParams(window.location.search).get('code');
if(code!=null && code.length > 10 && localStorage.getItem('dc') != null)
{
const url = new URL(window.location.href);
window.history.replaceState({}, document.title, "https://pradheep-14225.csez.zohocorpin.com:8091/tokenGen.jsp");
getTokens(code)
}
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
function getDomain()
{
let dc = document.querySelector('input[name="dc"]:checked').value;
if(dc == "dev")
return "https://accounts.csez.zohocorpin.com/oauth/v2";
if(dc == "local")
return "https://accounts.localzoho.com/oauth/v2";
if(dc == "us")
return "https://accounts.zoho.com/oauth/v2";
if(dc == "in")
return "https://accounts.zoho.in/oauth/v2";
}
function redirect()
{
    if(document.querySelector('input[name="dc"]:checked') == null)
    {
        alert("Please choose DC");
        return;
    }
     if(document.getElementById("scope").value == "")
     {
         alert("Scope is mandatory");
         return;
      }

      localStorage.setItem('dc', document.querySelector('input[name="dc"]:checked').value);
      localStorage.setItem('scope', document.getElementById("scope").value);

    const data = {
            'scope' : document.getElementById("scope").value,
            'url' : getDomain() + "/auth"
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
    window.open (data, "_self");
      }).catch(error => {
         alert("Something went wrong. Server might be down");
      });

}
function getTokens(code)
{

document.getElementById(localStorage.getItem('dc')).checked = true;
document.getElementById("scope").value = localStorage.getItem('scope')
localStorage.clear()

document.getElementById("response").style.display="block";
document.getElementById("output").value = "Generating ........";

const data = {
'code': code,
'url': getDomain() + "/token"
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
document.getElementById("output").value = JSON.stringify(JSON.parse(data), null, 2);
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
if(document.querySelector('input[name="dc"]:checked') == null)
    {
        alert("Please choose DC");
        return;
}

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
  'url': getDomain() + "/token"
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

</script>
</body>
</html>
