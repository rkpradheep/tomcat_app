
    function addJob() {

    const seconds = document.getElementById('time').value;

    if( seconds == undefined || seconds.length <1 || isNaN(seconds))
    {
        alert("Invalid delay seconds");
        return;
    }

        var data = {
        "task" : document.getElementById('task').value,
        "time" : seconds,
        "data" : document.getElementById('data').value
        }
          fetch( "/api/v1/jobs", {
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
            alert(data)
          }).catch(error => {
             alert("Something went wrong. Server might be down");
          });

    }

        function getJobs() {
              fetch( "/api/v1/jobs/list", {
                   method: "GET"
              })
              .then(response =>
              {
              return response.text();
              }
              ).then(data=> {
               var res = JSON.parse(data)
                var jobListOptions = ""
                for (var job in res) {
                    jobListOptions += "<option value=" + job + ">" + res[job] + "</option>";
                }
                document.getElementById("task").innerHTML = jobListOptions;
              }).catch(error => {
                  console.log(error)
                 alert("Something went wrong. Server might be down");
              });
        }

        getJobs()