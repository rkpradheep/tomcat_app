
   async function addJob() {
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

    var data;

    var otpReference;

    var otp;

    if(document.getElementById('task').value == "mail")
    {
    const subject = document.getElementById('subject').value;
    const fromAddress = document.getElementById('fromAddress').value;
    const toAddress = document.getElementById('toAddress').value;
    const emailMessage = document.getElementById('emailMessage').value;

    if(emailMessage.length < 1)
    {
        alert("Please enter valid message");
        return;
    }

    if(subject.length < 1)
    {
        alert("Please enter valid subject");
        return;
    }
    if(! /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(fromAddress))
    {
        alert("Invalid from address");
        return;
    }

    if(! /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(toAddress))
    {
        alert("Invalid to address");
        return;
    }

    const email = {
    "to" : toAddress,
    "message" : emailMessage,
    "subject" : subject,
    "from_address" : fromAddress,
    "to_address" : toAddress
    }

    data = JSON.stringify(email)

   unHideElement("loading");
   otpReference = await initiateOTP(fromAddress)
   hideElement("loading");

   otp = prompt("Please enter the OTP sent to your from email to verify it.")
   if(otp == undefined)
   {
    alert("Invalid OTP")
    return
    }

    }

        var payload = {
        "task" : document.getElementById('task').value,
        "data" : data,
        "execution_date_time" : date,
        "day_interval" : dayInterval,
        "is_recurring" : isRecurring,
        "otp_reference" : otpReference,
        "otp" : otp
        }
        unHideElement("loading");
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

        hideElement("loading");
    }


  function initiateOTP(email) {
    return new Promise(resolve => {
       fetch( "/api/v1/initiate/otp?email=" + email, {
            method: "POST"
       })
       .then(response =>
       {
       return response.text();
       }
       ).then(data=> {
        var res = JSON.parse(data)
        resolve(res["otp_reference"]);
       }).catch(error => {
           console.log(error)
           alert("Something went wrong. Server might be down");
           resolve(null);
       });
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
                handleFields()
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
        document.getElementById('emailTask').style.display = "block"
    }
    else
    {
        document.getElementById('emailTask').style.display = "none"
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
