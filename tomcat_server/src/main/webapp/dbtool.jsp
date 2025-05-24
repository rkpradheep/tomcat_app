<%@page contentType="text/html" %>
<%@page session="false"%>
<html>
    <head>
        <title>DB Tool</title>
        <!--<meta name="viewport" content="width=device-width, initial-scale=1.0" />-->
         <link rel="stylesheet" href="css/loading.css" />
    </head>
    <script src="js/common.js"></script>
    <style>
        body {
            margin: 0;
        }
        html {
            overflow-y: scroll;
        }
        button {
            background-color: rgba(76, 175, 80);
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
            border-radius: 10px
        }

        button:hover {
            background-color: rgba(76, 175, 80, 0.8)
        }

        option:hover {
            cursor: pointer;
            background-color: #d3d3d3;
        }

        .fixTableHead {
            overflow: auto;
            max-height: 500px;
            width: 100%;
        }
        .fixTableHead thead th {
            position: sticky;
            top: 0;
        }
        table {
            border-collapse: collapse;
        }
        th,
        td {
            padding: 8px 15px;
            border: 2px solid #529432;
        }
        th {
            background: #abdd93;
        }

        tr:nth-child(even) {
            background-color: #dddddd;
        }
    </style>
    <body style="padding:30px">
        <div class="loading" id="loading" style="display:none">Loading&#8230;</div>
        <br/>
        <br/>
        Product &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <select id="product" onchange="getDBCredential(true)">
        <option value="custom">CUSTOM</option>
        </select>
        <br />
         <br />
        <div id="credentials_box" style="display:none">
        <form>
            Server &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" id="mysql" name="server" value="mysql" />
            <label for="mysql">MySql</label>
            <input type="radio" id="postgresql" name="server" value="postgresql" />
            <label for="postgresql">Postgresql</label>
        </form>
        Main Cluster IP &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="ip" style="width: 400px; height: 25px;" /><br />
        <br />
        User &nbsp;&nbsp;&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="user" style="width: 400px; height: 25px;" />
        <br />
        <br />
        Password &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="password" id="password" style="width: 400px; height: 25px;" /><br />
        </div>
        <!-- <select name="columnList" id="columnList">
            <option value="" disabled selected>COLUMN</option>
        </select> -->
        <br />
        <br />
      <!--  Enter either ZSID or PK Value : <br />
        <br /> -->
        DataSpace Name&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="zsid" style="width: 100px; height: 25px;" /><br /><br />
        <!-- PK Value &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; -->
        <input type="text" id="pk" style="width: 200px; height: 19px;display: none" placeholder="PK"/><br />
        <!--<p>Click on <b>Enable Autocomplete and Quick Execution</b> button to enable intellisense support.</p>-->
        <button id="quickExe" onclick="getTables()">Populate tables</button><br />
        <div id="tableSelection" style="display: none;">
            Table&nbsp;&nbsp;&nbsp;
            <select name="tableList" id="tableList" onchange="getColumns()">
                <option value="" disabled selected>Loading .. </option>
            </select>
            &nbsp;&nbsp;&nbsp;Limit :
            <select name="recordLimit" id="recordLimit" onchange="handleLimitChange()">
                <option value="50" selected>50</option>
                <option value="100">100</option>
                <option value="1000">1000</option>
                <option value="5000">5000</option>
            </select>
            &nbsp;&nbsp;&nbsp <button onclick="refreshTable()">REFRESH</button>
            <br>
             SET &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp; <select name="columnListForSet" id="columnListForSet">
                   <option value="" disabled selected>Loading .. </option>
             </select>
             &nbsp;&nbsp;&nbsp<input type="text" id="set_column_value"/> <br/>

             WHERE &nbsp;&nbsp;&nbsp; <select name="columnListForWhere" id="columnListForWhere">
                   <option value="" disabled selected>Loading .. </option>
             </select>
             &nbsp;&nbsp;&nbsp<input type="text" id="where_column_value"/>
            &nbsp;&nbsp;&nbsp<button onclick="updateRow()">UPDATE</button> &nbsp;&nbsp;&nbsp

        </div>
        <br />
        <br />
        <br />
        <div id="queryOutputContainer" style="display: none;max-width: fit-content;" class="fixTableHead"><table id="queryOutput"></table></div>
        <br />
        <br />

        <b>Select Query</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br />
        <br />
        <textarea id="query" rows="10" cols="50" style="height: 100px; width: 500px;"> </textarea><br />
        <p style="font-size: 11px; display: none;" id="suggestionHint">* Click on the matching suggestion to autocomplete</p>
        <select id="autocomplete-container" style="height: 80px; width: 250px; overflow-y: scroll; display: none;"></select>
        <br />
        <button onclick="execute()">Execute</button><button onclick="runStats()">Run As Stats</button> <button onclick="window.open('/sasstats')">View Stats</button><br />
        <br />
        <br />
        <div id="response" style="display: none;">
            <textarea readonly id="output" name="output" rows="10" cols="50" style="font-size: 18px;color:red"></textarea>
        </div>


        <br />
        <br />
        Product &nbsp;&nbsp;&nbsp;&nbsp;
        <select id="isc_product">
        </select>
        <button onclick="generateIsc()">GENERATE ISC</button>
        <p id="isc" style="color:red;cursor:pointer" onclick="copyToClipboard(document.getElementById('isc').innerHTML)"></p>
        <br>
        <br>
         Product &nbsp;
        <select id="ear_product">
        </select>
         &nbsp;&nbsp;&nbsp;Key Label &nbsp;<input type="text" id="key_label"/>
         &nbsp;&nbsp;&nbsp;Cipher Text &nbsp;<input type="text" id="cipher_text"/>
         &nbsp;&nbsp;&nbsp; <input type="checkbox" id="is_oek"> IS OEK &nbsp;&nbsp;
         &nbsp;&nbsp;&nbsp;<input type="checkbox" id="is_searchable" checked> IS SEARCHABLE &nbsp;&nbsp
        <button onclick="doEARDecrypt()">EAR DECRYPT</button>
        <p id="ear_decrypted" style="color:red"></p>
        <br>
        <br>
         Product &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <select id="taskengine_product" onchange="doTaskEngineProductSwitchChanges()">
        </select>
         &nbsp;&nbsp;&nbsp;ThreadPool Name &nbsp;&nbsp;<select id="thread_pool">
         </select>
        <div id="idc_jobs_container" style="display:none">
          &nbsp;&nbsp;&nbsp;User Id &nbsp;&nbsp;&nbsp;<input type="text" id="user_id"/>
         &nbsp;&nbsp;&nbsp;Customer Id  &nbsp;<input type="text" id="customer_id"/>
        </div>
        <div id="local_jobs_container" style="display:inline">
         &nbsp;&nbsp;DataSpace Name &nbsp;&nbsp;&nbsp;<input type="text" id="dataspace_id"/>
        </div>
        <br>
           <label for="job-manage">JOB</label>
            <input type="radio" name="taskengine-manage" value="job-manage" checked onclick="doChangeForTaskEngineManagement()"/>
             &nbsp;&nbsp;&nbsp;<label for="repetition-manage">REPETITION</label>
            <input type="radio" name="taskengine-manage" value="repetition-manage" onclick="doChangeForTaskEngineManagement()"/>
                  <br>
                  <br>
         <div id="job-management">
         &nbsp;&nbsp;&nbsp; <input type="checkbox" id="is_repetitive" style="margin-left:-15" onchange="changeIsRepetitive()"> IS REPETITIVE &nbsp;&nbsp;
        <br>
         Job ID &nbsp;&nbsp;<input type="text" id="job_id"/>
         <label id="repetition_label" style="display:none"> &nbsp;&nbsp;&nbsp;Repetition Name &nbsp;&nbsp;</label><input type="text" id="repetition" style="display:none"/>
         &nbsp;&nbsp;&nbsp;Retry Repetition Name &nbsp;&nbsp;<input type="text" id="retry_repetition"/>
         &nbsp;&nbsp;&nbsp;Class Name &nbsp;<input type="text" id="class_name"/>
         <label id="delay_seconds_label"> &nbsp;&nbsp;&nbsp;Delay second(s)</label> &nbsp;<input type="text" id="delay_seconds"/>
        <br>
        <button onclick="getJobDetails()">FETCH JOB DETAILS</button>
        <button onclick="addOrUpdateJob()">ADD OR UPDATE JOB</button>
        <button onclick="deleteJob()">DELETE JOB</button>
        </div>
        <div id="repetition-management" style="display:none">
         Repetition Name &nbsp;<input type="text" id="repetition_name"/>
          &nbsp; &nbsp;Is Common &nbsp;<input type="checkbox" id="is_common" onchange="showAlertForIsCommon()"/>

          <label for="periodicity" id="periodicity_label">  &nbsp;&nbsp;&nbsp;Periodicity &nbsp;</label>
        <input type="text" id="periodicity" placeholder="Enter seconds"/>
          <label for="frequency" id="frequency_label" style="display:none"> &nbsp;&nbsp;&nbsp;Frequency &nbsp;</label>
                 <select id="frequency" style="display:none" onclick="doChangeForFrequency()">
                 <option value="daily">DAILY</option>
                 <option value="weekly">WEEKLY</option>
                 </select>
         <label for="day-of-week" id="day-of-week-label" style="display:none">&nbsp;&nbsp;&nbsp;Day of Week &nbsp;</label>
         <input type="text" id="day-of-week" style="display:none"/>
        <label for="time" id="time_label" style="display:none">&nbsp;&nbsp;&nbsp;Time Of Day &nbsp;</label>
        <input type="text" id="time" placeholder="hh:mm:ss" style="display:none"/> <br>
           <label for="periodic_repetition">IS PERIODIC</label>
            <input type="radio" id="periodic_repetition" name="repetition-type" value="periodic_repetition" checked onclick="doChangeForRepetitionType()"/>
             &nbsp;&nbsp;&nbsp;<label for="calender_repetition">IS CALENDER</label>
            <input type="radio" id="calender_repetition" name="repetition-type" value="calender_repetition" onclick="doChangeForRepetitionType()"/><br>
         <button onclick="getRepetitionDetails()">FETCH REPETITION DETAILS </button>
         <button onclick="addOrUpdateRepetition()">ADD OR UPDATE REPETITION </button>
         <button onclick="deleteRepetition()">DELETE REPETITION </button>
        </div>
        <p id="taskengine" style=""></p>
        <br>
        <br>
        <script>
        var SAS_META;
        var QUERY_OUTPUT = "";
        var CURRENT_TABLE_PK = "";
        var TABLE_LIST = [];
        var COLUMN_LIST = [];
        var isEncrypted = true;


        function updateRow()
        {
            const tableName = document.getElementById("tableList").value;
            const setColumnName = document.getElementById("columnListForSet").value;
            if(setColumnName == 'Select Column')
            {
                alert("Please choose column name to update!");
                return;
            }
            const whereColumnName = document.getElementById("columnListForWhere").value;
            if(setColumnName == 'Select Column')
            {
                alert("Please choose column name for criteria!");
                return;
            }
            const setColumnValue = document.getElementById("set_column_value").value;
            const whereColumnValue = document.getElementById("where_column_value").value;
             var gotConsent = true;
            if(setColumnValue == undefined || setColumnValue.length < 1)
            {
                alert("Please enter valid column value");
                return;
            }

            var updateQuery = "Update " + tableName + " set " + setColumnName + " = '" + setColumnValue + "'";

           if(whereColumnValue == undefined || whereColumnValue.length < 1)
           {
                alert('Criteria is mandatory for update query')
                return;
           }
            else
            {
            updateQuery = updateQuery + " Where " + whereColumnName + " = '" + whereColumnValue + "'"
            }

            document.getElementById("query").value = updateQuery
            confirmAndExecute();
        }

        function confirmAndExecute()
        {
        if(confirm("Do you want to continue?\n\nQuery to be executed : \n" + document.getElementById("query").value))
        {
            execute();
         }
        }

