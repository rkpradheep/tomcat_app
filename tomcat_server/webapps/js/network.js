function makeApiRequest() {
  const apiUrl = document.getElementById('apiUrl').value;
  const apiMethod = document.getElementById('apiMethod').value;
  const apiParams = document.getElementById('apiParams').value;
  const concurrencyCalls = document.getElementById('concurrencyCalls').value;
  const payload = document.getElementById('payload').value || "{}";
  const headers = document.getElementById('headers').value;

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
    headerJson[headerSplit[0].trim()] = headerSplit[1].trim()
    }
   }

  const json = {
                   "url": apiUrl,
                   "method": apiMethod,
                   "headers": headerJson,
                   "concurrency_calls": concurrencyCalls,
                   "query_string": apiParams,
                   "json": JSON.parse(payload)
               }

  const requestOptions = {
    method: "POST",
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(json),
  };

  fetch("/api/v1/concurrency", requestOptions)
    .then(response => response.json())
    .then(data => {
      if(handleRedirection(data))
      {
          hideElement("loading");
          return;
      }
      document.getElementById('output').value = JSON.stringify(data, null, 2);
    })
    .catch(error => {
      document.getElementById('output').value = 'Error: ' + error.message;
    });
}