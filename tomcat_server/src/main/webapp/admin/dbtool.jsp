<%@page contentType="text/html; charset=UTF-8" %>
<%@page session="false"%>
<html>
    <head>
        <title>Admin DB Tool</title>
        <meta name="viewport" content="width=device-width, initial-scale=0.8" />
        <link rel="stylesheet" href="/css/loading.css" />
        <link rel="stylesheet" href="/css/navbar.css">
    </head>
    <style>

    #rootdiv
    {
    margin-left:50px;
    }
    @media (min-width:1000px)
    {
    #rootdiv
    {
    margin-left:200px;
    }
    iframe
    {
     width:50%
    }
    }

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
    <body>
    <script src="/js/common.js"></script>
    <nav id="sidebar" class="sidebar close">
        <header>
            <span id="toggle">☰</span>
        </header>
        <br>
        <a href="javascript:void(0)" id="closebtn" style="display:none" class="closebtn" onclick="closeNav()">×</a>
        <br>
        <div class="menu-bar">
            <div class="menu">
                <ul class="menu-links">
                    <li class="nav-link">
                        <a href="/app">
                            <i class='bx bx--home-alt icon'></i>
                            <span class="text nav-text">Home</span>
                        </a>
                    </li>
                <li class="nav-link">
                    <a href="/snakegame" target="_blank">
                        <i class='bx fxemoji--snake icon'></i>
                        <span class="text nav-text">Snake Game</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/freessl">
                        <i class='bx material-symbols--shield-lock-outline icon'></i>
                        <span class="text nav-text">Free SSL</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="/chat.html">
                        <i class='bx quill--chat icon'></i>
                        <span class="text nav-text">Text Chat</span>
                    </a>
                </li>
                    <li class="nav-link">
                        <a href="/webrtc.html">
                            <i class='bx icon-park-solid--phone-video-call icon'></i>
                            <span class="text nav-text">Video Chat</span>
                        </a>
                    </li>
                    <li class="nav-link">
                        <a href="/jobs.html">
                            <i class='bx eos-icons--cronjob icon'></i>
                            <span class="text nav-text">Scheduler</span>
                        </a>
                    </li>
                    <li class="nav-link">
                        <a href="/stream.html">
                            <i class='bx ri--broadcast-line icon'></i>
                            <span class="text nav-text">Broadcast</span>
                        </a>
                    </li>
                    <li class="nav-link">
                        <a href="/live.html">
                            <i class='bx icon-park-outline--receiver icon'></i>
                            <span class="text nav-text">Subscribe</span>
                        </a>
                    </li>
                    <li class="nav-link">
                        <a href="/tokenGen.jsp">
                            <i class='bx logos--oauth icon'></i>
                            <span class="text nav-text">Oauth Tool</span>
                        </a>
                    </li>
                    <li class="nav-link">
                        <a href="/network">
                            <i class='bx ant-design--api-outlined icon'></i>
                            <span class="text nav-text">API Tool</span>
                        </a>
                    </li>
                    <li class="nav-link">
                        <a href="/hotswap">
                            <i class='bx devicon--java icon'></i>
                            <span class="text nav-text">JVM HotSwap</span>
                        </a>
                    </li>
                    <li class="nav-link">
                        <a href="/csv">
                            <i class='bx iwwa--csv icon'></i>
                            <span class="text nav-text">CSV Parser</span>
                        </a>
                    </li>
                    <li class="nav-link">
                        <a href="/commandExecutor.jsp">
                            <i class='bx mdi--powershell icon'></i>
                            <span class="text nav-text">Shell</span>
                        </a>
                    </li>
                <li class="nav-link">
                    <a href="/admin/dbtool.jsp" style="background-color:var(--primary-color);"  onclick="return false;">
                        <i class='bx clarity--administrator-line icon' style="color:var(--sidebar-color);"></i>
                        <span class="text nav-text">Admin</span>
                    </a>
                </li>
                </ul>
            </div>
            <div class="bottom-content">
                <li class="">
                    <a href="/logout">
                        <i class='bx bx--log-out icon'></i>
                        <span class="text nav-text">Logout</span>
                    </a>
                </li>
            </div>
        </div>
    </nav>

        <div class="loading" id="loading" style="display:none">Loading&#8230;</div>
        <br />

        <br><br><br>
        <div id="rootdiv">
        <a target="_blank" href="/manager">App Manager</a><br/><br/><br/>
        <a target="_blank" href="/files">File Manager</a><br/><br/><br/>
        <a target="_blank" href="/commandExecutor.jsp">Terminal Command Executor</a><br/><br/></br>

        <input type="file" id="file"/></br>
        <input type="text" id="path" style="width:300px" value="tomcat_build/webapps"/> &nbsp; &nbsp;
        <button onclick="transferFile()">TRANSFER FILE</button><br/><br/></br>

         <button style='background-color:red;' onclick="deleteExpired()">DELETE EXPIRED</button>
         </br>
         </br>

        <b>Add User : </b> <input type="text" id="user_name" placeholder="Name"/>
        &nbsp;&nbsp;<input type="password" id="user_password" placeholder="Password" />
        &nbsp;&nbsp;<select name="role" id="user_role">
                <option value="user">USER</option>
                <option value="admin">ADMIN</option>
            </select>
        &nbsp;&nbsp;<button onclick="addUser()">ADD</button> &nbsp;&nbsp;&nbsp
        <br/>
        <br/>
        <br/>

        <div>
            <b>Property Management</b>
            <select name="propertyList" id="propertyList" onchange="getProperties(false)">
                <option value="" disabled selected>Loading .. </option>
            </select>
            &nbsp;&nbsp;&nbsp;
            <input type="text" id="propertyValue"/>
             &nbsp;&nbsp;&nbsp<button onclick="updateProperty()">UPDATE</button> &nbsp;&nbsp;&nbsp
        </div>
        <br/>
        <br/>
        <br/>
        <div id="tableSelection" style="display: none;">
            <b>Quick Execution</b> (Select the table) &nbsp;&nbsp;&nbsp;
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
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp <button onclick="refreshTable()">REFRESH</button> &nbsp;&nbsp;&nbsp <br>
             SET &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp; <select name="columnListForSet" id="columnListForSet">
                   <option value="" disabled selected>Loading .. </option>
             </select>
             &nbsp;&nbsp;&nbsp<input type="text" id="set_column_value"/> <br/>

             WHERE &nbsp;&nbsp;&nbsp; <select name="columnListForWhere" id="columnListForWhere">
                   <option value="" disabled selected>Loading .. </option>
             </select>
             &nbsp;&nbsp;&nbsp<input type="text" id="where_column_value"/>
            &nbsp;&nbsp;&nbsp<button onclick="updateRow()">UPDATE</button> &nbsp;&nbsp;&nbsp
            <button style='background-color:red' onclick="deleteRow()">DELETE</button>
        </div>
        <br />
        <br />
        <br />
        <div id="queryOutputContainer" style="display: none;max-width: fit-content;" class="fixTableHead"><table id="queryOutput"></table></div>
        <br />
        <br />

        <b>Manual Execution</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br />
        <br />
        <textarea id="query" rows="10" cols="50" style="height: 100px; width: 500px;"> </textarea><br />
        <p style="font-size: 11px; display: none;" id="suggestionHint">* Click on the matching suggestion to autocomplete</p>
        <select id="autocomplete-container" style="height: 80px; width: 250px; overflow-y: scroll; display: none;"></select>
        <br />
        <br />
        <br />
        <br />
        <button onclick="execute()">Execute</button><br />
        <br />
        <br />
        <div id="response" style="display: none;">
            <textarea readonly id="output" name="output" rows="10" cols="50" style="font-size: 18px; color: white; background-color: black;"></textarea>
        </div>
        <br />
        <br />
        <h3>Live Logs</h3>
         <iframe src="/livelogs" height="400" width="100%" title="Live logs"></iframe>
        </div>
        <script>
        var QUERY_OUTPUT = "";
        var CURRENT_TABLE_PK = "";
        var TABLE_LIST = [];
        var COLUMN_LIST = [];

        populateNeededEntity();

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

        function updateRow()
        {
            const tableName = document.getElementById("tableList").value;
            const setColumnName = document.getElementById("columnListForSet").value;
            const whereColumnName = document.getElementById("columnListForWhere").value;
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
                gotConsent = confirm("You are trying to perform update operation without criteria. Do you want to proceed?");
            else
                updateQuery = updateQuery + " Where " + whereColumnName + " = '" + whereColumnValue + "'"


            document.getElementById("query").value = updateQuery
            confirmAndExecute();
        }

        function confirmAndExecute()
        {
        if(confirm("Do you want to continue?\n\nQuery to be executed : \n" + document.getElementById("query").value))
            execute();
        }

        function deleteRow()
        {
            const tableName = document.getElementById("tableList").value;
            const columnName = document.getElementById("columnListForWhere").value;
            const columnValue = document.getElementById("where_column_value").value;
            var gotConsent = true;

            var deleteQuery = "Delete from " + tableName;

            if(columnValue == undefined || columnValue.length < 1)
                gotConsent = confirm("You are trying to perform delete operation without criteria. Do you want to proceed?");
            else
                deleteQuery = deleteQuery + " Where " + columnName + " = '" + columnValue + "'"

            if(!gotConsent)
              return;

            document.getElementById("query").value = deleteQuery;
            confirmAndExecute();
        }


        function execute() {

            unHideElement("response");

            unHideElement("loading");

            const data = {
                "query": document.getElementById("query").value,
            }

            var res;
            fetch("/api/v1/admin/db/execute", {
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
                        throw new Error("API error")
                    }
                    hideElement("loading");
                    QUERY_OUTPUT = res["query_output"];
                    setElementValue("output", "EXECUTION_INFO : \n" + JSON.stringify(res['query_output'], null, 2));
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

        function getProperties(isImport) {
            unHideElement("loading");

            var res;

            fetch("/api/v1/admin/properties?property_name=" + (isImport ? "" : document.getElementById("propertyList").value), {
                    method: "GET",
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
                        throw new Error("API error")
                    }
                    hideElement("loading");
                    if(isImport)
                        populateProperties(res);
                    else
                    document.getElementById("propertyValue").value = res["property_value"]
                })
                .catch((error) => {
                    hideElement("loading");
                    console.log(error);
                    alert("Something went wrong. Please check the error response below and try again.");
                });
        }

        function updateProperty() {
            unHideElement("loading");

            var res;

            const data = {
            "property_name" : document.getElementById("propertyList").value,
            "property_value" : document.getElementById("propertyValue").value
            }
            fetch("/api/v1/admin/properties", {
                    method: "PUT",
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
                        throw new Error("API error")
                    }
                    hideElement("loading");
                    alert("Updated successfully")
                })
                .catch((error) => {
                    hideElement("loading");
                    console.log(error);
                    alert("Something went wrong. Please check the error response below and try again.");
                });
        }

        function getTables() {
            TABLE_LIST = []
            unHideElement("loading");

            const data = {
                "need_table": "true",
            }
            var res;

            fetch("/api/v1/admin/db/execute", {
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
                        throw new Error("API error")
                    }

                    TABLE_LIST = res;
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
                    setElementValue("output", JSON.stringify(res, null, 2));
                    hideElement("tableSelection");
                    hideElement("queryOutputContainer");
                    console.log(error);
                    alert("Something went wrong. Please check the error response below and try again.");
                });
        }

        function getColumns() {

            COLUMN_LIST = []
            setElementValue("recordLimit", "50")

            const data = {
                "need_column": "true",
                "table": document.getElementById("tableList").value,
            }

            unHideElement("loading");
            var res;
            fetch("/api/v1/admin/db/execute", {
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
                        throw new Error("API error")
                    }
                    populateColumn(res);
                    setElementValue("query", "Select * from " + document.getElementById("tableList").value + getLimitAndOrderBy());
                    execute();
                })
                .catch((error) => {
                    hideElement("loading");
                    setElementValue("output", JSON.stringify(res, null, 2));
                    handleQueryOutputForFailed();
                    console.log(error)
                    alert("Something went wrong. Please check the error response below and try again.");
                });
        }

        function populateProperties(propertyList) {
            var propertyListOptions = "";
            for (var i in propertyList) {
                propertyListOptions += "<option >" + propertyList[i] + "</option>";
            }
            document.getElementById("propertyList").innerHTML = propertyListOptions;
            getProperties(false)
        }

        function populateTables() {
            var tableListOptions = "";
            for (var i in TABLE_LIST) {
                tableListOptions += "<option >" + TABLE_LIST[i] + "</option>";
            }
            document.getElementById("tableList").innerHTML = tableListOptions;
        }

        function populateColumn(columnMeta) {
            const columnListRes = columnMeta.columns;
            CURRENT_TABLE_PK = columnMeta.pk;
            COLUMN_LIST = [];
            for (var column in columnListRes) {
                COLUMN_LIST.push(columnListRes[column]);
            }
            var columnListOptions = "";
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

        function isValidColumn(criteriaColumn, currentColumn, value) {
            return criteriaColumn == currentColumn && value == Number(value);
        }

        function setElementChecked(elementID, value) {
            return document.getElementById(elementID).checked = value;
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
            getProperties(true);

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

        function addUser()
        {
        const name = document.getElementById("user_name").value;
        const password = document.getElementById("user_password").value;
        const role = document.getElementById("user_role").value;

        if(name == undefined || password == undefined)
        {
            alert("Please enter valid name and password")
            return;
        }

        unHideElement("loading");

            const data = {
                "name": name,
                "password": password,
                "role": role,
            }

            var res;
            fetch("/api/v1/admin/users", {
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
                        throw new Error("API error")
                    }
                    hideElement("loading");
                    alert(res["message"])
                    setElementValue("user_name", "")
                    setElementValue("user_password", "")
                })
                .catch((error) => {
                    hideElement("loading");
                    console.log(error);
                    setElementValue("output", JSON.stringify(res, null, 2));
                    alert("Something went wrong. Please check the error response below and try again.");
                });

        }


        function deleteExpired()
        {
            unHideElement("loading");
            fetch("/api/v1/admin/delete/expired", {
                    method: "DELETE"
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
                        throw new Error("API error")
                    }
                    hideElement("loading");
                    alert(res["message"])
                })
                .catch((error) => {
                    hideElement("loading");
                    console.log(error);
                    setElementValue("output", JSON.stringify(res, null, 2));
                    alert("Something went wrong. Please check the error response below and try again.");
                });
        }

        function transferFile()
        {
                const fileInput = document.getElementById('file');
                const path = getElementValue('path');

                if (!fileInput.files || fileInput.files.length === 0) {
                    alert('Please select a file.');
                    return;
                }

                if (path == undefined || path.length < 1) {
                    alert('Please enter valid path.');
                    return;
                }

                const file = fileInput.files[0];
                const formData = new FormData();
                formData.append('file', file);
                formData.append('path', path);

                unHideElement("loading")
                fetch('/api/v1/admin/file/transfer', {
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
                     fileInput.value = "";
                     return;
                 }
                 if(data["message"])
                 {
                    alert(data["message"])
                    fileInput.value = "";
                    setElementValue("path", "tomcat_build/webapps")
                 }
                 else
                 {
                    alert(data["error"])
                 }
                hideElement("loading");
                })
                .catch(error => {
                    hideElement("loading");
                    alert('Something went wrong')
                    console.error('There was a problem with the fetch operation:', error);
                });
        }

        </script>
        <script src="/js/navbar.js"></script>
    </body>
</html>