function doChangeForTaskEngineManagement()
{

if(document.querySelector('input[name="taskengine-manage"]:checked').value == 'job-manage')
{
    hideElement('repetition-management')
    unHideElement('job-management')
}
else
{
    hideElement('job-management')
    unHideElement('repetition-management')
}
}

function showAlertForIsCommon()
{
if(document.getElementById("is_common").checked)
{
    alert("Common repetitions cannot be updated or deleted")
}

}
function doTaskEngineProductSwitchChanges()
{

var threadPoolsOptions;
const threadPoolsList = SAS_META['taskengine_meta']['thread_pools'][getElementValue("taskengine_product").split("-")[0]];
 for (var i in threadPoolsList) {
      threadPoolsOptions += "<option value=" + threadPoolsList[i] + ">" + threadPoolsList[i] + "</option>";
 }
document.getElementById('thread_pool').innerHTML = threadPoolsOptions
if(SAS_META.db_meta[getElementValue("taskengine_product")] != undefined)
{
document.getElementById("idc_jobs_container").style.display="none"
document.getElementById("local_jobs_container").style.display="inline"
}
else
{
document.getElementById("local_jobs_container").style.display="none"
document.getElementById("idc_jobs_container").style.display="inline"
}
}

function changeIsRepetitive()
{

if(document.getElementById("is_repetitive").checked)
{
document.getElementById("repetition").style.display="inline"
document.getElementById("repetition_label").style.display="inline"
}
else
{
document.getElementById("repetition").style.display="none"
document.getElementById("repetition_label").style.display="none"
}
}

