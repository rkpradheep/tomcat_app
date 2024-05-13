
    function addJob() {
    const date = document.getElementById('date').value.replace('T', ' ');
    const dayInterval = document.getElementById('day_interval').value;
    const isRecurring = document.getElementById("is_recurring").checked;

    if(date.length == 0)
    {
        alert("Please enter valid Execution Date & Time");
        return;
    }
    if(isRecurring)
    {
    if(dayInterval.length == 0)
    {
        alert("Please enter either valid Execution Date & Time");
        return;
    }
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
        "execution_date_time" : date,
        "day_interval" : dayInterval,
        "is_recurring" : isRecurring
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


function showOrHideRecurring()
{
    if(document.getElementById("recurring_info").style.display == "block")
    {
        document.getElementById("recurring_info").style.display = "none"
    }
    else
    {
        document.getElementById("recurring_info").style.display = "block"
    }
}


var dayIntervalOptions = "";
for (var i=1 ;i< 367; i++)  {
    dayIntervalOptions += "<option >" + i + "</option>";
}

document.getElementById("day_interval").innerHTML = dayIntervalOptions;

const defaultScheduleDate = new Date(new Date().getTime() + (5*60000));

document.getElementById('date').value = `${defaultScheduleDate.getFullYear()}-${getFormattedDateVal(defaultScheduleDate.getMonth() + 1)}-${getFormattedDateVal(defaultScheduleDate.getDate())}T${getFormattedDateVal(defaultScheduleDate.getHours())}:${getFormattedDateVal(defaultScheduleDate.getMinutes())}`
