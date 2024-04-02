function uploadFile() {
    const fileInput = document.getElementById('fileInput');

    if (!fileInput.files || fileInput.files.length === 0) {
        alert('Please select a file.');
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('file', file);
    fileInput.value = "";
    unHideElement("loading")
    fetch('/api/v1/csv/parse', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        return response.json();
    })
    .then(data => {
     if(handleRedirection(data))
     {
         hideElement("loading");
         return;
     }
    document.getElementById("queryOutput").innerHTML = data.table_data;
    document.getElementById("total").innerHTML = "TOTAL : " + data.total;
    hideElement("loading");
    })
    .catch(error => {
        hideElement("loading");
        alert('Something went wrong. Please check the console for error details.')
        console.error('There was a problem with the fetch operation:', error);
    });
}

function loadFile() {
    if (document.querySelector("#fileInput").files[0] != null) {
        uploadFile();
    }
}

setInterval(loadFile, 1000);