function doChangeForFrequency()
{
if(getElementValue("frequency") == 'weekly')
{
document.getElementById("day-of-week").style.display="inline"
document.getElementById("day-of-week-label").style.display="inline"
}
else
{
document.getElementById("day-of-week").style.display="none"
document.getElementById("day-of-week-label").style.display="none"
}

}
function getJobDetails()
{

               const data = {
                   "service":getElementValue("taskengine_product").split("-")[0],
                   "dc": getElementValue("taskengine_product").split("-")[1],
                   "zsid": getElementValue("dataspace_id"),
                   "customer_id": getElementValue("customer_id"),
                   "job_id": getElementValue("job_id"),
                   "thread_pool": getElementValue("thread_pool"),
                   "operation":"get"
               }

                    unHideElement("loading");
                    var res;
                    fetch("/api/v1/zoho/jobs", {
                            method: "POST",
                     headers: {
                         'Content-Type': 'application/json'
                     },
                     body: JSON.stringify(data)
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            if (error != undefined) {
                                alert(error)
                                console.log(error);
                                document.getElementById("taskengine").innerHTML=""
                            }
                            else
                            {
                            var output= ""
                            var jsonObj = JSON.parse(data).data;
                            for (const key in jsonObj) {
                                output+="<b>" + key + "</b> : " + jsonObj[key] +"<br/><br/>";
                            }
                             document.getElementById("taskengine").innerHTML=output
                             }
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });
}


function getRepetitionDetails()
{
               const data = {
                   "service":getElementValue("taskengine_product").split("-")[0],
                   "dc": getElementValue("taskengine_product").split("-")[1],
                   "zsid": getElementValue("dataspace_id"),
                   "customer_id": getElementValue("customer_id"),
                    "user_id": getElementValue("user_id"),
                   "thread_pool": getElementValue("thread_pool"),
                   "operation":"get",
                   "repetition_name" : getElementValue("repetition_name"),
                    "is_common" : document.getElementById("is_common").checked,
               }

                    unHideElement("loading");
                    var res;
                    fetch("/api/v1/zoho/repetitions", {
                            method: "POST",
                     headers: {
                         'Content-Type': 'application/json'
                     },
                     body: JSON.stringify(data)
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            if (error != undefined) {
                                alert(error)
                                console.log(error);
                                document.getElementById("taskengine").innerHTML= ""
                            }
                            else
                            {
                            var output= ""
                            var jsonObj = JSON.parse(data).data;
                            for (const key in jsonObj) {
                                output+="<b>" + key + "</b> : " + jsonObj[key] +"<br/><br/>";
                            }
                             document.getElementById("taskengine").innerHTML=output
                            }
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });
}


function addOrUpdateRepetition()
{
if(document.querySelector('input[name="repetition-type"]:checked').value == 'periodic_repetition')
    addOrUpdatePeriodicRepetition()
else
    addOrUpdateCalenderRepetition();
}

function doChangeForRepetitionType()
{
if(document.querySelector('input[name="repetition-type"]:checked').value == 'periodic_repetition')
{
document.getElementById("periodicity").style.display="inline"
document.getElementById("periodicity_label").style.display="inline"

document.getElementById("frequency").style.display="none"
document.getElementById("frequency_label").style.display="none"
document.getElementById("time").style.display="none"
document.getElementById("time_label").style.display="none"
}
else
{
document.getElementById("frequency").style.display="inline"
document.getElementById("frequency_label").style.display="inline"
document.getElementById("time").style.display="inline"
document.getElementById("time_label").style.display="inline"

document.getElementById("periodicity").style.display="none"
document.getElementById("periodicity_label").style.display="none"
}
}

function addOrUpdatePeriodicRepetition()
{

               const data = {
                   "service":getElementValue("taskengine_product").split("-")[0],
                   "dc": getElementValue("taskengine_product").split("-")[1],
                   "zsid": getElementValue("dataspace_id"),
                   "user_id": getElementValue("user_id"),
                   "customer_id": getElementValue("customer_id"),
                   "thread_pool": getElementValue("thread_pool"),
                   "repetition_name" : getElementValue("repetition_name"),
                   "periodicity" : getElementValue("periodicity"),
                   "is_common" : document.getElementById("is_common").checked,
                   "operation":"add_periodic"
               }

                    unHideElement("loading");
                    var res;
                    fetch("/api/v1/zoho/repetitions", {
                            method: "POST",
                     headers: {
                         'Content-Type': 'application/json'
                     },
                     body: JSON.stringify(data)
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            if (error != undefined) {
                                alert(error)
                                console.log(error);
                            }
                            else{
                            if(res.data.auth_uri != undefined)
                             {
                                   handleZohoAuth(res.data.auth_uri)
                                   return;
                             }
                            alert(res.data)
                            }
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });

}


