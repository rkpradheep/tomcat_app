
    function addJob() {

    const seconds = document.getElementById('time').value;
    const date = document.getElementById('date').value.replace('T', ' ');

    if((seconds == undefined || seconds.length <1 || isNaN(seconds)) &&  date.length == 0)
    {
        alert("Please enter either valid Delay Seconds or Date & Time");
        return;
    }

    var data = document.getElementById('data').value;
    if(document.getElementById('task').value == "mail")
    {

    if(data.length < 1)
    {
        alert("Please enter valid message");
        return;
    }
    var toAddress = prompt("Please enter your email address")
    if(toAddress == null)
    {
        return;
    }
    if(! /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(toAddress))
    {
        alert("Invalid email");
        return;
    }

    const email = {
    "to" : toAddress,
    "message" : document.getElementById('data').value
    }

    data = JSON.stringify(email)

    }

        var payload = {
        "task" : document.getElementById('task').value,
        "data" : data,
        "seconds" : seconds,
        "date_time" : date
        }
          fetch( "/api/v1/jobs", {
               method: "POST",
                headers: {
                    'Content-Type' : 'application/json'
                },
                body: JSON.stringify(payload)
          })
          .then(response =>
          {
          return response.text();
          }
          ).then(data=> {
            const res = JSON.parse(data);
            alert(res["message"] != undefined ? res["message"] : res["error"])
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

function handleFields()
{
    if(document.getElementById('task').value == "mail")
    {
        document.getElementById('data').placeholder = "Your message"
    }
    else
    {
        document.getElementById('data').placeholder = "Job data"
    }
}

function getFormattedDateVal(value)
{
return value < 10 ? '0' + value : value;
}

const defaultScheduleDate = new Date(new Date().getTime() + (5*60000));

document.getElementById('date').value = `${defaultScheduleDate.getFullYear()}-${getFormattedDateVal(defaultScheduleDate.getMonth() + 1)}-${getFormattedDateVal(defaultScheduleDate.getDate())}T${getFormattedDateVal(defaultScheduleDate.getHours())}:${getFormattedDateVal(defaultScheduleDate.getMinutes())}`


function removeDelaySeconds()
{
setElementValue('time', '')
}
function removeDate()
{
setElementValue('date', '')
}