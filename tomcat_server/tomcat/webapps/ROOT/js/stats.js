function loadStats() {
const statsIdInput = document.getElementById('statsId');

    if (statsIdInput.value == null || statsIdInput.value.length < 1) {
        alert('Please enter a valid Request Id');
        return;
    }

var stats_id = statsIdInput.value;
    unHideElement("loading")
    fetch('/api/v1/csv/parse?stats_id=' + stats_id, {
        method: 'POST'
    })
    .then(response => {
        return response.json();
    })
    .then(data => {
    hideElement("loading");
     if(handleRedirection(data))
     {
         return;
     }
     if(data.error != undefined)
     {
        alert(data.error)
        return;
     }
    document.getElementById("output").innerHTML = data.table_data;
    document.getElementById("total").innerHTML = "TOTAL : " + data.total;
    })
    .catch(error => {
        hideElement("loading");
        alert('Something went wrong. Please check the console for error details.')
        console.error('There was a problem with the fetch operation:', error);
    });
}

function initiateStats()
{


    if (document.getElementById('requestData').files[0] == undefined || document.getElementById('requestData').files[0].length == 0) {
        alert('Please upload request data file');
        return;
    }

        if (getElementValue('configuration_area') == undefined) {
            alert('Please enter valid xml configuration');
            return;
        }

const formData = new FormData();
formData.append("configuration", getElementValue('configuration_area'));
formData.append("request_data", document.getElementById('requestData').files[0]);


  const requestOptions = {
    method: "POST",
    body: formData,
  };

  unHideElement("loading");
  fetch("/api/v1/stats", requestOptions)
    .then(response => response.json())
    .then(data => {
      if(handleRedirection(data))
      {
          hideElement("loading");
          return;
      }
       hideElement("loading");
           if(data.error != undefined)
           {
              alert(data.error)
              return;
           }
      alert(data.message);
      setElementValue('statsId', data.data.request_id)
      loadStats()
    })
    .catch(error => {
     console.log(error)
      hideElement("loading");
    });
}

function downloadRawResponse()
{
const statsIdInput = document.getElementById('statsId');
    if (statsIdInput.value == null || statsIdInput.value.length < 1) {
        alert('Please enter a valid Request Id');
        return;
    }

    window.open("/uploads/RawResponse_" + statsIdInput.value + ".txt", "_self")
}

function downloadCSVResponse()
{
const statsIdInput = document.getElementById('statsId');

    if (statsIdInput.value == null || statsIdInput.value.length < 1) {
        alert('Please enter a valid Request Id');
        return;
    }

    window.open("/uploads/" + statsIdInput.value + ".csv", "_self")
}