function addOrUpdateCalenderRepetition()
{

               const data = {
                   "service":getElementValue("taskengine_product").split("-")[0],
                   "dc": getElementValue("taskengine_product").split("-")[1],
                   "zsid": getElementValue("dataspace_id"),
                   "user_id": getElementValue("user_id"),
                   "customer_id": getElementValue("customer_id"),
                   "thread_pool": getElementValue("thread_pool"),
                   "repetition_name" : getElementValue("repetition_name"),
                   "frequency" : getElementValue("frequency"),
                   "time" : getElementValue("time"),
                   "day-of-week" : getElementValue("day-of-week"),
                   "is_common" : document.getElementById("is_common").checked,
                   "operation":"add_calender"
               }

                    unHideElement("loading");
                    var res;
                    fetch("/api/v1/zoho/repetitions", {
                            method: "POST",
                     headers: {
                         'Content-Type': 'application/json'
                     },
                     body: JSON.stringify(data)
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            if (error != undefined) {
                                alert(error)
                                console.log(error);
                            }
                            else
                            alert(JSON.parse(data).data)
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });

}

function addOrUpdateJob()
{

               const data = {
                   "service":getElementValue("taskengine_product").split("-")[0],
                   "dc": getElementValue("taskengine_product").split("-")[1],
                   "zsid": getElementValue("dataspace_id"),
                   "user_id": getElementValue("user_id"),
                   "customer_id": getElementValue("customer_id"),
                   "job_id": getElementValue("job_id"),
                   "thread_pool": getElementValue("thread_pool"),
                   "class_name":getElementValue("class_name"),
                   "delay": getElementValue("delay_seconds"),
                   "repetition" : getElementValue("repetition"),
                   "retry_repetition" : getElementValue("retry_repetition"),
                   "operation":"add",
                   "is_repetitive" : document.getElementById("is_repetitive").checked
               }

                    unHideElement("loading");
                    var res;
                    fetch("/api/v1/zoho/jobs", {
                            method: "POST",
                     headers: {
                         'Content-Type': 'application/json'
                     },
                     body: JSON.stringify(data)
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            if (error != undefined) {
                                alert(error)
                                console.log(error);
                            }
                            else{
                            if(res.data.auth_uri != undefined)
                             {
                                   handleZohoAuth(res.data.auth_uri)
                                   return;
                             }
                            alert(res.data)
                            }
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });
}

function deleteRepetition()
{

               const data = {
                   "service":getElementValue("taskengine_product").split("-")[0],
                   "dc": getElementValue("taskengine_product").split("-")[1],
                   "zsid": getElementValue("dataspace_id"),
                   "customer_id": getElementValue("customer_id"),
                   "user_id": getElementValue("user_id"),
                   "repetition_name": getElementValue("repetition_name"),
                   "thread_pool": getElementValue("thread_pool"),
                   "operation":"delete"
               }

                    unHideElement("loading");
                    var res;
                    fetch("/api/v1/zoho/repetitions", {
                            method: "POST",
                     headers: {
                         'Content-Type': 'application/json'
                     },
                     body: JSON.stringify(data)
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            if (error != undefined) {
                                alert(error)
                                console.log(error);
                            }
                            else{
                            if(res.data.auth_uri != undefined)
                             {
                                   handleZohoAuth(res.data.auth_uri)
                                   return;
                             }
                            alert(res.data)
                            }
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });
}

function deleteJob()
{

               const data = {
                   "service":getElementValue("taskengine_product").split("-")[0],
                   "dc": getElementValue("taskengine_product").split("-")[1],
                   "zsid": getElementValue("dataspace_id"),
                   "customer_id": getElementValue("customer_id"),
                   "job_id": getElementValue("job_id"),
                   "thread_pool": getElementValue("thread_pool"),
                   "operation":"delete"
               }

                    unHideElement("loading");
                    var res;
                    fetch("/api/v1/zoho/jobs", {
                            method: "POST",
                     headers: {
                         'Content-Type': 'application/json'
                     },
                     body: JSON.stringify(data)
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            if (error != undefined) {
                                alert(error)
                                console.log(error);
                            }
                            else{
                            if(res.data.auth_uri != undefined)
                             {
                                   handleZohoAuth(res.data.auth_uri)
                                   return;
                             }
                            alert(res.data)
                            }
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });
}

