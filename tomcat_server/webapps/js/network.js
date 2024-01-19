function makeApiRequest() {
  const apiUrl = document.getElementById('apiUrl').value;
  const apiMethod = document.getElementById('apiMethod').value;
  const apiParams = document.getElementById('apiParams').value;
  const concurrencyCalls = document.getElementById('concurrencyCalls').value;
  const jsonPayload = document.getElementById('jsonPayload').value || null;
  const headers = document.getElementById('headers').value;
  const formTextFields = getElementValue('textData');

   if(!(concurrencyCalls >=1 && concurrencyCalls <= 100))
   {
    alert("Concurrency call range should be between 1 and 100");
    return;
   }
   const headerJson = {};
   const headersSplit = headers.split("\n")  || [];
   for(let i = 0; i< headersSplit.length ; i++)
   {
    if(headersSplit[i].trim().length > 0)
    {
    const headerSplit = headersSplit[i].split(":");
    headerJson[headerSplit[0].trim()] = headerSplit.slice(1).join(':').trim().trim()
    }
   }

  const json = {
                   "url": apiUrl,
                   "method": apiMethod,
                   "headers": headerJson,
                   "concurrency_calls": concurrencyCalls,
                   "query_string": apiParams,
               }

const formData = new FormData();
formData.append("meta_json", JSON.stringify(json));

if(document.getElementById("bodyEnable").checked)
{
if(getElementValue("body") == "json")
{
formData.append("json_payload", jsonPayload);
}
else
{
   const formTextFieldsSplit = formTextFields.split("\n")  || [];
   for(let i = 0; i< formTextFieldsSplit.length ; i++)
   {
    if(formTextFieldsSplit[i].trim().length > 0)
    {
    const formTextFieldSplit = formTextFieldsSplit[i].split(":");
    formData.append(formTextFieldSplit[0].trim(), formTextFieldSplit.slice(1).join(':').trim());
    }
   }

    const fileKey1 = getElementValue('fileKey1') || "";
    if(fileKey1.length > 0)
    {
    const files1 = document.getElementById('fileValue1').files;
       for(let i = 0; i< files1.length ; i++)
       {
        formData.append(fileKey1, files1[i]);
       }
    }

    const fileKey2 = getElementValue('fileKey2') || "";
    if(fileKey2.length > 0)
    {
    const files2 = document.getElementById('fileValue2').files;
       for(let i = 0; i< files2.length ; i++)
       {
        formData.append(fileKey2, files2[i]);
       }
    }
}
}
  const requestOptions = {
    method: "POST",
    body: formData,
  };

  unHideElement("loading");
  fetch("/api/v1/concurrency", requestOptions)
    .then(response => response.json())
    .then(data => {
      if(handleRedirection(data))
      {
          hideElement("loading");
          return;
      }
      document.getElementById('output').value = JSON.stringify(data, null, 2);
      hideElement("loading");
    })
    .catch(error => {
      hideElement("loading");
      document.getElementById('output').value = 'Error: ' + error.message;
    });
}

function changeBody()
{
   if(getElementValue("body") == "form")
   {
    hideElement("jsonPayload");
    hideElement("jsonLabel");

    unHideElement("formData");
    unHideElement("formLabel");
   }
   else
   {
    unHideElement("jsonPayload");
    unHideElement("jsonLabel");

    hideElement("formData");
    hideElement("formLabel");
   }
}

function enableBody()
{
if(document.getElementById("bodyEnable").checked)
{
unHideElement("showBody");
}
else
{
hideElement("showBody")
}
}


function copyAll(){
if(getElementValue("output").length < 1)
{
alert("No response found");
return;
}
copyToClipboard(document.getElementById("output").value);
alert("copied");
}

function downloadResponse()
{
if(getElementValue("output").length < 1)
{
alert("No response found");
return;
}
const data = {
"text" : getElementValue("output")
}

      fetch( "/api/v1/download/text", {
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
window.open(`/api/v1/download/text?file_name=${res.file_name}`, "_self")
}).catch(error => {
   console.log("Something went wrong. Server might be down");
});
}