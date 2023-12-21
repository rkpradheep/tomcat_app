<%@page contentType="text/html" %>
<%@page session="false"%>
<html>
    <head>
        <title>DB Tool</title>
        <!--<meta name="viewport" content="width=device-width, initial-scale=1.0" />-->
    </head>
    <script src="js/common.js"></script>
    <style>
    .loading {
      position: fixed;
      z-index: 999;
      overflow: show;
      margin: auto;
      top: 0;
      left: 0;
      bottom: 0;
      right: 0;
      width: 50px;
      height: 50px;
    }

    /* Transparent Overlay */
    .loading:before {
      content: '';
      display: block;
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(255,255,255,0.5);
    }

    .loading:not(:required) {
      /* hide "loading..." text */
      font: 0/0 a;
      color: transparent;
      text-shadow: none;
      background-color: transparent;
      border: 0;
    }

    .loading:not(:required):after {
      content: '';
      display: block;
      font-size: 10px;
      width: 50px;
      height: 50px;
      margin-top: -0.5em;

      border: 15px solid rgba(33, 150, 243, 1.0);
      border-radius: 100%;
      border-bottom-color: transparent;
      -webkit-animation: spinner 1s linear 0s infinite;
      animation: spinner 1s linear 0s infinite;


    }


    @-webkit-keyframes spinner {
      0% {
        -webkit-transform: rotate(0deg);
        -moz-transform: rotate(0deg);
        -ms-transform: rotate(0deg);
        -o-transform: rotate(0deg);
        transform: rotate(0deg);
      }
      100% {
        -webkit-transform: rotate(360deg);
        -moz-transform: rotate(360deg);
        -ms-transform: rotate(360deg);
        -o-transform: rotate(360deg);
        transform: rotate(360deg);
      }
    }
    @-moz-keyframes spinner {
      0% {
        -webkit-transform: rotate(0deg);
        -moz-transform: rotate(0deg);
        -ms-transform: rotate(0deg);
        -o-transform: rotate(0deg);
        transform: rotate(0deg);
      }
      100% {
        -webkit-transform: rotate(360deg);
        -moz-transform: rotate(360deg);
        -ms-transform: rotate(360deg);
        -o-transform: rotate(360deg);
        transform: rotate(360deg);
      }
    }
    @-o-keyframes spinner {
      0% {
        -webkit-transform: rotate(0deg);
        -moz-transform: rotate(0deg);
        -ms-transform: rotate(0deg);
        -o-transform: rotate(0deg);
        transform: rotate(0deg);
      }
      100% {
        -webkit-transform: rotate(360deg);
        -moz-transform: rotate(360deg);
        -ms-transform: rotate(360deg);
        -o-transform: rotate(360deg);
        transform: rotate(360deg);
      }
    }
    @keyframes spinner {
      0% {
        -webkit-transform: rotate(0deg);
        -moz-transform: rotate(0deg);
        -ms-transform: rotate(0deg);
        -o-transform: rotate(0deg);
        transform: rotate(0deg);
      }
      100% {
        -webkit-transform: rotate(360deg);
        -moz-transform: rotate(360deg);
        -ms-transform: rotate(360deg);
        -o-transform: rotate(360deg);
        transform: rotate(360deg);
      }
    }
        body {
            margin: 0;
        }
        html {
            overflow-y: scroll;
        }
        button {
            background-color: #4caf50; /* Green */
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
        }

        button:hover {
            box-shadow: 0 12px 16px 0 rgba(0, 0, 0, 0.24), 0 17px 50px 0 rgba(0, 0, 0, 0.19);
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
        <div class="loading" id="loading" style="display:none">Loading&#8230;</div>
        <br/>
        <br/>
        Product &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <select id="product" onchange="getDBCredential(true)">
        <option value="custom">CUSTOM</option>
        </select>
        <br />
        <br />
        <br />
        <form>
            Server &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" id="mysql" name="server" value="mysql" />
            <label for="mysql">MySql</label>
            <input type="radio" id="postgresql" name="server" value="postgresql" />
            <label for="postgresql">Postgresql</label><br />
            <br />
        </form>
        Main Cluster IP &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="ip" style="width: 400px; height: 25px;" /><br />
        <br />
        User &nbsp;&nbsp;&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="user" style="width: 400px; height: 25px;" />
        <br />
        <br />
        Password &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="password" id="password" style="width: 400px; height: 25px;" /><br />
        <!-- <select name="columnList" id="columnList">
            <option value="" disabled selected>COLUMN</option>
        </select> -->
        <br />
        <br />
      <!--  Enter either ZSID or PK Value : <br />
        <br /> -->
        DataSpace ID&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="zsid" style="width: 400px; height: 25px;" /><br />
        <br />
        <!-- PK Value &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; --> <input type="text" id="pk" style="width: 400px; height: 25px;display: none" /><br />
        <br />
        <!--<p>Click on <b>Enable Autocomplete and Quick Execution</b> button to enable intellisense support.</p>-->
        <br />
        <button id="quickExe" onclick="getTables()">Enable Quick Execution</button><br />
        <br />
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
            &nbsp;&nbsp;&nbsp <button onclick="refreshTable()">REFRESH</button>
           <!-- <br><p style="font-size: 15px;"> Note: Use Manual Execution below after selecting the table from the above dropdown for complex query.</p>
            <button onclick="generateSQL('')" id="sqlGen">Generate Select query for selected table</button> -->
        </div>
        <br />
        <br />
        <br />
        <div id="queryOutputContainer" style="display: none;" class="fixTableHead"><table id="queryOutput"></table></div>
        <br />
        <br />

        <b>Manual Execution</b>(Only DQL)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br />
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
        <script>
        var SAS_META;
        var SERVICE_META;
        var QUERY_OUTPUT = "";
        var CURRENT_TABLE_PK = "";
        var TABLE_LIST = [];
        var COLUMN_LIST = [];
        var isEncrypted = true;

        setElementValue("zsid", "admin");

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
                        return;
                    }
                    SAS_META = res;
                    getDBCredential(false);
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

        var res;
        fetch("/api/v1/sas/services", {
                method: "GET"
            })
            .then((response) => {
                return response.text();
            })
            .then((data) => {
                var res = JSON.parse(data);
                    if(handleRedirection(res))
                    {
                        return;
                    }
                SERVICE_META = res;
                var serviceListOptions = ""
                for (var service in SERVICE_META) {
                    serviceListOptions += "<option value=" + service + ">" + SERVICE_META[service].display_name + "</option>";
                }
                document.getElementById("product").innerHTML = document.getElementById("product").innerHTML + serviceListOptions;
                setElementValue("product", "books");
                getSASMeta();
            })
            .catch((error) => {
                console.log(error);
            });

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
                    res = JSON.parse(data)
                    if(handleRedirection(res))
                    {
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
                    } else if (temp["query_output"] != undefined && temp["query_output"] != null) {
                        handleQueryOutputForFailed();
                        setElementValue("output", temp["query_output"]);
                        return;
                    }
                    setElementValue("output", "EXECUTION_INFO : \n" + JSON.stringify(temp, null, 2));
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

        function generateSQL(criteria) {
            if (getElementValue("tableList").length == 0) {
                alert("Populate table first");
                return;
            }
            if (criteria.length == 0) document.getElementById("query").value = "Select * from " + document.getElementById("tableList").value + getLimitAndOrderBy();
            else document.getElementById("query").value = "Select * from " + document.getElementById("tableList").value + " where " + criteria + " LIKE" + '"%' + document.getElementById(criteria).value + '%"' + getLimitAndOrderBy();
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

                    TABLE_LIST = res;
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
                if (product == "custom")
                    unHideElement("quickExe");
                setElementValue("query", "")
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
                setElementChecked("postgresql", SERVICE_META[product].server == "postgresql");
                setElementChecked("mysql", SERVICE_META[product].server == "mysql");

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
                predicate = " Where " + criteriaColumn + " LIKE '%" + value + "%'";
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