var initCompleted = false;

        function getSASMeta() {
            fetch("/api/v1/sas/meta", {
                    method: "GET",
                })
                .then((response) => {
                    return response.text();
                })
                .then((data) => {
                    var res = JSON.parse(data);
                    if(handleRedirection(res))
                    {
                        hideElement("loading");
                        return;
                    }
                    SAS_META = res;
                    if(initCompleted)
                    {
                       getDBCredential(false);
                        return
                     }
                    populateISCProducts();
                    populateTaskEngineProducts();
                    populateEARProducts()
                    populateDBProducts()
                    setElementValue("zsid", "admin");
                    getDBCredential(false);
                    initCompleted = true;
                })
                .catch((error) => {
                    console.log(error);
                });
        }
        const textArea = document.getElementById("query");
        const autocompleteContainer = document.getElementById("autocomplete-container");

        const isMobile =
            navigator.userAgent.match(/Android/i) ||
            navigator.userAgent.match(/webOS/i) ||
            navigator.userAgent.match(/iPhone/i) ||
            navigator.userAgent.match(/iPad/i) ||
            navigator.userAgent.match(/iPod/i) ||
            navigator.userAgent.match(/BlackBerry/i) ||
            navigator.userAgent.match(/Windows Phone/i);
        if (!isMobile) {
            textArea.addEventListener("input", function() {
                autocompleteContainer.style.display = "block";
                document.getElementById("suggestionHint").style.display = "block";
                autocompleteContainer.innerHTML = "";
                const userInput = textArea.value;
                var lastWords = userInput.split(" ");
                var lastWord = lastWords.pop();
                lastWord = lastWord == "" ? lastWords.pop() : lastWord;
                const defaultList = ["Select", "Where", "From", "Order By", "Group By", "Top", "Having", "Limit", "DESC", "ASC", "Count(*)", "LIKE"];
                const filteredSuggestions = defaultList.concat(COLUMN_LIST.map(column => document.getElementById("tableList").value + "." + column)).concat(TABLE_LIST).filter((suggestion) => suggestion.toLowerCase().match(lastWord.toLowerCase() + "(.*)"));
                //console.log(filteredSuggestions);
                if (filteredSuggestions.length == 0) {
                    autocompleteContainer.style.display = "none";
                    document.getElementById("suggestionHint").style.display = "none";
                    return;
                }
                autocompleteContainer.innerHTML = "";
                filteredSuggestions.forEach((suggestion) => {
                    const li = document.createElement("option");
                    li.textContent = suggestion;
                    li.addEventListener("click", function() {
                        const words = userInput.split(" ");
                        if (words.pop() == " ") words.pop(); // Remove the last incomplete word
                        words.push(suggestion.split(".").length > 1 ? suggestion.split(".")[1] : suggestion);
                        textArea.value = words.join(" ") + " "; // Update the textarea content
                        autocompleteContainer.innerHTML = "";
                        autocompleteContainer.style.display = "none";
                        document.getElementById("suggestionHint").style.display = "none";
                        textArea.focus();
                    });
                    autocompleteContainer.appendChild(li);
                });
                autocompleteContainer.size = autocompleteContainer.options.length == 1 ? 2 : autocompleteContainer.options.length;
            });
        }



function populateISCProducts()
{

var iscProductOptions;
const iscProducts = SAS_META['isc_meta'];
 for (var key in iscProducts) {
      iscProductOptions += "<option value=" + key + ">" + iscProducts[key] + "</option>";
 }
 document.getElementById("isc_product").innerHTML = iscProductOptions
}

function populateEARProducts()
{

var earProductOptions;
const earProducts = SAS_META['ear_meta'];
 for (var key in earProducts) {
      earProductOptions += "<option value=" + key + ">" + earProducts[key] + "</option>";
 }
 document.getElementById("ear_product").innerHTML = earProductOptions
}

function populateTaskEngineProducts()
{

var taskEngineProductOptions;
const taskEngineProducts = SAS_META['taskengine_meta'].products;
 for (var key in taskEngineProducts) {
      taskEngineProductOptions += "<option value=" + key + ">" + taskEngineProducts[key] + "</option>";
 }
 document.getElementById("taskengine_product").innerHTML = taskEngineProductOptions
 doTaskEngineProductSwitchChanges()
}

function populateDBProducts()
{
 var dbProductOptions = ""
 const dbProducts = SAS_META['db_meta'];
 for (var key in dbProducts) {
     dbProductOptions += "<option value=" + key + ">" + dbProducts[key].display_name + "</option>";
 }
 document.getElementById("product").innerHTML = dbProductOptions + document.getElementById("product").innerHTML;

}

        getSASMeta();
        function execute() {
            unHideElement("response");
            if (!preExecutionValidation()) {
                return;
            }

            if (document.getElementById("pk").value.length == 0 && document.getElementById("zsid").value.length == 0) {
                alert("Please enter a valid ZSID or PK value");
                return;
            }

            unHideElement("loading");

            const data = {
                "server": document.querySelector('input[name="server"]:checked').value,
                "ip": document.getElementById("ip").value,
                "user": document.getElementById("user").value,
                "password": document.getElementById("password").value,
                "zsid": document.getElementById("zsid").value,
                "pk": document.getElementById("pk").value,
                "query": document.getElementById("query").value,
                "is_encrypted": isEncrypted,
            }

            var res;
            fetch("/api/v1/sas/execute", {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                })
                .then((response) => {
                    return response.text();
                })
                .then((data) => {
                    res = JSON.parse(data)
                    if(handleRedirection(res))
                    {
                        hideElement("loading");
                        return;
                    }
                    var error = res["error"]
                    if (error != undefined) {
                        if (res["error"] == "key_expired") {
                            getSASMeta();
                            return;
                        }
                        throw new Error("API error")
                    }
                    hideElement("loading");
                    var temp = JSON.parse(data);
                    if (Array.isArray(temp["query_output"])) {
                        QUERY_OUTPUT = temp["query_output"];
                        delete temp["query_output"];
                    }
                    else if(temp.auth_uri != undefined)
                    {
                      handleZohoAuth(temp.auth_uri)
                      return;
                     }

                     else if (temp["query_output"] != undefined && temp["query_output"] != null) {
                        if(temp["query_output"] == "Update query executed successfully")
                        {
                            refreshTable();
                            alert("Updated successfully!")
                            return;
                        }
                        alert(temp["query_output"]);
                        handleQueryOutputForFailed();
                        setElementValue("output", temp["query_output"]);
                        return;
                    }
                    setElementValue("output", "EXECUTION_INFO : \n" + JSON.stringify(temp.sas_meta, null, 2));
                    handleQueryOutput(res, "", "");
                })
                .catch((error) => {
                    hideElement("loading");
                    console.log(error);
                    setElementValue("output", JSON.stringify(res, null, 2));
                    alert("Something went wrong. Please check the error response below and try again.");
                    handleQueryOutputForFailed();
                });
        }

        function runStats() {
            unHideElement("response");
            if (!preExecutionValidation()) {
                return;
            }

            unHideElement("loading");

            const data = {
                "server": document.querySelector('input[name="server"]:checked').value,
                "ip": document.getElementById("ip").value,
                "user": document.getElementById("user").value,
                "password": document.getElementById("password").value,
                "zsid": document.getElementById("zsid").value,
                "pk": document.getElementById("pk").value,
                "query": document.getElementById("query").value,
                "is_encrypted": isEncrypted,
                "is_stats" : true
            }

            var res;
            fetch("/api/v1/sas/execute", {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                })
                .then((response) => {
                    return response.text();
                })
                .then((data) => {
                    res = JSON.parse(data)
                    if(handleRedirection(res))
                    {
                        hideElement("loading");
                        return;
                    }
                    var error = res["error"]
                    if (error != undefined) {
                        if (error == "key_expired") {
                            getSASMeta();
                            return;
                        }
                        hideElement("loading");
                        alert(error)
                        return
                    }
                    hideElement("loading");
                    var temp = JSON.parse(data);
                    if(temp.auth_uri != undefined)
                    {
                      handleZohoAuth(temp.auth_uri)
                      return;
                     }
                     alert(temp.message)
                })
                .catch((error) => {
                    hideElement("loading");
                    console.log(error);
                    setElementValue("output", JSON.stringify(res, null, 2));
                    alert("Something went wrong. Please check the error response below and try again.");
                });
        }

