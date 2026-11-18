/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.963125, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "06 - Lancer"], "isController": true}, {"data": [1.0, 500, 1500, "04 - Ajouter signataire"], "isController": true}, {"data": [1.0, 500, 1500, "POST /api/clm/trousses/{id}/lancer"], "isController": false}, {"data": [1.0, 500, 1500, "PUT /api/clm/trousses/{id}/parcours"], "isController": false}, {"data": [1.0, 500, 1500, "02 - Creer trousse"], "isController": true}, {"data": [0.705, 500, 1500, "POST /api/auth/login"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/clm/trousses/{id}"], "isController": false}, {"data": [1.0, 500, 1500, "03 - Deposer document"], "isController": true}, {"data": [0.705, 500, 1500, "01 - Login"], "isController": true}, {"data": [1.0, 500, 1500, "08 - Suivi"], "isController": true}, {"data": [1.0, 500, 1500, "POST /api/clm/trousses/{id}/documents"], "isController": false}, {"data": [1.0, 500, 1500, "07 - Detail trousse"], "isController": true}, {"data": [1.0, 500, 1500, "POST /api/clm/trousses"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/clm/suivi"], "isController": false}, {"data": [1.0, 500, 1500, "POST /api/clm/trousses/{id}/participants"], "isController": false}, {"data": [1.0, 500, 1500, "05 - Definir parcours"], "isController": true}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 4000, 0, 0.0, 97.36050000000014, 11, 1154, 21.0, 369.9000000000001, 612.9499999999998, 898.9399999999987, 66.3239927043608, 224.58230133891558, 31.19412009306085], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["06 - Lancer", 500, 0, 0.0, 39.487999999999985, 29, 121, 38.0, 44.0, 51.0, 80.93000000000006, 8.450085345861993, 17.838988376907608, 3.441099208227003], "isController": true}, {"data": ["04 - Ajouter signataire", 500, 0, 0.0, 19.11000000000001, 14, 78, 18.0, 21.0, 22.94999999999999, 55.0, 8.448657508321928, 5.1649019533296165, 4.653362143255437], "isController": true}, {"data": ["POST /api/clm/trousses/{id}/lancer", 500, 0, 0.0, 39.487999999999985, 29, 121, 38.0, 44.0, 51.0, 80.93000000000006, 8.450228156160216, 17.839289863951326, 3.441157364373838], "isController": false}, {"data": ["PUT /api/clm/trousses/{id}/parcours", 500, 0, 0.0, 16.08400000000001, 11, 65, 15.0, 18.0, 19.0, 38.98000000000002, 8.454514710855596, 5.746427967534664, 4.4501791300304365], "isController": false}, {"data": ["02 - Creer trousse", 500, 0, 0.0, 25.733999999999984, 17, 256, 23.0, 27.900000000000034, 42.94999999999999, 91.87000000000012, 8.425309630128908, 9.024164841182914, 4.00518156542253], "isController": true}, {"data": ["POST /api/auth/login", 500, 0, 0.0, 569.9340000000001, 228, 1154, 554.0, 869.6000000000001, 952.6499999999999, 1112.8500000000001, 8.320159747067143, 7.2070133746567935, 1.958162596721857], "isController": false}, {"data": ["GET /api/clm/trousses/{id}", 500, 0, 0.0, 16.731999999999996, 11, 86, 16.0, 18.0, 20.0, 37.98000000000002, 8.452656669991377, 17.844416703294847, 2.9386189204266904], "isController": false}, {"data": ["03 - Deposer document", 500, 0, 0.0, 16.40200000000001, 12, 151, 15.0, 17.0, 20.0, 77.76000000000022, 8.439958137807636, 6.2393049905472475, 7.427443394255765], "isController": true}, {"data": ["01 - Login", 500, 0, 0.0, 569.9359999999999, 228, 1154, 554.0, 869.6000000000001, 952.6499999999999, 1112.8500000000001, 8.289674381590292, 7.180606617647059, 1.950987818323496], "isController": true}, {"data": ["08 - Suivi", 500, 0, 0.0, 75.40399999999998, 55, 197, 67.0, 105.90000000000003, 130.95, 180.95000000000005, 8.444376889429329, 159.59324755957508, 2.8697687085169985], "isController": true}, {"data": ["POST /api/clm/trousses/{id}/documents", 500, 0, 0.0, 16.4, 12, 151, 15.0, 17.0, 20.0, 77.76000000000022, 8.440100605999223, 6.239410311270911, 7.427568770994751], "isController": false}, {"data": ["07 - Detail trousse", 500, 0, 0.0, 16.731999999999996, 11, 86, 16.0, 18.0, 20.0, 37.98000000000002, 8.452656669991377, 17.844416703294847, 2.9386189204266904], "isController": true}, {"data": ["POST /api/clm/trousses", 500, 0, 0.0, 25.73199999999998, 17, 256, 23.0, 27.900000000000034, 42.94999999999999, 91.87000000000012, 8.425593583067927, 9.024468976964426, 4.0053165495509155], "isController": false}, {"data": ["GET /api/clm/suivi", 500, 0, 0.0, 75.40399999999998, 55, 197, 67.0, 105.90000000000003, 130.95, 180.95000000000005, 8.444519506840061, 159.59594293615942, 2.8698171761526767], "isController": false}, {"data": ["POST /api/clm/trousses/{id}/participants", 500, 0, 0.0, 19.11000000000001, 14, 78, 18.0, 21.0, 22.94999999999999, 55.0, 8.448800270361609, 5.164989227779655, 4.653440773910105], "isController": false}, {"data": ["05 - Definir parcours", 500, 0, 0.0, 16.08400000000001, 11, 65, 15.0, 18.0, 19.0, 38.98000000000002, 8.454371755634838, 5.746330802658055, 4.450103883092948], "isController": true}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 4000, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