function handleZohoAuth(authURI)
{
                              let screenX = screen.width / 2 - 325;
                              let screenY = screen.height / 2 - 400;
                              var popupOptions = "width=650,height=750,top="+ screenY + ",left=" + screenX;
                              window.open(authURI, "Authentication", popupOptions);

}
        function generateIsc()
        {

                    unHideElement("loading");

                    var res;
                    fetch("/api/v1/zoho/isc?service=" + getElementValue("isc_product").split("-")[0] +"&dc=" + getElementValue("isc_product").split("-")[1], {
                      method: "POST"
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            if (error != undefined) {
                                console.log(error);
                                throw new Error("API error")
                            }

                            document.getElementById("isc").innerText= JSON.parse(data).data
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });

        }

        function doEARDecrypt()
        {

               if (getElementValue("key_label").length == 0) {
                   alert("Please enter a valid Key Label");
                   return;
               }
                if (getElementValue("cipher_text").length == 0) {
                    alert("Please enter a valid Cipher Text");
                    return;
                }
               const data = {
                   "service":getElementValue("ear_product").split("-")[0],
                   "dc": getElementValue("ear_product").split("-")[1],
                   "key_label": getElementValue("key_label"),
                   "cipher_text": getElementValue("cipher_text"),
                   "is_oek": document.getElementById("is_oek").checked,
                   "is_searchable": document.getElementById("is_searchable").checked,
               }

                    unHideElement("loading");
                    var res;
                    fetch("/api/v1/zoho/ear", {
                            method: "POST",
                     headers: {
                         'Content-Type': 'application/json'
                     },
                     body: JSON.stringify(data)
                        })
                        .then((response) => {
                            hideElement("loading");
                            return response.text();
                        })
                        .then((data) => {
                            res = JSON.parse(data)
                            if(handleRedirection(res))
                            {
                                return;
                            }
                            var error = res["error"]
                            document.getElementById("ear_decrypted").innerText= error
                            if (error != undefined) {
                                console.log(error);
                                throw new Error(error)
                            }

                            document.getElementById("ear_decrypted").innerText= JSON.parse(data).data
                        })
                        .catch((error) => {
                            hideElement("loading");
                            console.log(error);
                            alert("Something went wrong. Please try again later.");
                        });

        }

        function getTables() {
            TABLE_LIST = []
            if (!preExecutionValidation()) {
                return;
            }


            unHideElement("loading");

            const data = {
                "server": document.querySelector('input[name="server"]:checked').value,
                "ip": document.getElementById("ip").value,
                "user": document.getElementById("user").value,
                "password": document.getElementById("password").value,
                "pk": document.getElementById("pk").value,
                "need_table": "true",
                "is_encrypted": isEncrypted
            }
            var res;

            fetch("/api/v1/sas/execute", {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                })
                .then((response) => {
                    return response.text();
                })
                .then((data) => {
                    res = JSON.parse(data);
                    if(handleRedirection(res))
                    {
                        hideElement("loading");
                        return;
                    }
                    var error = res["error"]
                    if (error != undefined) {
                        if (res["error"] == "key_expired") {
                            getSASMeta();
                            return;
                        }
                        throw new Error("API error")
                    }

                    if(!res.is_multigrid)
                    {
                        unHideElement("pk");
                    }
                    TABLE_LIST = res.tables;
                    hideElement("quickExe")
                    hideElement("loading");
                    unHideElement("response");
                    populateTables();
                    unHideElement("tableSelection")
                    unHideElement("response");
                    getColumns();
                })
                .catch((error) => {
                    unHideElement("response");
                    hideElement("loading");
                    unHideElement("quickExe");
                    setElementValue("output", JSON.stringify(res, null, 2));
                    hideElement("tableSelection");
                    hideElement("queryOutputContainer");
                    console.log(error);
                    alert("Something went wrong. Please check the error response below and try again.");
                });
        }

        function getColumns() {
            if (!preExecutionValidation()) {
                return;
            }
            COLUMN_LIST = []
            setElementValue("recordLimit", "50")

            const data = {
                "server": document.querySelector('input[name="server"]:checked').value,
                "ip": document.getElementById("ip").value,
                "user": document.getElementById("user").value,
                "password": document.getElementById("password").value,
                "need_column": "true",
                "table": document.getElementById("tableList").value,
                "is_encrypted": isEncrypted
            }

            unHideElement("loading");
            var res;
            fetch("/api/v1/sas/execute", {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                })
                .then((response) => {
                    return response.text();
                })
                .then((data) => {
                    res = JSON.parse(data);
                    if(handleRedirection(res))
                    {
                        hideElement("loading");
                        return;
                    }
                    var error = res["error"]
                    if (error != undefined) {
                        if (res["error"] == "key_expired") {
                            getSASMeta();
                            return;
                        }
                        throw new Error("API error")
                    }
                    populateColumn(res);
                    if (getElementValue("pk").length != 0 || getElementValue("zsid").length != 0) {
                        setElementValue("query", "Select * from " + document.getElementById("tableList").value + getLimitAndOrderBy());
                        execute();
                    }
                })
                .catch((error) => {
                    hideElement("loading");
                    setElementValue("output", JSON.stringify(res, null, 2));
                    handleQueryOutputForFailed();
                    console.log(error)
                    alert("Something went wrong. Please check the error response below and try again.");
                });
        }

        function populateTables() {
            var tableListOptions = "";
            for (var i in TABLE_LIST) {
                tableListOptions += "<option >" + TABLE_LIST[i] + "</option>";
            }
            document.getElementById("tableList").innerHTML = tableListOptions;
            if (getElementValue("zsid") == "admin")
                if (document.getElementById("mysql").checked)
                    document.querySelector("#tableList").value = "SASAccounts";
                else
                    document.querySelector("#tableList").value = "sasaccounts";

        }

        function populateColumn(columnMeta) {
            const columnListRes = columnMeta.columns;
            CURRENT_TABLE_PK = columnMeta.pk;
            COLUMN_LIST = [];
            for (var column in columnListRes) {
                COLUMN_LIST.push(columnListRes[column]);
            }
            var columnListOptions = "<option>Select Column</option>";
            for (var i in COLUMN_LIST) {
                columnListOptions += "<option >" + COLUMN_LIST[i] + "</option>";
            }
            document.getElementById("columnListForWhere").innerHTML = columnListOptions;
            document.getElementById("columnListForSet").innerHTML = columnListOptions;
        }

        function getLimitAndOrderBy() {
            const orderBy = CURRENT_TABLE_PK.length == 0 ? "" : " Order By " + CURRENT_TABLE_PK + " DESC"
            const limit = getElementValue("recordLimit");
            return orderBy + " LIMIT " + limit;
        }

        function handleQueryOutput(outputJson, criteria, criteriaValue) {

            if (!Array.isArray(outputJson.query_output) || outputJson.query_output.length == 0) {
                document.getElementById("queryOutput").innerHTML = "";
                hideElement("queryOutputContainer");
                return;
            }

            var tableOutput = "";
            var tableHeader = "<thead><tr><th>S.No</th>";
            for (var i in outputJson.query_output[0]) {
                var criteriaTag = "";
                if (criteria != i) criteriaTag = '<br><input type="text" autocomplete="off" id="' + i + '" oninput="getFiltered(' + "'" + i + "'" + ')" placeholder="Search" title="eg.(abc or >5 or <10 or>5<10)"/><br><button style="width:80px;background-color:grey;font-size: 10px;" title="Search for the criteria without applying limit" onclick="getFilteredFromDB(' + "'" + i + "'" + ')">Hit DB</button>';
                else criteriaTag = '<br><input type="text" autocomplete="off" value="' + criteriaValue + '" id="' + i + '" oninput="getFiltered(' + "'" + i + "'" + ')" placeholder="Search" title="eg.(abc or >5 or <10 or>5<10)"/><br><button style="width:80px;background-color:grey;font-size: 10px;" title="Search for the criteria without applying limit" onclick="getFilteredFromDB(' + "'" + i + "'" + ')">Hit DB</button>';
                tableHeader += "<th>" + i + criteriaTag + "</th>";
            }
            tableOutput += tableHeader + "</tr></thead>";

            var tableRows = "<tbody>";
            var isEmpty = outputJson.query_output[0][COLUMN_LIST[0]] == "<EMPTY>";
            for (var i in outputJson.query_output) {
                if (!isEmpty) tableRows += "<tr><td>" + (Number(i) + 1) + "</td>";
                else tableRows += "<tr><td></td>";
                for (var j in outputJson.query_output[i]) {
                    tableRows += "<td>" + escapeHtml(outputJson.query_output[i][j]) + "</td>";
                }
                tableRows += "</tr></tbody>";
            }
            tableOutput += tableRows;
            unHideElement("queryOutputContainer");
            document.getElementById("queryOutput").innerHTML = tableOutput;

            if (document.getElementById(criteria) != null) {
                var end = getElementValue(criteria).length;
                document.getElementById(criteria).setSelectionRange(end, end);
                document.getElementById(criteria).focus();
            }
        }

        function getFiltered(criteriaColumn) {
            var filteredRows = [];
            var criteriaVal = getElementValue(criteriaColumn);
            var criteriaValAux = criteriaVal;
            criteriaVal = criteriaVal.toLowerCase();
            var gl = new RegExp(">(.*)<(.+)");
            var gt = new RegExp(">(.+)");
            var lt = new RegExp("<(.+)");
            if (gl.exec(criteriaVal) != null) {
                const num1 = Number((gl.exec(criteriaVal)[1]).trim());
                const num2 = Number((gl.exec(criteriaVal)[2]).trim());

                for (var i in QUERY_OUTPUT) {
                    for (var column in QUERY_OUTPUT[i]) {
                        const columnValue = Number(QUERY_OUTPUT[i][column]);
                        if (isValidColumn(criteriaColumn, column, columnValue) && columnValue > num1 && columnValue < num2) {
                            filteredRows.push(QUERY_OUTPUT[i]);
                        }
                    }
                }
            } else if (gt.exec(criteriaVal) != null) {
                const num = Number((gt.exec(criteriaVal)[1]).trim());
                for (var i in QUERY_OUTPUT) {
                    for (var column in QUERY_OUTPUT[i]) {
                        const columnValue = Number(QUERY_OUTPUT[i][column]);
                        if (isValidColumn(criteriaColumn, column, columnValue) && columnValue > num) {
                            filteredRows.push(QUERY_OUTPUT[i]);
                        }
                    }
                }
            } else if (lt.exec(criteriaVal) != null) {
                const num = Number((lt.exec(criteriaVal)[1]).trim());
                for (var i in QUERY_OUTPUT) {
                    for (var column in QUERY_OUTPUT[i]) {
                        const columnValue = Number(QUERY_OUTPUT[i][column]);
                        if (isValidColumn(criteriaColumn, column, columnValue) && columnValue < num) {
                            filteredRows.push(QUERY_OUTPUT[i]);
                        }
                    }
                }
            } else {
                var filteredRowsAux = [];
                for (var i in QUERY_OUTPUT) {
                    for (var j in QUERY_OUTPUT[i]) {
                        if (j == criteriaColumn && QUERY_OUTPUT[i][j].toLowerCase() == criteriaVal.trim()) {
                            filteredRows.push(QUERY_OUTPUT[i]);
                        } else if (j == criteriaColumn && QUERY_OUTPUT[i][j].toLowerCase().match("(.*)" + criteriaVal.trim() + "(.*)")) {
                            filteredRowsAux.push(QUERY_OUTPUT[i]);
                        }
                    }
                }
                filteredRows = filteredRows.concat(filteredRowsAux);
            }
            if (filteredRows.length == 0) {
                var emptyRow = {};
                for (var i in QUERY_OUTPUT[0]) {
                    emptyRow[i] = "<EMPTY>";
                }

                filteredRows.push(emptyRow);
            }
            handleQueryOutput({
                query_output: filteredRows
            }, criteriaColumn, criteriaValAux);
        }

        function getDBCredential(isProductSwitch) {
            var product = getElementValue("product");
            var space = getElementValue("zsid");
            var pkValue = getElementValue("pk");
            isEncrypted = true;

            if (isProductSwitch) {
                TABLE_LIST = [];
                COLUMN_LIST = [];
                setElementValue("zsid", "admin");
                hideElement("queryOutputContainer");
                hideElement("tableSelection");
                hideElement("pk");
                if (product == "custom"){
                    unHideElement("quickExe");
                    unHideElement("credentials_box")
                    }
                  else
                    hideElement("credentials_box")
                setElementValue("query", "")
                setElementValue("where_column_value", "")
                setElementValue("set_column_value", "")
                hideElement("response", "")
            }

            setElementValue("recordLimit", "50");

            if (product == "custom") {
                isEncrypted = false;

                if (isProductSwitch) {
                    setElementChecked("postgresql", false);
                    setElementChecked("mysql", false);

                    setElementValue("ip", "");
                    setElementValue("user", "");
                    setElementValue("password", "");
                }

                if (getElementValue("ip").length != 0 && getElementValue("user").length != 0)
                    populateNeededEntity();
                else
                    setElementValue("zsid", "admin")
            }
            else
            {
                setElementChecked("postgresql", SAS_META.db_meta[product].server == "postgresql");
                setElementChecked("mysql", SAS_META.db_meta[product].server == "mysql");

                setElementValue("ip", SAS_META[product].ip);
                setElementValue("user", SAS_META[product].user);
                setElementValue("password", SAS_META[product].password);

                populateNeededEntity();
            }
        }

        document.addEventListener("click", function(event) {
            if (!document.getElementById("query").contains(event.target)) {
                autocompleteContainer.style.display = "none";
                hideElement("suggestionHint");
            }
        });

        document.addEventListener("keyup", (event) => {
            if (event.code === "Space") {
                autocompleteContainer.style.display = "none";
                document.getElementById("suggestionHint").style.display = "none";
            }
        });

        function escapeHtml(text) {
            if (text == "<EMPTY>" || text == null)
                return text;
            var map = {
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#039;'
            };
            return text.replace(/[&<>"']/g, function(m) {
                return map[m];
            });
        }

        function handleQueryOutputForFailed() {
            var failed = {};
            for (var column in COLUMN_LIST) {
                failed[COLUMN_LIST[column]] = "Failed";
            }
            if (COLUMN_LIST.length == 0)
                hideElement("queryOutputContainer")
            else
                handleQueryOutput({
                    query_output: [failed]
                }, "", "");
        }

        function preExecutionValidation() {
            if (document.querySelector('input[name="server"]:checked') == null) {
                alert("Choose DB Server");
                return false;
            }
            if (document.getElementById("ip").value.length == 0) {
                alert("Please enter a valid Main Cluster IP ");
                return false;
            }
            if (document.getElementById("user").value.length == 0) {
                alert("Please enter a valid User");
                return false;
            }
            return true;
        }

        function isValidColumn(criteriaColumn, currentColumn, value) {
            return criteriaColumn == currentColumn && value == Number(value);
        }

        function getFilteredFromDB(criteriaColumn) {
            var filteredRows = [];
            var criteriaVal = getElementValue(criteriaColumn);
            var predicate = ""
            var gl = new RegExp(">(.*)<(.+)");
            var gt = new RegExp(">(.+)");
            var lt = new RegExp("<(.+)");
            if (gl.exec(criteriaVal) != null) {
                const num1 = Number((gl.exec(criteriaVal)[1]).trim());
                const num2 = Number((gl.exec(criteriaVal)[2]).trim());
                predicate = " Where " + criteriaColumn + " > " + num1 + " AND " + criteriaColumn + " < " + num2;

            } else if (gt.exec(criteriaVal) != null) {
                const num = Number((gt.exec(criteriaVal)[1]).trim());
                predicate = " Where " + criteriaColumn + " > " + num;
            } else if (lt.exec(criteriaVal) != null) {
                const num = Number((lt.exec(criteriaVal)[1]).trim());
                predicate = " Where " + criteriaColumn + " < " + num;
            } else if (criteriaVal.trim().length != 0) {
                const value = criteriaVal.trim();
                const comparator = isNaN(value) ? " LIKE" + "'%" + value + "%'" :  " = " + value
                predicate = " Where " + criteriaColumn + comparator
            } else {
                alert("Invalid criteria");
                return;
            }
            setElementValue("query", "Select * from " + getElementValue("tableList") + predicate);
            execute();
        }

        function populateNeededEntity() {
            if (TABLE_LIST.length == 0)
                getTables();
            else if (COLUMN_LIST.length == 0)
                getColumns()
            else
                execute();
        }

        function refreshTable() {
            setElementValue("query", "Select * from " + getElementValue("tableList") + getLimitAndOrderBy());
            execute();
        }

        function handleLimitChange() {
            setElementValue("query", "Select * from " + document.getElementById("tableList").value + getLimitAndOrderBy())
            execute()
        }
        </script>
    </body>
</html>
